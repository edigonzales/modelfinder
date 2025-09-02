package ch.so.agi.modelfinder;

import ch.interlis.ili2c.metamodel.*;

import java.util.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ili2Mermaid renders a MermaidJS classDiagram from an INTERLIS TransferDescription.
 * <p>
 * Design highlights:
 * <ul>
 *   <li><b>Clean architecture</b>: domain-neutral <em>Diagram</em> model + tiny <em>Renderer</em>.</li>
 *   <li><b>Adapter/Visitor</b>: Ili2cAdapter walks ili2c types and builds the Diagram.</li>
 *   <li><b>Deterministic output</b>: stable sorting of models, topics, classes, attrs, and edges.</li>
 *   <li><b>Separation of concerns</b>: extraction ↔ formatting ↔ rendering kept separate.</li>
 * </ul>
 * </p>
 */
public final class Ili2Mermaid {
  private Ili2Mermaid() {}

  /** Entry point. */
  public static String render(TransferDescription td) {
    Objects.requireNonNull(td, "TransferDescription is null");
    Diagram diagram = new Ili2cAdapter().buildDiagram(td);
    return new MermaidRenderer().render(diagram);
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Domain-agnostic diagram model
  // ─────────────────────────────────────────────────────────────────────────────

  static final class Diagram {
    final Map<String, Namespace> namespaces = new LinkedHashMap<>(); // key: fully-qualified namespace label
    final Map<String, Node> nodes = new LinkedHashMap<>();           // key: fully-qualified node name
    final List<Inheritance> inheritances = new ArrayList<>();
    final List<Assoc> assocs = new ArrayList<>();

    Namespace getOrCreateNamespace(String label) {
      return namespaces.computeIfAbsent(label, Namespace::new);
    }
  }

  static final class Namespace {
    final String label; // e.g. "ModelA::TopicB" or just "ModelA" or "<root>"
    final List<String> nodeOrder = new ArrayList<>(); // store node fqns for deterministic ordering

    Namespace(String label) { this.label = label; }
  }

  static final class Node {
    final String fqn;              // e.g. Model.Topic.Class or Model.Class
    final String displayName;      // shown inside Mermaid class block (without package)
    final Set<String> stereotypes; // e.g. Abstract, Structure, Enumeration, External
    final List<String> attributes; // lines like: name[1] : TypeName

    Node(String fqn, String displayName, Set<String> stereotypes) {
      this.fqn = fqn; this.displayName = displayName; this.stereotypes = stereotypes;
      this.attributes = new ArrayList<>();
    }
  }

  static final class Inheritance {
    final String subFqn;  // child
    final String supFqn;  // parent (may be external)
    Inheritance(String subFqn, String supFqn) { this.subFqn = subFqn; this.supFqn = supFqn; }
  }

  static final class Assoc {
    final String leftFqn;
    final String rightFqn;
    final String leftCard;   // e.g. "1", "0..1", "1..*"
    final String rightCard;  // e.g. "*"
    final String label;      // optional text label (e.g., role names)

    Assoc(String leftFqn, String rightFqn, String leftCard, String rightCard, String label) {
      this.leftFqn = leftFqn; this.rightFqn = rightFqn;
      this.leftCard = leftCard; this.rightCard = rightCard; this.label = label;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Ili2c → Diagram adapter (Visitor-like traversal)
  // ─────────────────────────────────────────────────────────────────────────────

  static final class Ili2cAdapter {
      Diagram buildDiagram(TransferDescription td) {
          Diagram d = new Diagram();

          // 1) Only models from the last file.
          Model[] lastModels = td.getModelsFromLastFile();
          if (lastModels == null)
              lastModels = new Model[0];

          // quick lookup for "is in last file" decisions
          Set<Model> lastModelSet = Arrays.stream(lastModels).collect(Collectors.toCollection(LinkedHashSet::new));

          // 2) Register namespaces for each model and topic (and root).
          Namespace root = d.getOrCreateNamespace("<root>");

          // 3) First pass: collect nodes (classes, structures, enumerations, externals
          // when needed)
          for (Model m : sortByName(lastModels)) {
              // Topics
              for (Topic t : getElements(m, Topic.class)) {
                  String nsLabel = m.getName() + "::" + t.getName();
                  d.getOrCreateNamespace(nsLabel); // ensure it exists

                  // Classes/Structures/Associations inside topic
                  collectViewablesInContainer(d, lastModelSet, m, t);

                  // Enums/domains inside topic
                  collectDomains(d, lastModelSet, m, t);
              }

              // Model-level (outside any topic): classes/structures/assocs
              collectViewablesInContainer(d, lastModelSet, m, m);
              collectDomains(d, lastModelSet, m, m);
          }

          // 4) Second pass: inheritance edges and external parents
          for (Node n : d.nodes.values()) {
              Object def = lookupDefinition(td, n.fqn); // our fqn uses names only; we best-effort map back
              if (def instanceof Table tbl) {
                  Element extEl = tbl.getExtending();
                  Table base = (extEl instanceof Table) ? (Table) extEl : null;
                  if (base != null) {
                      String supFqn = fqnOf(base);
                      // ensure parent node exists; if parent is outside last-file models, mark
                      // External & place at root
                      if (!belongsToLastFile(base, lastModelSet)) {
                          Node ext = d.nodes.get(supFqn);
                          if (ext == null) {
                              Node extNode = new Node(supFqn, localName(supFqn), setOf("External"));
                              d.nodes.put(extNode.fqn, extNode);
                              root.nodeOrder.add(extNode.fqn);
                          } else {
                              ext.stereotypes.add("External");
                          }
                      }
                      d.inheritances.add(new Inheritance(n.fqn, supFqn));
                  }
              }
          }

          // 5) Associations (with cardinalities on both ends)
          for (Model m : sortByName(lastModels)) {
              // Topics first
              for (Topic t : getElements(m, Topic.class)) {
                  collectAssociations(d, lastModelSet, m, t);
              }
              // Model level
              collectAssociations(d, lastModelSet, m, m);
          }

//          // Sort edges deterministically
//          d.inheritances.sort(Comparator.comparing((Inheritance i) -> i.subFqn).thenComparing(i -> i.supFqn));
//          d.assocs.sort(Comparator.comparing((Assoc a) -> a.leftFqn).thenComparing(a -> a.rightFqn)
//                  .thenComparing(a -> a.label == null ? "" : a.label));
          
          return d;
      }

    // ── collection helpers ────────────────────────────────────────────────────

    private void collectViewablesInContainer(Diagram d, Set<Model> lastModelSet, Model m, Container container) {
        String namespace = (container instanceof Topic)
          ? m.getName() + "::" + container.getName()
          : "<root>";
        
      Namespace ns = d.getOrCreateNamespace(namespace);
      
      for (Viewable v : getElements(container, Viewable.class)) {
        if (v instanceof AssociationDef) {
          // associations handled later to ensure endpoints/nodes exist first
          continue;
        }
        if (v instanceof Table tbl) {
          String fqn = fqnOf(m, container, tbl);
          Set<String> stereos = new LinkedHashSet<>();
          if (tbl.isAbstract()) stereos.add("Abstract");
          if (!tbl.isIdentifiable()) stereos.add("Structure");
          Node node = d.nodes.computeIfAbsent(fqn, k -> new Node(k, tbl.getName(), stereos));
          node.stereotypes.addAll(stereos);

          // attributes          
          for (AttributeDef a : getElements(tbl, AttributeDef.class)) {
            String card = formatCardinality(a.getCardinality());
            String typeName = TypeNamer.nameOf(a);
            node.attributes.add(a.getName() + "[" + card + "] : " + typeName);
          }
          ns.nodeOrder.add(fqn);
        }
      }
    }

    private void collectDomains(Diagram d, Set<Model> lastModelSet, Model m, Container container) {
        String namespace = (container instanceof Topic) ? m.getName() + "::" + container.getName() : "<root>";
        Namespace ns = d.getOrCreateNamespace(namespace);

        for (Domain dom : getElements(container, Domain.class)) {
            Type t = dom.getType();
            if (t instanceof EnumerationType) {
                String fqn = fqnOf(m, container, dom);
                Node node = d.nodes.computeIfAbsent(fqn, k -> new Node(k, dom.getName(), setOf("Enumeration")));
                node.stereotypes.add("Enumeration");
                ns.nodeOrder.add(fqn);
            }
        }
    }

    private void collectAssociations(Diagram d, Set<Model> lastModelSet, Model m, Container container) {
      for (AssociationDef as : getElements(container, AssociationDef.class)) {
          List<RoleDef> roles = as.getRoles();
          if (roles == null || roles.size() != 2) continue; // only binary associations rendered

          RoleDef a = roles.get(0);
          RoleDef b = roles.get(1);
          
        AbstractClassDef aEnd = a.getDestination();
        AbstractClassDef bEnd = b.getDestination();
        if (!(aEnd instanceof Table) || !(bEnd instanceof Table)) continue;

        Table aTbl = (Table) aEnd;
        Table bTbl = (Table) bEnd;

        String left = fqnOf(m, containerOf(aTbl), aTbl);
        String right = fqnOf(m, containerOf(bTbl), bTbl);

        // Ensure external placeholders for associations if endpoints not in last-file models
        if (!belongsToLastFile(aTbl, lastModelSet)) ensureExternalNode(d, aTbl);
        if (!belongsToLastFile(bTbl, lastModelSet)) ensureExternalNode(d, bTbl);

        String leftCard = formatCardinality(a.getCardinality());
        String rightCard = formatCardinality(b.getCardinality());

        String label = roleLabel(a) + "–" + roleLabel(b);

        d.assocs.add(new Assoc(left, right, leftCard, rightCard, label));
      }
    }

    private void ensureExternalNode(Diagram d, Table t) {
      String fqn = fqnOf(t);
      if (!d.nodes.containsKey(fqn)) {
        Node ext = new Node(fqn, t.getName(), setOf("External"));
        d.nodes.put(ext.fqn, ext);
        d.getOrCreateNamespace("<root>").nodeOrder.add(ext.fqn);
      } else {
        d.nodes.get(fqn).stereotypes.add("External");
      }
    }

    // ── tiny utilities over ili2c model ───────────────────────────────────────

    private static String roleLabel(RoleDef r) {
      String n = r.getName();
      return n != null && !n.isEmpty() ? n : "role";
    }

    private static boolean belongsToLastFile(Element e, Set<Model> lastModels) {
      return lastModels.contains(modelOf(e));
    }

    private static Model modelOf(Element e) {
      Element cur = e;
      while (cur != null && !(cur instanceof Model)) {
        cur = cur.getContainer();
      }
      return (Model) cur;
    }

    private static Container containerOf(Element e) {
      Element cur = e.getContainer();
      if (cur instanceof Container) return (Container) cur;
      return null;
    }

    private static String fqnOf(Model m, Container c, Element e) {
      if (c instanceof Topic) return m.getName() + "." + c.getName() + "." + e.getName();
      return m.getName() + "." + e.getName();
    }

    private static String fqnOf(Element e) {
      Model m = modelOf(e);
      Container c = containerOf(e);
      return fqnOf(m, c, e);
    }

    private static String localName(String fqn) {
      int i = fqn.lastIndexOf('.') ;
      return i < 0 ? fqn : fqn.substring(i + 1);
    }

    private static <T extends Element> List<T> getElements(Container c, Class<T> type) {
        List<T> out = new ArrayList<>();
        for (Iterator<?> it = c.iterator(); it.hasNext();) {
            Object e = it.next();
            if (type.isInstance(e)) {
                out.add(type.cast(e));
            }
        }
        out.sort(Comparator.comparing(Element::getName, Comparator.nullsLast(String::compareTo)));
        return out;
    }


    private static <T extends Element> List<T> sortByName(T[] arr) {
        if (arr == null) return List.of();

        return Arrays.stream(arr)
            .sorted(Comparator.comparing(
                Element::getName,
                Comparator.nullsLast(String::compareTo) // handles null names
            ))
            .collect(Collectors.toList());
    }

    private static <T extends Element> Function<Element, T> typeSafe() {
      @SuppressWarnings("unchecked") Function<Element, T> f = e -> (T) e;
      return f;
    }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Mermaid renderer
  // ─────────────────────────────────────────────────────────────────────────────
  static final class MermaidRenderer {
      String render(Diagram d) {
          StringBuilder sb = new StringBuilder(4_096);
          sb.append("classDiagram\n");

          // 1) Print namespaces (topics/models). We print classes as we encounter them.
          d.namespaces.values().forEach(ns -> {
              if (ns.label.equals("<root>"))
                  return; // print root nodes later
              sb.append("  namespace ").append(nsId(ns.label)).append(" {\n");
              for (String fqn : ns.nodeOrder) {
                  Node n = d.nodes.get(fqn);
                  printClassBlock(sb, n, "    ");
              }
              sb.append("  }\n");
          });

          // 2) Root-level nodes (classes outside topics and externals)
          Namespace root = d.namespaces.get("<root>");
          if (root != null) {
              for (String fqn : root.nodeOrder) {
                  Node n = d.nodes.get(fqn);
                  printClassBlock(sb, n, "  ");
              }
          }
//
//          // 3) Inheritance edges
//          for (Inheritance i : d.inheritances) {
//              sb.append("  ").append(id(i.subFqn)).append(" --|> ").append(id(i.supFqn)).append("\n");;
//          }
//
//          // 4) Associations with cardinalities on both ends
//          for (Assoc a : d.assocs) {
//              sb.append("  ").append(id(a.leftFqn)).append(" \"").append(a.leftCard).append("\" -- \"")
//                      .append(a.rightCard).append("\" ").append(id(a.rightFqn));
//              if (a.label != null && !a.label.isEmpty())
//                  sb.append(" : ").append(escape(a.label));
//              sb.append("\n");
//          }

          return sb.toString();
      }

      private static String id(String s) { return s; }
      
      private static String nsId(String s) { return s.replaceAll("[^A-Za-z0-9_]", "_"); }
      
      private void printClassBlock(StringBuilder sb, Node n, String indent) {
          sb.append(indent).append("class ").append(id(n.fqn)).append("[\"").append(escape(n.displayName)).append("\"] {\n");
          //sb.append(indent).append("class ").append(id(n.fqn)).append(" {\n");
          for (String stereo : n.stereotypes) {
              sb.append(indent).append("  ").append("<<").append(stereo).append(">>\n");
          }
          for (String attr : n.attributes) {
              sb.append(indent).append("  ").append(escape(attr)).append("\n");
          }
          sb.append(indent).append("}\n");
      }

      private static String quote(String id) {
          // Mermaid class ids allow dots, but quoting is safer and lets us use dots
          // freely.
          return '"' + id + '"';
      }

      private static String escape(String s) {
          return s.replace("\"", "\\\"");
      }
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Type naming and cardinality formatting helpers
  // ─────────────────────────────────────────────────────────────────────────────
  static final class TypeNamer {
    static String nameOf(AttributeDef a) {
        Type t = a.getDomainResolvingAliases();
      if (t == null) return "<Unknown>";

      // Show referenced structure/class names, or base type names where sensible.
      if (t instanceof ReferenceType ref) {
        AbstractClassDef target = ref.getReferred();
        if (target != null) return target.getName();
      }
      if (t instanceof CompositionType comp) {
        AbstractClassDef target = comp.getComponentType();
        if (target != null) return target.getName();
      }
//      if (t instanceof DomainType domType) {
//        Domain base = domType.getDomain();
//        if (base != null) return base.getName();
//      }

      // Fallbacks for common geometry and primitive types
      if (t instanceof SurfaceType) return "MultiSurface"; 
      if (t instanceof AreaType) return "Area";
      if (t instanceof LineType) return "Polyline";
      if (t instanceof CoordType) {
          NumericalType[] nts = ((CoordType) t).getDimensions();
          return "Coord" + nts.length;
      }
      if (t instanceof NumericType) return "Numeric";
      if (t instanceof TextType) return "String";
      if (t instanceof EnumerationType) {
          return a.isDomainBoolean() ? "Boolean" : a.getContainer().getName();
      }
      String n = t.getName();
      return (n != null && !n.isEmpty()) ? n : t.getClass().getSimpleName();
    }
  }

  static String formatCardinality(Cardinality c) {
    if (c == null) return "1";
    long min = c.getMinimum();
    long max = c.getMaximum(); // convention: -1 == unbounded

    String left = String.valueOf(min);
    String right = (max == Long.MAX_VALUE) ? "*" : String.valueOf(max);

    // Compact: show single value when min==max and not unbounded
    if (max >= 0 && min == max) return String.valueOf(min);
    return left + ".." + right;
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Lookup helpers (best-effort; used only for inheritance/external placement)
  // ─────────────────────────────────────────────────────────────────────────────
  private static Object lookupDefinition(TransferDescription td, String fqn) {
    // Map by names. This is best-effort for creating inheritance edges if we
    // already have the node. Not performance-critical.
    Map<String, Object> byFqn = new HashMap<>();
    for (Model m : Optional.ofNullable(td.getModelsFromLastFile()).orElse(new Model[0])) {
      // topics
      for (Topic t : getElements(m, Topic.class)) {
        for (Table tbl : getElements(t, Table.class)) {
          byFqn.put(fqnOf(m, t, tbl), tbl);
        }
      }
      // model-level
      for (Table tbl : getElements(m, Table.class)) {
        byFqn.put(fqnOf(m, m, tbl), tbl);
      }
    }
    return byFqn.get(fqn);
  }

  private static <T extends Element> List<T> getElements(Container c, Class<T> type) {
      List<T> out = new ArrayList<>();
      for (Iterator<?> it = c.iterator(); it.hasNext();) {
          Object e = it.next();
          if (type.isInstance(e)) {
              out.add(type.cast(e));
          }
      }
      out.sort(Comparator.comparing(Element::getName, Comparator.nullsLast(String::compareTo)));
      return out;
  }

  private static String fqnOf(Model m, Container c, Element e) {
    if (c instanceof Topic) return m.getName() + "." + c.getName() + "." + e.getName();
    return m.getName() + "." + e.getName();
  }

  private static Set<String> setOf(String... s) { return new LinkedHashSet<>(Arrays.asList(s)); }
}
