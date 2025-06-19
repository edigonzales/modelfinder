package ch.so.agi.modelfinder;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "indexing")
public record IndexingProperties(
        boolean reindexOnStartup,
        int intervalHours,
        String cronExpression,
        List<String> repositories,
        String directory,
        int queryMaxRecords
    ) {}