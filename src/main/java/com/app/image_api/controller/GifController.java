package com.app.image_api.controller;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.service.GifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gifs")
public class GifController {

    private final GifService gifService;

    @Autowired
    public GifController(GifService gifService) {
        this.gifService = gifService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateGif(@RequestBody Map<String, String> request) {
        try {
            String prompt = request.get("prompt");
            String style = request.get("style");
            int frames = 10;

            if (request.get("frames") != null) {
                try {
                    frames = Integer.parseInt(request.get("frames"));
                } catch (NumberFormatException e) {
                    System.out.println("Nieprawidłowa liczba klatek, używam domyślnej wartości 10");
                }
            }

            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Opis GIF-a jest wymagany"));
            }

            if (style == null || style.trim().isEmpty()) {
                style = "vivid";
            }
            
            System.out.println("Generowanie GIF-a z prompt=" + prompt + ", style=" + style + ", frames=" + frames);
            
            GeneratedImage gif = gifService.createGif(prompt, frames, style);
            System.out.println("Wygenerowano GIF z ID=" + gif.getId() + ", contentType=" + gif.getContentType());
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", gif.getId());
            response.put("url", "/images/" + gif.getId());
            response.put("contentType", gif.getContentType());
            response.put("message", "GIF został wygenerowany pomyślnie");
            response.put("base64Image", "data:" + gif.getContentType() + ";base64," + gif.getImageData());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Błąd podczas generowania GIF-a: " + e.getMessage()
            ));
        }
    }
} 