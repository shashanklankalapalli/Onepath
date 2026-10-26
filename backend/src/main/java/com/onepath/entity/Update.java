package com.onepath.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_updates", indexes = {
    @Index(name = "idx_update_category", columnList = "category"),
    @Index(name = "idx_update_effective_date", columnList = "effective_date")
})
public class Update {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String category; // Fee Changes, Deadline Updates, Policy & Rules, Process Simplification, College Notices

    @Column(name = "related_service_or_process", length = 200)
    private String relatedServiceOrProcess;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "urgent", nullable = false)
    private boolean urgent = false;

    @Column(name = "official_source_url", length = 500)
    private String officialSourceUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Update() {
    }

    public Update(String title, String description, String category, String relatedServiceOrProcess, LocalDate effectiveDate, boolean urgent) {
        this(title, description, category, relatedServiceOrProcess, effectiveDate, urgent, null);
    }

    public Update(String title, String description, String category, String relatedServiceOrProcess, LocalDate effectiveDate, boolean urgent, String officialSourceUrl) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.relatedServiceOrProcess = relatedServiceOrProcess;
        this.effectiveDate = effectiveDate;
        this.urgent = urgent;
        this.officialSourceUrl = officialSourceUrl;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRelatedServiceOrProcess() {
        return relatedServiceOrProcess;
    }

    public void setRelatedServiceOrProcess(String relatedServiceOrProcess) {
        this.relatedServiceOrProcess = relatedServiceOrProcess;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public String getOfficialSourceUrl() {
        return officialSourceUrl;
    }

    public void setOfficialSourceUrl(String officialSourceUrl) {
        this.officialSourceUrl = officialSourceUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
