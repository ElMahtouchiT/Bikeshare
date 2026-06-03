package be.iccbxl.tfe.Bikeshare.DTO;

import lombok.Data;

@Data
public class DocumentDTO {
    private Long id;
    private String documentType;
    private String filePath;
}
