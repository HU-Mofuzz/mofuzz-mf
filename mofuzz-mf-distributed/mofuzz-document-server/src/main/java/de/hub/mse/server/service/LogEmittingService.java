package de.hub.mse.server.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.TeeOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class LogEmittingService {

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    public static final String TOPIC_LOGS = "/mofuzz/logs";

    @Autowired
    public LogEmittingService(SimpMessagingTemplate template) throws IOException {

        var pipedInput = new PipedInputStream();
        var separateStream = new PipedOutputStream(pipedInput);
        TeeOutputStream teeStream = new TeeOutputStream(System.err, separateStream);
        System.setErr(new PrintStream(teeStream));

        service.submit(new StreamGobbler(pipedInput, template));
    }

    @AllArgsConstructor
    private static class StreamGobbler implements Runnable {

        private final InputStream stream;
        private final SimpMessagingTemplate template;
        @SneakyThrows
        @Override
        public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                template.convertAndSend(TOPIC_LOGS, line+'\n');
            }
        }
    }
}
