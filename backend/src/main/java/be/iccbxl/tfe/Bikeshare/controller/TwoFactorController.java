package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.security.CustomUserDetail;
import be.iccbxl.tfe.Bikeshare.security.TotpService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Double authentification (TOTP) :
 *  - /2fa                   : portail de vérification après connexion ;
 *  - /account/2fa/setup     : activation/désactivation depuis le profil.
 */
@Controller
public class TwoFactorController {

    @Autowired private TotpService totp;
    @Autowired private UserService userService;

    /* ─── Portail de vérification (après le mot de passe) ─── */

    @GetMapping("/2fa")
    public String verifyPage(@AuthenticationPrincipal CustomUserDetail ud, HttpSession session) {
        if (ud == null) return "redirect:/login";
        if (!ud.getUser().isTwoFactorEnabled() || Boolean.TRUE.equals(session.getAttribute("2FA_OK")))
            return "redirect:/account";
        return "auth/2fa-verify";
    }

    @PostMapping("/2fa")
    public String verify(@RequestParam String code,
                         @AuthenticationPrincipal CustomUserDetail ud,
                         HttpSession session, RedirectAttributes ra) {
        if (ud == null) return "redirect:/login";
        if (totp.verify(ud.getUser().getTwoFactorSecret(), code)) {
            session.setAttribute("2FA_OK", Boolean.TRUE);
            Object target = session.getAttribute("2FA_TARGET");
            return "redirect:" + (target != null ? target.toString() : "/account");
        }
        ra.addFlashAttribute("error", "Code invalide. Réessayez.");
        return "redirect:/2fa";
    }

    /* ─── Activation / désactivation depuis le profil ─── */

    @GetMapping("/account/2fa/setup")
    public String setup(@AuthenticationPrincipal CustomUserDetail ud, HttpSession session, Model model) {
        if (ud == null) return "redirect:/login";
        User u = ud.getUser();
        String secret;
        if (u.isTwoFactorEnabled()) {
            secret = u.getTwoFactorSecret();
        } else {
            secret = (String) session.getAttribute("2FA_SETUP_SECRET");
            if (secret == null) {
                secret = totp.generateSecret();
                session.setAttribute("2FA_SETUP_SECRET", secret);
            }
        }
        model.addAttribute("enabled", u.isTwoFactorEnabled());
        model.addAttribute("secret", secret);
        model.addAttribute("otpUri", totp.getOtpAuthUri(secret, u.getEmail()));
        return "account/2fa-setup";
    }

    @PostMapping("/account/2fa/enable")
    public String enable(@RequestParam String code,
                         @AuthenticationPrincipal CustomUserDetail ud,
                         HttpSession session, RedirectAttributes ra) {
        if (ud == null) return "redirect:/login";
        String secret = (String) session.getAttribute("2FA_SETUP_SECRET");
        if (secret != null && totp.verify(secret, code)) {
            User u = ud.getUser();
            u.setTwoFactorSecret(secret);
            u.setTwoFactorEnabled(true);
            userService.saveUser(u);
            session.removeAttribute("2FA_SETUP_SECRET");
            session.setAttribute("2FA_OK", Boolean.TRUE);   // la session courante reste de confiance
            ra.addFlashAttribute("success", "Double authentification activée.");
        } else {
            ra.addFlashAttribute("error", "Code incorrect : l'activation a échoué. Vérifiez l'heure de votre téléphone et réessayez.");
        }
        return "redirect:/account/2fa/setup";
    }

    @PostMapping("/account/2fa/disable")
    public String disable(@AuthenticationPrincipal CustomUserDetail ud, RedirectAttributes ra) {
        if (ud == null) return "redirect:/login";
        User u = ud.getUser();
        u.setTwoFactorEnabled(false);
        u.setTwoFactorSecret(null);
        userService.saveUser(u);
        ra.addFlashAttribute("success", "Double authentification désactivée.");
        return "redirect:/account/2fa/setup";
    }
}
