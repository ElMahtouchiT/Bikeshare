package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Role;
import be.iccbxl.tfe.Bikeshare.repository.RoleRepository;
import be.iccbxl.tfe.Bikeshare.service.RoleServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService implements RoleServiceI {

    @Autowired private RoleRepository roleRepository;

    @Override public Role getRoleById(Long id) { return roleRepository.findById(id).orElse(null); }
    @Override public Role saveRole(Role role) { return roleRepository.save(role); }

    @Override
    public Role updateRole(Long id, Role newRole) {
        Role existing = roleRepository.findById(id).orElse(null);
        if (existing != null) { existing.setRole(newRole.getRole()); return roleRepository.save(existing); }
        return null;
    }

    @Override public void deleteRole(Long id) { roleRepository.deleteById(id); }
}
