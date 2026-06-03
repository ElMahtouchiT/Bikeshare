package be.iccbxl.tfe.Bikeshare.config;

import be.iccbxl.tfe.Bikeshare.model.Role;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.RoleRepository;
import be.iccbxl.tfe.Bikeshare.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initialise la base de données au démarrage :
 *  - Crée les 3 rôles s'ils n'existent pas (ROLE_VISITOR, ROLE_MEMBER, ROLE_ADMIN)
 *  - Crée le compte administrateur par défaut s'il n'existe pas
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // ── 1. Créer les rôles ────────────────────────────────
        Role visitor = createRoleIfAbsent("ROLE_VISITOR");
        Role member  = createRoleIfAbsent("ROLE_MEMBER");
        Role admin   = createRoleIfAbsent("ROLE_ADMIN");

        // ── 2. Créer le compte admin par défaut ───────────────
        if (userRepository.findByEmail("admin@bikeshare.be") == null) {
            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("BikeShare");
            adminUser.setEmail("admin@bikeshare.be");
            adminUser.setPassword(passwordEncoder.encode("Admin1234!"));
            adminUser.setAdresse("Rue de la Loi 1");
            adminUser.setLocality("Bruxelles");
            adminUser.setPostalCode("1000");
            adminUser.setPhone("+32 000 000 000");
            adminUser.setVerified(true);
            adminUser.getRoles().add(admin);
            adminUser.getRoles().add(member);
            userRepository.save(adminUser);
            log.info("Compte admin créé : admin@bikeshare.be / Admin1234!");
        }

        log.info("Rôles disponibles : ROLE_VISITOR, ROLE_MEMBER, ROLE_ADMIN");
    }

    private Role createRoleIfAbsent(String name) {
        Role role = roleRepository.findByRole(name);
        if (role == null) {
            role = new Role();
            role.setRole(name);
            role = roleRepository.save(role);
            log.info("Rôle créé : {}", name);
        }
        return role;
    }
}
