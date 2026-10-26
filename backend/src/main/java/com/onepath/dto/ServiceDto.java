package com.onepath.dto;

import java.util.ArrayList;
import java.util.List;

public class ServiceDto {
    private Long id;
    private String name;
    private String slug;
    private String category;
    private String description;
    private String eligibility;
    private String timeline;
    private String fees;
    private String importantNotes;
    private String officialPortalUrl;
    private boolean hasQuestionnaire;
    private List<ServiceStepDto> steps = new ArrayList<>();
    private List<ServiceRequiredDocumentDto> requiredDocuments = new ArrayList<>();

    public ServiceDto() {
    }

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

    public List<ServiceStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<ServiceStepDto> steps) {
        this.steps = steps;
    }

    public List<ServiceRequiredDocumentDto> getRequiredDocuments() {
        return requiredDocuments;
    }

    public void setRequiredDocuments(List<ServiceRequiredDocumentDto> requiredDocuments) {
        this.requiredDocuments = requiredDocuments;
    }
}
