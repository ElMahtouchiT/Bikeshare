package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Document;
import be.iccbxl.tfe.Bikeshare.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {
    @Autowired private DocumentRepository documentRepository;

    public Document save(Document document) { return documentRepository.save(document); }
    public List<Document> getByUser(Long userId) { return documentRepository.findByUserId(userId); }
}
