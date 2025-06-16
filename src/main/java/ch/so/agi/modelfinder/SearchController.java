package ch.so.agi.modelfinder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final SearchService searchService;
    private final IndexingProperties properties;
    
    public SearchController(SearchService searchService, IndexingProperties properties) {
        this.searchService = searchService;
        this.properties = properties;
    }
    
    // Ggf Endpunkt umbenennen, da wir einen weiteren für den Modelcontent benötigen.
    // /models?q=xxxx
    // /models/serverUrl/file ? geht das wegen url? encoden? dito ja auch file.
    @GetMapping("/search")
    public List<?> search(@RequestParam(value = "query", required = false) String queryString) {
        return searchService.getDocumentsByQuery(queryString, properties.queryMaxRecords());
    }
}
