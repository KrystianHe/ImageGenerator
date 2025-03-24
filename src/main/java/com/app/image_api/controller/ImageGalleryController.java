package com.app.image_api.controller;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class ImageGalleryController {
    
    private final ImageService imageService;
    
    @Autowired
    public ImageGalleryController(ImageService imageService) {
        this.imageService = imageService;
    }
    
    @GetMapping("/latest")
    public ResponseEntity<List<GeneratedImage>> getLatestImages() {
        return ResponseEntity.ok(imageService.getLatestImages());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<GeneratedImage>> searchImages(@RequestParam String prompt) {
        return ResponseEntity.ok(imageService.searchImagesByPrompt(prompt));
    }
    
    @GetMapping("/style/{style}")
    public ResponseEntity<List<GeneratedImage>> getImagesByStyle(@PathVariable String style) {
        return ResponseEntity.ok(imageService.getImagesByStyle(style));
    }
} 