package de.hub.mse.server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@PropertySource("file:${SERVICE_CONFIG_PATH:service.properties}")
@Getter
public class ServiceConfig {

    @Value("${service.mail.host:localhost}")
    private String smtpHost;
    @Value("${service.mail.port:587}")
    private int smtpPort;
    @Value("${service.mail.user:user}")
    private String smtpUser;
    @Value("${service.mail.password:password}")
    private String smtpPassword;
    @Value("${service.mail.receiver:}")
    private String[] mailReceiver;
    @Value("${service.mail.from:}")
    private String mailFrom;

    @Value("${service.documents.dir:${user.home}}")
    private String documentDirectory;

    @Value("${service.health.cpuWarnQuota:0.9}")
    private double healthCpuWarnQuota;

    @Value("${service.health.memoryWarnQuota:0.9}")
    private double healthMemoryWarnQuota;
    @Value("${service.health.diskWarnQuota:0.8}")
    private double healthDiskWarnQuota;

    @Value("${service.persistence.bucket:mofuzz-bucket1}")
    private String awsBucketId;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(smtpHost);
        mailSender.setPort(smtpPort);

        mailSender.setUsername(smtpUser);
        mailSender.setPassword(smtpPassword);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return mailSender;
    }

}

