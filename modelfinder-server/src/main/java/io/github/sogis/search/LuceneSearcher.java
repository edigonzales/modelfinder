package io.github.sogis.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.modelscan.IliFile;
import ch.interlis.ilirepository.IliFiles;
import ch.interlis.ilirepository.IliManager;
import ch.interlis.ilirepository.impl.ModelMetadata;
import ch.interlis.ilirepository.impl.RepositoryAccess;
import ch.interlis.ilirepository.impl.RepositoryAccessException;
import io.github.sogis.Settings;

@Repository("LuceneSearcher")
public class LuceneSearcher {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
    private Settings settings;

//    NIOFSDirectory oldIndex;
    static final String INDEX_DIR = "/Users/stefan/tmp/lucene/"; 
    NIOFSDirectory fsIndex;
    StandardAnalyzer analyzer;
    private QueryParser queryParser;

    // PostConstruct: Anwendung ist noch nicht fertig gestartet / ready -> Abklären wegen liveness und readyness probes.
    //@PostConstruct
    public void init() throws IOException {
        log.info("Building index ...");
        
//        Path indexDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), "modelfinder_idx");
        Path indexDir = Paths.get(INDEX_DIR);
        log.info("Index folder: " + indexDir);
        
        fsIndex = new NIOFSDirectory(indexDir);
        analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(fsIndex, indexWriterConfig);
        writer.prepareCommit();  
        
        writer.deleteAll();
        
        try {
            IliManager manager = new IliManager();
            manager.setRepositories(settings.getDefaultRepositories().toArray(new String[0]));

            List<String> repositories = settings.getRepositories();
            for (String repository : repositories) {
                log.info(repository);

                RepositoryAccess repoAccess = new RepositoryAccess();
                IliFiles iliFiles = repoAccess.getIliFiles(repository);
                log.info(iliFiles.toString());

                List<ModelMetadata> modelMetadataList = repoAccess.readIlimodelsXml2(repository);
                for (ModelMetadata modelMetadata : modelMetadataList) {
                    Document document = new Document();
                    document.add(new TextField("name", modelMetadata.getName(), Store.YES));
                    document.add(new TextField("version", modelMetadata.getVersion(), Store.YES));
                    document.add(new TextField("file", modelMetadata.getFile(), Store.YES));
                    document.add(new TextField("repository", repository, Store.YES));
                    if (modelMetadata.getFile().contains("replaced") || modelMetadata.getFile().contains("obsolete")) {
                        document.add(new DoubleDocValuesField("boost", 0.5));
                    }
                    if (modelMetadata.getIssuer() != null) {
                        document.add(new TextField("issuer", modelMetadata.getIssuer(), Store.YES));
                    }
                    if (modelMetadata.getPrecursorVersion() != null) {
                        document.add(new TextField("precursorversion", modelMetadata.getPrecursorVersion(), Store.YES));
                    }
                    if (modelMetadata.getTechnicalContact() != null) {
                        document.add(new TextField("technicalcontact", modelMetadata.getTechnicalContact(), Store.YES));
                    } 
                    if (modelMetadata.getFurtherInformation() != null) {
                        document.add(new TextField("furtherinformation", modelMetadata.getFurtherInformation(), Store.YES));
                    } 
                    if (modelMetadata.getFurtherInformation() != null) {
                        document.add(new TextField("md5", modelMetadata.getMd5(), Store.YES));
                    }                     
                    if (!modelMetadata.getSchemaLanguage().equalsIgnoreCase(modelMetadata.ili1)) {                    
                        ArrayList<String> ilifiles = new ArrayList<String>();
                        ilifiles.add(modelMetadata.getName());
                        Configuration config = manager.getConfigWithFiles(ilifiles);
                        TransferDescription td = Ili2c.runCompiler(config);
    
                        Model model = td.getLastModel();
                        ch.ehi.basics.settings.Settings msettings = model.getMetaValues();
                        Iterator<String> jt = msettings.getValuesIterator();
                        while (jt.hasNext()) {
                            String key = jt.next();
                            if (key.equalsIgnoreCase("IDGeoIV")) {
                                document.add(new TextField("idgeoiv", msettings.getValue(key), Store.YES));
                            }                        
                        }
                    }
                    
                    // TODO: whole model as text

                    writer.addDocument(document);
                }           
            }      
        } catch (RepositoryAccessException | Ili2cException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            writer.rollback();
        }
        
        writer.commit();
        writer.close();
        
//        Iterator<IliFile> it = iliFiles.iteratorFile();
//        while(it.hasNext()) {
//            IliFile iliFile = it.next();
//            log.info(iliFile.getFilename().toString());
//        }
        
//        this.oldIndex = this.fsIndex;
//        this.fsIndex = tmpIndex;
//        
//        if (this.oldIndex != null) {
//            log.info("*********************");
//            log.info(oldIndex.getDirectory().toFile().getAbsolutePath());
//        }
    }

