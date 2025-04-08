package com.app.image_api.controller;

import com.app.image_api.model.TextAnalysisResponse;
import com.app.image_api.service.TextAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/text")
public class TextAnalysisController {
    private final TextAnalysisService textAnalysisService;

    public TextAnalysisController(TextAnalysisService textAnalysisService) {
        this.textAnalysisService = textAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<TextAnalysisResponse> analyzeText(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) MultipartFile file) {
        try {
            TextAnalysisResponse response;
            if (file != null && !file.isEmpty()) {
                response = textAnalysisService.analyzeTextFromFile(file);
            } else if (text != null && !text.trim().isEmpty()) {
                response = textAnalysisService.analyzeText(text);
            } else {
                return ResponseEntity.badRequest().body(null);
            }
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
} 