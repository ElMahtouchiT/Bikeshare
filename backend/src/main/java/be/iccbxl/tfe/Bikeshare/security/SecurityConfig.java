package be.iccbxl.tfe.Bikeshare.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.multipart.support.MultipartFilter;

/**
 * Sécurité : Spring Security + BCrypt,
 * trois niveaux d'accès (visiteur public, membre, administrateur).
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailService userDetailService;
    private final LoginSuccessHandler     loginSuccessHandler;

    public SecurityConfig(CustomUserDetailService userDetailService,
                          LoginSuccessHandler loginSuccessHandler) {
        this.userDetailService  = userDetailService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/bikes", "/bikes/**", "/search", "/register",
                                 "/login", "/error", "/css/**", "/js/**", "/images/**", "/uploads/**",
                                 "/ws/**", "/v3/api-docs/**", "/swagger-ui/**", "/api/bikes/**").permitAll()
                .requestMatchers("/admin/**", "/api/admin/**", "/api/dashboard/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/account/**", "/reservations",
                                 "/api/reservations/**", "/api/messages/**")
                    .hasAnyAuthority("ROLE_MEMBER", "ROLE_ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login")
                .successHandler(loginSuccessHandler).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
            .authenticationProvider(authenticationProvider())
            // CSRF activé pour les formulaires navigateur (Thymeleaf injecte le token).
            // Exempté pour l'API REST stateless (/api) et le WebSocket (/ws).
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/ws/**"));
        return http.build();
    }

    /**
     * Le MultipartFilter doit s'exécuter AVANT la chaîne de sécurité pour que le token
     * CSRF (champ caché _csrf) soit lisible dans les formulaires multipart (upload de photos).
     */
    @Bean
    public FilterRegistrationBean<MultipartFilter> multipartFilterRegistration() {
        FilterRegistrationBean<MultipartFilter> registration =
                new FilterRegistrationBean<>(new MultipartFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