    @PreDestroy
    private void close() {
        try {
            fsIndex.close();
            log.info("Lucene Index closed");
        } catch (IOException e) {
            log.warn("Issue closing Lucene Index: " + e.getMessage());
        }
    }
    
    /**
     * Search Lucene Index for records matching querystring
     * @param querystring - human written query string from e.g. a search form
     * @param numRecords - number of requested records 
     * @param showAvailable - check for number of matching available records 
     * @return Top Lucene query results as a Result object
     * @throws LuceneSearcherException 
     * @throws InvalidLuceneQueryException 
     */
    public Result searchIndex(String queryString, int numRecords, boolean showAvailable)
            throws LuceneSearcherException, InvalidLuceneQueryException {
        IndexReader reader = null;
        IndexSearcher indexSearcher = null;
        Query query;
        TopDocs documents;
        TotalHitCountCollector collector = null;
        try {
            reader = DirectoryReader.open(fsIndex);
            indexSearcher = new IndexSearcher(reader);
            queryParser = new QueryParser("name", analyzer); // 'name' is default field if we don't prefix search string
            queryParser.setAllowLeadingWildcard(true);
            
            String luceneQueryString = "";
            String[] splitedQuery = queryString.split("\\s+");
            for (int i=0; i<splitedQuery.length; i++) {
                String token = splitedQuery[i];
                log.info("***"+token);
                
                // Das Feld, welches bestimmend sein soll (also in der Suche zuoberst gelistet), bekommt
                // einen sehr hohen Boost.
                luceneQueryString += "(name:*" + token + "*^10 OR "
                        + "version:*" + token + "* OR "
                        + "file:*" + token + "* OR "
                        + "issuer:*" + token + "* OR "
                        + "technicalcontact:*" + token + "* OR "
                        + "furtherinformation:*" + token + "* OR "
                        //+ "md5:" + token + "* OR "
                        + "idgeoiv:" + token + "*^20 "
                                + ")";
                if (i<splitedQuery.length-1) {
                    luceneQueryString += " AND ";
                }
            }
                        
            Query tmpQuery = queryParser.parse(luceneQueryString);
            query = FunctionScoreQuery.boostByValue(tmpQuery, DoubleValuesSource.fromDoubleField("boost"));
            
            log.info("'" + luceneQueryString + "' ==> '" + query.toString() + "'");
            
            if (showAvailable) {
                collector = new TotalHitCountCollector();
                indexSearcher.search(query, collector);
            }
            documents = indexSearcher.search(query, numRecords);
            log.info("{}", documents.totalHits.value);
            List<Map<String, String>> mapList = new LinkedList<Map<String, String>>();
            for (ScoreDoc scoreDoc : documents.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
                Map<String, String> docMap = new HashMap<String, String>();
                List<IndexableField> fields = document.getFields();
                for (IndexableField field : fields) {
                    docMap.put(field.name(), field.stringValue());
                }
                mapList.add(docMap);
            }
            
            log.info("{}", mapList.size());
            log.info("{}", numRecords);
            
            Result result = new Result(mapList, mapList.size(),
                    collector == null ? (mapList.size() < numRecords ? mapList.size() : -1) : collector.getTotalHits());
            return result;
        } catch (ParseException e) {
            e.printStackTrace();            
            throw new InvalidLuceneQueryException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new LuceneSearcherException(e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ioe) {
                log.warn("Could not close IndexReader: " + ioe.getMessage());
            }
        }
    }
}
