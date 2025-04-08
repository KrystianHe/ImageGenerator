package com.app.image_api.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TextAnalysisRequest {
    private String text;
    private MultipartFile file;
} 