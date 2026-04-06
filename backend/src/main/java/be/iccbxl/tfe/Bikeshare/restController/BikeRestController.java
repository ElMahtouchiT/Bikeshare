package be.iccbxl.tfe.Bikeshare.restController;

import be.iccbxl.tfe.Bikeshare.DTO.BikeDTO;
import be.iccbxl.tfe.Bikeshare.DTO.MapperDTO;
import be.iccbxl.tfe.Bikeshare.service.serviceImpl.BikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bikes")
@Tag(name = "Bike Management", description = "Consultation et recherche des vélos disponibles")
public class BikeRestController {

    @Autowired private BikeService bikeService;

    @Operation(summary = "Lister les vélos en ligne")
    @GetMapping
    public List<BikeDTO> all() {
        return bikeService.getAllOnlineBikes().stream()
                .map(MapperDTO::toBikeDTO).collect(Collectors.toList());
    }

    @Operation(summary = "Détail d'un vélo")
    @GetMapping("/{id}")
    public BikeDTO one(@PathVariable Long id) {
        return MapperDTO.toBikeDTO(bikeService.getBikeById(id));
    }

    @Operation(summary = "Rechercher des vélos")
    @GetMapping("/search")
    public List<BikeDTO> search(@RequestParam(required = false) String locality,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) Boolean electric) {
        return bikeService.search(locality, categoryId, electric).stream()
                .map(MapperDTO::toBikeDTO).collect(Collectors.toList());
    }
}
