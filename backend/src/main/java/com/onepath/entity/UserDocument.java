package com.onepath.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_documents", indexes = {
    @Index(name = "idx_user_documents_user", columnList = "user_id"),
    @Index(name = "idx_user_documents_category", columnList = "user_id, category")
})
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank
    @Column(name = "document_name", nullable = false, length = 200)
    private String documentName;

    @Column(name = "category", length = 100)
    private String category;

    @NotBlank
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @NotBlank
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @NotBlank
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserDocument() {
    }

    public UserDocument(Long userId, String documentName, String category,
                        String originalFileName, String storedFileName,
                        String contentType, Long fileSize) {
        this.userId = userId;
        this.documentName = documentName;
        this.category = category;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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
