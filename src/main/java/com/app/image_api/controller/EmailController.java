package com.app.image_api.controller;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.service.EmailService;
import com.app.image_api.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    private final ImageService imageService;

    @Autowired
    public EmailController(EmailService emailService, ImageService imageService) {
        this.emailService = emailService;
        this.imageService = imageService;
    }

    @GetMapping("/show")
    public String showEmailForm(@RequestParam("imageId") Long imageId, Model model) {
        GeneratedImage image = imageService.getImageById(imageId);
        
        if (image == null) {
            model.addAttribute("error", "Nie znaleziono obrazu o ID: " + imageId);
            return "index";
        }
        
        model.addAttribute("image", image);
        model.addAttribute("imageId", imageId);
        return "index";
    }

    @PostMapping("/send")
    public String sendImageEmail(
            @RequestParam("imageId") Long imageId,
            @RequestParam("email") String email,
            @RequestParam(value = "message", required = false) String message,
            RedirectAttributes redirectAttributes) {
        
        try {
            GeneratedImage image = imageService.getImageById(imageId);
            
            if (image == null) {
                redirectAttributes.addFlashAttribute("error", "Nie znaleziono obrazu o ID: " + imageId);
                return "redirect:/gallery";
            }
            
            String subject = "Wygenerowany obraz: " + 
                    (image.getPrompt() != null ? image.getPrompt().substring(0, Math.min(50, image.getPrompt().length())) + "..." : "Bez opisu");
            
            emailService.sendEmailWithImageId(email, subject, imageId);
            
            redirectAttributes.addFlashAttribute("message", "Email z obrazem został wysłany na adres: " + email);
            return "redirect:/gallery";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Błąd podczas wysyłania emaila: " + e.getMessage());
            return "redirect:/gallery";
        }
    }
} 