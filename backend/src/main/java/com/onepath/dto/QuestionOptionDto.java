package com.onepath.dto;

public class QuestionOptionDto {
    private String value;
    private String label;
    private String description;
    private String badge;

    public QuestionOptionDto() {
    }

    public QuestionOptionDto(String value, String label, String description) {
        this.value = value;
        this.label = label;
        this.description = description;
    }

    public QuestionOptionDto(String value, String label, String description, String badge) {
        this.value = value;
        this.label = label;
        this.description = description;
        this.badge = badge;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
