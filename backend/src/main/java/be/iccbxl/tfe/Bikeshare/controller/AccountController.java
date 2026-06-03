package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Espace membre : profil, mes vélos, mes réservations, mes gains. */
@Controller
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired private UserService userService;
    @Autowired private BikeService bikeService;
    @Autowired private ReservationService reservationService;
    @Autowired private GainService gainService;
    @Autowired private NotificationService notificationService;

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userDetails.getUser();

        long bikeCount = bikeService.getBikesByUser(user).size();
        int confirmedAsTenant = 0;
        if (user.getReservations() != null) {
            for (Reservation r : user.getReservations()) {
                if ("CONFIRMED".equalsIgnoreCase(r.getStatut()) || "NOW".equalsIgnoreCase(r.getStatut()))
                    confirmedAsTenant++;
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("bikeCount", bikeCount);
        model.addAttribute("confirmedRentalsAsTenant", confirmedAsTenant);
        return "account/index";
    }

    @GetMapping("/account/bikes")
    public String myBikes(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userDetails.getUser();
        List<Bike> bikes = bikeService.getBikesByUser(user);
        model.addAttribute("bikes", bikes);
        return "account/bikes/index";
    }

    @GetMapping("/account/reservations")
    public String myReservations(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userDetails.getUser();
        model.addAttribute("reservations", reservationService.getReservationsByUser(user));
        return "account/reservations/index";
    }

    @GetMapping("/account/gains")
    public String myGains(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userDetails.getUser();
        model.addAttribute("gains", gainService.getGainsForOwner(user));
        return "account/gains/index";
    }

    /* ─── Profil ────────────────────────────────────────────── */

    @GetMapping("/account/profile")
    public String editProfileForm(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("user", userDetails.getUser());
        return "account/profile/edit";
    }

    @PostMapping("/account/profile")
    public String editProfileSave(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) String postalCode,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String bic,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";
        userService.updateProfile(
                userDetails.getUser().getId(),
                firstName, lastName, adresse, locality, postalCode, phone, iban, bic);
        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/account/profile";
    }

    /* ─── Suppression de compte ─────────────────────────────── */

    @GetMapping("/account/delete-request")
    public String deleteRequestForm(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("user", userDetails.getUser());
        return "account/delete-request";
    }

    @PostMapping("/account/delete-request")
    public String deleteRequestConfirm(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";
        userService.requestDeletion(userDetails.getUser().getId());
        SecurityContextHolder.clearContext();
        redirectAttributes.addFlashAttribute("info",
                "Votre demande de suppression a été enregistrée. Un administrateur traitera votre demande sous 30 jours.");
        return "redirect:/";
    }
}
