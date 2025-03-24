package com.app.image_api.service;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.model.ImageGenerationRequest;
import com.app.image_api.model.ImageGenerationResponse;
import com.app.image_api.repository.ImageRepository;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final OpenAiImageModel imageModel;
    private final ImageRepository imageRepository;

    @Autowired
    public ImageService(OpenAiImageModel imageModel, ImageRepository imageRepository) {
        this.imageModel = imageModel;
        this.imageRepository = imageRepository;
    }

    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .withN(request.getN())
                .withModel("dall-e-3")
                .withQuality(request.getQuality() != null ? request.getQuality() : "hd")
                .withStyle(request.getStyle() != null ? request.getStyle() : "natural")
                .build();

        String enhancedPrompt = request.getStyle() != null && request.getStyle().equals("vivid") 
                ? enhancePromptForVivid(request.getPrompt())
                : enhancePromptForRealism(request.getPrompt());
        
        var imageResponse = imageModel.call(new ImagePrompt(enhancedPrompt, options));
        
        List<String> imageUrls = imageResponse.getResults().stream()
                .map(image -> image.getOutput().getUrl())
                .collect(Collectors.toList());
        
        saveGeneratedImages(request, imageUrls);
        
        return new ImageGenerationResponse(imageUrls);
    }
    
    private void saveGeneratedImages(ImageGenerationRequest request, List<String> imageUrls) {
        String style = request.getStyle() != null ? request.getStyle() : "natural";
        String quality = request.getQuality() != null ? request.getQuality() : "hd";
        
        for (String url : imageUrls) {
            GeneratedImage image = new GeneratedImage();
            image.setPrompt(request.getPrompt());
            image.setImageUrl(url);
            image.setStyle(style);
            image.setQuality(quality);
            image.setSize(request.getSize());
            imageRepository.save(image);
        }
    }
    
    public List<GeneratedImage> getLatestImages() {
        return imageRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    public List<GeneratedImage> searchImagesByPrompt(String prompt) {
        return imageRepository.findByPromptContainingIgnoreCase(prompt);
    }
    
    public List<GeneratedImage> getImagesByStyle(String style) {
        return imageRepository.findByStyle(style);
    }
    
    private String enhancePromptForRealism(String originalPrompt) {
        if (originalPrompt.toLowerCase().contains("realistyczny") || 
            originalPrompt.toLowerCase().contains("fotorealistyczny") ||
            originalPrompt.toLowerCase().contains("fotograficzny")) {
            return originalPrompt;
        }
        
        return originalPrompt + ". Zdjęcie w stylu fotorealistycznym, wysokiej jakości, z naturalnym oświetleniem i szczegółami jak prawdziwa fotografia. Głęboka głębia ostrości z wyraźnym pierwszym planem.";
    }
    
    private String enhancePromptForVivid(String originalPrompt) {
        if (originalPrompt.toLowerCase().contains("kolorowy") || 
            originalPrompt.toLowerCase().contains("jaskrawy") ||
            originalPrompt.toLowerCase().contains("żywy")) {
            return originalPrompt;
        }
        
        return originalPrompt + ". Obraz w żywych, intensywnych kolorach, z dynamicznym kontrastem i jaskrawą kolorystyką. Kreatywne oświetlenie podkreślające atmosferę.";
    }
} 