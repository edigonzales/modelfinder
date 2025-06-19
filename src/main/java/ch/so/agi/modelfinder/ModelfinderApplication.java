package ch.so.agi.modelfinder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.scheduling.annotation.EnableScheduling;

@ImportRuntimeHints(ResourceRuntimeHints.class)
@EnableScheduling
@Configuration
@EnableConfigurationProperties({IndexingProperties.class})
@SpringBootApplication
public class ModelfinderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModelfinderApplication.class, args);
    }

}
