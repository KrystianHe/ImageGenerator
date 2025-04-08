package com.app.image_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GifGenerationResponse {
    private String gifUrl;
    private String gifBase64;
    private int framesGenerated;
    private String originalPrompt;
    private long generationTimeMs;
} 