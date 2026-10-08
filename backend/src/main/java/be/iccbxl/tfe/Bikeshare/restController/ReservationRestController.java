package be.iccbxl.tfe.Bikeshare.restController;

import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation Management", description = "Gestion des réservations et des annulations")
public class ReservationRestController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationRestController.class);

    @Autowired private ReservationService reservationService;
    @Autowired private BikeService bikeService;
    @Autowired private EmailService emailService;

    @Operation(summary = "Créer une réservation")
    @PostMapping
    public ResponseEntity<ReservationDTO> create(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestParam Long bikeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String assurance) {

        Bike bike = bikeService.getBikeById(bikeId);
        if (bike == null) return ResponseEntity.notFound().build();

        // Même vérification anti-chevauchement que le site (cohérence : évite la double réservation).
        if (reservationService.hasBookingOverlap(bikeId, start, end)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();   // 409 Conflict
        }

        // Règles centralisées dans le service (durée + statut + enregistrement).
        Reservation r = reservationService.createReservation(userDetails.getUser(), bike, start, end, assurance);
        return ResponseEntity.ok(MapperDTO.toReservationDTO(r));
    }

    @Operation(summary = "Réservations du propriétaire")
    @GetMapping("/owner")
    public List<ReservationDTO> ownerReservations(@AuthenticationPrincipal CustomUserDetail userDetails) {
        return reservationService.getReservationsByUser(userDetails.getUser());
    }

    @Operation(summary = "Annuler une réservation (locataire ou propriétaire uniquement)")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable Long id,
                                         @AuthenticationPrincipal CustomUserDetail userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Reservation r = reservationService.getReservationById(id);
        if (r == null) return ResponseEntity.notFound().build();
        // Contrôle de propriété : seul le locataire ou le propriétaire du vélo peut annuler.
        if (!reservationService.isParticipant(r, userDetails.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        // Une location payée ne s'annule pas directement : elle passe par l'administrateur (remboursement).
        if (r.getPayment() != null && "PAID".equalsIgnoreCase(r.getPayment().getStatut())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Une location payée ne peut être annulée que par l'administrateur.");
        }
        r.setStatut("CANCELLED");
        reservationService.saveReservation(r);
        return ResponseEntity.ok("Réservation annulée");
    }
}
