package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Utilitaires de dates pour les réservations. */
@Service
public class DateService {
    public long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }
}
