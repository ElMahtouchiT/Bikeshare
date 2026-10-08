package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Génère la facture PDF d'une réservation payée. */
@Service
public class InvoiceService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] genererFacture(Reservation r) {
        Payment p = r.getPayment();
        Bike bike = r.getBike();
        User locataire = r.getUser();
        User proprietaire = bike != null ? bike.getUser() : null;
        int jours = r.getDuration() != null ? r.getDuration() : 0;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 40, 40, 40);

        doc.add(new Paragraph("BikeShare").setFontSize(20).setBold());
        doc.add(new Paragraph("Facture n° FAC-" + p.getId()).setFontSize(14));
        doc.add(new Paragraph("Date : " + (p.getCreatedAt() != null ? p.getCreatedAt().format(DATE) : "—")));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Locataire : " + nom(locataire)).setBold());
        if (locataire != null) {
            doc.add(new Paragraph(texte(locataire.getEmail())));
            doc.add(new Paragraph(texte(locataire.getAdresse()) + " " + texte(locataire.getPostalCode())
                    + " " + texte(locataire.getLocality())));
        }
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Propriétaire : " + nom(proprietaire)));
        doc.add(new Paragraph("Vélo : " + (bike != null ? texte(bike.getBrand()) + " " + texte(bike.getModel()) : "—")));
        doc.add(new Paragraph("Période : du " + formater(r.getStartLocation()) + " au "
                + formater(r.getEndLocation()) + " (" + jours + " jour(s))"));
        doc.add(new Paragraph(" "));

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Montant").setBold()
                .setTextAlignment(TextAlignment.RIGHT)));

        Double tarif = bike != null && bike.getPrice() != null ? bike.getPrice().getMiddlePrice() : null;
        if (tarif != null && jours > 0) {
            double sousTotal = tarif * jours;
            ajouterLigne(table, "Location : " + euros(tarif) + " / jour × " + jours + " jour(s)", euros(sousTotal), false);
            double reduction = sousTotal - p.getTotalPrice();
            if (reduction > 0.005) {
                ajouterLigne(table, "Réduction", "-" + euros(reduction), false);
            }
        }
        ajouterLigne(table, "Total payé", euros(p.getTotalPrice()), true);
        doc.add(table);

        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Mode de paiement : " + libelleMode(p.getPaymentMode())));
        doc.add(new Paragraph("Statut : " + libelleStatut(p.getStatut())));

        doc.close();
        return out.toByteArray();
    }

    private static void ajouterLigne(Table table, String description, String montant, boolean gras) {
        Paragraph desc = new Paragraph(description);
        Paragraph total = new Paragraph(montant).setTextAlignment(TextAlignment.RIGHT);
        if (gras) {
            desc.setBold();
            total.setBold();
        }
        table.addCell(new Cell().add(desc));
        table.addCell(new Cell().add(total));
    }

    private static String euros(double valeur) {
        return String.format(Locale.FRENCH, "%.2f €", valeur);
    }

    private static String nom(User u) {
        return u == null ? "—" : (texte(u.getFirstName()) + " " + texte(u.getLastName())).trim();
    }

    private static String formater(java.time.LocalDate date) {
        return date != null ? date.format(DATE) : "—";
    }

    private static String texte(String valeur) {
        return valeur == null ? "" : valeur;
    }

    private static String libelleMode(String mode) {
        return "STRIPE".equals(mode) ? "Carte bancaire (Stripe)" : texte(mode);
    }

    private static String libelleStatut(String statut) {
        if ("PAID".equals(statut)) return "Payée";
        if ("REFUNDED".equals(statut)) return "Remboursée";
        if ("FAILED".equals(statut)) return "Échouée";
        return texte(statut);
    }
}
