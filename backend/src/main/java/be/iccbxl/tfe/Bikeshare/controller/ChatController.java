package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.model.ChatMessage;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ChatMessageService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.NotificationService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/** Messagerie temps réel par réservation (WebSocket/STOMP). */
@Controller
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired private ChatMessageService chatMessageService;
    @Autowired private ReservationService reservationService;
    @Autowired private EmailService emailService;
    @Autowired private NotificationService notificationService;

    @MessageMapping("/chat/{reservationId}")
    @SendTo("/topic/messages/{reservationId}")
    public ChatMessageDTO sendMessage(ChatMessageDTO dto,
                                      @DestinationVariable Long reservationId,
                                      Principal principal) {
        if (principal == null) throw new RuntimeException("No authentication available");

        CustomUserDetail userDetails =
                (CustomUserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long currentUserId = userDetails.getUser().getId();

        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) throw new RuntimeException("Reservation not found");

        Long renterId = reservation.getUser().getId();
        Long ownerId = reservation.getBike().getUser().getId();

        if (currentUserId.equals(renterId)) {
            dto.setFromUserId(renterId);
            dto.setToUserId(ownerId);
        } else {
            dto.setFromUserId(ownerId);
            dto.setToUserId(renterId);
        }

        ChatMessage message = new ChatMessage();
        message.setContent(dto.getContent());
        message.setReservation(reservation);
        message.setFromUserId(dto.getFromUserId());
        message.setToUserId(dto.getToUserId());
        message.setSentAt(LocalDateTime.now());
        chatMessageService.save(message);

        // Notifier le destinataire du nouveau message (cloche + page notifications)
        try {
            User renter = reservation.getUser();
            User owner  = reservation.getBike().getUser();
            boolean currentIsRenter = currentUserId.equals(renterId);
            User from = currentIsRenter ? renter : owner;
            User to   = currentIsRenter ? owner  : renter;
            String preview = dto.getContent();
            if (preview != null && preview.length() > 80) preview = preview.substring(0, 80) + "…";
            notificationService.notify(to, from, reservation.getBike(), "MESSAGE", preview,
                    "/account/reservations/" + reservationId + "/chat");
        } catch (Exception e) {
            logger.warn("Notification de message non créée : {}", e.getMessage());
        }

        return MapperDTO.toChatMessageDTO(message);
    }
}
