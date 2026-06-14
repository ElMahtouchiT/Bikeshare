package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expose des attributs communs à toutes les vues MVC.
 * Ici : le nombre de notifications non lues, pour le badge de la cloche dans la navbar.
 * Ne s'exécute (requête en base) que pour un utilisateur connecté.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired private NotificationService notificationService;

    @ModelAttribute
    public void addGlobalAttributes(Model model,
                                    @AuthenticationPrincipal CustomUserDetail userDetails) {
        if (userDetails == null || userDetails.getUser() == null) return;
        try {
            Long userId = userDetails.getUser().getId();
            model.addAttribute("notifUnreadCount", notificationService.getUnreadCount(userId));
        } catch (Exception ignored) {
            // Ne jamais empêcher le rendu d'une page à cause des notifications.
        }
    }
}
