package com.app.image_api.controller;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
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
            @RequestParam(required = false) String prompt,
            @RequestParam(required = false) String style,
            Model model
    ) {
        try {
            System.out.println("Wywołano kontroler galerii z parametrami: prompt=" + prompt + ", style=" + style);
            
            List<GeneratedImage> images;
            
            if (prompt != null && !prompt.trim().isEmpty()) {
                System.out.println("Wyszukiwanie obrazów z opisem: " + prompt);
                images = imageService.searchImagesByPrompt(prompt);
                model.addAttribute("currentPrompt", prompt);
            } else if (style != null && !style.trim().isEmpty()) {
                System.out.println("Wyszukiwanie obrazów w stylu: " + style);
                images = imageService.getImagesByStyle(style);
                model.addAttribute("currentStyle", style);
            } else {
                System.out.println("Pobieranie najnowszych obrazów");
                images = imageService.getLatestImages();
            }
            
            System.out.println("Znaleziono " + images.size() + " obrazów");
            for (GeneratedImage image : images) {
                System.out.println("Obraz ID=" + image.getId() + 
                                 ", type=" + image.getContentType() + 
                                 ", hasImageData=" + (image.getImageData() != null && !image.getImageData().isEmpty()) +
                                 ", prompt=" + image.getPrompt());
            }
            
            model.addAttribute("images", images);
            
            List<String> availableStyles = Arrays.asList("natural", "vivid");
            model.addAttribute("availableStyles", availableStyles);
            
            model.addAttribute("activeTab", "gallery");
            
            return "index";
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Błąd w kontrolerze galerii: " + e.getMessage());
            model.addAttribute("error", "Błąd podczas wczytywania galerii: " + e.getMessage());
            model.addAttribute("activeTab", "gallery");
            return "index";
        }
    }
} 