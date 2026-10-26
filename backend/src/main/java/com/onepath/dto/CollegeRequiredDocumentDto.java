package com.onepath.dto;

import jakarta.validation.constraints.NotBlank;

public class CollegeRequiredDocumentDto {
    private Long id;
    private Long processId;

    @NotBlank(message = "Document name is required")
    private String documentName;

    private String description;
    private boolean mandatory = true;
    private String acceptedFormats;
    private String howToObtain;

    public CollegeRequiredDocumentDto() {
    }

    public CollegeRequiredDocumentDto(Long id, Long processId, String documentName, String description, boolean mandatory, String acceptedFormats, String howToObtain) {
        this.id = id;
        this.processId = processId;
        this.documentName = documentName;
        this.description = description;
        this.mandatory = mandatory;
        this.acceptedFormats = acceptedFormats;
        this.howToObtain = howToObtain;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getAcceptedFormats() {
        return acceptedFormats;
    }

    public void setAcceptedFormats(String acceptedFormats) {
        this.acceptedFormats = acceptedFormats;
    }

    public String getHowToObtain() {
        return howToObtain;
    }

    public void setHowToObtain(String howToObtain) {
        this.howToObtain = howToObtain;
    }
}
