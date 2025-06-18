package ch.so.agi.modelfinder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;


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
    @GetMapping(path = "/models", produces = "application/json")
    public ResponseEntity<?> getModelsAsJson(
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
    
    @GetMapping(path = "/models", produces = "text/html")
    public ModelAndView getModelsAsHtml(
            @RequestParam(value = "query", required = false) String queryString, 
            Model model
            ) {   
                
        List<ModelSearchResult> modelSearchResults = new ArrayList<>();
        if (queryString != null && !queryString.isEmpty()) {
            modelSearchResults = searchService.getDocumentsByQuery(queryString, properties.queryMaxRecords()).orElse(new ArrayList<>());            
        }

        ModelAndView mav = new ModelAndView("search-results");
        mav.addObject("modelSearchResults", modelSearchResults);
        return mav;
    }

    
}
