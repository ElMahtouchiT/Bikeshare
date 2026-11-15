package be.iccbxl.tfe.Bikeshare.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Redirige l'utilisateur après connexion selon son rôle :
 *  ROLE_ADMIN  → /admin
 *  ROLE_MEMBER → /account
 *  autre       → /
 * Si la double authentification est activée, on passe d'abord par le portail /2fa.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String destination;
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            destination = "/admin";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBER"))) {
            destination = "/account";
        } else {
            destination = "/";
        }

        boolean twoFactor = authentication.getPrincipal() instanceof CustomUserDetail ud
                && ud.getUser().isTwoFactorEnabled();

        if (twoFactor) {
            HttpSession session = request.getSession();
            session.setAttribute("2FA_TARGET", destination);
            session.setAttribute("2FA_OK", Boolean.FALSE);
            response.sendRedirect("/2fa");
        } else {
            response.sendRedirect(destination);
        }
    }
}
