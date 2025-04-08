package com.app.image_api.service;

import com.app.image_api.config.OpenAiConfig;
import com.app.image_api.model.GeneratedGif;
import com.app.image_api.model.GeneratedImage;
import com.app.image_api.model.GifGenerationRequest;
import com.app.image_api.model.GifGenerationResponse;
import com.app.image_api.repository.GifRepository;
import com.app.image_api.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.UUID;

@Service
public class GifService {

    private final OpenAiConfig openAiConfig;
    private final ImageRepository imageRepository;
    private final GifRepository gifRepository;
    private final RestTemplate restTemplate;
    private final String tempDir = System.getProperty("java.io.tmpdir");

    @Autowired
    public GifService(OpenAiConfig openAiConfig, ImageRepository imageRepository, GifRepository gifRepository, RestTemplate restTemplate) {
        this.openAiConfig = openAiConfig;
        this.imageRepository = imageRepository;
        this.gifRepository = gifRepository;
        this.restTemplate = restTemplate;
    }

    // Nowa metoda dla nowego interfejsu zgodna z GifGenerationRequest/Response
    public GifGenerationResponse generateGif(GifGenerationRequest request) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Konwersja stylu na format kompatybilny z API OpenAI
        String openAiStyle = convertStyle(request.getStyle());
        
        // Ulepszone promptowanie dla GIF-ów
        String enhancedPrompt = enhancePromptForGifGeneration(request.getPrompt(), request.getStyle());
        
        System.out.println("Generowanie GIF-a dla opisu: " + enhancedPrompt);
        
