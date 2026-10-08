package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Photo;
import be.iccbxl.tfe.Bikeshare.model.Price;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Espace membre : profil, mes vélos, mes réservations, mes gains. */
@Controller
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired private UserService userService;
    @Autowired private BikeService bikeService;
    @Autowired private CategoryService categoryService;
    @Autowired private ReservationService reservationService;
    @Autowired private GainService gainService;
    @Autowired private NotificationService notificationService;
    @Autowired private EvaluationService evaluationService;
    @Autowired private EvaluationLocataireService evaluationLocataireService;
    @Autowired private FileStorageService fileStorageService;

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userService.getUserById(userDetails.getUser().getId());
        if (user == null) return "redirect:/login";

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
        reservationService.terminerLocationsPassees(LocalDate.now());
        model.addAttribute("reservations", reservationService.getReservationsByUser(user));
        return "account/reservations/index";
    }

    /* ─── Réservations reçues (côté propriétaire) ───────────── */

    @GetMapping("/account/received-reservations")
    public String receivedReservations(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        reservationService.terminerLocationsPassees(LocalDate.now());
        model.addAttribute("reservations",
                reservationService.getReservationsOnOwnerBikes(userDetails.getUser()));
        model.addAttribute("aujourdhui", LocalDate.now());
        model.addAttribute("evaluees", evaluationLocataireService.idsReservationsEvaluees());
        return "account/reservations/received";
    }

    @PostMapping("/account/reservations/{id}/confirm")
    public String confirmReservation(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetail userDetails,
                                     RedirectAttributes redirectAttributes) {
        return updateReservationStatusAsOwner(id, userDetails, "CONFIRMED",
                "Réservation confirmée.", redirectAttributes);
    }

    @PostMapping("/account/reservations/{id}/refuse")
    public String refuseReservation(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetail userDetails,
                                    RedirectAttributes redirectAttributes) {
        return updateReservationStatusAsOwner(id, userDetails, "REFUSED",
                "Réservation refusée.", redirectAttributes);
    }

    private String updateReservationStatusAsOwner(Long id, CustomUserDetail userDetails,
                                                  String status, String successMsg,
                                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        if (r == null || r.getBike() == null || r.getBike().getUser() == null
                || !r.getBike().getUser().getId().equals(userDetails.getUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Réservation introuvable ou accès refusé.");
            return "redirect:/account/received-reservations";
        }
        r.setStatut(status);
        reservationService.saveReservation(r);
        redirectAttributes.addFlashAttribute("success", successMsg);
        return "redirect:/account/received-reservations";
    }

    /* ─── Annulation par le locataire ───────────────────────── */

    @PostMapping("/account/reservations/{id}/cancel")
    public String annulerParLocataire(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetail userDetails,
                                      RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        if (r == null || r.getUser() == null || !r.getUser().getId().equals(userDetails.getUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Réservation introuvable ou accès refusé.");
        } else if (!r.peutEtreAnnuleeDirectement()) {
            redirectAttributes.addFlashAttribute("error",
                    "Cette réservation ne peut pas être annulée directement. Si elle est payée, demandez son annulation.");
        } else {
            r.setStatut("CANCELLED");
            reservationService.saveReservation(r);
            notifierProprietaire(r, "Réservation annulée par le locataire.");
            redirectAttributes.addFlashAttribute("success", "Réservation annulée.");
        }
        return "redirect:/account/reservations";
    }

    @PostMapping("/account/reservations/{id}/request-cancel")
    public String demanderAnnulation(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetail userDetails,
                                     RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        if (r == null || r.getUser() == null || !r.getUser().getId().equals(userDetails.getUser().getId())) {
            redirectAttributes.addFlashAttribute("error", "Réservation introuvable ou accès refusé.");
        } else if (!r.peutDemanderAnnulation()) {
            redirectAttributes.addFlashAttribute("error",
                    "Cette réservation ne peut pas faire l'objet d'une demande d'annulation.");
        } else {
            r.setCancellationRequested(true);
            reservationService.saveReservation(r);
            redirectAttributes.addFlashAttribute("success",
                    "Demande d'annulation envoyée. L'administrateur la traitera.");
        }
        return "redirect:/account/reservations";
    }

    /** Prévient le propriétaire qu'une réservation de son vélo est annulée (cloche). */
    private void notifierProprietaire(Reservation r, String message) {
        try {
            notificationService.notify(r.getBike().getUser(), r.getUser(), r.getBike(),
                    "RESERVATION", message, "/account/received-reservations");
        } catch (Exception e) {
            logger.warn("Notification d'annulation non créée : {}", e.getMessage());
        }
    }

    /* ─── Fin de location et évaluation du locataire (par le propriétaire) ─── */

    @PostMapping("/account/reservations/{id}/complete")
    public String marquerRendu(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetail userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        if (!estProprietaire(r, userDetails.getUser())) {
            redirectAttributes.addFlashAttribute("error", "Réservation introuvable ou accès refusé.");
        } else if (!r.peutEtreMarqueeRendue(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error",
                    "Cette location ne peut pas être marquée comme rendue : elle doit être payée et commencée.");
        } else {
            r.setStatut("COMPLETED");
            reservationService.saveReservation(r);
            redirectAttributes.addFlashAttribute("success",
                    "Location terminée. Vous pouvez maintenant évaluer le locataire.");
        }
        return "redirect:/account/received-reservations";
    }

    @GetMapping("/account/reservations/{id}/evaluate-locataire")
    public String evaluerLocataireForm(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetail userDetails,
                                       Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        String erreur = evaluationLocataireBloquee(r, userDetails.getUser());
        if (erreur != null) {
            redirectAttributes.addFlashAttribute("error", erreur);
            return "redirect:/account/received-reservations";
        }
        model.addAttribute("reservation", r);
        return "account/reservations/evaluate-locataire";
    }

    @PostMapping("/account/reservations/{id}/evaluate-locataire")
    public String evaluerLocataireSubmit(@PathVariable Long id,
                                         @AuthenticationPrincipal CustomUserDetail userDetails,
                                         @RequestParam int note,
                                         @RequestParam(required = false) String comment,
                                         RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        String erreur = evaluationLocataireBloquee(r, userDetails.getUser());
        if (erreur != null) {
            redirectAttributes.addFlashAttribute("error", erreur);
            return "redirect:/account/received-reservations";
        }
        if (note < 1 || note > 5) {
            redirectAttributes.addFlashAttribute("error", "La note doit être comprise entre 1 et 5.");
            return "redirect:/account/reservations/" + id + "/evaluate-locataire";
        }

        evaluationLocataireService.creer(r, note, comment);

        // Notifier le locataire de l'évaluation reçue (cloche)
        try {
            String preview = (comment != null && !comment.isBlank())
                    ? (comment.length() > 80 ? comment.substring(0, 80) + "…" : comment)
                    : (note + "/5");
            notificationService.notify(r.getUser(), r.getBike().getUser(), r.getBike(),
                    "EVALUATION", preview, "/account/reservations");
        } catch (Exception e) {
            logger.warn("Notification d'évaluation du locataire non créée : {}", e.getMessage());
        }

        redirectAttributes.addFlashAttribute("success", "Merci ! Votre évaluation du locataire a été enregistrée.");
        return "redirect:/account/received-reservations";
    }

    /** Raison pour laquelle le propriétaire ne peut pas évaluer ce locataire, sinon null. */
    private String evaluationLocataireBloquee(Reservation r, User user) {
        if (!estProprietaire(r, user)) return "Réservation introuvable ou accès refusé.";
        if (!"COMPLETED".equalsIgnoreCase(r.getStatut()))
            return "Vous ne pouvez évaluer le locataire qu'une fois la location terminée.";
        if (evaluationLocataireService.existePour(r.getId())) return "Vous avez déjà évalué ce locataire.";
        return null;
    }

    private boolean estProprietaire(Reservation r, User user) {
        return r != null && r.getBike() != null && r.getBike().getUser() != null
                && r.getBike().getUser().getId().equals(user.getId());
    }

    /* ─── Messagerie d'une réservation ──────────────────────── */

    @GetMapping("/account/reservations/{id}/chat")
    public String reservationChat(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetail userDetails,
                                  Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        Long uid = userDetails.getUser().getId();
        boolean isRenter = r != null && r.getUser() != null && r.getUser().getId().equals(uid);
        boolean isOwner  = r != null && r.getBike() != null && r.getBike().getUser() != null
                && r.getBike().getUser().getId().equals(uid);
        if (r == null || (!isRenter && !isOwner)) {
            redirectAttributes.addFlashAttribute("error", "Conversation introuvable ou accès refusé.");
            return "redirect:/account/reservations";
        }
        User other = isRenter ? r.getBike().getUser() : r.getUser();
        model.addAttribute("reservation", r);
        model.addAttribute("currentUserId", uid);
        model.addAttribute("otherName",
                other != null ? other.getFirstName() + " " + other.getLastName() : "—");
        return "account/reservations/chat";
    }

    /* ─── Évaluation d'une location (par le locataire) ──────── */

    @GetMapping("/account/reservations/{id}/evaluate")
    public String evaluateForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetail userDetails,
                               Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        String error = evaluationBlockedReason(r, userDetails.getUser().getId());
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/account/reservations";
        }
        model.addAttribute("reservation", r);
        return "account/reservations/evaluate";
    }

    @PostMapping("/account/reservations/{id}/evaluate")
    public String evaluateSubmit(@PathVariable Long id,
                                 @AuthenticationPrincipal CustomUserDetail userDetails,
                                 @RequestParam int note,
                                 @RequestParam(required = false) String comment,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Reservation r = reservationService.getReservationById(id);
        String error = evaluationBlockedReason(r, userDetails.getUser().getId());
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/account/reservations";
        }
        if (note < 1 || note > 5) {
            redirectAttributes.addFlashAttribute("error", "La note doit être comprise entre 1 et 5.");
            return "redirect:/account/reservations/" + id + "/evaluate";
        }

        evaluationService.createForReservation(r, note, comment);

        // Notifier le propriétaire du vélo de l'évaluation reçue (cloche)
        try {
            User renter = r.getUser();
            User owner = r.getBike().getUser();
            String preview = (comment != null && !comment.isBlank())
                    ? (comment.length() > 80 ? comment.substring(0, 80) + "…" : comment)
                    : (note + "/5");
            notificationService.notify(owner, renter, r.getBike(), "EVALUATION", preview,
                    "/bikes/" + r.getBike().getId());
        } catch (Exception e) {
            logger.warn("Notification d'évaluation non créée : {}", e.getMessage());
        }

        redirectAttributes.addFlashAttribute("success", "Merci ! Votre évaluation a été enregistrée.");
        return "redirect:/account/reservations";
    }

    /** Retourne la raison pour laquelle la réservation n'est pas évaluable par cet utilisateur, sinon null. */
    private String evaluationBlockedReason(Reservation r, Long userId) {
        if (r == null || r.getUser() == null || !r.getUser().getId().equals(userId))
            return "Réservation introuvable ou accès refusé.";
        if (!"COMPLETED".equalsIgnoreCase(r.getStatut()))
            return "Vous ne pouvez évaluer qu'une location terminée.";
        if (r.getEvaluation() != null)
            return "Vous avez déjà évalué cette réservation.";
        return null;
    }

    @GetMapping("/account/gains")
    public String myGains(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userDetails.getUser();
        model.addAttribute("gains", gainService.getGainsForOwner(user));
        return "account/gains/index";
    }

    /* ─── Proposer un vélo ──────────────────────────────────── */

    @GetMapping("/account/bikes/create")
    public String createBikeForm(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("categories", categoryService.getAllCategory());
        return "account/bikes/create";
    }

    @PostMapping("/account/bikes/create")
    public String createBikeSave(
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestParam String brand,
            @RequestParam String bikeModel,
            @RequestParam String bikeType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean electric,
            @RequestParam(required = false) String wheelSize,
            @RequestParam(required = false) Integer gears,
            @RequestParam(required = false) String frameSize,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String postalCode,
            @RequestParam String locality,
            @RequestParam(required = false, defaultValue = "MANUAL") String reservationMode,
            @RequestParam Double middlePrice,
            @RequestParam(required = false) Double promo1,
            @RequestParam(required = false) Double promo2,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        Price price = new Price();
        price.setMiddlePrice(middlePrice);
        price.setPromo1(promo1);   // reduction (%) des 3 jours
        price.setPromo2(promo2);   // reduction (%) des 7 jours

        Bike bike = new Bike();
        bike.setBrand(brand);
        bike.setModel(bikeModel);
        bike.setBikeType(bikeType);
        bike.setElectric(electric);
        bike.setWheelSize(wheelSize);
        bike.setGears(gears);
        bike.setFrameSize(frameSize);
        bike.setAdresse(adresse);
        bike.setPostalCode(postalCode);
        bike.setLocality(locality);
        bike.setReservationMode(reservationMode);
        bike.setOnline(false);
        bike.setUser(userDetails.getUser());
        bike.setPrice(price);

        if (categoryId != null) {
            bike.setCategory(categoryService.getCategoryById(categoryId));
        }

        storePhotos(bike, photos);

        bikeService.saveBike(bike);
        redirectAttributes.addFlashAttribute("success", "Votre vélo a été enregistré et sera mis en ligne après vérification.");
        return "redirect:/account/bikes";
    }

    /* ─── Modifier une annonce ──────────────────────────────── */

    @GetMapping("/account/bikes/{id}/edit")
    public String editBikeForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetail userDetails,
                               Model model, RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Bike bike = getOwnedBikeOrNull(id, userDetails.getUser());
        if (bike == null) {
            redirectAttributes.addFlashAttribute("error", "Vélo introuvable ou accès refusé.");
            return "redirect:/account/bikes";
        }
        model.addAttribute("bike", bike);
        model.addAttribute("categories", categoryService.getAllCategory());
        return "account/bikes/edit";
    }

    @PostMapping("/account/bikes/{id}/edit")
    public String editBikeSave(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetail userDetails,
            @RequestParam String brand,
            @RequestParam String bikeModel,
            @RequestParam String bikeType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean electric,
            @RequestParam(required = false) String wheelSize,
            @RequestParam(required = false) Integer gears,
            @RequestParam(required = false) String frameSize,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String postalCode,
            @RequestParam String locality,
            @RequestParam(required = false, defaultValue = "MANUAL") String reservationMode,
            @RequestParam Double middlePrice,
            @RequestParam(required = false) Double promo1,
            @RequestParam(required = false) Double promo2,
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";
        Bike bike = getOwnedBikeOrNull(id, userDetails.getUser());
        if (bike == null) {
            redirectAttributes.addFlashAttribute("error", "Vélo introuvable ou accès refusé.");
            return "redirect:/account/bikes";
        }

        bike.setBrand(brand);
        bike.setModel(bikeModel);
        bike.setBikeType(bikeType);
        bike.setElectric(electric);
        bike.setWheelSize(wheelSize);
        bike.setGears(gears);
        bike.setFrameSize(frameSize);
        bike.setAdresse(adresse);
        bike.setPostalCode(postalCode);
        bike.setLocality(locality);
        bike.setReservationMode(reservationMode);

        if (bike.getPrice() != null) {
            bike.getPrice().setMiddlePrice(middlePrice);
            bike.getPrice().setPromo1(promo1);
            bike.getPrice().setPromo2(promo2);
        } else {
            Price price = new Price();
            price.setMiddlePrice(middlePrice);
            price.setPromo1(promo1);
            price.setPromo2(promo2);
            bike.setPrice(price);
        }

        bike.setCategory(categoryId != null ? categoryService.getCategoryById(categoryId) : null);

        storePhotos(bike, photos);

        bikeService.saveBike(bike);
        redirectAttributes.addFlashAttribute("success", "Votre annonce a été mise à jour.");
        return "redirect:/account/bikes";
    }

    /* ─── Supprimer une annonce ─────────────────────────────── */

    @PostMapping("/account/bikes/{id}/delete")
    public String deleteBike(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetail userDetails,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Bike bike = getOwnedBikeOrNull(id, userDetails.getUser());
        if (bike == null) {
            redirectAttributes.addFlashAttribute("error", "Vélo introuvable ou accès refusé.");
            return "redirect:/account/bikes";
        }
        if (reservationService.hasActiveReservations(id)) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer ce vélo : il a des réservations en cours " +
                    "(en attente, confirmées ou en cours de location). Attendez qu'elles soient " +
                    "terminées ou annulées avant de supprimer l'annonce.");
            return "redirect:/account/bikes";
        }
        List<String> photoUrls = bike.getPhotos().stream().map(Photo::getUrl).toList();
        if (bikeService.deleteBike(id)) {
            photoUrls.forEach(fileStorageService::delete);
            redirectAttributes.addFlashAttribute("success", "Votre annonce a été supprimée.");
        } else {
            redirectAttributes.addFlashAttribute("success",
                    "Votre annonce a été retirée de la location. Son historique de réservations et de paiements est conservé.");
        }
        return "redirect:/account/bikes";
    }

    /* ─── Supprimer une photo ───────────────────────────────── */

    @PostMapping("/account/bikes/{bikeId}/photos/{photoId}/delete")
    public String deleteBikePhoto(@PathVariable Long bikeId, @PathVariable Long photoId,
                                  @AuthenticationPrincipal CustomUserDetail userDetails,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        Bike bike = getOwnedBikeOrNull(bikeId, userDetails.getUser());
        if (bike == null) {
            redirectAttributes.addFlashAttribute("error", "Vélo introuvable ou accès refusé.");
            return "redirect:/account/bikes";
        }
        bike.getPhotos().removeIf(photo -> {
            if (photo.getId().equals(photoId)) {
                fileStorageService.delete(photo.getUrl());
                return true;
            }
            return false;
        });
        bikeService.saveBike(bike);
        redirectAttributes.addFlashAttribute("success", "Photo supprimée.");
        return "redirect:/account/bikes/" + bikeId + "/edit";
    }

    /* ─── Helpers ───────────────────────────────────────────── */

    /** Stocke les fichiers uploadés non vides et les rattache au vélo. */
    private void storePhotos(Bike bike, MultipartFile[] photos) {
        if (photos == null) return;
        for (MultipartFile file : photos) {
            if (file == null || file.isEmpty()) continue;
            String filename = fileStorageService.store(file, "bikes");
            Photo photo = new Photo();
            photo.setUrl("/uploads/bikes/" + filename);
            bike.addPhoto(photo);
        }
    }

    /** Retourne le vélo s'il existe et appartient à l'utilisateur, sinon null. */
    private Bike getOwnedBikeOrNull(Long bikeId, User user) {
        Bike bike = bikeService.getBikeById(bikeId);
        if (bike == null || bike.getUser() == null || user == null
                || !bike.getUser().getId().equals(user.getId())) {
            return null;
        }
        return bike;
    }

    /* ─── Profil ────────────────────────────────────────────── */

    @GetMapping("/account/profile")
    public String editProfileForm(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        model.addAttribute("user", userService.getUserById(userDetails.getUser().getId()));
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
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";
        User user = userService.updateProfile(
                userDetails.getUser().getId(),
                firstName, lastName, adresse, locality, postalCode, phone, iban, bic);

        if (user != null && photo != null && !photo.isEmpty()) {
            String filename = fileStorageService.store(photo, "profiles");
            user.setPhotoUrl("/uploads/profiles/" + filename);
            userService.saveUser(user);
        }

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
