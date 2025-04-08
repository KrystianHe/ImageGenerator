package com.app.image_api.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;

@Service
public class EmailService {
    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    public void sendEmailWithImage(String toEmail, String subject, String textContent, List<String> imageUrls) throws IOException {
        SendGrid sendGrid = new SendGrid(sendGridApiKey);
        Mail mail = new Mail();
        mail.setFrom(new Email(fromEmail, "AI Image Generator"));
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(toEmail));
        mail.addPersonalization(personalization);

        Content content = new Content("text/html", createHtmlContent(textContent, imageUrls));
        mail.addContent(content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sendGrid.api(request);

        if (response.getStatusCode() >= 400) {
            throw new IOException("Błąd podczas wysyłania emaila. Kod statusu: " + response.getStatusCode());
        }
    }

    private String createHtmlContent(String textContent, List<String> imageUrls) {
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif;'>");
        html.append("<p>").append(textContent).append("</p>");
        html.append("<div style='margin-top: 20px;'>");
        
        for (String imageUrl : imageUrls) {
            html.append("<img src='").append(imageUrl)
                .append("' style='max-width: 100%; margin-bottom: 10px;' alt='Wygenerowany obraz'>");
        }
        
        html.append("</div></div>");
        return html.toString();
    }
} 