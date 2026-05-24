package be.iccbxl.tfe.Bikeshare.restController.admin;

import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.DTO.UserDTO;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "Gestion des utilisateurs par les administrateurs")
public class AdminUserRestController {

    @Autowired private UserService userService;

    @GetMapping
    public List<UserDTO> all() {
        return userService.getAllUsers().stream()
                .map(MapperDTO::toUserDTO).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
