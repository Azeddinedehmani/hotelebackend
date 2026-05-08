package com.hotel.presentation.controller;

import com.hotel.application.dto.response.ApiResponse;
import com.hotel.application.dto.response.InvoiceResponse;
import com.hotel.application.usecase.InvoiceUseCase;
import com.hotel.domain.exception.ClientNotFoundException;
import com.hotel.domain.repository.ClientRepository;
import com.hotel.domain.repository.ReservationRepository;
import com.hotel.infrastructure.security.config.CustomUserDetails;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PRESENTATION LAYER — Invoice REST Controller.
 *
 *  GET  /api/invoices                          → toutes les factures     (ADMIN, RECEPTIONNISTE)
 *  GET  /api/invoices/my                       → mes factures            (CLIENT)
 *  GET  /api/invoices/{id}                     → facture par ID
 *  GET  /api/invoices/{id}/pdf                 → télécharger PDF (OpenPDF)
 *  GET  /api/invoices/reservation/{resId}      → facture par réservation
 *  POST /api/invoices                          → générer une facture     (ADMIN, RECEPTIONNISTE)
 *
 * FIX PDF : le stub texte est remplacé par un vrai générateur PDF via OpenPDF (librairie libre).
 *           Dépendance à ajouter dans pom.xml :
 *             <dependency>
 *               <groupId>com.github.librepdf</groupId>
 *               <artifactId>openpdf</artifactId>
 *               <version>1.3.30</version>
 *             </dependency>
 */
