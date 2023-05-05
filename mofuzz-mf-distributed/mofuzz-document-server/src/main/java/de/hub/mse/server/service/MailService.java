package de.hub.mse.server.service;

import de.hub.mse.server.config.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    private final ServiceConfig serviceConfig;
    private final MailSender mailSender;

    @Autowired
    public MailService(ServiceConfig serviceConfig, MailSender mailSender) {
        this.serviceConfig = serviceConfig;
        this.mailSender = mailSender;
    }

    public void sendSimpleMessageOrThrow(String subject, String message) throws MailException {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(serviceConfig.getMailFrom());
        simpleMailMessage.setTo(serviceConfig.getMailReceiver());
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(message);

        mailSender.send(simpleMailMessage);
        log.info("Sent mail with subject [{}]", subject);
    }

    public boolean sendSimpleMessage(String subject, String message) {
        try {
            sendSimpleMessageOrThrow(subject, message);
            return true;
        } catch (MailException e) {
            log.error("Error sending mail!", e);
            return false;
        }
    }


}
