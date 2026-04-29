package edu.tcu.projectpulse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@Slf4j
public class EmailService {

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.host:}")
    private String host;

    @Value("${app.mail.port:587}")
    private int port;

    @Value("${app.mail.username:}")
    private String username;

    @Value("${app.mail.password:}")
    private String password;

    @Value("${app.mail.from:}")
    private String from;

    public boolean sendEmail(String to, String subject, String body) {
        if (!enabled || isBlank(host) || isBlank(username) || isBlank(password)) {
            log.info("Email delivery is disabled or incomplete. Prepared email for {} with subject '{}'.", to, subject);
            return false;
        }

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties properties = sender.getJavaMailProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(isBlank(from) ? username : from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
            return true;
        } catch (RuntimeException ex) {
            log.warn("Email delivery failed for {}. The invitation was still stored.", to, ex);
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
