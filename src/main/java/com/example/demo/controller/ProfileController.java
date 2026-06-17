package com.example.demo.controller;

import com.example.demo.model.Profile;
import com.example.demo.model.ProfileType;
import com.example.demo.service.PdfExportService;
import com.example.demo.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Controller
@RequestMapping("/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PdfExportService pdfExportService;

    @GetMapping
    public String displayControlDashboard(Model model) {
        model.addAttribute("profiles", profileService.getAllProfiles());
        return "index";
    }

    @GetMapping("/new")
    public String serveRegistrationForm(Model model) {
        model.addAttribute("profile", new Profile());
        model.addAttribute("profileTypes", ProfileType.values());
        return "id-card-preview";
    }

    @PostMapping("/save")
    public String saveTargetProfile(@ModelAttribute Profile formProfile,
                                    @RequestParam("file") MultipartFile file) throws Exception {
        
        String computedRegNumber = "REG-" + System.currentTimeMillis() % 100000;

        Profile finalizedProfile = Profile.builder()
                .registrationNumber(computedRegNumber)
                .fullName(formProfile.getFullName())
                .department(formProfile.getDepartment())
                .title(formProfile.getTitle())
                .email(formProfile.getEmail())
                .phone(formProfile.getPhone())
                .bloodGroup(formProfile.getBloodGroup())
                .type(formProfile.getType())
                .dateOfBirth(formProfile.getDateOfBirth())
                .expiryDate(LocalDate.now().plusYears(4))
                .build();

        profileService.saveProfile(finalizedProfile, file);
        return "redirect:/profiles";
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<byte[]> streamPdfDownload(@PathVariable Long id) throws Exception {
        Profile target = profileService.getProfileById(id);
        byte[] pdfOutput = pdfExportService.generateIdCardPdf(target);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Card_" + target.getRegistrationNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfOutput);
    }

    // PURE PREVIEW MODE (Hides form, centers card layout)
    @GetMapping("/preview/{id}")
    public String liveCardPreview(@PathVariable Long id, Model model) {
        try {
            Profile profile = profileService.getProfileById(id);
            model.addAttribute("profile", profile);
            model.addAttribute("qrCode", profileService.convertToQrCodeBase64(profile));
            model.addAttribute("barcode", profileService.convertToBarcodeBase64(profile));
            
            // Activate preview flag to cleanly hide input forms
            model.addAttribute("isPreview", true); 
            
            return "id-card-preview";
        } catch (Exception e) {
            return "redirect:/profiles";
        }
    }

    // EDIT MODE (Displays form layout side-by-side with preview card)
    @GetMapping("/edit/{id}")
    public String editProfileForm(@PathVariable Long id, Model model) {
        try {
            Profile profile = profileService.getProfileById(id);
            model.addAttribute("editId", id);
            profile.setId(null); // Unlocks form text boxes
            
            model.addAttribute("profile", profile);
            model.addAttribute("profileTypes", ProfileType.values());
            model.addAttribute("qrCode", profileService.convertToQrCodeBase64(profile));
            model.addAttribute("barcode", profileService.convertToBarcodeBase64(profile));
            
            return "id-card-preview";
        } catch (Exception e) {
            return "redirect:/profiles";
        }
    }

    @PostMapping("/update/{id}")
    public String updateExistingProfile(@PathVariable Long id,
                                        @ModelAttribute Profile formProfile,
                                        @RequestParam("file") MultipartFile file) throws Exception {
        Profile existingProfile = profileService.getProfileById(id);
        
        existingProfile.setFullName(formProfile.getFullName());
        existingProfile.setDepartment(formProfile.getDepartment());
        existingProfile.setTitle(formProfile.getTitle());
        existingProfile.setEmail(formProfile.getEmail());
        existingProfile.setPhone(formProfile.getPhone());
        existingProfile.setBloodGroup(formProfile.getBloodGroup());
        existingProfile.setType(formProfile.getType());
        existingProfile.setDateOfBirth(formProfile.getDateOfBirth());
        
        profileService.saveProfile(existingProfile, file);
        return "redirect:/profiles";
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> dashboardPdfFallback(@PathVariable Long id) throws Exception {
        return streamPdfDownload(id);
    }

    @GetMapping("/delete/{id}")
    public String deleteProfile(@PathVariable Long id) {
        profileService.deleteProfileById(id);
        return "redirect:/profiles";
    }
}