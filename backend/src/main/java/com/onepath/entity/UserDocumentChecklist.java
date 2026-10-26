package com.onepath.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_document_checklists", indexes = {
    @Index(name = "idx_user_checklist_user", columnList = "user_id"),
    @Index(name = "idx_user_checklist_target", columnList = "target_type, target_id")
})
public class UserDocumentChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType; // COLLEGE_PROCESS or SERVICE

    @NotNull
    @Column(name = "target_id", nullable = false)
    private Long targetId; // college_process_id or service_id

    @NotBlank
    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "is_missing", nullable = false)
    private boolean isMissing = false;

    @Column(columnDefinition = "TEXT")
    private String userNotes;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserDocumentChecklist() {
    }

    public UserDocumentChecklist(Long userId, String targetType, Long targetId, String documentName, boolean completed, boolean isMissing, String userNotes) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.documentName = documentName;
        this.completed = completed;
        this.isMissing = isMissing;
        this.userNotes = userNotes;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isMissing() {
        return isMissing;
    }

    public void setMissing(boolean missing) {
        isMissing = missing;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String userNotes) {
        this.userNotes = userNotes;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
