package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Role;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.RoleRepository;
import be.iccbxl.tfe.Bikeshare.repository.UserRepository;
import be.iccbxl.tfe.Bikeshare.service.UserServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements UserServiceI {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @Override public List<User> getAllUsers() { return userRepository.findAll(); }
    @Override public User getUserById(Long id) { return userRepository.findById(id).orElse(null); }
    @Override public User findByEmail(String email) { return userRepository.findByEmail(email); }
    @Override public User saveUser(User user) { return userRepository.save(user); }
    @Override public void deleteUser(Long id) { userRepository.deleteById(id); }
    @Override public long getTotalUsers() { return userRepository.count(); }

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
}
