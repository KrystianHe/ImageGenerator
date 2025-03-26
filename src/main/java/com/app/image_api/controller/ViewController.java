package com.app.image_api.controller;

import com.app.image_api.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    private final ImageService imageService;

    @Autowired
    public ViewController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("images", imageService.getLatestImages());
        return "index";
    }
} 