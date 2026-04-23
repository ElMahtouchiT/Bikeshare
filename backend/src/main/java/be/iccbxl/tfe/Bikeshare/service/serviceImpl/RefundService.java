package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Refund;
import be.iccbxl.tfe.Bikeshare.repository.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefundService {
    @Autowired private RefundRepository refundRepository;
    public void saveRefund(Refund refund) { refundRepository.save(refund); }
}
