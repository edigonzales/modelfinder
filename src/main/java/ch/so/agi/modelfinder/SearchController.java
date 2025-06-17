package ch.so.agi.modelfinder;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class SearchController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final SearchService searchService;
    private final IndexingProperties properties;
    
    public SearchController(SearchService searchService, IndexingProperties properties) {
        this.searchService = searchService;
        this.properties = properties;
    }
    
    // http://localhost:8080/models?serverUrl=https://models.interlis.ch&file=tools/IliVErrors.ili    
    @GetMapping("/models")
    public ResponseEntity<?> getModels(
            @RequestParam(value = "query", required = false) String queryString, 
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file
            ) {        
        
        if (serverUrl == null && file == null) {
            return searchService.getDocumentsByQuery(queryString, properties.queryMaxRecords())
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        } 
        // return: eher empty list? -> einfacher für den client event. htmx?
        
        return searchService.getDocumentById(serverUrl, file)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());   
    }
    
    
}
