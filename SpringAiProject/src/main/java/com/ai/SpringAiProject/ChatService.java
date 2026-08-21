package com.ai.SpringAiProject;



import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    public final ChatModel chatModel;

    public ChatService(ChatModel chatModel){
        this.chatModel=chatModel;
    }

    public String getResponse(String prompt){
        return chatModel.call(prompt);
    }
}
