package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRole(String role);
}
