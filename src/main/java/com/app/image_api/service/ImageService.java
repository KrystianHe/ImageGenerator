package com.app.image_api.service;

import com.app.image_api.config.OpenAiConfig;
import com.app.image_api.model.DallEResponse;
import com.app.image_api.model.GeneratedImage;
import com.app.image_api.model.ImageGenerationRequest;
import com.app.image_api.model.ImageGenerationResponse;
import com.app.image_api.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {

    private final OpenAiConfig openAiConfig;
    private final ImageRepository imageRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public ImageService(OpenAiConfig openAiConfig, ImageRepository imageRepository, RestTemplate restTemplate) {
        this.openAiConfig = openAiConfig;
        this.imageRepository = imageRepository;
        this.restTemplate = restTemplate;
    }

    public ImageGenerationResponse generateImage(ImageGenerationRequest request) {
        try {
            List<String> imageUrls = new ArrayList<>();
            int n = request.getN() != null ? request.getN() : 1;

            for (int i = 0; i < n; i++) {
                String imageUrl = generateSingleImage(request);
                imageUrls.add(imageUrl);
            }

            return new ImageGenerationResponse(imageUrls);
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania obrazu: " + e.getMessage(), e);
        }
    }

    private String generateSingleImage(ImageGenerationRequest request) {
        try {
            String enhancedPrompt = request.getPrompt();
            
            if (!enhancedPrompt.toLowerCase().contains("photorealistic") &&
                !enhancedPrompt.toLowerCase().contains("realistic") &&
                !enhancedPrompt.toLowerCase().contains("high quality")) {
                
                if (request.getStyle().equalsIgnoreCase("natural")) {
                    enhancedPrompt += ", ultra-detailed photorealistic image, 8k resolution, professional lighting, studio quality, sharp focus, professional photography, award-winning photograph, stunning detail, perfect composition";
                } else if (request.getStyle().equalsIgnoreCase("vivid")) {
                    enhancedPrompt += ", hyper-realistic, ultra-detailed, ultra HD, 8k quality, perfect lighting, cinematic, vibrant colors, stunning detail, professional photography, masterpiece";
                }
            }
            
            System.out.println("Generowanie obrazu z ulepszonym opisem: " + enhancedPrompt);
            
            // Przygotowanie żądania HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiConfig.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "dall-e-3");
            requestBody.put("prompt", enhancedPrompt);
            requestBody.put("size", request.getSize());
            requestBody.put("n", 1);
            requestBody.put("quality", "hd"); // Zwiększenie jakości na "hd" zamiast "standard"
            requestBody.put("style", request.getStyle());
            requestBody.put("response_format", "b64_json");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // Wysłanie żądania do API DALL-E
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.openai.com/v1/images/generations",
                HttpMethod.POST,
                requestEntity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                if (!data.isEmpty()) {
                    Map<String, Object> imageData = data.get(0);
                    String base64Image = (String) imageData.get("b64_json");
                    
                    if (base64Image != null) {
                        GeneratedImage image = new GeneratedImage();
                        image.setPrompt(request.getPrompt());
                        image.setStyle(request.getStyle());
                        image.setFileName(UUID.randomUUID().toString() + ".png");
                        image.setImageData(base64Image);
                        image.setContentType("image/png");
                        image.setSize(request.getSize());
                        image.setQuality("hd"); // Zapisujemy jakość jako "hd"
                        
                        imageRepository.save(image);
                        return "/images/" + image.getId();
                    }
                }
            }
            throw new RuntimeException("Nie udało się wygenerować obrazu");
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania obrazu: " + e.getMessage());
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

    public GeneratedImage getImageById(Long id) {
        return imageRepository.findById(id).orElse(null);
    }

} 