package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Refund;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.GainRepository;
import be.iccbxl.tfe.Bikeshare.repository.PaymentRepository;
import be.iccbxl.tfe.Bikeshare.repository.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Lecture des flux financiers et versements aux propriétaires, pour le tableau de bord admin. */
@Service
public class FinanceService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private GainRepository gainRepository;
    @Autowired private RefundRepository refundRepository;

    /** Paiements, du plus récent au plus ancien. */
    public List<Payment> getPaiements() {
        return paymentRepository.findAll().stream()
                .sorted(Comparator.comparing(Payment::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** Gains des propriétaires, du plus récent au plus ancien. */
    public List<Gain> getGains() {
        return gainRepository.findAll().stream()
                .sorted(Comparator.comparing(Gain::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** Remboursements, du plus récent au plus ancien. */
    public List<Refund> getRemboursements() {
        return refundRepository.findAll().stream()
                .sorted(Comparator.comparing(Refund::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** Montant encore dû aux propriétaires : somme des gains non versés. */
    public double getMontantDu() {
        return gainRepository.findAll().stream()
                .filter(g -> "PENDING".equals(g.getStatus()))
                .mapToDouble(Gain::getAmountEarned)
                .sum();
    }

    /** Marque un gain comme versé. Retourne false si le gain n'existe pas ou n'est pas en attente. */
    @Transactional
    public boolean marquerVerse(Long gainId) {
        Gain gain = gainRepository.findById(gainId).orElse(null);
        if (gain == null || !"PENDING".equals(gain.getStatus())) return false;
        gain.setStatus("TRANSFERRED");
        gainRepository.save(gain);
        return true;
    }

    /**
     * Export CSV des paiements : séparateur « ; », décimales à la française, BOM UTF-8
     * pour qu'Excel ouvre correctement les accents.
     */
    public String exporterCsv(List<Payment> paiements) {
        StringBuilder csv = new StringBuilder("﻿");
        csv.append(ligneCsv("Paiement", "Réservation", "Date", "Locataire", "Propriétaire", "Vélo",
                "Total payé", "Commission (TVA comprise)", "Part propriétaire", "Statut paiement", "Statut versement"))
           .append('\n');
        for (Payment p : paiements) {
            Reservation r = p.getReservation();
            Gain g = p.getGain();
            User locataire = r != null ? r.getUser() : null;
            Bike bike = r != null ? r.getBike() : null;
            User proprietaire = bike != null ? bike.getUser() : null;
            csv.append(ligneCsv(
                    p.getId(),
                    r != null ? r.getId() : null,
                    p.getCreatedAt() != null ? p.getCreatedAt().format(DATE) : "",
                    nom(locataire),
                    nom(proprietaire),
                    bike != null ? bike.getBrand() + " " + bike.getModel() : "",
                    montant(p.getTotalPrice()),
                    montant(p.getPartBikeshare()),
                    g != null ? montant(g.getAmountEarned()) : "",
                    p.getStatut(),
                    g != null ? g.getStatus() : ""))
               .append('\n');
        }
        return csv.toString();
    }

    /** Une ligne CSV : champs séparés par « ; ». Un champ contenant « ; », un guillemet ou un saut de ligne est entre guillemets. */
    static String ligneCsv(Object... champs) {
        StringBuilder ligne = new StringBuilder();
        for (int i = 0; i < champs.length; i++) {
            if (i > 0) ligne.append(';');
            String texte = champs[i] == null ? "" : champs[i].toString();
            if (texte.contains(";") || texte.contains("\"") || texte.contains("\n") || texte.contains("\r")) {
                ligne.append('"').append(texte.replace("\"", "\"\"")).append('"');
            } else {
                ligne.append(texte);
            }
        }
        return ligne.toString();
    }

    private static String montant(double valeur) {
        return String.format(Locale.FRENCH, "%.2f", valeur);
    }

    private static String nom(User u) {
        return u == null ? "" : (u.getFirstName() + " " + u.getLastName()).trim();
    }
}
