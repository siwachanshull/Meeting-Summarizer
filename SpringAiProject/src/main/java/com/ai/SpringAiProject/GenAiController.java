package com.ai.SpringAiProject;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenAiController {

    ChatService chatService;

    public GenAiController(ChatService chatService) {
        this.chatService = chatService;
    }



    @GetMapping("ask-ai")
    public String getResponse(String prompt){
        return chatService.getResponse(prompt);
    }
}
