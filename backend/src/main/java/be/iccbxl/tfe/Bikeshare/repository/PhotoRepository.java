package be.iccbxl.tfe.Bikeshare.repository;

import be.iccbxl.tfe.Bikeshare.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    java.util.List<Photo> findByBikeId(Long bikeId);
}
