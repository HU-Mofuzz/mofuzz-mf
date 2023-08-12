package de.hub.mse.server.config;

import de.hub.mse.server.service.execution.AwsPersistence;
import de.hub.mse.server.service.execution.FilePersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilePersistenceConfig {

    @Bean
    public FilePersistence getFilePersistence(ServiceConfig config) {
        return new AwsPersistence(config);
    }
}
