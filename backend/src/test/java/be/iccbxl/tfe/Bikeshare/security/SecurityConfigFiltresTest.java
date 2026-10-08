package be.iccbxl.tfe.Bikeshare.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.multipart.support.MultipartFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Le filtre multipart doit s'exécuter après le filtre d'encodage UTF-8 de Spring Boot
 * (HIGHEST_PRECEDENCE). À priorité égale, les champs texte des formulaires sont lus en
 * ISO-8859-1 et les accents sont doublement encodés.
 */
class SecurityConfigFiltresTest {

    @Test
    void filtreMultipart_passeApresLEncodageUtf8() {
        SecurityConfig config = new SecurityConfig(null, null);

        FilterRegistrationBean<MultipartFilter> registration = config.multipartFilterRegistration();

        assertThat(registration.getOrder()).isGreaterThan(Ordered.HIGHEST_PRECEDENCE);
    }
}
