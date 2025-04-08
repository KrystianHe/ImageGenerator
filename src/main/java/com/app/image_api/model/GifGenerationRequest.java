package com.app.image_api.model;

import lombok.Data;

@Data
public class GifGenerationRequest {
    private String prompt;
    private String size = "1024x1024";
    private int framesCount = 10;
    private double frameDuration = 0.5; // czas trwania klatki w sekundach
    private String style = "realistic"; // realistic, cartoon, artistic
    private String quality = "hd";
} 