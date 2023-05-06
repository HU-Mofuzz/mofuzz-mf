package de.hub.mse.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableConfigurationProperties
@EnableWebSocketMessageBroker
@EnableScheduling
public class MofuzzDocumentServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MofuzzDocumentServerApplication.class, args);
	}

}
