package de.hub.mse.server.log;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.SendTo;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Data
@EqualsAndHashCode(callSuper = true)
public class ListenableAppender extends AppenderBase<ILoggingEvent> {

    private static final Set<Consumer<String>> LOG_LISTENERS = new HashSet<>();

    public static void addListener(Consumer<String> consumer) {
        LOG_LISTENERS.add(consumer);
    }

    // encoder is required. And it has to have legal getter/setter methods.
    private PatternLayoutEncoder encoder;

    @Override
    protected void append(ILoggingEvent eventObject) {

        // Use encoder to encode logs.
        String log = new String(this.encoder.encode(eventObject), StandardCharsets.UTF_8);

        // Push to client.
        LOG_LISTENERS.forEach(consumer -> consumer.accept(log));
    }
}
