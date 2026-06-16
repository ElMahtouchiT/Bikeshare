package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.NotificationService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class ReservationController {

    @Autowired private ReservationService reservationService;
    @Autowired private BikeService bikeService;
    @Autowired private NotificationService notificationService;

    @PostMapping("/reservations")
    public String create(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestParam Long bikeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) String assurance,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        Bike bike = bikeService.getBikeById(bikeId);
        if (bike == null) {
            redirectAttributes.addFlashAttribute("error", "Vélo introuvable.");
            return "redirect:/bikes";
        }

        if (start == null || end == null || !end.isAfter(start)) {
            redirectAttributes.addFlashAttribute("error", "Les dates sélectionnées sont invalides.");
            return "redirect:/bikes/" + bikeId;
        }

        if (reservationService.hasBookingOverlap(bikeId, start, end)) {
            redirectAttributes.addFlashAttribute("error",
                    "Ces dates ne sont plus disponibles : le vélo est déjà réservé sur cette période.");
            return "redirect:/bikes/" + bikeId;
        }

        Reservation r = new Reservation();
        r.setBike(bike);
        r.setUser(userDetails.getUser());
        r.setStartLocation(start);
        r.setEndLocation(end);
        r.setDuration((int) ChronoUnit.DAYS.between(start, end));
        r.setAssurance(assurance);
        r.setStatut("AUTOMATIC".equalsIgnoreCase(bike.getReservationMode()) ? "CONFIRMED" : "PENDING");
        reservationService.addReservation(r);

        // Notifier le propriétaire du vélo de la nouvelle réservation (cloche)
        try {
            User owner = bike.getUser();
            User renter = userDetails.getUser();
            if (owner != null && renter != null && !owner.getId().equals(renter.getId())) {
                String preview = bike.getBrand() + " " + bike.getModel() + " · " + start + " → " + end;
                notificationService.notify(owner, renter, bike, "RESERVATION", preview,
                        "/account/received-reservations");
            }
        } catch (Exception e) {
            // Ne pas bloquer la réservation si la notification échoue.
        }

        redirectAttributes.addFlashAttribute("success", "Réservation créée avec le statut : " + r.getStatut());
        return "redirect:/account/reservations";
    }
}
