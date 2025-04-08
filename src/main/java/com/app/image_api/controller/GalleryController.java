package com.app.image_api.controller;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class GalleryController {

    private final ImageService imageService;

    @Autowired
    public GalleryController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/gallery")
    public String showGallery(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String style,
            Model model) {
        
        List<GeneratedImage> images;
        
        if (search != null && !search.isEmpty()) {
            images = imageService.searchImagesByPrompt(search);
            model.addAttribute("search", search);
        } else if (style != null && !style.isEmpty()) {
            images = imageService.getImagesByStyle(style);
            model.addAttribute("style", style);
        } else {
            images = imageService.getLatestImages();
        }
        
        model.addAttribute("images", images);
        return "gallery";
    }
} 