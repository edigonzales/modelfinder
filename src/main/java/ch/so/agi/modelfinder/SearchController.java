package ch.so.agi.modelfinder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import ch.so.agi.umleditor.UmlDiagramVendor;
import ch.so.agi.umleditor.UmlEditorUtility;

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
        return searchService.getDocumentById(serverUrl, file)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());   
    }
    
    @GetMapping(path = "/models", produces = "text/html")
    public ModelAndView getModelsAsHtml(
            @RequestParam(value = "query", required = false) String queryString, 
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file, 
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

    @GetMapping(path = "/modelmetadata", produces = "text/html")
    public ModelAndView getModelMetadataAsHtml(
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file, 
            Model model
            ) { 
        
        ModelMetadata modelMetadata = null;
        if ((serverUrl != null && !serverUrl.isEmpty()) && (file != null && !file.isEmpty())) {
            modelMetadata = searchService.getDocumentById(serverUrl, file).orElse(null);
        }
        
        if (modelMetadata == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        
        ModelAndView mav = new ModelAndView("modelmetadata");
        mav.addObject("metadata", modelMetadata);
        return mav;
    }
    
    @GetMapping(path = "/uml", produces = "text/html")
    public String getUmlAsHtml(
            @RequestParam(value = "serverUrl", required = false) String serverUrl, 
            @RequestParam(value = "file", required = false) String file, 
            Model model
            ) {

        ModelMetadata modelMetadata = null;
        if ((serverUrl != null && !serverUrl.isEmpty()) && (file != null && !file.isEmpty())) {
            modelMetadata = searchService.getDocumentById(serverUrl, file).orElse(null);
        }
        
        String htmlString = "";
        if (modelMetadata == null) {
            htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
        } else {
            try {
                Path tempDir = Files.createTempDirectory("uml_");
                Path iliFile = tempDir.resolve(modelMetadata.name()+".ili");
                FileUtils.writeStringToFile(iliFile.toFile(), modelMetadata.modelContent(), Charset.defaultCharset());
                
                Path umlFile = UmlEditorUtility.createUmlDiagram(iliFile, null, tempDir, UmlDiagramVendor.MERMAID);
                
                if (umlFile == null) {
                    htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
                } else {
                    String mermaidString = Files.readString(umlFile);                    
                    htmlString = "<pre class=\"mermaid\">\n"+mermaidString+"</pre>";
                }
            } catch (IOException e) {
                e.printStackTrace();
                htmlString = "<pre class=\"mermaid\">could not create uml</pre>";
            }
        }
        return htmlString;
    }
}
