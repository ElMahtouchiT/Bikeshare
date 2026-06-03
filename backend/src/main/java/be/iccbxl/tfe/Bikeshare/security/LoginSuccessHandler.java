package be.iccbxl.tfe.Bikeshare.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/admin");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEMBER"))) {
            response.sendRedirect("/account");
        } else {
            response.sendRedirect("/");
        }
    }
}
