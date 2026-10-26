package com.onepath.dto;

public class ServiceRequiredDocumentDto {
    private Long id;
    private String documentName;
    private String description;
    private boolean mandatory = true;
    private String acceptedFormats;
    private String howToObtain;

    public ServiceRequiredDocumentDto() {
    }

    public ServiceRequiredDocumentDto(Long id, String documentName, String description, boolean mandatory, String acceptedFormats, String howToObtain) {
        this.id = id;
        this.documentName = documentName;
        this.description = description;
        this.mandatory = mandatory;
        this.acceptedFormats = acceptedFormats;
        this.howToObtain = howToObtain;
    }

    public ServiceRequiredDocumentDto(String documentName, String description, boolean mandatory, String acceptedFormats, String howToObtain) {
        this(null, documentName, description, mandatory, acceptedFormats, howToObtain);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
