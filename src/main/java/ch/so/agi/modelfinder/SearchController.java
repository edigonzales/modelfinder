package ch.so.agi.modelfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class SearchController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final SearchService searchService;
    private final IndexingProperties properties;
    private final UmlMermaidService umlMermaidService;
    
    public SearchController(SearchService searchService, IndexingProperties properties, UmlMermaidService umlMermaidService) {
        this.searchService = searchService;
        this.properties = properties;
        this.umlMermaidService = umlMermaidService;
    }
    
    // http://localhost:8080/models?serverUrl=https://models.interlis.ch&file=tools/IliVErrors.ili    
    @GetMapping(path = "/models", produces = "application/json")
    public ResponseEntity<?> getModelsAsJson(
            @RequestParam(value = "query", required = false) String queryString, 
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file
            ) {        
        
        if (serverUrl == null && file == null) {
            return searchService.getDocumentsByQuery(queryString, properties.queryMaxRecords())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        }         
        return searchService.getDocumentById(serverUrl, file)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());   
    }
    
    @GetMapping(path = "/models", produces = "text/html")
    public ModelAndView getModelsAsHtml(
            @RequestParam(value = "query", required = false) String queryString, 
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file, 
            Model model
            ) {   
                
        List<ModelSearchResult> modelSearchResults = new ArrayList<>();
        if (queryString != null && !queryString.isEmpty()) {
            modelSearchResults = searchService.getDocumentsByQuery(queryString, properties.queryMaxRecords()).orElse(new ArrayList<>());            
        } 
        
        ModelAndView mav = new ModelAndView("search-results");
        mav.addObject("modelSearchResults", modelSearchResults);
        return mav;
    }

    @GetMapping(path = "/modelmetadata", produces = "text/html")
    public ModelAndView getModelMetadataAsHtml(
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file, 
            Model model
            ) { 
        
        ModelMetadata modelMetadata = null;
        if ((serverUrl != null && !serverUrl.isEmpty()) && (file != null && !file.isEmpty())) {
            modelMetadata = searchService.getDocumentById(serverUrl, file).orElse(null);
        }
        
        if (modelMetadata == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        
        ModelAndView mav = new ModelAndView("modelmetadata");
        mav.addObject("metadata", modelMetadata);
        return mav;
    }
    
    @GetMapping(path = "/uml-embedded", produces = "text/html")
    public String getEmbeddedUml(
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file, 
            Model model
            ) {

        ModelMetadata modelMetadata = null;
        if ((serverUrl != null && !serverUrl.isEmpty()) && (file != null && !file.isEmpty())) {
            modelMetadata = searchService.getDocumentById(serverUrl, file).orElse(null);
        }  
        
        String htmlString = "";
        if (modelMetadata == null) {
            htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
        } else {
            try {
                Path tempDir = Files.createTempDirectory("uml_");
                Path iliFile = tempDir.resolve(modelMetadata.name()+".ili");                
                Files.write(iliFile, modelMetadata.modelContent().getBytes());
                
                String umlDiagram = umlMermaidService.create(iliFile);
                
                if (umlDiagram == null) {
                    htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
                } else {
                    String mermaidString = umlDiagram;    
                    htmlString = "<pre class=\"mermaid\">\n"+mermaidString.replace("<<", "&#60;&#60;").replace(">>", "&#62;&#62;")+"</pre>";
                }
            } catch (IOException e) {
                e.printStackTrace();
                htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
            }
        }
        
        return htmlString;
    }
    
    @GetMapping(path = "/uml", produces = "text/html")
    public ModelAndView getPageUml(
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file, 
            Model model
            ) {

        ModelMetadata modelMetadata = null;
        if ((serverUrl != null && !serverUrl.isEmpty()) && (file != null && !file.isEmpty())) {
            modelMetadata = searchService.getDocumentById(serverUrl, file).orElse(null);
        }
        
//        String path = "/Users/stefan/tmp/foo.ili";
//        try {
//            Files.write(Paths.get(path), modelMetadata.modelContent().getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
        String htmlString = "";
        if (modelMetadata == null) {
            htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
        } else {
            try {
                Path tempDir = Files.createTempDirectory("uml_");
                Path iliFile = tempDir.resolve(modelMetadata.name()+".ili");                
                Files.write(iliFile, modelMetadata.modelContent().getBytes());
                
                String umlDiagram = umlMermaidService.create(iliFile);
                
                if (umlDiagram == null) {
                   //htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
                    htmlString = "could not create uml";
                } else {
                    String mermaidString = umlDiagram;    
                    htmlString = mermaidString;//.replace("<<", "&#60;&#60;").replace(">>", "&#62;&#62;");
                }
            } catch (IOException e) {
                e.printStackTrace();
                htmlString = "could not create uml";
            }
        }
        
        ModelAndView mav = new ModelAndView("uml");
        mav.addObject("mermaidString", htmlString);
        return mav;
    }

}
