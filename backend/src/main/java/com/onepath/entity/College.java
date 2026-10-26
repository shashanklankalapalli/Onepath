package com.onepath.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "colleges", indexes = {
    @Index(name = "idx_college_name", columnList = "name"),
    @Index(name = "idx_college_short_name", columnList = "short_name")
})
public class College {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String website;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "college", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollegeProcess> processes = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public College() {
    }

    public College(String name, String shortName, String location, String website, String description) {
        this.name = name;
        this.shortName = shortName;
        this.location = location;
        this.website = website;
        this.description = description;
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

    public List<CollegeProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<CollegeProcess> processes) {
        this.processes = processes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
