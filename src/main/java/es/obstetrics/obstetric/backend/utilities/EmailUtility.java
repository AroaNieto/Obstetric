package es.obstetrics.obstetric.backend.utilities;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailUtility {

    @Autowired
    JavaMailSender mailSender;

    public void sendEmail(String addressee, String subject, String body){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setText(body);
        mailMessage.setTo(addressee);
        mailMessage.setSubject(subject);

        mailSender.send(mailMessage);
    }

    public void sendEmail(String addressee, String subject, String body, String calendarEvent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_16.name());

        helper.setTo(addressee);
        helper.setSubject(subject);
        helper.setText(body);
        // Adjuntar archivo .ics
        helper.addAttachment("event.ics", new ByteArrayResource(calendarEvent.getBytes(StandardCharsets.UTF_8)), "text/calendar");

        mailSender.send(message);
    }
}
