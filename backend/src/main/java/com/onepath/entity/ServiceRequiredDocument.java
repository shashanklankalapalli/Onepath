package com.onepath.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_required_documents")
public class ServiceRequiredDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @NotBlank
    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean mandatory = true;

    @Column(name = "accepted_formats", length = 200)
    private String acceptedFormats;

    @Column(columnDefinition = "TEXT")
    private String howToObtain; // Missing document guidance

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ServiceRequiredDocument() {
    }

    public ServiceRequiredDocument(String documentName, String description, boolean mandatory, String acceptedFormats, String howToObtain) {
        this.documentName = documentName;
        this.description = description;
        this.mandatory = mandatory;
        this.acceptedFormats = acceptedFormats;
        this.howToObtain = howToObtain;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
