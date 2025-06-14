package ch.so.agi.modelfinder;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.interlis.ilirepository.impl.ModelLister;
import ch.interlis.ilirepository.impl.ModelMetadata;
import ch.interlis.ilirepository.impl.RepositoryAccess;
import ch.interlis.ilirepository.impl.RepositoryAccessException;
import ch.interlis.ilirepository.impl.RepositoryVisitor;

@Service
public class RemoteReaderService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final IndexingProperties properties;

    public RemoteReaderService(IndexingProperties properties) {
        this.properties = properties;
    }
    
    public Optional<String> fetchData(String serverUrl) {
        // Use your special XML reader library here
        // return Optional.of(parsedData);
        
        try {
            List<String> repositories = properties.repositories();
            RepositoryAccess repoAccess = new RepositoryAccess();
            
            ModelLister modelLister = new ModelLister();
            modelLister.setIgnoreDuplicates(true);
            
            RepositoryVisitor visitor = new RepositoryVisitor(repoAccess, modelLister);
            visitor.setRepositories(repositories.toArray(new String[repositories.size()]));
            visitor.visitRepositories();           
            
            List<ModelMetadata> mergedModelMetadatav = modelLister.getResult2();
            log.debug("mergedModelMetadatav: {}", mergedModelMetadatav.size());
                      
            List<ModelMetadata> latestMergedModelMetadatav = RepositoryAccess.getLatestVersions2(mergedModelMetadatav);
            log.debug("latestMergedModelMetadatav: {}", latestMergedModelMetadatav.size());  
            
            for (ModelMetadata modelMetadata : latestMergedModelMetadatav) {
                //addDocument(modelMetadata, false);
                System.out.println(modelMetadata);
            }
            
        } catch (RepositoryAccessException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Optional.empty();
        }        
        return Optional.empty(); // placeholder
    }
}
