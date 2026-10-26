package com.onepath.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class ChatRequestDto {

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    private List<ChatMessageDto> conversationHistory = new ArrayList<>();

    private String currentPage;

    public ChatRequestDto() {
    }

    public ChatRequestDto(String message) {
        this.message = message;
    }

    public ChatRequestDto(String message, List<ChatMessageDto> conversationHistory, String currentPage) {
        this.message = message;
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
        this.currentPage = currentPage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageDto> getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(List<ChatMessageDto> conversationHistory) {
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }
}
