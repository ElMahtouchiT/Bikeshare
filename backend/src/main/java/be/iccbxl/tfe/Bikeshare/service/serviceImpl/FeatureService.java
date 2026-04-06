package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Feature;
import be.iccbxl.tfe.Bikeshare.repository.FeatureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeatureService {
    @Autowired private FeatureRepository featureRepository;
    public List<Feature> getAll() { return featureRepository.findAll(); }
    public Feature save(Feature f) { return featureRepository.save(f); }
}
