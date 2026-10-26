package com.onepath.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CollegeProcessStepDto {
    private Long id;
    private Long processId;

    @NotNull(message = "Step number is required")
    private Integer stepNumber;

    @NotBlank(message = "Step title is required")
    private String title;

    private String description;
    private String estimatedTime;
    private String notes;

    public CollegeProcessStepDto() {
    }

    public CollegeProcessStepDto(Long id, Long processId, Integer stepNumber, String title, String description, String estimatedTime, String notes) {
        this.id = id;
        this.processId = processId;
        this.stepNumber = stepNumber;
        this.title = title;
        this.description = description;
        this.estimatedTime = estimatedTime;
        this.notes = notes;
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

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
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

    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
