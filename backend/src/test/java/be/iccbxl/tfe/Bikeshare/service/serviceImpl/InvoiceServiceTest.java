package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Price;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceServiceTest {

    @Test
    void facture_estUnPdfQuiContientLeNumeroEtLeVelo() throws Exception {
        User locataire = new User();
        locataire.setFirstName("Léa");
        locataire.setLastName("Martin");
        locataire.setEmail("lea@test.be");
        User proprietaire = new User();
        proprietaire.setFirstName("Tarik");
        proprietaire.setLastName("Dupont");
        Price prix = new Price();
        prix.setMiddlePrice(20.0);
        Bike bike = new Bike();
        bike.setBrand("Trek");
        bike.setModel("fx44");
        bike.setUser(proprietaire);
        bike.setPrice(prix);
        Payment payment = new Payment();
        payment.setId(42L);
        payment.setTotalPrice(54.0);
        payment.setPartBikeshare(8.1);
        payment.setStatut("PAID");
        payment.setPaymentMode("STRIPE");
        payment.setCreatedAt(LocalDateTime.of(2026, 10, 8, 12, 0));
        Reservation reservation = new Reservation();
        reservation.setUser(locataire);
        reservation.setBike(bike);
        reservation.setPayment(payment);
        reservation.setStartLocation(LocalDate.of(2026, 10, 10));
        reservation.setEndLocation(LocalDate.of(2026, 10, 12));
        reservation.setDuration(3);

        byte[] pdf = new InvoiceService().genererFacture(reservation);

        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII)).isEqualTo("%PDF");
        PdfDocument doc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdf)));
        String texte = PdfTextExtractor.getTextFromPage(doc.getPage(1));
        doc.close();
        assertThat(texte).contains("FAC-42").contains("Trek fx44").contains("Total payé");
    }
}
