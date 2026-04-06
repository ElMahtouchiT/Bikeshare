package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Photo;
import be.iccbxl.tfe.Bikeshare.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhotoService {
    @Autowired private PhotoRepository photoRepository;

    public Photo save(Photo photo) { return photoRepository.save(photo); }
    public List<Photo> getByBike(Long bikeId) { return photoRepository.findByBikeId(bikeId); }
    public void delete(Long id) { photoRepository.deleteById(id); }
}
