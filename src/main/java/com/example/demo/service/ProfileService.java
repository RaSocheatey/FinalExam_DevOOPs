package com.example.demo.service;

import com.example.demo.model.BarcodeType;
import com.example.demo.model.Profile;
import com.example.demo.repository.ProfileRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    public List<Profile> getAllProfiles() {
        return profileRepository.findAll();
    }

    public Profile saveProfile(Profile profile, MultipartFile file) throws IOException {
        // 1. Process and save profile image locally
        if (file != null && !file.isEmpty()) {
            String contentType = file.getContentType();
            if (contentType != null && !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed.");
            }

            File uploadFolder = new File(UPLOAD_DIR);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetPath = Paths.get(UPLOAD_DIR + uniqueFileName);
            Files.write(targetPath, file.getBytes());

            profile.setPhotoFileName(uniqueFileName);
            profile.setPhotoContentType(contentType);
        }

        // 2. Enforce system requirement parameters if not explicitly provided
        if (profile.getUuid() == null) {
            profile.setUuid(UUID.randomUUID().toString());
        }

        return profileRepository.save(profile);
    }

    public Profile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target profile not found with database key: " + id));
    }

    // --- ADDED METHOD TO SOLVE COMPILATION FAILURE ---
    public void deleteProfileById(Long id) {
        profileRepository.deleteById(id);
    }

    // Dynamic Render Engine: QR Code Matrix Output String
    public String convertToQrCodeBase64(Profile profile) throws Exception {
        String payload = "Verification URL Data: System Code [" + profile.getUuid() + "]";
        BitMatrix bitMatrix = new MultiFormatWriter().encode(payload, BarcodeFormat.QR_CODE, 180, 180);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
        return Base64.getEncoder().encodeToString(os.toByteArray());
    }

    // Dynamic Render Engine: Barcode Layout Matrix Output String
    public String convertToBarcodeBase64(Profile profile) throws Exception {
        BarcodeFormat format = (profile.getBarcodeType() == BarcodeType.EAN_13) ? BarcodeFormat.EAN_13 : BarcodeFormat.CODE_128;
        String payload = profile.getRegistrationNumber().replaceAll("[^0-9]", ""); // Sanitize for numeric fallback checks if needed
        if (payload.isBlank()) payload = "123456789";
        
        // Safety pad sequence length for EAN specifications
        if (format == BarcodeFormat.EAN_13 && payload.length() < 13) {
            payload = String.format("%13s", payload).replace(' ', '0');
        }

        BitMatrix bitMatrix = new MultiFormatWriter().encode(payload.substring(0, Math.min(payload.length(), 13)), format, 240, 60);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
        return Base64.getEncoder().encodeToString(os.toByteArray());
    }
}