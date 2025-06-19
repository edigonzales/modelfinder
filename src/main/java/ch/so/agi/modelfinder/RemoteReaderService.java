package ch.so.agi.modelfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.interlis.ilirepository.impl.ModelLister;
import ch.interlis.ilirepository.impl.RepositoryAccess;
import ch.interlis.ilirepository.impl.RepositoryAccessException;

@Service
public class RemoteReaderService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public Optional<List<ModelMetadata>> fetchData(String serverUrl) {
        log.info("Processing repository: {}", serverUrl);
        List<ModelMetadata> modelMetadataRecordList = new ArrayList<>();
        try {
            RepositoryAccess repoAccess = new RepositoryAccess();
            
            ModelLister modelLister = new ModelLister();
            modelLister.setIgnoreDuplicates(true);
            
            List<ch.interlis.ilirepository.impl.ModelMetadata> modelMetadataList = repoAccess.readIlimodelsXml2(serverUrl);
            for (var modelMetadata : modelMetadataList) {                
                if (modelMetadata.getFile().contains("obsolete") || modelMetadata.getFile().contains("replaced")) {
                    continue;
                }
                log.debug("Processing model: {}", modelMetadata.getName());

                String idGeoIV = modelMetadata.getFile().contains("geo.admin.ch") ? modelMetadata.getTags() : null;
                String modelContent = readUrlToString(serverUrl + "/" + modelMetadata.getFile());
                Organisation organisation = getOrganisation(serverUrl);
                
                ModelMetadata modelMetadataRecord = new ModelMetadata(
                        serverUrl,
                        modelMetadata.getName(),
                        modelMetadata.getName() + " (" + modelMetadata.getVersion() + ")",
                        modelMetadata.getShortDescription(),
                        modelMetadata.getVersion(),
                        modelMetadata.getFile(),
                        modelMetadata.getSchemaLanguage(),
                        modelMetadata.getIssuer(),
                        modelMetadata.getPrecursorVersion(),
                        modelMetadata.getTechnicalContact(),
                        modelMetadata.getFurtherInformation(),
                        modelMetadata.getMd5(),
                        modelMetadata.getTags(),
                        idGeoIV,
                        organisation.name(),
                        organisation.abbreviation(),
                        modelContent
                        );
                modelMetadataRecordList.add(modelMetadataRecord);
            }
        } catch (RepositoryAccessException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return Optional.empty();
        }        
        return Optional.of(modelMetadataRecordList);
    }
    
    private Organisation getOrganisation(String serverUrl) {
        Organisation organisation = null; 
        
        switch (serverUrl) {
            case String s when s.contains("geo.so") ->
                organisation = new Organisation("Solothurn", "SO");
            case String s when s.contains("geo.ai") ->
                organisation = new Organisation("Appenzell Innerrhoden", "AI");
            case String s when s.contains("geo.ar") ->
                organisation = new Organisation("Appenzell Ausserrhoden", "AR");
            case String s when s.contains("geo.be") ->
                organisation = new Organisation("Bern", "BE");
            case String s when s.contains("geo.bl") ->
                organisation = new Organisation("Basel-Landschaft", "BL");
            case String s when s.contains("geo.bs") ->
                organisation = new Organisation("Basel-Stadt", "BS");
            case String s when s.contains("geo.gl") ->
                organisation = new Organisation("Glarus", "GL");
            case String s when s.contains("geo.gr") ->
                organisation = new Organisation("Graubünden", "GR");
            case String s when s.contains("geo.llv") -> 
                organisation = new Organisation("Liechtenstein", "LI"); 
            case String s when s.contains("geo.lu") ->
                organisation = new Organisation("Luzern", "LU");
            case String s when s.contains("geo.sg") ->
                organisation = new Organisation("St. Gallen", "SG");
            case String s when s.contains("geo.sh") ->
                organisation = new Organisation("Schaffhausen", "SH");
            case String s when s.contains("geo.sz") ->
                organisation = new Organisation("Schwyz", "SZ");
            case String s when s.contains("geo.ti") ->
                organisation = new Organisation("Ticino", "TI");
            case String s when s.contains("geo.zg") ->
                organisation = new Organisation("Zug", "ZG");
            case String s when s.contains("geo.zh") ->
                organisation = new Organisation("Zürich", "ZH");
            case String s when s.contains("models.interlis.ch") ->
                organisation = new Organisation("models.interlis.ch", "INTERLIS");
            case String s when s.contains("models.kgk-cgc.ch") ->
                organisation = new Organisation("kgk-cgc.ch", "KGK-CGC");
            default -> {
                log.warn("No matching administration found for: " + serverUrl);
            }
        }
        return organisation;
    }
    
    private String readUrlToString(String urlString) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                    content.append(System.lineSeparator()); // Add back line separators
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Error reading URL: " + urlString + " - " + e.getMessage());
            return null;
        }
        return content.toString();
    }
}
