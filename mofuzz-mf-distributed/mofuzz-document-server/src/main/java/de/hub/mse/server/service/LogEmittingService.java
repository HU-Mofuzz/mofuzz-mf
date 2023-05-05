package de.hub.mse.server.service;

import de.hub.mse.server.log.ListenableAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogEmittingService {

    public static final String TOPIC_LOGS = "/mofuzz/logs";

    private final SimpMessagingTemplate template;

    @Autowired
    public LogEmittingService(SimpMessagingTemplate template) {
        this.template = template;
        ListenableAppender.addListener(log -> template.convertAndSend(TOPIC_LOGS, log));
    }
}
