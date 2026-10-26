package com.onepath.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class CollegeProcessUpdateDto {
    private Long id;
    private Long processId;

    @JsonAlias({"updateTitle"})
    private String title;

    @JsonAlias({"announcementDetails"})
    private String description;

    private String updateType;
    private LocalDate effectiveDate;

    public CollegeProcessUpdateDto() {
    }

    public CollegeProcessUpdateDto(Long id, Long processId, String title, String description, String updateType, LocalDate effectiveDate) {
        this.id = id;
        this.processId = processId;
        this.title = title;
        this.description = description;
        this.updateType = updateType;
        this.effectiveDate = effectiveDate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("updateTitle")
    public String getUpdateTitle() {
        return this.title;
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

    @JsonProperty("announcementDetails")
    public String getAnnouncementDetails() {
        return this.description;
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
}
