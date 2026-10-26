package com.onepath.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatResponseDto {
    private String reply;
    private List<String> suggestedActions = new ArrayList<>();
    private boolean configured;
    private boolean error;

    public ChatResponseDto() {
    }

    public ChatResponseDto(String reply, List<String> suggestedActions, boolean configured, boolean error) {
        this.reply = reply;
        this.suggestedActions = suggestedActions != null ? suggestedActions : new ArrayList<>();
        this.configured = configured;
        this.error = error;
    }

    public static ChatResponseDto success(String reply, List<String> suggestedActions) {
        return new ChatResponseDto(reply, suggestedActions, true, false);
    }

    public static ChatResponseDto notConfigured(String message) {
        List<String> suggestions = List.of(
                "How do I apply for a Passport?",
                "What are the fees for a Driving Licence?",
                "Who is eligible for an EWS certificate?"
        );
        return new ChatResponseDto(message, suggestions, false, false);
    }

    public static ChatResponseDto error(String message) {
        return new ChatResponseDto(message, List.of("Try asking again", "How do I use OnePath?"), true, true);
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<String> suggestedActions) {
        this.suggestedActions = suggestedActions != null ? suggestedActions : new ArrayList<>();
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
