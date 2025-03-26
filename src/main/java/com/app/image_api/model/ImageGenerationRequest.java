package com.app.image_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationRequest {
    private String prompt;
    private String size = "1024x1024"; // Domyślny rozmiar obrazu (inne opcje: 1024x1792, 1792x1024)
    private Integer n = 1; // Domyślna liczba obrazów do wygenerowania
    private String style; // Styl obrazu: "natural" (bardziej realistyczny) lub "vivid" (bardziej kolorowy)
    private String quality; // Jakość obrazu: "standard" lub "hd"
    
    public ImageGenerationRequest(String prompt, String size, Integer n) {
        this.prompt = prompt;
        this.size = size;
        this.n = n;
        this.style = "natural";
        this.quality = "hd";
    }
} 