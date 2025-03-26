package com.app.image_api.service;

import com.app.image_api.model.GeneratedImage;
import com.app.image_api.repository.ImageRepository;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import org.json.JSONArray;

@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;
    
    private final ImageRepository imageRepository;
    
    @Autowired
    public EmailService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    private String createHtmlContent(String textContent, List<String> imageUrls) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html><head>");
        htmlBuilder.append("<meta charset=\"UTF-8\">");
        htmlBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 800px; margin: 0 auto; padding: 20px; }");
        htmlBuilder.append("h1 { color: #2c3e50; margin-bottom: 20px; }");
        htmlBuilder.append("p { margin-bottom: 15px; }");
        htmlBuilder.append(".image-container { text-align: center; margin: 30px 0; }");
        htmlBuilder.append(".image-container img { max-width: 100%; height: auto; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }");
        htmlBuilder.append(".image-details { font-size: 14px; color: #666; margin-top: 15px; text-align: center; }");
        htmlBuilder.append(".image-card { margin-bottom: 40px; border-bottom: 1px solid #eee; padding-bottom: 20px; }");
        htmlBuilder.append(".image-card:last-child { border-bottom: none; }");
        htmlBuilder.append(".footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; font-size: 12px; color: #777; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head><body>");
        htmlBuilder.append("<h1>Wygenerowane obrazy</h1>");
        
        if (textContent != null && !textContent.isEmpty()) {
            htmlBuilder.append("<p>").append(textContent).append("</p>");
        }
        
        htmlBuilder.append("<div class=\"image-container\">");

        int imageIndex = 0;
        for (String imageUrl : imageUrls) {
            try {
                Long imageId = Long.parseLong(imageUrl.substring(imageUrl.lastIndexOf('/') + 1));
                GeneratedImage image = imageRepository.findById(imageId).orElse(null);
                
                if (image != null && image.getImageData() != null && !image.getImageData().isEmpty()) {
                    System.out.println("Dodaję obraz w formacie załącznika, typ: " + image.getContentType() + 
                                     ", długość danych: " + image.getImageData().length());
                    
                    String cid = "image" + imageIndex + "_" + image.getId();
                    imageIndex++;
                    
                    htmlBuilder.append("<div class=\"image-card\">");
                    // Używamy odniesienia do załącznika przez Content-ID
                    htmlBuilder.append("<img src=\"cid:")
                               .append(cid)
                               .append("\" alt=\"Wygenerowany obraz\" style=\"max-width: 100%; display: block; margin: 0 auto;\">");
                    
                    // Dodajemy opis obrazu jeśli jest dostępny
                    if (image.getPrompt() != null && !image.getPrompt().isEmpty()) {
                        htmlBuilder.append("<p class=\"image-details\">")
                                   .append(image.getPrompt())
                                   .append("</p>");
                    }
                    
                    if (image.getStyle() != null) {
                        htmlBuilder.append("<div class=\"image-details\">")
                                   .append("Styl: <strong>").append(image.getStyle()).append("</strong>");
                        
                        if (image.getSize() != null) {
                            htmlBuilder.append(" | Rozmiar: <strong>").append(image.getSize()).append("</strong>");
                        }
                        
                        htmlBuilder.append("</div>");
                    }
                    
                    htmlBuilder.append("</div>");
                } else {
                    System.out.println("Nie znaleziono danych obrazu dla ID: " + imageId);
                    htmlBuilder.append("<div class=\"image-card\">");
                    htmlBuilder.append("<p style=\"color: red; text-align: center;\">Nie można wyświetlić obrazu - brak danych.</p>");
                    htmlBuilder.append("</div>");
                }
            } catch (Exception e) {
                System.out.println("Błąd podczas przetwarzania obrazu: " + e.getMessage());
                e.printStackTrace();
                
                // Próbujemy użyć oryginalnego URL jako ostateczność
                htmlBuilder.append("<div class=\"image-card\">");
                htmlBuilder.append("<img src=\"")
                           .append(imageUrl)
                           .append("\" alt=\"Obraz z URL\" style=\"max-width: 100%; display: block; margin: 0 auto;\">");
                htmlBuilder.append("<p class=\"image-details\">Obraz z URL</p>");
                htmlBuilder.append("</div>");
            }
        }

        htmlBuilder.append("</div>");
        htmlBuilder.append("<div class=\"footer\">");
        htmlBuilder.append("<p>Ten email został wygenerowany automatycznie przez aplikację AI Image Generator.</p>");
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }

    public void sendEmailWithImage(String toEmail, String subject, String textContent, List<String> imageUrls) throws IOException {
        try {
            SendGrid sendGrid = new SendGrid(sendGridApiKey);
    
            String htmlContent = createHtmlContent(textContent, imageUrls);
    
            JSONObject mailJson = new JSONObject();
            
            JSONObject from = new JSONObject();
            from.put("email", fromEmail);
            from.put("name", "AI Image Generator");
            mailJson.put("from", from);
            
            mailJson.put("subject", subject);
            
            JSONArray personalizationsArr = new JSONArray();
            JSONObject personalization = new JSONObject();
            JSONArray toArr = new JSONArray();
            JSONObject toObj = new JSONObject();
            toObj.put("email", toEmail);
            toArr.put(toObj);
            personalization.put("to", toArr);
            personalizationsArr.put(personalization);
            mailJson.put("personalizations", personalizationsArr);
            
            // Dodajemy zawartość HTML
            JSONArray contentArr = new JSONArray();
            JSONObject contentObj = new JSONObject();
            contentObj.put("type", "text/html");
            contentObj.put("value", htmlContent);
            contentArr.put(contentObj);
            mailJson.put("content", contentArr);
            
            JSONArray attachmentsArr = new JSONArray();
            int imageIndex = 0;
            
            for (String imageUrl : imageUrls) {
                try {
                    Long imageId = Long.parseLong(imageUrl.substring(imageUrl.lastIndexOf('/') + 1));
                    GeneratedImage image = imageRepository.findById(imageId).orElse(null);
                    
                    if (image != null && image.getImageData() != null && !image.getImageData().isEmpty()) {
                        String cid = "image" + imageIndex + "_" + image.getId();
                        String fileName = (image.getFileName() != null) ? image.getFileName() : "image_" + UUID.randomUUID().toString() + ".png";
                        
                        JSONObject attachmentObj = new JSONObject();
                        attachmentObj.put("content", image.getImageData());
                        attachmentObj.put("type", image.getContentType());
                        attachmentObj.put("filename", fileName);
                        attachmentObj.put("disposition", "inline");
                        attachmentObj.put("content_id", cid);
                        
                        attachmentsArr.put(attachmentObj);
                        imageIndex++;
                        
                        System.out.println("Dodano załącznik z CID: " + cid);
                    }
                } catch (Exception e) {
                    System.out.println("Błąd podczas dodawania załącznika: " + e.getMessage());
                }
            }
            
            if (attachmentsArr.length() > 0) {
                mailJson.put("attachments", attachmentsArr);
            }
    
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mailJson.toString());
    
            System.out.println("Wysyłanie emaila z " + imageIndex + " obrazami do: " + toEmail);
            Response response = sendGrid.api(request);
    
            if (response.getStatusCode() >= 400) {
                throw new IOException("Błąd podczas wysyłania emaila. Kod statusu: " + response.getStatusCode() + ", Odpowiedź: " + response.getBody());
            }
            
            System.out.println("Email został wysłany pomyślnie, kod odpowiedzi: " + response.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Błąd podczas wysyłania emaila: " + e.getMessage(), e);
        }
    }

    private String createHtmlContentForSingleImage(GeneratedImage image) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<!DOCTYPE html>");
        htmlBuilder.append("<html><head>");
        htmlBuilder.append("<meta charset=\"UTF-8\">");
        htmlBuilder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        htmlBuilder.append("<style>");
        htmlBuilder.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 800px; margin: 0 auto; padding: 20px; }");
        htmlBuilder.append("h1 { color: #2c3e50; margin-bottom: 20px; }");
        htmlBuilder.append("p { margin-bottom: 15px; }");
        htmlBuilder.append(".image-container { text-align: center; margin: 30px 0; }");
        htmlBuilder.append(".image-container img { max-width: 100%; height: auto; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }");
        htmlBuilder.append(".image-details { font-size: 14px; color: #666; margin-top: 15px; text-align: center; }");
        htmlBuilder.append(".footer { margin-top: 40px; padding-top: 20px; border-top: 1px solid #eee; font-size: 12px; color: #777; }");
        htmlBuilder.append("</style>");
        htmlBuilder.append("</head><body>");
        htmlBuilder.append("<h1>Wygenerowany obraz</h1>");
        
        if (image.getPrompt() != null && !image.getPrompt().isEmpty()) {
            htmlBuilder.append("<p>Oto wygenerowany obraz na podstawie opisu: <strong>\"")
                       .append(image.getPrompt())
                       .append("\"</strong></p>");
        }
        
        htmlBuilder.append("<div class=\"image-container\">");
        
        if (image.getImageData() != null && !image.getImageData().isEmpty()) {
            System.out.println("Dodaję obraz w formacie base64, typ: " + image.getContentType() + 
                        ", długość danych: " + image.getImageData().length());
            
            // Tworzymy nazwę pliku dla załącznika
            String cid = "image" + image.getId();
            
            htmlBuilder.append("<img src=\"cid:")
                       .append(cid)
                       .append("\" alt=\"Wygenerowany obraz\" style=\"max-width: 100%; height: auto; display: block; margin: 0 auto;\">");
            
            // Dodajemy informacje o obrazie
            htmlBuilder.append("<div class=\"image-details\">");
            if (image.getStyle() != null) {
                htmlBuilder.append("Styl: <strong>").append(image.getStyle()).append("</strong>");
                
                if (image.getSize() != null) {
                    htmlBuilder.append(" | Rozmiar: <strong>").append(image.getSize()).append("</strong>");
                }
            }
            htmlBuilder.append("</div>");
        } else {
            htmlBuilder.append("<p style=\"color: red; text-align: center;\">Nie można wyświetlić obrazu - brak danych.</p>");
        }
        
        htmlBuilder.append("</div>");
        htmlBuilder.append("<div class=\"footer\">");
        htmlBuilder.append("<p>Ten email został wygenerowany automatycznie przez aplikację AI Image Generator.</p>");
        htmlBuilder.append("</div>");
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }
    
    public void sendEmailWithImageId(String toEmail, String subject, Long imageId) throws IOException {
        GeneratedImage image = imageRepository.findById(imageId).orElse(null);
        
        if (image == null) {
            throw new IOException("Nie znaleziono obrazu o ID: " + imageId);
        }
        
        try {
            SendGrid sendGrid = new SendGrid(sendGridApiKey);
    
            String htmlContent = createHtmlContentForSingleImage(image);
    
            JSONObject mailJson = new JSONObject();
            
            JSONObject from = new JSONObject();
            from.put("email", fromEmail);
            from.put("name", "AI Image Generator");
            mailJson.put("from", from);
            
            mailJson.put("subject", subject);
            
            JSONArray personalizationsArr = new JSONArray();
            JSONObject personalization = new JSONObject();
            JSONArray toArr = new JSONArray();
            JSONObject toObj = new JSONObject();
            toObj.put("email", toEmail);
            toArr.put(toObj);
            personalization.put("to", toArr);
            personalizationsArr.put(personalization);
            mailJson.put("personalizations", personalizationsArr);
            
            JSONArray contentArr = new JSONArray();
            JSONObject contentObj = new JSONObject();
            contentObj.put("type", "text/html");
            contentObj.put("value", htmlContent);
            contentArr.put(contentObj);
            mailJson.put("content", contentArr);
            
            // Dodajemy załącznik z obrazem
            if (image.getImageData() != null && !image.getImageData().isEmpty()) {
                String cid = "image" + image.getId();
                String fileName = (image.getFileName() != null) ? image.getFileName() : "image_" + UUID.randomUUID().toString() + ".png";
                
                JSONArray attachmentsArr = new JSONArray();
                JSONObject attachmentObj = new JSONObject();
                
                // Base64 jest już w formacie zgodnym z SendGrid
                attachmentObj.put("content", image.getImageData());
                attachmentObj.put("type", image.getContentType());
                attachmentObj.put("filename", fileName);
                attachmentObj.put("disposition", "inline");
                attachmentObj.put("content_id", cid);
                
                attachmentsArr.put(attachmentObj);
                mailJson.put("attachments", attachmentsArr);
                
                System.out.println("Dodano załącznik z CID: " + cid);
            }
    
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mailJson.toString());
    
            System.out.println("Wysyłanie emaila z obrazem ID: " + imageId + " do: " + toEmail);
            Response response = sendGrid.api(request);
    
            if (response.getStatusCode() >= 400) {
                throw new IOException("Błąd podczas wysyłania emaila. Kod statusu: " + response.getStatusCode() + ", Odpowiedź: " + response.getBody());
            }
            
            System.out.println("Email został wysłany pomyślnie, kod odpowiedzi: " + response.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Błąd podczas wysyłania emaila: " + e.getMessage(), e);
        }
    }
} 