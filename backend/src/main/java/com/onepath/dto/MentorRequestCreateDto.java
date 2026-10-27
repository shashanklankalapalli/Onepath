package com.onepath.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MentorRequestCreateDto {

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name cannot exceed 120 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @NotBlank(message = "Process or department selection is required")
    @Size(max = 150, message = "Process selection cannot exceed 150 characters")
    private String departmentOrProcess;

    @NotBlank(message = "Problem description is required")
    private String problemDescription;

    @NotBlank(message = "Desired outcome is required")
    private String desiredOutcome;

    @NotBlank(message = "Preferred contact method is required")
    @Size(max = 30, message = "Preferred contact method cannot exceed 30 characters")
    private String preferredContactMethod;

    @Size(max = 30, message = "Phone number cannot exceed 30 characters")
    private String phoneNumber;

    private String additionalInfo;

    public MentorRequestCreateDto() {
    }

    public MentorRequestCreateDto(String fullName, String email, String departmentOrProcess,
                                  String problemDescription, String desiredOutcome,
                                  String preferredContactMethod, String phoneNumber,
                                  String additionalInfo) {
        this.fullName = fullName;
        this.email = email;
        this.departmentOrProcess = departmentOrProcess;
        this.problemDescription = problemDescription;
        this.desiredOutcome = desiredOutcome;
        this.preferredContactMethod = preferredContactMethod;
        this.phoneNumber = phoneNumber;
        this.additionalInfo = additionalInfo;
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
}
