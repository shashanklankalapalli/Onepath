package com.onepath.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "college_process_updates")
public class CollegeProcessUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id", nullable = false)
    private CollegeProcess process;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String updateType; // FEE_CHANGE, DEADLINE_EXTENSION, RULE_REVISION, GENERAL

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CollegeProcessUpdate() {
    }

    public CollegeProcessUpdate(String title, String description, String updateType, LocalDate effectiveDate) {
        this.title = title;
        this.description = description;
        this.updateType = updateType;
        this.effectiveDate = effectiveDate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdateTitle() {
        return title;
    }

    public void setUpdateTitle(String updateTitle) {
        if (updateTitle != null) {
            this.title = updateTitle;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAnnouncementDetails() {
        return description;
    }

    public void setAnnouncementDetails(String announcementDetails) {
        if (announcementDetails != null) {
            this.description = announcementDetails;
        }
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
