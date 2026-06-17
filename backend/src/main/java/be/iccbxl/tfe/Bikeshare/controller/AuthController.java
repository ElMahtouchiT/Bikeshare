package be.iccbxl.tfe.Bikeshare.controller;

import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private EmailService emailService;

    /** Mode démo : si true, le lien de réinitialisation est affiché à l'écran (utile quand le SMTP
     *  n'est pas configuré). Désactivé par défaut pour rester sécurisé. */
    @Value("${app.password-reset.expose-link:false}")
    private boolean exposeResetLink;

    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) return "auth/register";
        if (userService.findByEmail(user.getEmail()) != null) {
            result.rejectValue("email", "error.user", "Cet email est déjà utilisé");
            return "auth/register";
        }
        userService.register(user);
        return "redirect:/login?registered";
    }

    /* ─── Mot de passe oublié / réinitialisation ──────────── */

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() { return "auth/forgot-password"; }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email, Model model) {
        String token = userService.createPasswordResetToken(email);
        if (token != null) {
            String link = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/reset-password").queryParam("token", token).toUriString();
            try {
                emailService.sendEmail(email, "BikeShare — réinitialisation de votre mot de passe",
                        "Bonjour,\n\nPour réinitialiser votre mot de passe, cliquez sur ce lien "
                        + "(valable 1 heure) :\n" + link
                        + "\n\nSi vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\nL'équipe BikeShare");
            } catch (Exception ignored) {
                // L'envoi peut échouer si le SMTP n'est pas configuré : on ne bloque pas l'utilisateur.
            }
            if (exposeResetLink) model.addAttribute("resetLink", link); // aide à la démo
        }
        // Message générique : ne révèle pas si l'email correspond à un compte existant.
        model.addAttribute("submitted", true);
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("valid", userService.findByValidResetToken(token) != null);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token, @RequestParam String password,
                                      RedirectAttributes redirectAttributes) {
        if (password == null || password.length() < 8) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 8 caractères.");
            return "redirect:/reset-password?token=" + token;
        }
        if (!userService.resetPassword(token, password)) {
            redirectAttributes.addFlashAttribute("error", "Lien invalide ou expiré. Veuillez refaire une demande.");
            return "redirect:/forgot-password";
        }
        redirectAttributes.addFlashAttribute("success", "Mot de passe réinitialisé. Vous pouvez vous connecter.");
        return "redirect:/login";
    }
}
