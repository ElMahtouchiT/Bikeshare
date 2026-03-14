package be.iccbxl.tfe.Bikeshare.service;

import be.iccbxl.tfe.Bikeshare.model.Role;

public interface RoleServiceI {
    Role getRoleById(Long id);
    Role saveRole(Role role);
    Role updateRole(Long id, Role newRole);
    void deleteRole(Long id);
}
