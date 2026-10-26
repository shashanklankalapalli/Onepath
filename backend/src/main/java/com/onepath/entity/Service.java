package com.onepath.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services", indexes = {
    @Index(name = "idx_service_category", columnList = "category"),
    @Index(name = "idx_service_slug", columnList = "slug", unique = true)
})
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String category; // Government & Civic Services, Education, Documents & Certificates, Other Institutional Services

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    @Column(columnDefinition = "TEXT")
    private String timeline;

    @Column(columnDefinition = "TEXT")
    private String fees;

    @Column(columnDefinition = "TEXT")
    private String importantNotes;

    @Column(length = 255)
    private String officialPortalUrl;

    @Column(name = "has_questionnaire", nullable = false)
    private boolean hasQuestionnaire = false;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<ServiceStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceRequiredDocument> requiredDocuments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Service() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addStep(ServiceStep step) {
        steps.add(step);
        step.setService(this);
    }

    public void removeStep(ServiceStep step) {
        steps.remove(step);
        step.setService(null);
    }

    public void addDocument(ServiceRequiredDocument doc) {
        requiredDocuments.add(doc);
        doc.setService(this);
    }

    public void removeDocument(ServiceRequiredDocument doc) {
        requiredDocuments.remove(doc);
        doc.setService(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEligibility() {
        return eligibility;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public String getTimeline() {
        return timeline;
    }

    public void setTimeline(String timeline) {
        this.timeline = timeline;
    }

    public String getFees() {
        return fees;
    }

    public void setFees(String fees) {
        this.fees = fees;
    }

    public String getImportantNotes() {
        return importantNotes;
    }

    public void setImportantNotes(String importantNotes) {
        this.importantNotes = importantNotes;
    }

    public String getOfficialPortalUrl() {
        return officialPortalUrl;
    }

    public void setOfficialPortalUrl(String officialPortalUrl) {
        this.officialPortalUrl = officialPortalUrl;
    }

    public boolean isHasQuestionnaire() {
        return hasQuestionnaire;
    }

    public void setHasQuestionnaire(boolean hasQuestionnaire) {
        this.hasQuestionnaire = hasQuestionnaire;
    }

    public List<ServiceStep> getSteps() {
        return steps;
    }

    public void setSteps(List<ServiceStep> steps) {
        this.steps = steps;
    }

    public List<ServiceRequiredDocument> getRequiredDocuments() {
        return requiredDocuments;
    }

    public void setRequiredDocuments(List<ServiceRequiredDocument> requiredDocuments) {
        this.requiredDocuments = requiredDocuments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
