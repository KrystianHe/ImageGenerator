package com.app.image_api.service;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final OpenAiChatModel chatModel;

    public ChatService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String generateChatResponse() {
        return chatModel.call("Powiedz jakis fajny żart po polsku");
    }
    
    public String generateJokeAbout(String topic) {
        return chatModel.call("Powiedz fajny żart po polsku na temat: " + topic);
    }
}
