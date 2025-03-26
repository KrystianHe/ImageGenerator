package com.app.image_api.controller;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.model.ImageEmailRequest;
import com.app.image_api.model.ImageGenerationRequest;
import com.app.image_api.model.ImageGenerationResponse;
import com.app.image_api.service.EmailService;
import com.app.image_api.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.util.Base64;

@RestController
public class ImageController {

    private final ImageService imageService;
    private final EmailService emailService;

    @Autowired
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
            imageRequest.setQuality(request.getQuality());
            
            ImageGenerationResponse imageResponse = imageService.generateImage(imageRequest);
            
            String subject = "Wygenerowany obraz AI: " + request.getPrompt();
            String content = "Oto wygenerowany obraz na podstawie opisu: \"" + request.getPrompt() + "\"";
            
            emailService.sendEmailWithImage(
                    request.getEmail(),
                    subject,
                    content,
                    imageResponse.getImageUrls()
            );
            
            return ResponseEntity.ok("Email z wygenerowanym obrazem został wysłany na adres: " + request.getEmail());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Błąd podczas wysyłania emaila: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Błąd podczas generowania obrazu: " + e.getMessage());
        }
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<?> getImage(@PathVariable Long id) {
        try {
            System.out.println("Próba pobrania obrazu o ID: " + id);
            GeneratedImage image = imageService.getImageById(id);
            
            if (image == null) {
                System.out.println("Nie znaleziono obrazu o ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Znaleziono obraz, contentType: " + image.getContentType() + 
                            ", data length: " + (image.getImageData() != null ? image.getImageData().length() : "null"));
            
            if (image.getImageData() == null || image.getImageData().isEmpty()) {
                System.out.println("Brak danych obrazu dla ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            try {
                byte[] imageBytes = Base64.getDecoder().decode(image.getImageData());
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(image.getContentType()))
                    .body(imageBytes);
            } catch (IllegalArgumentException e) {
                System.out.println("Błąd dekodowania Base64: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500)
                    .body("Błąd dekodowania danych obrazu: " + e.getMessage());
            }
                
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Błąd podczas pobierania obrazu: " + e.getMessage());
            return ResponseEntity.internalServerError()
                .body("Błąd podczas pobierania obrazu: " + e.getMessage());
        }
    }
} 