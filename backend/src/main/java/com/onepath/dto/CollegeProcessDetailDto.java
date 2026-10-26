package com.onepath.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CollegeProcessDetailDto {
    private Long id;
    private Long collegeId;
    private String collegeName;
    private String collegeShortName;

    @NotBlank(message = "Process title is required")
    private String title;

    private String description;
    private String category;
    private String eligibility;

    public String getEligibilityCriteria() {
        return eligibility;
    }

    public void setEligibilityCriteria(String eligibilityCriteria) {
        if (eligibilityCriteria != null) {
            this.eligibility = eligibilityCriteria;
        }
    }
    private String timeline;
    private String fees;
    private String status = "ACTIVE";
    private String importantNotes;
    private String officialPortalUrl;

    private List<CollegeProcessStepDto> steps = new ArrayList<>();
    private List<CollegeRequiredDocumentDto> requiredDocuments = new ArrayList<>();
    private List<CollegeProcessUpdateDto> updates = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CollegeProcessDetailDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(Long collegeId) {
        this.collegeId = collegeId;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCollegeShortName() {
        return collegeShortName;
    }

    public void setCollegeShortName(String collegeShortName) {
        this.collegeShortName = collegeShortName;
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

    public List<CollegeProcessStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<CollegeProcessStepDto> steps) {
        this.steps = steps;
    }

    public List<CollegeRequiredDocumentDto> getRequiredDocuments() {
        return requiredDocuments;
    }

    public void setRequiredDocuments(List<CollegeRequiredDocumentDto> requiredDocuments) {
        this.requiredDocuments = requiredDocuments;
    }

    public List<CollegeProcessUpdateDto> getUpdates() {
        return updates;
    }

    public void setUpdates(List<CollegeProcessUpdateDto> updates) {
        this.updates = updates;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
