package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.PaymentStatus;
import be.iccbxl.tfe.Bikeshare.model.GainStatus;
import be.iccbxl.tfe.Bikeshare.model.ReservationStatus;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Gain;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Refund;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import be.iccbxl.tfe.Bikeshare.repository.GainRepository;
import be.iccbxl.tfe.Bikeshare.repository.PaymentRepository;
import be.iccbxl.tfe.Bikeshare.repository.RefundRepository;
import be.iccbxl.tfe.Bikeshare.repository.ReservationRepository;
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
    @Autowired private ReservationRepository reservationRepository;

    /** Réservations dont le locataire a demandé l'annulation (location payée). */
    public List<Reservation> getDemandesAnnulation() {
        return reservationRepository.findAll().stream()
                .filter(Reservation::isCancellationRequested)
                .toList();
    }

    /**
     * Annulation décidée par l'administrateur. 100 % : remboursement intégral (paiement REFUNDED,
     * gain du propriétaire annulé). 0 % : sans remboursement (le propriétaire garde son gain).
     * Retourne false si la réservation n'est pas payée, si elle est déjà annulée, ou si le pourcentage
     * n'est ni 0 ni 100. Le remboursement réel doit aussi être fait sur Stripe.
     */
    @Transactional
    public boolean annulerAvecRemboursement(Long reservationId, int pourcentage) {
        if (pourcentage != 0 && pourcentage != 100) return false;
        Reservation r = reservationRepository.findById(reservationId).orElse(null);
        if (r == null || r.getPayment() == null || r.getPayment().getStatut() != PaymentStatus.PAID) return false;
        if (r.getStatut() == ReservationStatus.CANCELLED) return false;

        Payment p = r.getPayment();
        if (pourcentage == 100) {
            Refund refund = new Refund();
            refund.setPayment(p);
            refund.setAmount(p.getTotalPrice());
            refund.setRefundPercentage(100);
            refund.setCreatedAt(LocalDateTime.now());
            refundRepository.save(refund);
            p.setStatut(PaymentStatus.REFUNDED);
            paymentRepository.save(p);
            Gain gain = p.getGain();
            if (gain != null) {
                gain.setStatus(GainStatus.ANNULE);
                gainRepository.save(gain);
            }
        }
        r.setStatut(ReservationStatus.CANCELLED);
        r.setCancellationRequested(false);
        reservationRepository.save(r);
        return true;
    }

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
                .filter(g -> g.getStatus() == GainStatus.PENDING)
                .mapToDouble(Gain::getAmountEarned)
                .sum();
    }

    /** Marque un gain comme versé. Retourne false si le gain n'existe pas ou n'est pas en attente. */
    @Transactional
    public boolean marquerVerse(Long gainId) {
        Gain gain = gainRepository.findById(gainId).orElse(null);
        if (gain == null || gain.getStatus() != GainStatus.PENDING) return false;
        gain.setStatus(GainStatus.TRANSFERRED);
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
                    p.getStatut().getLibelle(),
                    g != null ? g.getStatus().getLibelle() : ""))
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
