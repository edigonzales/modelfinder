package ch.so.agi.modelfinder;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.lucene.search.BooleanClause;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final Directory directory;
    private final Analyzer analyzer;
    
    private QueryParser queryParser;

    public SearchService(Directory directory, Analyzer analyzer) {
        this.directory = directory;
        this.analyzer = analyzer;
    }
    
    // https://chat.deepseek.com/a/chat/s/94ab1ea4-9dd1-463a-855f-6b224b836ccb
    
    public List<Document> search(String queryString, int limit) {
        Query query;
        TopDocs documents;


        if (queryString == null || queryString.trim().isEmpty()) {
            query = new MatchAllDocsQuery();
        } else {
            queryParser = new QueryParser("name", analyzer);
            queryParser.setAllowLeadingWildcard(true);
        }

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            String luceneQueryString = "";
            String[] splitedQuery = queryString.split("\\s+");
            for (int i=0; i<splitedQuery.length; i++) {
                String token = QueryParser.escape(splitedQuery[i]);
                log.debug("token: " + token);

                luceneQueryString += "(name:*" + token + "*^10 OR "
                        + "file:*" + token + "* OR "
                        + "title:*" + token + "* OR "
                        + "issuer:*" + token + "* OR "
                        + "organisation:*" + token + "* OR "
                        + "technicalContact:*" + token + "* OR "
                        + "furtherInformation:*" + token + "* OR "
                        + "idGeoIV:" + token + "*^20";
                luceneQueryString += ")";
                if (i<splitedQuery.length-1) {
                    luceneQueryString += " AND ";
                }
            }
            log.debug(luceneQueryString);
            
            Query tmpQuery = queryParser.parse(luceneQueryString);
            query = FunctionScoreQuery.boostByValue(tmpQuery, DoubleValuesSource.fromDoubleField("boost"));

            documents = searcher.search(query, limit);

            List<Map<String, String>> result = new LinkedList<Map<String, String>>();
            for (ScoreDoc scoreDoc : documents.scoreDocs) {
                Document document = searcher.storedFields().document(scoreDoc.doc);
                System.out.println(document.get("name"));
                System.out.println(scoreDoc.score);

//                Explanation explanation = searcher.explain(query, scoreDoc.doc);
//                log.debug(explanation.toString());
                
                Map<String, String> docMap = new HashMap<String, String>();
                List<IndexableField> fields = document.getFields();
                for (IndexableField field : fields) {
                    docMap.put(field.name(), field.stringValue());
                }
                result.add(docMap);
            }
            
            return null;
        } catch (IOException | ParseException e) {
            log.error("Error searching Lucene index", e);
            throw new RuntimeException("Search failed", e);
        }
    }
    
    public record SearchResult(
            String serverUrl,
            String name,
            String version,
            String dispName,
            String shortDescription
        ) {}
}
