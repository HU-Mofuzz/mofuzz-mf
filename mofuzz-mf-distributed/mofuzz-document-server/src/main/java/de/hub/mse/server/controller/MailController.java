package de.hub.mse.server.controller;

import de.hub.mse.server.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mail")
public class MailController {

    private static final String TEST_MAIL_SUBJECT = "Test Mail";
    private static final String TEST_MAIL_MESSAGE = "This message is a test.";

    private final MailService mailService;

    @Autowired
    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/test")
    public ResponseEntity<Void> testMail() {
        if(mailService.sendSimpleMessage(TEST_MAIL_SUBJECT, TEST_MAIL_MESSAGE)) {
            return ResponseEntity.ok(null);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
