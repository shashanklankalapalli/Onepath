package com.onepath.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CollegeDto {
    private Long id;

    @NotBlank(message = "College name is required")
    private String name;

    private String shortName;
    private String location;
    private String website;
    private String logoUrl;
    private String description;
    private boolean active = true;
    private int processCount;
    private List<CollegeProcessSummaryDto> processes;

    public CollegeDto() {
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getProcessCount() {
        return processCount;
    }

    public void setProcessCount(int processCount) {
        this.processCount = processCount;
    }

    public List<CollegeProcessSummaryDto> getProcesses() {
        return processes;
    }

    public void setProcesses(List<CollegeProcessSummaryDto> processes) {
        this.processes = processes;
    }
}
