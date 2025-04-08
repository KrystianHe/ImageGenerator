package com.app.image_api.service;

import com.app.image_api.model.TextAnalysisResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TextAnalysisService {

    private final OpenAiChatModel chatModel;
    private static final int MAX_TEXT_LENGTH = 10000; // Maksymalna długość tekstu w znakach
    
    @Autowired
    public TextAnalysisService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }
    
    public TextAnalysisResponse analyzeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Tekst do analizy nie może być pusty");
        }
        
        // Ograniczamy długość tekstu, aby nie przekroczyć limitu tokenów
        String trimmedText = limitTextSize(text);
        
        String systemPrompt = """
                Jesteś zaawansowanym asystentem do analizy tekstu.
                Przeanalizuj podany tekst i zwróć:
                1. Krótkie podsumowanie (1-3 zdania)
                2. Listę kluczowych punktów/tez (max 5)
                3. Analizę sentymentu (pozytywny, neutralny, negatywny) z wartościami liczbowymi (1-10)
                4. Dodatkowe interesujące spostrzeżenia
                
                Odpowiedź zwróć dokładnie w poniższym formacie JSON:
                
                {
                  "summary": "Tutaj podsumowanie tekstu",
                  "keyPoints": [
                    "Pierwszy kluczowy punkt",
                    "Drugi kluczowy punkt",
                    "Trzeci kluczowy punkt"
                  ],
                  "sentiment": {
                    "pozytywny": 7,
                    "neutralny": 5,
                    "negatywny": 3
                  },
                  "insights": "Tutaj dodatkowe spostrzeżenia",
                  "complexity": "niski/średni/wysoki"
                }
                
                Ważne: odpowiedz TYLKO czystym JSON, bez dodatkowych wyjaśnień i komentarzy.
                """;
        
        List<Message> messages = List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(trimmedText)
        );
        
        Prompt prompt = new Prompt(messages);
        try {
            ChatResponse response = chatModel.call(prompt);
            String responseContent = response.getResult().toString();
            
            return parseJsonResponse(responseContent);
        } catch (Exception e) {
            // Obsługa błędu - zwracamy podstawową odpowiedź z informacją o błędzie
            return createErrorResponse("Wystąpił błąd podczas analizy: " + e.getMessage(), trimmedText.length());
        }
    }
    
    public TextAnalysisResponse analyzeTextFromFile(MultipartFile file) throws IOException {
        // Sprawdzamy rozmiar pliku
        if (file.getSize() > 1024 * 1024 * 5) { // 5MB limit
            return createErrorResponse("Plik jest zbyt duży. Maksymalny rozmiar to 5MB.", 0);
        }
        
        String fileContent = readFileContent(file);
        return analyzeText(fileContent);
    }
    
    private String readFileContent(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    private String limitTextSize(String text) {
        if (text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        
        // Jeśli tekst jest za długi, zwracamy tylko pierwszy fragment
        // i dodajemy informację o obcięciu
        return text.substring(0, MAX_TEXT_LENGTH) + 
               "\n\n[Tekst został skrócony ze względu na ograniczenia. Oryginalna długość: " + 
               text.length() + " znaków]";
    }
    
    private TextAnalysisResponse createErrorResponse(String errorMessage, int textLength) {
        TextAnalysisResponse response = new TextAnalysisResponse();
        
        response.setSummary("Nie udało się przeprowadzić pełnej analizy.");
        response.setKeyPoints(Arrays.asList("Wystąpił błąd podczas analizy", errorMessage));
        
        Map<String, Integer> sentiment = new HashMap<>();
        sentiment.put("błąd", 0);
        response.setSentimentAnalysis(sentiment);
        
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("długośćTekstu", textLength);
        additionalInfo.put("status", "ERROR");
        additionalInfo.put("wiadomość", errorMessage);
        response.setAdditionalInfo(additionalInfo);
        
        return response;
    }
    
    private TextAnalysisResponse parseJsonResponse(String jsonResponse) {
        TextAnalysisResponse response = new TextAnalysisResponse();
        
        try {
            // Szukamy początku i końca JSON w odpowiedzi
            int jsonStart = jsonResponse.indexOf('{');
            int jsonEnd = jsonResponse.lastIndexOf('}') + 1;
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                // Wycinamy czysty tekst JSON
                String jsonContent = jsonResponse.substring(jsonStart, jsonEnd);
                
                // Proste parsowanie kluczowych elementów przy pomocy wyrażeń regularnych
                // Podsumowanie
                String summary = extractValueByPattern(jsonContent, "\"summary\"\\s*:\\s*\"([^\"]+)\"");
                if (summary != null) {
                    response.setSummary(summary);
                } else {
                    response.setSummary("Brak podsumowania w odpowiedzi.");
                }
                
                // Kluczowe punkty
                List<String> keyPoints = extractArrayValues(jsonContent, "\"keyPoints\"\\s*:\\s*\\[(.*?)\\]");
                if (keyPoints != null && !keyPoints.isEmpty()) {
                    response.setKeyPoints(keyPoints);
                } else {
                    response.setKeyPoints(Arrays.asList("Nie znaleziono kluczowych punktów."));
                }
                
                // Sentyment
                Map<String, Integer> sentiment = new HashMap<>();
                String sentimentSection = extractSection(jsonContent, "\"sentiment\"\\s*:\\s*\\{(.*?)\\}");
                if (sentimentSection != null) {
                    extractSentiment(sentimentSection, sentiment);
                } else {
                    sentiment.put("pozytywny", 5);
                    sentiment.put("neutralny", 5);
                    sentiment.put("negatywny", 5);
                }
                response.setSentimentAnalysis(sentiment);
                
                // Dodatkowe informacje
                Map<String, Object> additionalInfo = new HashMap<>();
                additionalInfo.put("długośćTekstu", jsonResponse.length());
                
                String insights = extractValueByPattern(jsonContent, "\"insights\"\\s*:\\s*\"([^\"]+)\"");
                if (insights != null) {
                    additionalInfo.put("spostrzeżenia", insights);
                }
                
                String complexity = extractValueByPattern(jsonContent, "\"complexity\"\\s*:\\s*\"([^\"]+)\"");
                if (complexity != null) {
                    additionalInfo.put("poziomSkomplikowania", complexity);
                } else {
                    additionalInfo.put("poziomSkomplikowania", "średni");
                }
                
                response.setAdditionalInfo(additionalInfo);
            } else {
                setDefaultResponse(response, "Nie można znaleźć prawidłowej struktury JSON w odpowiedzi.");
            }
        } catch (Exception e) {
            setDefaultResponse(response, "Błąd podczas parsowania odpowiedzi: " + e.getMessage());
        }
        
        return response;
    }
    
    private void setDefaultResponse(TextAnalysisResponse response, String error) {
        response.setSummary("Nie udało się przeanalizować odpowiedzi AI.");
        response.setKeyPoints(Arrays.asList("Błąd parsowania", error));
        
        Map<String, Integer> sentiment = new HashMap<>();
        sentiment.put("pozytywny", 5);
        sentiment.put("neutralny", 5);
        sentiment.put("negatywny", 5);
        response.setSentimentAnalysis(sentiment);
        
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("błąd", error);
        additionalInfo.put("poziomSkomplikowania", "nieznany");
        response.setAdditionalInfo(additionalInfo);
    }
    
    private String extractValueByPattern(String json, String pattern) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }
    
    private List<String> extractArrayValues(String json, String arrayPattern) {
        String arrayContent = extractValueByPattern(json, arrayPattern);
        if (arrayContent == null) return new ArrayList<>();
        
        List<String> results = new ArrayList<>();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"([^\"]+)\"");
        java.util.regex.Matcher m = p.matcher(arrayContent);
        
        while (m.find()) {
            results.add(m.group(1));
        }
        
        return results;
    }
    
    private String extractSection(String json, String sectionPattern) {
        return extractValueByPattern(json, sectionPattern);
    }
    
    private void extractSentiment(String sentimentSection, Map<String, Integer> result) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"(\\w+)\"\\s*:\\s*(\\d+)");
        java.util.regex.Matcher m = p.matcher(sentimentSection);
        
        while (m.find()) {
            String key = m.group(1);
            int value = Integer.parseInt(m.group(2));
            result.put(key, value);
        }
        
        if (result.isEmpty()) {
            // Domyślne wartości, jeśli nie udało się nic znaleźć
            result.put("pozytywny", 5);
            result.put("neutralny", 5);
            result.put("negatywny", 5);
        }
    }
} 