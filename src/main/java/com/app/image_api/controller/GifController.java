package com.app.image_api.controller;

import com.app.image_api.model.GeneratedGif;
import com.app.image_api.model.GifGenerationRequest;
import com.app.image_api.model.GifGenerationResponse;
import com.app.image_api.service.GifService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/gifs")
public class GifController {
    private final GifService gifService;

    public GifController(GifService gifService) {
        this.gifService = gifService;
    }

    @PostMapping("/generate")
    public ResponseEntity<GifGenerationResponse> generateGif(@RequestBody GifGenerationRequest request) {
        try {
            GifGenerationResponse response = gifService.generateGif(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @GetMapping("/latest")
    public ResponseEntity<List<GeneratedGif>> getLatestGifs() {
        return ResponseEntity.ok(gifService.getLatestGifs());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<GeneratedGif>> searchGifs(@RequestParam String prompt) {
        return ResponseEntity.ok(gifService.searchGifsByPrompt(prompt));
    }
    
    @GetMapping("/style/{style}")
    public ResponseEntity<List<GeneratedGif>> getGifsByStyle(@PathVariable String style) {
        return ResponseEntity.ok(gifService.getGifsByStyle(style));
    }
} 