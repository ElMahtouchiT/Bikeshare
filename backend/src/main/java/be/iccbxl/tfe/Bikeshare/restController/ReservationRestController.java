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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

        Reservation r = new Reservation();
        r.setBike(bike);
        r.setUser(userDetails.getUser());
        r.setStartLocation(start);
        r.setEndLocation(end);
        r.setDuration((int) ChronoUnit.DAYS.between(start, end));
        r.setAssurance(assurance);
        r.setStatut("AUTOMATIC".equalsIgnoreCase(bike.getReservationMode()) ? "CONFIRMED" : "PENDING");
        reservationService.addReservation(r);

        return ResponseEntity.ok(MapperDTO.toReservationDTO(r));
    }

    @Operation(summary = "Réservations du propriétaire")
    @GetMapping("/owner")
    public List<ReservationDTO> ownerReservations(@AuthenticationPrincipal CustomUserDetail userDetails) {
        return reservationService.getReservationsByUser(userDetails.getUser());
    }

    @Operation(summary = "Annuler une réservation")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancel(@PathVariable Long id) {
        Reservation r = reservationService.getReservationById(id);
        if (r == null) return ResponseEntity.notFound().build();
        r.setStatut("CANCELLED");
        reservationService.saveReservation(r);
        return ResponseEntity.ok("Réservation annulée");
    }
}
