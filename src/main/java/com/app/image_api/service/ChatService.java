package com.app.image_api.service;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final OpenAiChatModel chatModel;

    @Autowired
    public ChatService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String generateChatResponse() {
        return chatModel.call("Powiedz jakis fajny żart po polsku");
    }
    
    public String generateJokeAbout(String topic) {
        String prompt = "Powiedz fajny żart po polsku na temat: " + topic;
        return chatModel.call(prompt);
    }
}
