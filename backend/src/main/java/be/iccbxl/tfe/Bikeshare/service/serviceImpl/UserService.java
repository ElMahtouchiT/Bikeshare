package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Role;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.NotificationRepository;
import be.iccbxl.tfe.Bikeshare.repository.RoleRepository;
import be.iccbxl.tfe.Bikeshare.repository.UserRepository;
import be.iccbxl.tfe.Bikeshare.service.UserServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserServiceI {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @Override public List<User> getAllUsers() { return userRepository.findAll(); }
    @Override public User getUserById(Long id) { return userRepository.findById(id).orElse(null); }
    @Override public User findByEmail(String email) { return userRepository.findByEmail(email); }
    @Override public User saveUser(User user) { return userRepository.save(user); }
    @Override public long getTotalUsers() { return userRepository.count(); }

    /**
     * Suppression d'un membre. Un compte qui a laissé une trace (vélos, réservations, notifications)
     * est anonymisé et désactivé : réservations, paiements et gains restent en base pour la comptabilité.
     * Un compte sans aucune trace est supprimé physiquement.
     * @return true si le compte a été supprimé, false s'il a été anonymisé
     */
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return false;
        boolean aDesTraces = !user.getOwnedBikes().isEmpty()
                || !user.getReservations().isEmpty()
                || !notificationRepository.findByToUserOrFromUser(user, user).isEmpty();
        if (aDesTraces) {
            anonymise(user);
            return false;
        }
        userRepository.delete(user);
        return true;
    }

    /** Retire les données personnelles et désactive le compte. Ne touche ni aux réservations ni aux paiements. */
    private void anonymise(User user) {
        for (Bike bike : user.getOwnedBikes()) {
            bike.setOnline(false);
            bike.setAdresse(null);
            bike.setLatitude(null);
            bike.setLongitude(null);
        }
        user.getDocuments().clear();
        user.setFirstName("Ancien");
        user.setLastName("membre");
        user.setEmail("anonyme-" + user.getId() + "@supprime.invalid");
        user.setAdresse(null);
        user.setLocality(null);
        user.setPostalCode(null);
        user.setPhone(null);
        user.setIban(null);
        user.setBic(null);
        user.setPhotoUrl(null);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setDeleteRequested(false);
        user.setVerified(false);
        user.getRoles().clear();
        userRepository.save(user);
    }

    /** Inscription : encodage du mot de passe + rôle membre par défaut. */
    @Override
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setVerified(false);
        Role member = roleRepository.findByRole("ROLE_MEMBER");
        if (member != null) user.getRoles().add(member);
        return userRepository.save(user);
    }

    /** Mise à jour du profil : seuls les champs autorisés sont modifiés (pas email, pas mot de passe). */
    @Override
    public User updateProfile(Long userId, String firstName, String lastName,
                               String adresse, String locality, String postalCode,
                               String phone, String iban, String bic) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        if (firstName != null && !firstName.isBlank()) user.setFirstName(firstName);
        if (lastName  != null && !lastName.isBlank())  user.setLastName(lastName);
        user.setAdresse(adresse);
        user.setLocality(locality);
        user.setPostalCode(postalCode);
        user.setPhone(phone);
        user.setIban(iban);
        user.setBic(bic);
        return userRepository.save(user);
    }

    /* ─── Mot de passe oublié / réinitialisation ─── */

    /** Crée un token de réinitialisation (valable 1h) pour l'email donné. Retourne le token, ou null si l'email est inconnu. */
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return null;
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        return token;
    }

    /** Retourne l'utilisateur si le token existe et n'est pas expiré, sinon null. */
    public User findByValidResetToken(String token) {
        if (token == null || token.isBlank()) return null;
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry() == null
                || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) return null;
        return user;
    }

    /** Réinitialise le mot de passe si le token est valide, puis invalide le token. */
    public boolean resetPassword(String token, String newPassword) {
        User user = findByValidResetToken(token);
        if (user == null) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        return true;
    }

    /** Marque le compte comme demandant sa suppression. */
    @Override
    public void requestDeletion(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setDeleteRequested(true);
            userRepository.save(user);
        }
    }
}
