package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Page « Mes notifications » : liste les notifications reçues et les marque comme lues. */
@Controller
public class NotificationController {

    @Autowired private NotificationService notificationService;

    @GetMapping("/account/notifications")
    public String list(@AuthenticationPrincipal CustomUserDetail userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";
        Long userId = userDetails.getUser().getId();

        // On charge d'abord la liste (avec l'état lu/non-lu courant pour la mise en évidence),
        // puis on marque tout comme lu : le badge se vide à la prochaine navigation.
        model.addAttribute("notifications", notificationService.getAllForUser(userId));
        notificationService.markAllRead(userId);
        model.addAttribute("notifUnreadCount", 0L); // cloche à zéro sur cette page
        return "account/notifications/index";
    }
}
