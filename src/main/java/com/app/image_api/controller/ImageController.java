package com.app.image_api.controller;

import com.app.image_api.model.ImageEmailRequest;
import com.app.image_api.model.ImageGenerationRequest;
import com.app.image_api.model.ImageGenerationResponse;
import com.app.image_api.service.EmailService;
import com.app.image_api.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
public class ImageController {
    private final ImageService imageService;
    private final EmailService emailService;

    public ImageController(ImageService imageService, EmailService emailService) {
        this.imageService = imageService;
        this.emailService = emailService;
    }

    @PostMapping("/images/generate")
    public ImageGenerationResponse generateImage(@RequestBody ImageGenerationRequest request) {
        return imageService.generateImage(request);
    }
    
    @PostMapping("/images/email")
    public ResponseEntity<String> generateAndEmailImage(@RequestBody ImageEmailRequest request) {
        try {
            ImageGenerationRequest imageRequest = new ImageGenerationRequest();
            imageRequest.setPrompt(request.getPrompt());
            imageRequest.setSize(request.getSize());
            imageRequest.setN(1);
            imageRequest.setStyle(request.getStyle());
            imageRequest.setQuality("hd");
            
            ImageGenerationResponse imageResponse = imageService.generateImage(imageRequest);
            
            String subject = "Wygenerowany obraz AI: " + request.getPrompt();
            String content = "Oto wygenerowany obraz na podstawie opisu: \"" + request.getPrompt() + "\"";
            
            emailService.sendEmailWithImage(request.getEmail(), subject, content, imageResponse.getImageUrls());
            
            return ResponseEntity.ok("Email z wygenerowanym obrazem został wysłany na adres: " + request.getEmail());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Błąd podczas wysyłania emaila: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Błąd podczas generowania obrazu: " + e.getMessage());
        }
    }
}