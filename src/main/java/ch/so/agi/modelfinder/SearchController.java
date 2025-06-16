package ch.so.agi.modelfinder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    // /models?q=xxxx -> zusammenlegen
    
    
    // Sortieren!!! Wo?
    
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(value = "query", required = false) String queryString) {
        return searchService.getDocumentsByQuery(queryString, properties.queryMaxRecords())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());   
    }
    
    
    // http://localhost:8080/models?serverUrl=https://models.interlis.ch&file=tools/IliVErrors.ili
        
    // noch nicht fertig. ich kann hier alles reinpacken q=, nichts, nur serverUrl= aber nicht nur file=
    @GetMapping("/models")
    public ResponseEntity<?> getModels(@RequestParam(value = "serverUrl", required = false) String serverUrl, @RequestParam(value = "file", required = false) String file) {        
        return searchService.getDocumentById(serverUrl, file)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());   
    }
    
    
}
