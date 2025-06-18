package ch.so.agi.modelfinder;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IndexingService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final RemoteReaderService readerService;
    private final LuceneIndexService indexService;
    private final IndexingProperties properties;
    
    public IndexingService(RemoteReaderService readerService, LuceneIndexService indexService,
            IndexingProperties properties) {
        this.readerService = readerService;
        this.indexService = indexService;
        this.properties = properties;
    }
    
    public void performFullIndex() {
        properties.repositories().forEach(server -> {
            Optional<List<ModelMetadata>> data = readerService.fetchData(server);
            data.ifPresent(indexService::index);
        });
    }    
}
