package com.onepath.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "college_processes", indexes = {
    @Index(name = "idx_college_proc_category", columnList = "category"),
    @Index(name = "idx_college_proc_status", columnList = "status")
})
public class CollegeProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id", nullable = false)
    private College college;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category; // Admissions, Hostel, Scholarships, Examinations, Certificates, Placement

    @Column(columnDefinition = "TEXT")
    private String eligibility;

    @Column(columnDefinition = "TEXT")
    private String timeline; // e.g. "3 - 5 business days", "June 1 - July 15"

    @Column(columnDefinition = "TEXT")
    private String fees; // e.g. "₹500 application fee" or "Free"

    @Column(length = 50)
    private String status = "ACTIVE"; // ACTIVE, UPCOMING, CLOSED

    @Column(columnDefinition = "TEXT")
    private String importantNotes;

    @Column(length = 255)
    private String officialPortalUrl;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<CollegeProcessStep> steps = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollegeRequiredDocument> requiredDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("effectiveDate DESC")
    private List<CollegeProcessUpdate> updates = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public CollegeProcess() {
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

    // Helper methods for bidirectional synchronization
    public void addStep(CollegeProcessStep step) {
        steps.add(step);
        step.setProcess(this);
    }

    public void removeStep(CollegeProcessStep step) {
        steps.remove(step);
        step.setProcess(null);
    }

    public void addDocument(CollegeRequiredDocument doc) {
        requiredDocuments.add(doc);
        doc.setProcess(this);
    }

    public void removeDocument(CollegeRequiredDocument doc) {
        requiredDocuments.remove(doc);
        doc.setProcess(null);
    }

    public void addUpdate(CollegeProcessUpdate update) {
        updates.add(update);
        update.setProcess(this);
    }

    public void removeUpdate(CollegeProcessUpdate update) {
        updates.remove(update);
        update.setProcess(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<CollegeProcessStep> getSteps() {
        return steps;
    }

    public void setSteps(List<CollegeProcessStep> steps) {
        this.steps = steps;
    }

    public List<CollegeRequiredDocument> getRequiredDocuments() {
        return requiredDocuments;
    }

    public void setRequiredDocuments(List<CollegeRequiredDocument> requiredDocuments) {
        this.requiredDocuments = requiredDocuments;
    }

    public List<CollegeProcessUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<CollegeProcessUpdate> updates) {
        this.updates = updates;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
