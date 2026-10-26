package com.onepath.service;

import com.onepath.dto.UserDocumentDto;
import com.onepath.entity.UserDocument;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.UserDocumentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserDocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB limit

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf", ".jpg", ".jpeg", ".png"
    );

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/pjpeg",
            "image/png",
            "image/x-png"
    );

    private final UserDocumentRepository userDocumentRepository;

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    private Path storageLocation;

    public UserDocumentService(UserDocumentRepository userDocumentRepository) {
        this.userDocumentRepository = userDocumentRepository;
    }

    @PostConstruct
    public void init() {
        this.storageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize document storage folder at: " + this.storageLocation, e);
        }
    }

    public Path getStorageLocation() {
        return this.storageLocation;
    }

    @Transactional
    public UserDocumentDto uploadDocument(Long userId, String documentName, String category, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select a valid non-empty file to upload.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum permitted limit of 10 MB.");
        }

        if (!StringUtils.hasText(documentName)) {
            throw new BadRequestException("Document name is required.");
        }

        String rawOriginalFilename = file.getOriginalFilename();
        if (rawOriginalFilename == null || rawOriginalFilename.trim().isEmpty()) {
            throw new BadRequestException("Invalid filename.");
        }

        // Clean filename and validate extension
        String originalFilename = StringUtils.cleanPath(rawOriginalFilename);
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Filename contains invalid relative path sequence: " + originalFilename);
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Unsupported file type. Only PDF, JPG, JPEG, and PNG files are supported.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            // Some browsers send generic application/octet-stream; if extension is valid, allow or standardize
            if ("application/octet-stream".equalsIgnoreCase(contentType)) {
                contentType = resolveMimeFromExtension(extension);
            } else {
                throw new BadRequestException("Unsupported file format: " + contentType + ". Only PDF and Images (JPG, PNG) are allowed.");
            }
        } else if (contentType == null) {
            contentType = resolveMimeFromExtension(extension);
        }

        // Generate non-colliding secure stored filename
        String storedFileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = this.storageLocation.resolve(storedFileName).normalize();

        // Extra path-traversal safety check
        if (!targetLocation.startsWith(this.storageLocation)) {
            throw new BadRequestException("Security exception: Invalid file storage destination path.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to save file on storage server: " + storedFileName, ex);
        }

        String resolvedCategory = (category != null && !category.trim().isEmpty()) ? category.trim() : "GENERAL";

        UserDocument userDocument = new UserDocument(
                userId,
                documentName.trim(),
                resolvedCategory,
                originalFilename,
                storedFileName,
                contentType,
                file.getSize()
        );

        UserDocument saved = userDocumentRepository.save(userDocument);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<UserDocumentDto> getUserDocuments(Long userId, String category) {
        List<UserDocument> docs;
        if (StringUtils.hasText(category) && !"ALL".equalsIgnoreCase(category)) {
            docs = userDocumentRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category);
        } else {
            docs = userDocumentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return docs.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDocument getDocumentForUser(Long userId, Long documentId) {
        return userDocumentRepository.findByIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long userId, Long documentId) {
        UserDocument doc = getDocumentForUser(userId, documentId);
        try {
            Path filePath = this.storageLocation.resolve(doc.getStoredFileName()).normalize();
            if (!filePath.startsWith(this.storageLocation)) {
                throw new BadRequestException("Unauthorized file path access.");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("The requested physical file was not found on disk: " + doc.getOriginalFileName());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File path resolution error: " + doc.getOriginalFileName());
        }
    }

    @Transactional
    public void deleteDocument(Long userId, Long documentId) {
        UserDocument doc = getDocumentForUser(userId, documentId);

        // Delete the physical file from disk
        try {
            Path filePath = this.storageLocation.resolve(doc.getStoredFileName()).normalize();
            if (filePath.startsWith(this.storageLocation)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log but proceed to remove record
        }

        userDocumentRepository.delete(doc);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return "";
    }

    private String resolveMimeFromExtension(String ext) {
        return switch (ext.toLowerCase()) {
            case ".pdf" -> "application/pdf";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }

    public UserDocumentDto toDto(UserDocument doc) {
        UserDocumentDto dto = new UserDocumentDto();
        dto.setId(doc.getId());
        dto.setUserId(doc.getUserId());
        dto.setDocumentName(doc.getDocumentName());
        dto.setCategory(doc.getCategory());
        dto.setOriginalFileName(doc.getOriginalFileName());
        dto.setContentType(doc.getContentType());
        dto.setFileSize(doc.getFileSize());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        return dto;
    }
}
