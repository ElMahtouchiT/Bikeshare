package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/** Stockage des fichiers uploadés (photos de vélos, documents). */
@Service
public class FileStorageService {

    private final Path root = Paths.get("uploads");

    public String store(MultipartFile file, String subFolder) {
        try {
            Path dir = root.resolve(subFolder);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Échec du stockage du fichier", e);
        }
    }
}
