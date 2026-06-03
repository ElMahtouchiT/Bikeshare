package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.model.User;
import java.util.List;

public interface UserServiceI {
    List<User> getAllUsers();
    User getUserById(Long id);
    User findByEmail(String email);
    User register(User user);
    User saveUser(User user);
    void deleteUser(Long id);
    long getTotalUsers();
    User updateProfile(Long userId, String firstName, String lastName,
                       String adresse, String locality, String postalCode,
                       String phone, String iban, String bic);
    void requestDeletion(Long userId);
}
