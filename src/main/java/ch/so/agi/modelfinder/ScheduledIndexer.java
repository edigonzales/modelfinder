package ch.so.agi.modelfinder;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledIndexer {

    private final IndexingService indexingService;

    public ScheduledIndexer(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @Async("asyncTaskExecutor")
    @Scheduled(cron="${indexing.cron-expression}")
    public void scheduledIndexing() {
        indexingService.performFullIndex();
    }
}
