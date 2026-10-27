package com.onepath.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_requests", indexes = {
    @Index(name = "idx_mentor_request_id", columnList = "request_id", unique = true),
    @Index(name = "idx_mentor_status", columnList = "status"),
    @Index(name = "idx_mentor_user_id", columnList = "user_id"),
    @Index(name = "idx_mentor_created_at", columnList = "created_at")
})
public class MentorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "request_id", nullable = false, unique = true, length = 30)
    private String requestId;

    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Email
    @NotBlank
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank
    @Column(name = "department_or_process", nullable = false, length = 150)
    private String departmentOrProcess;

    @NotBlank
    @Column(name = "problem_description", nullable = false, columnDefinition = "TEXT")
    private String problemDescription;

    @NotBlank
    @Column(name = "desired_outcome", nullable = false, columnDefinition = "TEXT")
    private String desiredOutcome;

    @NotBlank
    @Column(name = "preferred_contact_method", nullable = false, length = 30)
    private String preferredContactMethod;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MentorRequestStatus status = MentorRequestStatus.NEW;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public MentorRequest() {
    }

    public MentorRequest(String requestId, Long userId, String fullName, String email,
                         String departmentOrProcess, String problemDescription,
                         String desiredOutcome, String preferredContactMethod,
                         String phoneNumber, String additionalInfo) {
        this.requestId = requestId;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.departmentOrProcess = departmentOrProcess;
        this.problemDescription = problemDescription;
        this.desiredOutcome = desiredOutcome;
        this.preferredContactMethod = preferredContactMethod;
        this.phoneNumber = phoneNumber;
        this.additionalInfo = additionalInfo;
        this.status = MentorRequestStatus.NEW;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = MentorRequestStatus.NEW;
        }
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
