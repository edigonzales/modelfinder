package ch.so.agi.modelfinder;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LuceneIndexService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final IndexWriter indexWriter;
    private final IndexingProperties properties;

    public LuceneIndexService(IndexWriter indexWriter, IndexingProperties properties) {
        this.indexWriter = indexWriter;
        this.properties = properties;
    }
    
    public void index(List<ModelMetadata> models) {
        if (models.isEmpty()) return;
        
        String serverUrl = models.get(0).serverUrl();
        
        try {            
            log.debug("Deleting: " + serverUrl);
            
            Term term = new Term("serverUrl", serverUrl);
            TermQuery query = new TermQuery(term);
            indexWriter.deleteDocuments(query );
            
            for (var metadata : models) {
                Document doc = new Document();            
            
                doc.add(new StringField("serverUrl", serverUrl, Field.Store.YES)); // Muss StringField (resp. jedenfalls nicht TextField) sind, sonst kann es nicht als Löschfilter verwendet werden.
                doc.add(new TextField("name", metadata.name(), Field.Store.YES));
                doc.add(new StoredField("dispName", nullable(metadata.dispName())));
                doc.add(new TextField("shortDescription", nullable(metadata.shortDescription()), Field.Store.YES));
                doc.add(new TextField("version", metadata.version(), Field.Store.YES));
                doc.add(new TextField("file", nullable(metadata.file()), Field.Store.YES));
                doc.add(new StringField("file_exact", nullable(metadata.file()), Field.Store.NO)); // For exact searches
                doc.add(new StringField("schemaLanguage", nullable(metadata.schemaLanguage()), Field.Store.YES));
                doc.add(new TextField("issuer", nullable(metadata.issuer()), Field.Store.YES));
                doc.add(new TextField("precursorVersion", nullable(metadata.precursorVersion()), Field.Store.YES));
                doc.add(new TextField("technicalContact", nullable(metadata.technicalContact()), Field.Store.YES));
                doc.add(new TextField("furtherInformation", nullable(metadata.furtherInformation()), Field.Store.YES));
                doc.add(new TextField("md5", nullable(metadata.md5()), Field.Store.YES));
                doc.add(new TextField("tags", nullable(metadata.tags()), Field.Store.YES));
                doc.add(new StringField("idGeoIV", nullable(metadata.idGeoIV()), Field.Store.YES));
                doc.add(new TextField("organisationName", nullable(metadata.organisationName()), Field.Store.YES));
                doc.add(new StringField("organisationAbbreviation", nullable(metadata.organisationAbbreviation()), Field.Store.YES));                
                doc.add(new StoredField("modelContent", nullable(metadata.modelContent())));
                indexWriter.addDocument(doc );
            }

            indexWriter.flush();
            indexWriter.commit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to index ModelMetadata", e);
        }
    }
    
    private String nullable(String s) {
        return s == null ? "" : s;
    }    
}
