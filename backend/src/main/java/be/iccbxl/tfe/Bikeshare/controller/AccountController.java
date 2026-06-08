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

import java.util.List;

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
            @RequestParam(value = "photos", required = false) MultipartFile[] photos,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) return "redirect:/login";

        Price price = new Price();
        price.setMiddlePrice(middlePrice);

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
        } else {
            Price price = new Price();
            price.setMiddlePrice(middlePrice);
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
        for (Photo photo : bike.getPhotos()) {
            fileStorageService.delete(photo.getUrl());
        }
        bikeService.deleteBike(id);
        redirectAttributes.addFlashAttribute("success", "Votre annonce a été supprimée.");
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
