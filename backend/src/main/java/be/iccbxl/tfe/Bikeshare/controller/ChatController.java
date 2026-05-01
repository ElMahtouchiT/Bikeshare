package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.model.ChatMessage;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ChatMessageService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.EmailService;
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

        return MapperDTO.toChatMessageDTO(message);
    }
}
