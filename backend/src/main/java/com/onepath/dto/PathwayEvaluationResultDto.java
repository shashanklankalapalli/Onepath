package com.onepath.dto;

import java.util.ArrayList;
import java.util.List;

public class PathwayEvaluationResultDto {
    private String pathwayCode;
    private String pathwayTitle;
    private String badgeText;
    private String matchReason;
    private String validity;
    private String emigrationStatus;
    private String policeVerificationMode;
    private String policeVerificationDetails;
    private String calculatedFee;
    private String feeBreakdown;
    private String estimatedTimeline;
    private String officialPortalUrl;
    private List<ServiceRequiredDocumentDto> documents = new ArrayList<>();
    private List<ServiceStepDto> steps = new ArrayList<>();
    private List<String> criticalTips = new ArrayList<>();

    public PathwayEvaluationResultDto() {
    }

    public String getPathwayCode() {
        return pathwayCode;
    }

    public void setPathwayCode(String pathwayCode) {
        this.pathwayCode = pathwayCode;
    }

    public String getPathwayTitle() {
        return pathwayTitle;
    }

    public void setPathwayTitle(String pathwayTitle) {
        this.pathwayTitle = pathwayTitle;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getEmigrationStatus() {
        return emigrationStatus;
    }

    public void setEmigrationStatus(String emigrationStatus) {
        this.emigrationStatus = emigrationStatus;
    }

    public String getPoliceVerificationMode() {
        return policeVerificationMode;
    }

    public void setPoliceVerificationMode(String policeVerificationMode) {
        this.policeVerificationMode = policeVerificationMode;
    }

    public String getPoliceVerificationDetails() {
        return policeVerificationDetails;
    }

    public void setPoliceVerificationDetails(String policeVerificationDetails) {
        this.policeVerificationDetails = policeVerificationDetails;
    }

    public String getCalculatedFee() {
        return calculatedFee;
    }

    public void setCalculatedFee(String calculatedFee) {
        this.calculatedFee = calculatedFee;
    }

    public String getFeeBreakdown() {
        return feeBreakdown;
    }

    public void setFeeBreakdown(String feeBreakdown) {
        this.feeBreakdown = feeBreakdown;
    }

    public String getEstimatedTimeline() {
        return estimatedTimeline;
    }

    public void setEstimatedTimeline(String estimatedTimeline) {
        this.estimatedTimeline = estimatedTimeline;
    }

    public String getOfficialPortalUrl() {
        return officialPortalUrl;
    }

    public void setOfficialPortalUrl(String officialPortalUrl) {
        this.officialPortalUrl = officialPortalUrl;
    }

    public List<ServiceRequiredDocumentDto> getDocuments() {
        return documents;
    }

    public void setDocuments(List<ServiceRequiredDocumentDto> documents) {
        this.documents = documents;
    }

    public List<ServiceStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<ServiceStepDto> steps) {
        this.steps = steps;
    }

    public List<String> getCriticalTips() {
        return criticalTips;
    }

    public void setCriticalTips(List<String> criticalTips) {
        this.criticalTips = criticalTips;
    }
}
