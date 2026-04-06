package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Equipment;
import be.iccbxl.tfe.Bikeshare.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentService {
    @Autowired private EquipmentRepository equipmentRepository;
    public List<Equipment> getAll() { return equipmentRepository.findAll(); }
    public Equipment save(Equipment e) { return equipmentRepository.save(e); }
}
