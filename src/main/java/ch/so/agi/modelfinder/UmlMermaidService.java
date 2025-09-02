package ch.so.agi.modelfinder;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.config.GenerateOutputKind;
import ch.interlis.ili2c.metamodel.TransferDescription;

@Service
public class UmlMermaidService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Path create(Path iliFile) {
        Configuration config = new Configuration();
        config.addFileEntry(new FileEntry(iliFile.toFile().getAbsolutePath(), FileEntryKind.ILIMODELFILE));
        config.setGenerateWarnings(false);
        config.setOutputKind(GenerateOutputKind.NOOUTPUT);
        config.setAutoCompleteModelList(true);
        
        Settings settings = new Settings();
        settings.setValue(Ili2cSettings.ILIDIRS, Ili2cSettings.DEFAULT_ILIDIRS);

        TransferDescription td = ch.interlis.ili2c.Main.runCompiler(config, settings);
        
        String umlDiagram = Ili2Mermaid.render(td);
        
        System.err.println(umlDiagram);
        
        return null;
    }
}
