package com.onepath.controller;

import com.onepath.dto.ChatRequestDto;
import com.onepath.dto.ChatResponseDto;
import com.onepath.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/chat", "/chat"})
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDto> chat(@Valid @RequestBody ChatRequestDto request) {
        ChatResponseDto response = chatbotService.processChat(request);
        return ResponseEntity.ok(response);
    }
}
