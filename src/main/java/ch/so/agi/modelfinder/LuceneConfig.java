package ch.so.agi.modelfinder;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LuceneConfig {
    private final IndexingProperties properties;

    public LuceneConfig(IndexingProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    public Analyzer analyzer() {
        return new StandardAnalyzer();
    }

    @Bean
    public Directory luceneDirectory() throws IOException {
        return NIOFSDirectory.open(Paths.get(properties.directory()));
    }

    @Bean
    public IndexWriter indexWriter(Analyzer analyzer, Directory directory) throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        return new IndexWriter(directory, config);
    }
}
