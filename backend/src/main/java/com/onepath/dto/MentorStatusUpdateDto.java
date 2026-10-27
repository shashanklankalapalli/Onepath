package com.onepath.dto;

import com.onepath.entity.MentorRequestStatus;
import jakarta.validation.constraints.NotNull;

public class MentorStatusUpdateDto {

    @NotNull(message = "Status is required")
    private MentorRequestStatus status;

    private String adminNotes;

    public MentorStatusUpdateDto() {
    }

    public MentorStatusUpdateDto(MentorRequestStatus status, String adminNotes) {
        this.status = status;
        this.adminNotes = adminNotes;
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
}
