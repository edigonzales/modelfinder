package ch.so.agi.modelfinder;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class StartupIndexer implements ApplicationRunner {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private final IndexingProperties properties;
    private final RemoteReaderService remoteReaderService; // wird wieder entfernt
    
    public StartupIndexer(IndexingProperties properties, RemoteReaderService remoteReaderService) {
        this.properties = properties;
        this.remoteReaderService = remoteReaderService;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        log.info(properties.toString());
        
        remoteReaderService.fetchData("foo");
    }

}
