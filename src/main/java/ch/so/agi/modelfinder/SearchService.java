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
import org.apache.lucene.search.TermQuery;
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
import java.util.Optional;

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
    // IliVErrors
    // https://models.interlis.ch
    // tools/IliVErrors.ili

    
    // http://localhost:8080/models?serverUrl=https://models.interlis.ch&file=tools/IliVErrors.ili
        
    public Optional<ModelMetadata> getDocumentById(String serverUrl, String file) {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            Term serverUrlTerm = new Term("serverUrl", serverUrl);
            Term fileTerm = new Term("file_exact", file);
            
            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
            queryBuilder.add(new TermQuery(serverUrlTerm), BooleanClause.Occur.MUST);
            queryBuilder.add(new TermQuery(fileTerm), BooleanClause.Occur.MUST);
            
            TopDocs results = searcher.search(queryBuilder.build(), 1);
            
            if (results.scoreDocs.length > 0) {
                Document document = searcher.storedFields().document(results.scoreDocs[0].doc);
                ModelMetadata metadata = mapDocument(document);
                return Optional.of(metadata);
            }
        } catch (IOException e) {
            log.error("Error searching Lucene index", e);
            throw new RuntimeException("Search failed", e);
        }
        return Optional.empty();
    }
    
    public Optional<List<ModelMetadata>> getDocumentsByQuery(String queryString, int limit) {
        Query query;

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

            TopDocs results = searcher.search(query, limit);
            
            if (results.scoreDocs.length == 0) {
                return Optional.empty();
            }

            List<ModelMetadata> metadataList = new ArrayList<>();
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document document = searcher.storedFields().document(scoreDoc.doc);
                ModelMetadata metadata = mapDocument(document);
                metadataList.add(metadata);
            }
            
            return Optional.of(metadataList);
        } catch (IOException | ParseException e) {
            log.error("Error searching Lucene index", e);
            throw new RuntimeException("Search failed", e);
        }
    }
    
    private ModelMetadata mapDocument(Document document) {
        ModelMetadata metadata = new ModelMetadata(
                document.get("serverUrl"),
                document.get("name"),
                document.get("dispName"),
                document.get("shortDescription"),
                document.get("version"),
                document.get("file"),
                document.get("schemaLanguage"),
                document.get("issuer"),
                document.get("precursorVersion"),
                document.get("technicalContact"),
                document.get("furtherInformation"),
                document.get("md5"),
                document.get("tags"),
                document.get("idGeoIV"),
                document.get("organisationName"),
                document.get("organisationAbbreviation"),
                null
                );
        return metadata;
    }
    
    public record SearchResult(
            String serverUrl,
            String name,
            String version,
            String dispName,
            String shortDescription
        ) {}
}
