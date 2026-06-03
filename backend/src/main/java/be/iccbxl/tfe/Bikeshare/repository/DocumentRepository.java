package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    java.util.List<Document> findByUserId(Long userId);
}
