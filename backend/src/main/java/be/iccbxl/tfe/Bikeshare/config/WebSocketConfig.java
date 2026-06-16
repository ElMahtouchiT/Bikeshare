package be.iccbxl.tfe.Bikeshare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/** Configuration WebSocket/STOMP pour la messagerie de réservation. */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // setAllowedOriginPatterns("*") : derrière le proxy TLS de Railway, le contrôle
        // same-origin par défaut rejette (403) le handshake du navigateur (l'app se croit
        // en http://interne:8080). On autorise les origines ; l'authentification reste
        // assurée par la session côté message STOMP.
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
