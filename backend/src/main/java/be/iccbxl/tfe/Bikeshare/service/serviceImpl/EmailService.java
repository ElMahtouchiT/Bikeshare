package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /** Envoi asynchrone : la requête HTTP ne doit jamais attendre le SMTP (sinon la page « tourne »). */
    @Async
    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null) return; // mail non configuré
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // SMTP indisponible ou bloqué (ex. port sortant filtré par l'hébergeur) :
            // on ne propage pas l'erreur, on se contente de la tracer.
            log.warn("Envoi d'email échoué pour {} : {}", to, e.getMessage());
        }
    }
}
