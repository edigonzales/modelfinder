package ch.so.agi.modelfinder;

import java.util.List;

public record ModelSearchResult(
        String serverDisplayName,
        int modelCount,
        List<ModelMetadata> models
        ) {}
