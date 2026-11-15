package be.iccbxl.tfe.Bikeshare.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Portail de double authentification : si l'utilisateur connecté a la 2FA activée
 * et que la session n'a pas encore validé le code TOTP, toute requête est redirigée
 * vers /2fa (sauf la page 2FA elle-même, la déconnexion et les ressources statiques).
 */
public class TwoFactorInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserDetail ud)) {
            return true;
        }
        if (!ud.getUser().isTwoFactorEnabled()) return true;

        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("2FA_OK"))) return true;

        String path = request.getRequestURI();
        if (path.equals("/2fa") || path.equals("/logout") || path.equals("/favicon.ico")
                || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/images/") || path.startsWith("/uploads/")) {
            return true;
        }
        response.sendRedirect("/2fa");
        return false;
    }
}
