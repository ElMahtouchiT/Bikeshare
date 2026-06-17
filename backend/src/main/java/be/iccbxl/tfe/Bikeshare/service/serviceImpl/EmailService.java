package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    /** Clé API Brevo : si présente, on envoie via l'API HTTPS (recommandé en prod). */
    @Value("${brevo.api.key:}")
    private String brevoApiKey;

    /** Adresse expéditrice (doit être un expéditeur vérifié dans Brevo). */
    @Value("${app.mail.from:no-reply@bikeshare.be}")
    private String fromEmail;

    @Value("${app.mail.from-name:BikeShare}")
    private String fromName;

    /** Envoi asynchrone : la requête HTTP ne doit jamais attendre l'envoi du mail (sinon la page « tourne »). */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            if (brevoApiKey != null && !brevoApiKey.isBlank()) {
                sendViaBrevo(to, subject, body);   // API HTTPS : passe par le 443 (non bloqué par Railway)
            } else if (mailSender != null) {
                sendViaSmtp(to, subject, body);    // SMTP : dev local, ou si Brevo non configuré
            }
        } catch (Exception e) {
            // On ne propage pas l'erreur : l'utilisateur reçoit un message générique quoi qu'il arrive.
            log.warn("Envoi d'email échoué pour {} : {}", to, e.getMessage());
        }
    }

    /** Envoi via l'API transactionnelle Brevo (POST https://api.brevo.com/v3/smtp/email). */
    private void sendViaBrevo(String to, String subject, String body) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sender", Map.of("email", fromEmail, "name", fromName));
        payload.put("to", List.of(Map.of("email", to)));
        payload.put("subject", subject);
        payload.put("textContent", body);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(7000);
        factory.setReadTimeout(10000);

        new RestTemplate(factory).postForEntity("https://api.brevo.com/v3/smtp/email",
                new HttpEntity<>(payload, headers), String.class);
        log.info("Email envoyé à {} via Brevo", to);
    }

    /** Envoi via SMTP (JavaMailSender) — utilisé en l'absence de clé Brevo. */
    private void sendViaSmtp(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email envoyé à {} via SMTP", to);
    }
}
