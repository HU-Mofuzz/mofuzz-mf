package de.hub.mse.server.service;

import de.hub.mse.server.log.ListenableAppender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogEmittingService {

    public static final String TOPIC_LOGS = "/mofuzz/logs";

    @Autowired
    public LogEmittingService(SimpMessagingTemplate template) {
        ListenableAppender.addListener(log -> template.convertAndSend(TOPIC_LOGS, log));
    }
}
