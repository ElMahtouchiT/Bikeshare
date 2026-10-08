package be.iccbxl.tfe.Bikeshare.service.serviceImpl;

import be.iccbxl.tfe.Bikeshare.model.Bike;
import be.iccbxl.tfe.Bikeshare.model.Payment;
import be.iccbxl.tfe.Bikeshare.model.Reservation;
import be.iccbxl.tfe.Bikeshare.model.User;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Génère la facture PDF d'une réservation payée. */
@Service
public class InvoiceService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String SLOGAN = "Plateforme de location de vélos entre particuliers";

    private static final DeviceRgb NAVY = new DeviceRgb(0x13, 0x23, 0x3F);
    private static final DeviceRgb GREEN = new DeviceRgb(0x1F, 0xA6, 0x7A);
    private static final DeviceRgb GREY = new DeviceRgb(0xF4, 0xF6, 0xF8);
    private static final DeviceRgb MUTED = new DeviceRgb(0x6B, 0x72, 0x80);
    private static final DeviceRgb PALE = new DeviceRgb(0xD6, 0xE4, 0xF0);

    public byte[] genererFacture(Reservation r) {
        Payment p = r.getPayment();
        Bike bike = r.getBike();
        User locataire = r.getUser();
        User proprietaire = bike != null ? bike.getUser() : null;
        int jours = r.getDuration() != null ? r.getDuration() : 0;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(out));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(0, 36, 36, 36);

        // Bandeau d'en-tête : nom et slogan de la plateforme
        Table bandeau = new Table(1).useAllAvailableWidth();
        Cell entete = new Cell().setBackgroundColor(NAVY).setPadding(18);
        entete.add(new Paragraph("BikeShare").setFontSize(24).setBold()
                .setFontColor(ColorConstants.WHITE).setMargin(0));
        entete.add(new Paragraph(SLOGAN).setFontSize(10).setFontColor(PALE).setMargin(0));
        bandeau.addCell(entete);
        doc.add(bandeau);

        // Numéro de facture, date et statut
        Table infos = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        infos.setMarginTop(18);
        infos.addCell(new Cell()
                .add(new Paragraph("FACTURE").setFontSize(16).setBold().setFontColor(NAVY).setMargin(0))
                .add(new Paragraph("n° FAC-" + p.getId()).setMargin(0)));
        infos.addCell(new Cell()
                .add(new Paragraph("Date : " + (p.getCreatedAt() != null ? p.getCreatedAt().format(DATE) : "—"))
                        .setMargin(0))
                .add(new Paragraph(libelleStatut(p.getStatut())).setBold().setFontColor(GREEN).setMargin(0))
                .setTextAlignment(TextAlignment.RIGHT));
        doc.add(infos);

        // Locataire et propriétaire
        Table parties = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        parties.setMarginTop(16);
        parties.addCell(bloc("Locataire", nom(locataire),
                texte(locataire != null ? locataire.getEmail() : null),
                (texte(locataire != null ? locataire.getAdresse() : null) + " "
                        + texte(locataire != null ? locataire.getPostalCode() : null) + " "
                        + texte(locataire != null ? locataire.getLocality() : null)).trim()));
        parties.addCell(bloc("Propriétaire", nom(proprietaire),
                "Vélo : " + (bike != null ? texte(bike.getBrand()) + " " + texte(bike.getModel()) : "—"),
                "Période : du " + formater(r.getStartLocation()) + " au " + formater(r.getEndLocation())
                        + " (" + jours + " jour(s))"));
        doc.add(parties);

        // Détail des montants
        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1})).useAllAvailableWidth();
        table.setMarginTop(22);
        table.addHeaderCell(celluleEntete("Description", false));
        table.addHeaderCell(celluleEntete("Montant", true));

        Double tarif = bike != null && bike.getPrice() != null ? bike.getPrice().getMiddlePrice() : null;
        if (tarif != null && jours > 0) {
            double sousTotal = tarif * jours;
            ajouterLigne(table, "Location : " + euros(tarif) + " / jour × " + jours + " jour(s)",
                    euros(sousTotal), false);
            double reduction = sousTotal - p.getTotalPrice();
            if (reduction > 0.005) {
                ajouterLigne(table, "Réduction", "-" + euros(reduction), false);
            }
        }
        ajouterLigne(table, "Total payé", euros(p.getTotalPrice()), true);
        doc.add(table);

        doc.add(new Paragraph("Mode de paiement : " + libelleMode(p.getPaymentMode()))
                .setMarginTop(16).setFontSize(10));

        // Pied de page
        doc.add(new Paragraph("BikeShare — " + SLOGAN + ". Document généré automatiquement.")
                .setFontSize(8).setFontColor(MUTED).setTextAlignment(TextAlignment.CENTER).setMarginTop(40));

        doc.close();
        return out.toByteArray();
    }

    /** Bloc d'information (locataire ou propriétaire) sur fond gris. */
    private static Cell bloc(String titre, String nom, String ligne1, String ligne2) {
        Cell cell = new Cell().setBackgroundColor(GREY).setPadding(10);
        cell.add(new Paragraph(titre.toUpperCase()).setFontSize(8).setBold().setFontColor(MUTED).setMargin(0));
        cell.add(new Paragraph(nom).setBold().setMargin(0));
        cell.add(new Paragraph(ligne1).setFontSize(9).setMargin(0));
        cell.add(new Paragraph(ligne2).setFontSize(9).setMargin(0));
        return cell;
    }

    private static Cell celluleEntete(String texte, boolean droite) {
        Paragraph p = new Paragraph(texte).setBold().setFontColor(ColorConstants.WHITE).setMargin(0);
        if (droite) p.setTextAlignment(TextAlignment.RIGHT);
        return new Cell().setBackgroundColor(NAVY).setPadding(8).add(p);
    }

    private static void ajouterLigne(Table table, String description, String montant, boolean total) {
        Color fond = total ? GREY : ColorConstants.WHITE;
        Paragraph desc = new Paragraph(description).setMargin(0);
        Paragraph valeur = new Paragraph(montant).setTextAlignment(TextAlignment.RIGHT).setMargin(0);
        if (total) {
            desc.setBold();
            valeur.setBold().setFontColor(GREEN);
        }
        table.addCell(new Cell().setPadding(8).setBackgroundColor(fond).add(desc));
        table.addCell(new Cell().setPadding(8).setBackgroundColor(fond).add(valeur));
    }

    private static String euros(double valeur) {
        return String.format(Locale.FRENCH, "%.2f €", valeur);
    }

    private static String nom(User u) {
        return u == null ? "—" : (texte(u.getFirstName()) + " " + texte(u.getLastName())).trim();
    }

    private static String formater(LocalDate date) {
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
