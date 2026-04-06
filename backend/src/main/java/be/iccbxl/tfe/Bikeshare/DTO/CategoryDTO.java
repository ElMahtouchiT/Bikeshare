package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String category;
}
