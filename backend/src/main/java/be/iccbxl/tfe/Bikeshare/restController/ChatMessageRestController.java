package be.iccbxl.tfe.Bikeshare.restController;

import be.iccbxl.tfe.Bikeshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message Management", description = "Gestion des messages de réservation")
public class ChatMessageRestController {

    @Autowired private ChatMessageService chatMessageService;

    @Operation(summary = "Messages d'une réservation")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(@PathVariable Long reservationId,
                                                            Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        CustomUserDetail userDetails =
                (CustomUserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long currentUserId = userDetails.getUser().getId();
        return ResponseEntity.ok(chatMessageService.getMessagesByReservationId(reservationId, currentUserId));
    }
}
