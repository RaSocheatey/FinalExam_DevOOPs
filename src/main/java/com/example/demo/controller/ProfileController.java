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
        
        // Generate uniform layout patterns using functional design parameters
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
}