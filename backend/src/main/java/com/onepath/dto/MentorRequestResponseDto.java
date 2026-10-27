package com.onepath.dto;

import com.onepath.entity.MentorRequest;
import com.onepath.entity.MentorRequestStatus;
import java.time.LocalDateTime;

public class MentorRequestResponseDto {

    private Long id;
    private String requestId;
    private Long userId;
    private String fullName;
    private String email;
    private String departmentOrProcess;
    private String problemDescription;
    private String desiredOutcome;
    private String preferredContactMethod;
    private String phoneNumber;
    private String additionalInfo;
    private MentorRequestStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public MentorRequestResponseDto() {
    }

    public MentorRequestResponseDto(MentorRequest entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.requestId = entity.getRequestId();
            this.userId = entity.getUserId();
            this.fullName = entity.getFullName();
            this.email = entity.getEmail();
            this.departmentOrProcess = entity.getDepartmentOrProcess();
            this.problemDescription = entity.getProblemDescription();
            this.desiredOutcome = entity.getDesiredOutcome();
            this.preferredContactMethod = entity.getPreferredContactMethod();
            this.phoneNumber = entity.getPhoneNumber();
            this.additionalInfo = entity.getAdditionalInfo();
            this.status = entity.getStatus();
            this.adminNotes = entity.getAdminNotes();
            this.createdAt = entity.getCreatedAt();
            this.updatedAt = entity.getUpdatedAt();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartmentOrProcess() {
        return departmentOrProcess;
    }

    public void setDepartmentOrProcess(String departmentOrProcess) {
        this.departmentOrProcess = departmentOrProcess;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getDesiredOutcome() {
        return desiredOutcome;
    }

    public void setDesiredOutcome(String desiredOutcome) {
        this.desiredOutcome = desiredOutcome;
    }

    public String getPreferredContactMethod() {
        return preferredContactMethod;
    }

    public void setPreferredContactMethod(String preferredContactMethod) {
        this.preferredContactMethod = preferredContactMethod;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public MentorRequestStatus getStatus() {
        return status;
    }

    public void setStatus(MentorRequestStatus status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
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
