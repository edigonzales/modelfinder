package ch.so.agi.modelfinder;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "indexing")
public record IndexingProperties(
        boolean reindexOnStartup,
        int intervalHours,
        List<String> repositories,
        String directory,
        int queryMaxRecords
    ) {}