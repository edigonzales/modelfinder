package ch.so.agi.modelfinder;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StartupIndexer implements ApplicationRunner {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final IndexingService indexingService;
    private final IndexingProperties properties;
    
    public StartupIndexer(IndexingService indexingService, IndexingProperties properties) {
        this.indexingService = indexingService;
        this.properties = properties;
    }
    
    @Override
    public synchronized void run(ApplicationArguments args) throws Exception {
        if (properties.reindexOnStartup()) {
            indexingService.performFullIndex();
        }        
    }
}
