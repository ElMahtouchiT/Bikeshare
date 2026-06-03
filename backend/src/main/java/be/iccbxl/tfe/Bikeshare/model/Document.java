package be.iccbxl.tfe.Bikeshare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type")
    private String documentType; // ID_CARD, PROOF_OF_ADDRESS...

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