@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceUseCase        invoiceUseCase;
    private final ClientRepository      clientRepository;
    private final ReservationRepository reservationRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────── GET ALL ───────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getAllInvoices() {
        List<InvoiceResponse> invoices = invoiceUseCase.getAllInvoices()
                .stream()
                .map(this::enrichWithClientId)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    // ─────────────────────────── GET MY INVOICES ───────────────────────────

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getMyInvoices(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String email = userDetails.getUsername();
        var client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email", email));

        List<InvoiceResponse> invoices = invoiceUseCase.getAllInvoices()
                .stream()
                .map(this::enrichWithClientId)
                .filter(inv -> inv.clientId() != null
                        && inv.clientId().equals(client.getId()))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(invoices));
    }

    // ─────────────────────────── GET BY ID ───────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(enrichWithClientId(invoiceUseCase.getInvoiceById(id)))
        );
    }

    // ─────────────────────────── PDF DOWNLOAD ───────────────────────────

    /**
     * GET /api/invoices/{id}/pdf
     * Génère un vrai PDF avec OpenPDF et le renvoie en pièce jointe.
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        InvoiceResponse invoice = enrichWithClientId(invoiceUseCase.getInvoiceById(id));

        byte[] pdfBytes = generatePdf(invoice);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"facture-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // ─────────────────────────── GET BY RESERVATION ───────────────────────────

    @GetMapping("/reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE', 'CLIENT')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getByReservation(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        enrichWithClientId(invoiceUseCase.getInvoiceByReservationId(reservationId))
                )
        );
    }

    // ─────────────────────────── GENERATE ───────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONNISTE')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(
            @RequestBody Map<String, Long> body) {

        Long reservationId = body.get("reservationId");
        if (reservationId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("reservationId est requis"));
        }

        InvoiceResponse invoice = enrichWithClientId(
                invoiceUseCase.generateInvoice(
                        reservationId,
                        0L,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                )
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Facture générée avec succès", invoice));
    }

    // ─────────────────────────── PDF GENERATOR ───────────────────────────

    /**
     * Génère un PDF professionnel avec OpenPDF.
     * Structure :
     *   - En-tête avec nom de l'hôtel et numéro de facture
     *   - Tableau récapitulatif des lignes de facturation
     *   - Total TTC en surbrillance
     *   - Pied de page avec date d'émission
     */
    private byte[] generatePdf(InvoiceResponse inv) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // ── Couleurs ──
        Color primaryColor  = new Color(0x1A, 0x56, 0x76);   // bleu hôtel
        Color lightGray     = new Color(0xF3, 0xF4, 0xF6);
        Color white         = Color.WHITE;
        Color black         = Color.BLACK;

        // ── Polices ──
        Font titleFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   22, white);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   13, primaryColor);
        Font headerFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   10, white);
        Font bodyFont     = FontFactory.getFont(FontFactory.HELVETICA,         10, black);
        Font totalFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD,   12, white);
        Font smallFont    = FontFactory.getFont(FontFactory.HELVETICA,          8, Color.GRAY);

        // ── Bandeau en-tête ──
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(primaryColor);
        headerCell.setPadding(18);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph hotelName = new Paragraph("🏨  HÔTEL MANAGEMENT", titleFont);
        hotelName.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(hotelName);

        Paragraph invoiceLabel = new Paragraph("FACTURE N° " + inv.id(), subtitleFont);
        invoiceLabel.setAlignment(Element.ALIGN_CENTER);
        Font invoiceLabelWhite = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, white);
        Paragraph invoiceLabelW = new Paragraph("FACTURE  N°  " + inv.id(), invoiceLabelWhite);
        invoiceLabelW.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(invoiceLabelW);

        header.addCell(headerCell);
        doc.add(header);

        doc.add(new Paragraph(" "));  // espace

        // ── Infos réservation ──
        Paragraph info = new Paragraph();
        info.setFont(bodyFont);
        info.add("Réservation n° : " + (inv.reservationId() != null ? inv.reservationId() : "—") + "\n");
        if (inv.clientId() != null) {
            info.add("Client ID : " + inv.clientId() + "\n");
        }
        if (inv.createdAt() != null) {
            info.add("Date d'émission : " + inv.createdAt().format(DATE_FMT) + "\n");
        }
        doc.add(info);
        doc.add(new Paragraph(" "));

        // ── Titre section ──
        Paragraph detailsTitle = new Paragraph("Détail de la facturation", subtitleFont);
        doc.add(detailsTitle);
        doc.add(new Paragraph(" "));

        // ── Tableau lignes ──
        PdfPTable table = new PdfPTable(new float[]{50, 25, 25});
        table.setWidthPercentage(100);

        // Entêtes colonnes
        addTableHeader(table, "Description",     primaryColor, headerFont);
        addTableHeader(table, "Quantité / Taux", primaryColor, headerFont);
        addTableHeader(table, "Montant (MAD)",   primaryColor, headerFont);

        // Ligne : prix chambre / nuit
        BigDecimal pricePerNight = inv.roomPricePerNight() != null
                ? inv.roomPricePerNight() : BigDecimal.ZERO;
        addTableRow(table, "Prix chambre / nuit",
                pricePerNight.setScale(2, RoundingMode.HALF_UP) + " MAD",
                pricePerNight.setScale(2, RoundingMode.HALF_UP) + " MAD",
                lightGray, bodyFont);

        // Ligne : nombre de nuits
        BigDecimal nights = BigDecimal.valueOf(inv.nights());
        BigDecimal subtotal = pricePerNight.multiply(nights);
        addTableRow(table, "Nombre de nuits",
                String.valueOf(inv.nights()),
                subtotal.setScale(2, RoundingMode.HALF_UP) + " MAD",
                white, bodyFont);

        // Ligne : remise
        BigDecimal discount = inv.discountRate() != null ? inv.discountRate() : BigDecimal.ZERO;
        BigDecimal discountPct = discount.multiply(BigDecimal.valueOf(100));
        BigDecimal discountAmt = subtotal.multiply(discount);
        addTableRow(table, "Remise appliquée",
                discountPct.setScale(0, RoundingMode.HALF_UP) + " %",
                "- " + discountAmt.setScale(2, RoundingMode.HALF_UP) + " MAD",
                lightGray, bodyFont);

        doc.add(table);
        doc.add(new Paragraph(" "));

        // ── Total ──
        BigDecimal total = inv.totalAmount() != null
                ? inv.totalAmount() : BigDecimal.ZERO;

        PdfPTable totalTable = new PdfPTable(new float[]{70, 30});
        totalTable.setWidthPercentage(100);

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL TTC", totalFont));
        totalLabelCell.setBackgroundColor(primaryColor);
        totalLabelCell.setPadding(10);
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(totalLabelCell);

        PdfPCell totalAmtCell = new PdfPCell(
                new Phrase(total.setScale(2, RoundingMode.HALF_UP) + " MAD", totalFont));
        totalAmtCell.setBackgroundColor(primaryColor);
        totalAmtCell.setPadding(10);
        totalAmtCell.setBorder(Rectangle.NO_BORDER);
        totalAmtCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalTable.addCell(totalAmtCell);

        doc.add(totalTable);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph(" "));

        // ── Pied de page ──
        Paragraph footer = new Paragraph(
                "Ce document est généré automatiquement — Hôtel Management © 2025", smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String text, Color bg, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(Color.WHITE);
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table,
                             String label, String qty, String amount,
                             Color bg, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label,  font));
        PdfPCell c2 = new PdfPCell(new Phrase(qty,    font));
        PdfPCell c3 = new PdfPCell(new Phrase(amount, font));

        for (PdfPCell c : new PdfPCell[]{c1, c2, c3}) {
            c.setBackgroundColor(bg);
            c.setPadding(7);
            c.setBorderColor(new Color(0xE5, 0xE7, 0xEB));
        }
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(c1);
        table.addCell(c2);
        table.addCell(c3);
    }

    // ─────────────────────────── PRIVATE HELPERS ───────────────────────────

    private InvoiceResponse enrichWithClientId(InvoiceResponse inv) {
        if (inv == null) return null;
        Long clientId = resolveClientId(inv.reservationId());
        return new InvoiceResponse(
                inv.id(),
                inv.reservationId(),
                clientId,
                inv.nights(),
                inv.roomPricePerNight(),
                inv.discountRate(),
                inv.totalPrice(),
                inv.createdAt()
        );
    }

    private Long resolveClientId(Long reservationId) {
        if (reservationId == null) return null;
        return reservationRepository.findById(reservationId)
                .map(r -> r.getClientId())
                .orElse(null);
    }
}