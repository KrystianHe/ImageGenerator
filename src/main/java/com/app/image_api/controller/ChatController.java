package com.app.image_api.controller;

import com.app.image_api.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @Autowired
    ChatService chatService;

    @GetMapping("/chat")
    public String chat(){
        return chatService.generateChatResponse();
    }
    
    @GetMapping("/joke")
    public String joke(@RequestParam(required = false, defaultValue = "programowanie") String topic){
        return chatService.generateJokeAbout(topic);
    }
}
