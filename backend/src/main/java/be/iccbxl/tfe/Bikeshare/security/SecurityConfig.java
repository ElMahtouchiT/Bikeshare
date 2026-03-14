package be.iccbxl.tfe.Bikeshare.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Sécurité : Spring Security + BCrypt,
 * trois niveaux d'accès (visiteur public, membre, administrateur).
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailService userDetailService;

    public SecurityConfig(CustomUserDetailService userDetailService) {
        this.userDetailService = userDetailService;
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
                                 "/login", "/css/**", "/js/**", "/images/**", "/uploads/**",
                                 "/ws/**", "/v3/api-docs/**", "/swagger-ui/**", "/api/bikes/**").permitAll()
                .requestMatchers("/admin/**", "/api/admin/**", "/api/dashboard/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/account/**", "/reservations", "/api/reservations/**", "/api/messages/**")
                    .hasAnyAuthority("ROLE_MEMBER", "ROLE_ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login")
                .defaultSuccessUrl("/account", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
            .authenticationProvider(authenticationProvider())
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