        try {
            // Generowanie bazowego obrazu
            GeneratedImage baseImage = generateSingleImage(enhancedPrompt, openAiStyle);
            
            if (baseImage == null) {
                throw new RuntimeException("Nie udało się wygenerować bazowego obrazu dla GIF-a");
            }
            
            // Pobieranie danych obrazu
            String base64ImageData = baseImage.getImageData();
            if (base64ImageData == null || base64ImageData.isEmpty()) {
                throw new RuntimeException("Brak danych obrazu bazowego");
            }
            
            // Tworzenie animacji na podstawie bazowego obrazu
            String animatedGifBase64 = createAnimatedGifWithMotion(base64ImageData, request.getFramesCount(), request.getFrameDuration());
            
            // Zapisanie GIF-a do pliku tymczasowego
            String gifPath = tempDir + "/gif_" + UUID.randomUUID().toString() + ".gif";
            byte[] decodedGif = Base64.getDecoder().decode(animatedGifBase64);
            Files.write(Path.of(gifPath), decodedGif);
            
            // Zapisanie informacji o GIF-ie w bazie danych
            GeneratedGif gif = new GeneratedGif();
            gif.setPrompt(request.getPrompt());
            gif.setStyle(request.getStyle());
            gif.setSize(request.getSize());
            gif.setQuality(request.getQuality());
            gif.setFramesCount(request.getFramesCount());
            gif.setGifUrl("file://" + gifPath);
            gif.setCreatedAt(LocalDateTime.now());
            
            gifRepository.save(gif);
            
            long endTime = System.currentTimeMillis();
            
            // Zwracanie odpowiedzi
            return new GifGenerationResponse(
                    "file://" + gifPath,
                    animatedGifBase64,
                    request.getFramesCount(),
                    request.getPrompt(),
                    endTime - startTime
            );
        } catch (Exception e) {
            System.out.println("Błąd podczas generowania GIF-a: " + e.getMessage());
            e.printStackTrace();
            
            // Próba wygenerowania pojedynczego obrazu jako fallback
            try {
                GeneratedImage fallbackImage = generateSingleImage(
                        enhancedPrompt + ", high detail, 8k, photorealistic", 
                        openAiStyle
                );
                
                if (fallbackImage != null && fallbackImage.getImageData() != null) {
                    long endTime = System.currentTimeMillis();
                    
                    // Zapisanie informacji o GIF-ie w bazie danych (chociaż to pojedynczy obraz)
                    GeneratedGif gif = new GeneratedGif();
                    gif.setPrompt(request.getPrompt() + " (pojedyncza klatka)");
                    gif.setStyle(request.getStyle());
                    gif.setSize(request.getSize());
                    gif.setQuality(request.getQuality());
                    gif.setFramesCount(1);
                    gif.setGifUrl("fallback-image");
                    gif.setCreatedAt(LocalDateTime.now());
                    
                    gifRepository.save(gif);
                    
                    return new GifGenerationResponse(
                            "fallback-image",
                            fallbackImage.getImageData(),
                            1,
                            request.getPrompt() + " (pojedyncza klatka)",
                            endTime - startTime
                    );
                }
            } catch (Exception fallbackError) {
                System.out.println("Nie udało się wygenerować nawet pojedynczego obrazu: " + fallbackError.getMessage());
            }
            
            throw new IOException("Nie udało się wygenerować GIF-a: " + e.getMessage());
        }
    }

    // Istniejące metody z twojej klasy
    public GeneratedImage createGif(String prompt, int frames, String style) {
        System.out.println("Wywołano createGif z opisem: '" + prompt + "', styl: " + style + ", klatek: " + frames);
        
        try {
            System.out.println("Generowanie bazowego obrazu dla animacji");
            
            String enhancedPrompt = prompt;
            if (!enhancedPrompt.toLowerCase().contains("photorealistic") && 
                !enhancedPrompt.toLowerCase().contains("high quality")) {
                
                enhancedPrompt += ", ultra-detailed hyperrealistic 8k, professional photography, perfect lighting, cinematic quality, stunning detail";
            }
            
            GeneratedImage baseImage = generateSingleImage(enhancedPrompt, style);
            
            if (baseImage == null) {
                throw new RuntimeException("Nie udało się wygenerować bazowego obrazu dla GIF-a");
            }
            
            // Pobieranie danych obrazu
            String base64ImageData = baseImage.getImageData();
            if (base64ImageData == null || base64ImageData.isEmpty()) {
                throw new RuntimeException("Brak danych obrazu bazowego");
            }
            
            System.out.println("Wygenerowano bazowy obraz, tworzenie animacji");
            
            // Używamy domyślnej wartości 0.5s dla czasu trwania klatki
            String animatedGifBase64 = createAnimatedGifWithMotion(base64ImageData, frames, 0.5);
            
            // Tworzymy nowy obiekt GeneratedImage dla GIF-a
            GeneratedImage gif = new GeneratedImage();
            gif.setPrompt(prompt);
            gif.setStyle(style);
            gif.setFileName(UUID.randomUUID().toString() + ".gif");
            gif.setImageData(animatedGifBase64);
            gif.setContentType("image/gif");
            gif.setSize("1024x1024");
            gif.setQuality("hd");
            
            // Ustawiamy URL do późniejszego użycia
            String gifPath = tempDir + "/gif_" + gif.getFileName();
            byte[] decodedGif = Base64.getDecoder().decode(animatedGifBase64);
            Files.write(Path.of(gifPath), decodedGif);
            gif.setImageUrl("file://" + gifPath);
            
            GeneratedImage savedGif = imageRepository.save(gif);
            System.out.println("Zapisano animowany GIF z ID=" + savedGif.getId());
            
            return savedGif;
        } catch (Exception e) {
            System.out.println("Błąd w createGif: " + e.getMessage());
            e.printStackTrace();
            try {
                System.out.println("Próbuję wygenerować pojedynczy obraz jako zastępczy GIF");
                String enhancedPrompt = prompt + ", ultra-detailed masterpiece, photorealistic, 8k resolution, studio lighting, perfect detail";
                GeneratedImage image = generateSingleImage(enhancedPrompt, style);
                if (image != null) {
                    image.setContentType("image/gif");
                    System.out.println("Wygenerowano zastępczy obraz z ID=" + image.getId());
                    return image;
                }
            } catch (Exception fallbackError) {
                System.out.println("Nie udało się wygenerować zastępczego obrazu: " + fallbackError.getMessage());
            }
            
            throw new RuntimeException("Nie udało się utworzyć GIF-a: " + e.getMessage(), e);
        }
    }

    // Konwersja stylu dla API OpenAI
    private String convertStyle(String style) {
        if ("realistic".equalsIgnoreCase(style)) {
            return "natural";
        } else if ("artistic".equalsIgnoreCase(style) || "cartoon".equalsIgnoreCase(style)) {
            return "vivid";
        }
        return "natural"; // domyślny styl
    }

    // Ulepszanie prompta dla generowania bardziej realistycznych GIF-ów
    private String enhancePromptForGifGeneration(String originalPrompt, String style) {
        String basePrompt = originalPrompt;
        
        switch (style.toLowerCase()) {
            case "realistic":
                basePrompt += " Ultra realistic, photorealistic, cinematic quality, smooth motion, perfect for an animated sequence, consistent lighting and composition between frames, high detail preservation.";
                break;
            case "cartoon":
                basePrompt += " Cartoon style animation, smooth transitions, bright colors, expressive movement, stylized with clean lines, consistent character design across frames.";
                break;
            case "artistic":
                basePrompt += " Artistic animation style, creative movement, vivid colors, stylized interpretation, painterly quality, expressive motion, cohesive artistic vision across frames.";
                break;
            default:
                basePrompt += " Smooth animation sequence, consistent style across frames, fluid motion, natural transitions, high quality rendering.";
        }
        
        return basePrompt;
    }

    private GeneratedImage generateSingleImage(String prompt, String style) {
        try {
            System.out.println("Generowanie pojedynczego obrazu dla: " + prompt);
            
            // Przygotowanie żądania HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiConfig.getApiKey());

            String enhancedPrompt = prompt;
            if (!enhancedPrompt.toLowerCase().contains("photorealistic") && 
                !enhancedPrompt.toLowerCase().contains("realistic") &&
                !enhancedPrompt.toLowerCase().contains("high quality")) {
                
                if (style.equalsIgnoreCase("natural")) {
                    enhancedPrompt += ", ultra-detailed, photorealistic masterpiece, 8k resolution, professional photography, professional lighting, sharp focus, award-winning photograph, stunning detail, perfect composition, realistic texture";
                } else if (style.equalsIgnoreCase("vivid")) {
                    enhancedPrompt += ", hyper-realistic, ultra-detailed, ultra HD, 8k quality, perfect lighting, cinematic, vibrant colors, stunning detail, professional photography, masterpiece, perfect shadows and highlights";
                }
            }
            
            System.out.println("Ulepszony prompt: " + enhancedPrompt);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "dall-e-3");
            requestBody.put("prompt", enhancedPrompt);
            requestBody.put("size", "1024x1024");
            requestBody.put("n", 1);
            requestBody.put("quality", "hd"); // Używamy wysokiej jakości (HD)
            requestBody.put("style", style);
            requestBody.put("response_format", "b64_json");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

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
                        System.out.println("Otrzymano dane obrazu, długość: " + base64Image.length());
                        
                        String fileName = UUID.randomUUID().toString() + ".png";
                        
                        // Tworzymy plik na dysku z wygenerowanym obrazem
                        String imagePath = tempDir + "/" + fileName;
                        byte[] decodedImage = Base64.getDecoder().decode(base64Image);
                        Files.write(Path.of(imagePath), decodedImage);
                        
                        GeneratedImage image = new GeneratedImage();
                        image.setPrompt(prompt);
                        image.setStyle(style);
                        image.setFileName(fileName);
                        image.setImageData(base64Image);
                        image.setContentType("image/png");
                        image.setSize("1024x1024");
                        image.setQuality("hd");
                        image.setImageUrl("file://" + imagePath);
                        
                        return imageRepository.save(image);
                    }
                }
            }
            
            System.out.println("Nie udało się wygenerować obrazu");
            return null;
        } catch (Exception e) {
            System.out.println("Błąd podczas generowania pojedynczego obrazu: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Zmodyfikowana metoda, aby obsługiwać czas trwania klatki jako parametr
    private String createAnimatedGifWithMotion(String baseImageBase64, int frames, double frameDuration) {
        try {
            // Dekodujemy bazowy obraz
            byte[] imageData = Base64.getDecoder().decode(baseImageBase64);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage originalImage = ImageIO.read(bis);
            
            if (originalImage == null) {
                throw new IOException("Nie udało się odczytać bazowego obrazu");
            }
            
            List<BufferedImage> animationFrames = new ArrayList<>();
            
            // Limitujemy liczbę klatek dla wydajności
            int actualFrames = Math.min(frames, 10);
            
            for (int i = 0; i < actualFrames; i++) {
                BufferedImage modifiedFrame = applyMotionEffect(originalImage, i, actualFrames);
                animationFrames.add(modifiedFrame);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
            
            // Konwersja czasu trwania klatki z sekund na milisekundy
            int delayMs = (int)(frameDuration * 1000);
            
            // Używamy obrazu ARGB dla lepszej jakości
            GifSequenceWriter writer = new GifSequenceWriter(ios, BufferedImage.TYPE_INT_ARGB, delayMs, true);
            
            // Dodajemy wszystkie klatki do sekwencji
            for (BufferedImage frame : animationFrames) {
                writer.writeToSequence(frame);
            }
            
            // Dodajemy klatki w odwrotnej kolejności dla efektu pętli
            for (int i = animationFrames.size() - 2; i > 0; i--) {
                writer.writeToSequence(animationFrames.get(i));
            }
            
            writer.close();
            ios.close();
            
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            
        } catch (Exception e) {
            System.out.println("Błąd podczas tworzenia animowanego GIF-a: " + e.getMessage());
            e.printStackTrace();
            
            return baseImageBase64;
        }
    }
    
    private BufferedImage applyMotionEffect(BufferedImage original, int frameIndex, int totalFrames) {
        try {
            int width = original.getWidth();
            int height = original.getHeight();
            
            BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            double progress = (double) frameIndex / totalFrames;
            
            int effectType = frameIndex % 4;
            
            switch (effectType) {
                case 0: // Efekt pulsacji
                    double scale = 1.0 + 0.05 * Math.sin(progress * 2 * Math.PI);
                    int offsetX = (int) (width * (1 - scale) / 2);
                    int offsetY = (int) (height * (1 - scale) / 2);
                    int newWidth = (int) (width * scale);
                    int newHeight = (int) (height * scale);
                    
                    result.getGraphics().drawImage(original, offsetX, offsetY, newWidth, newHeight, null);
                    break;
                    
                case 1: // Efekt lekkiego przesunięcia
                    int shiftX = (int) (10 * Math.sin(progress * 2 * Math.PI));
                    int shiftY = (int) (10 * Math.cos(progress * 2 * Math.PI));
                    
                    result.getGraphics().drawImage(original, shiftX, shiftY, width, height, null);
                    break;
                    
                case 2: // Efekt zmiany jasności
                    result.getGraphics().drawImage(original, 0, 0, null);
                    
                    float brightness = (float) (1.0 + 0.2 * Math.sin(progress * 2 * Math.PI));
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int rgb = original.getRGB(x, y);
                            int alpha = (rgb >> 24) & 0xff;
                            int r = Math.min(255, (int) ((rgb >> 16 & 0xff) * brightness));
                            int g = Math.min(255, (int) ((rgb >> 8 & 0xff) * brightness));
                            int b = Math.min(255, (int) ((rgb & 0xff) * brightness));
                            result.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
                        }
                    }
                    break;
                    
                case 3: // Efekt lekkiego obrotu
                    double angle = progress * Math.PI / 36; // Maksymalnie 5 stopni
                    
                    java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
                    transform.translate(width / 2, height / 2);
                    transform.rotate(angle);
                    transform.translate(-width / 2, -height / 2);
                    
                    java.awt.Graphics2D g2d = result.createGraphics();
                    g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, 
                                         java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(original, transform, null);
                    g2d.dispose();
                    break;
                    
                default:
                    result.getGraphics().drawImage(original, 0, 0, null);
            }
            
            return result;
        } catch (Exception e) {
            System.out.println("Błąd podczas tworzenia efektu ruchu: " + e.getMessage());
            return original; // Zwracamy oryginalny obraz w przypadku błędu
        }
    }

    // Metody z nowego interfejsu
    public List<GeneratedGif> getLatestGifs() {
        return gifRepository.findTop10ByOrderByCreatedAtDesc();
    }
    
    public List<GeneratedGif> searchGifsByPrompt(String prompt) {
        return gifRepository.findByPromptContainingIgnoreCase(prompt);
    }
    
    public List<GeneratedGif> getGifsByStyle(String style) {
        return gifRepository.findByStyle(style);
    }

    // Klasa pomocnicza do generowania sekwencji GIF
    public class GifSequenceWriter {
        protected ImageWriter writer;
        protected ImageWriteParam params;
        protected IIOMetadata metadata;
        
        public GifSequenceWriter(ImageOutputStream out, int imageType, int delay, boolean loop) throws IOException {
            writer = ImageIO.getImageWritersBySuffix("gif").next();
            params = writer.getDefaultWriteParam();
            
            ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
            metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, params);
            
            configureRootMetadata(delay, loop);
            
            writer.setOutput(out);
            writer.prepareWriteSequence(null);
        }
        
        private void configureRootMetadata(int delay, boolean loop) throws IOException {
            String metaFormatName = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);
            
            IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
            graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
            graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
            graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
            graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");
            
            IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
            commentsNode.setAttribute("CommentExtension", "Created by GifSequenceWriter");
            
            IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
            IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
            child.setAttribute("applicationID", "NETSCAPE");
            child.setAttribute("authenticationCode", "2.0");
            
            int loopContinuously = loop ? 0 : 1;
            byte[] userObject = new byte[]{ 0x1, (byte) (loopContinuously & 0xFF), (byte) ((loopContinuously >> 8) & 0xFF)};
            child.setUserObject(userObject);
            appExtensionsNode.appendChild(child);
            
            metadata.setFromTree(metaFormatName, root);
        }
        
        private IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
            int nNodes = rootNode.getLength();
            for (int i = 0; i < nNodes; i++) {
                if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                    return (IIOMetadataNode) rootNode.item(i);
                }
            }
            
            IIOMetadataNode node = new IIOMetadataNode(nodeName);
            rootNode.appendChild(node);
            return node;
        }
        
        public void writeToSequence(BufferedImage img) throws IOException {
            BufferedImage normalizedImage = img;
            
            if (img.getType() != BufferedImage.TYPE_INT_ARGB && img.getType() != BufferedImage.TYPE_INT_RGB) {
                normalizedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
                normalizedImage.getGraphics().drawImage(img, 0, 0, null);
            }
            
            writer.writeToSequence(new IIOImage(normalizedImage, null, metadata), params);
        }
        
        public void close() throws IOException {
            writer.endWriteSequence();
        }
    }
} 