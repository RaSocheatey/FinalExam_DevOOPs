package com.example.demo.service;

import com.example.demo.model.Profile;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

@Service
public class PdfExportService {

    @Autowired
    private ProfileService profileService;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public byte[] generateIdCardPdf(Profile profile) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        
        // standard dimension layout footprint (350pt x 220pt)
        Document document = new Document(pdf, new PageSize(350, 220));
        document.setMargins(15, 15, 15, 15);

        // Header Title Block
        document.add(new Paragraph("OFFICIAL IDENTIFICATION CARD")
                .setBold()
                .setFontSize(12)
                .setMarginBottom(10));
        
        // Profile Metadata Block
        document.add(new Paragraph("ID: " + profile.getRegistrationNumber()).setFontSize(9).setMarginBottom(2));
        document.add(new Paragraph("Name: " + profile.getFullName()).setFontSize(9).setMarginBottom(2));
        document.add(new Paragraph("Dept: " + profile.getDepartment()).setFontSize(9).setMarginBottom(2));
        document.add(new Paragraph("Title: " + profile.getTitle()).setFontSize(9).setMarginBottom(2));
        document.add(new Paragraph("Type: " + profile.getType().toString()).setFontSize(9).setMarginBottom(2));

        // 1. Render and Position the Profile Photo Asset on the Right
        if (profile.getPhotoFileName() != null) {
            String fullPath = UPLOAD_DIR + profile.getPhotoFileName();
            File photoFile = new File(fullPath);
            if (photoFile.exists()) {
                Image photoImg = new Image(ImageDataFactory.create(fullPath)).scaleToFit(65, 65);
                photoImg.setFixedPosition(260, 125); 
                document.add(photoImg);
            }
        }

        // 2. Render and Position the Verification QR Code right underneath the photo
        String qrBase64 = profileService.convertToQrCodeBase64(profile);
        byte[] qrBytes = Base64.getDecoder().decode(qrBase64);
        Image qrImg = new Image(ImageDataFactory.create(qrBytes)).scaleToFit(55, 55);
        qrImg.setFixedPosition(265, 55);
        document.add(qrImg);

        // 3. Render and Position the Code-128 Barcode horizontally across the base row
        String barcodeBase64 = profileService.convertToBarcodeBase64(profile);
        byte[] barcodeBytes = Base64.getDecoder().decode(barcodeBase64);
        Image barcodeImg = new Image(ImageDataFactory.create(barcodeBytes)).scaleToFit(200, 30);
        barcodeImg.setFixedPosition(20, 15);
        document.add(barcodeImg);

        document.close();
        return out.toByteArray();
    }
}