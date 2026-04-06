package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Price;
import be.iccbxl.tfe.Bikeshare.repository.PriceRepository;
import be.iccbxl.tfe.Bikeshare.service.PriceServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PriceService implements PriceServiceI {
    @Autowired private PriceRepository priceRepository;
    public Price save(Price price) { return priceRepository.save(price); }
    public Price getById(Long id) { return priceRepository.findById(id).orElse(null); }
}
