package com.onepath.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "college_required_documents")
public class CollegeRequiredDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private CollegeProcess process;

    @NotBlank
    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean mandatory = true;

    @Column(name = "accepted_formats", length = 200)
    private String acceptedFormats; // e.g. "PDF, JPG (Max 2MB)"

    @Column(columnDefinition = "TEXT")
    private String howToObtain; // Missing document guidance

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CollegeRequiredDocument() {
    }

    public CollegeRequiredDocument(String documentName, String description, boolean mandatory, String acceptedFormats, String howToObtain) {
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

    public CollegeProcess getProcess() {
        return process;
    }

    public void setProcess(CollegeProcess process) {
        this.process = process;
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
