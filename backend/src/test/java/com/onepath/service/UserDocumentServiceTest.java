package com.onepath.service;

import com.onepath.dto.UserDocumentDto;
import com.onepath.entity.UserDocument;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.UserDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserDocumentServiceTest {

    private UserDocumentRepository userDocumentRepository;
    private UserDocumentService userDocumentService;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        userDocumentRepository = mock(UserDocumentRepository.class);
        userDocumentService = new UserDocumentService(userDocumentRepository);
        ReflectionTestUtils.setField(userDocumentService, "uploadDir", tempUploadDir.toString());
        userDocumentService.init();
    }

    @Test
    void uploadDocument_success_pdf() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "aadhaar_card.pdf",
                "application/pdf",
                "%PDF-1.4 sample content".getBytes()
        );

        when(userDocumentRepository.save(any(UserDocument.class))).thenAnswer(invocation -> {
            UserDocument doc = invocation.getArgument(0);
            doc.setId(101L);
            return doc;
        });

        UserDocumentDto result = userDocumentService.uploadDocument(1L, "My Aadhaar Card", "IDENTITY", mockFile);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("My Aadhaar Card", result.getDocumentName());
        assertEquals("IDENTITY", result.getCategory());
        assertEquals("aadhaar_card.pdf", result.getOriginalFileName());
        assertEquals("application/pdf", result.getContentType());
        assertTrue(result.getFileSize() > 0);

        ArgumentCaptor<UserDocument> captor = ArgumentCaptor.forClass(UserDocument.class);
        verify(userDocumentRepository).save(captor.capture());
        UserDocument saved = captor.getValue();
        Path storedFile = tempUploadDir.resolve(saved.getStoredFileName());
        assertTrue(Files.exists(storedFile), "Physical file should exist on disk in storage folder");
        assertEquals("%PDF-1.4 sample content", Files.readString(storedFile));
    }

    @Test
    void uploadDocument_success_image() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "pan_card.jpg",
                "image/jpeg",
                new byte[]{ (byte) 0xFF, (byte) 0xD8, (byte) 0xFF }
        );

        when(userDocumentRepository.save(any(UserDocument.class))).thenAnswer(invocation -> {
            UserDocument doc = invocation.getArgument(0);
            doc.setId(102L);
            return doc;
        });

        UserDocumentDto result = userDocumentService.uploadDocument(1L, "PAN Card", "IDENTITY", mockFile);

        assertNotNull(result);
        assertEquals("pan_card.jpg", result.getOriginalFileName());
        assertEquals("image/jpeg", result.getContentType());
    }

    @Test
    void uploadDocument_oversized_throwsException() {
        byte[] oversized = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "huge.pdf",
                "application/pdf",
                oversized
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userDocumentService.uploadDocument(1L, "Huge File", "OTHER", mockFile)
        );
        assertTrue(ex.getMessage().contains("10 MB"));
        verifyNoInteractions(userDocumentRepository);
    }

    @Test
    void uploadDocument_invalidFileType_throwsException() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "script.sh",
                "application/x-sh",
                "echo 'hello'".getBytes()
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userDocumentService.uploadDocument(1L, "Script", "OTHER", mockFile)
        );
        assertTrue(ex.getMessage().contains("Unsupported file type"));
        verifyNoInteractions(userDocumentRepository);
    }

    @Test
    void uploadDocument_pathTraversal_throwsException() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "../../etc/passwd.pdf",
                "application/pdf",
                "%PDF-1.4 fake".getBytes()
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userDocumentService.uploadDocument(1L, "Hacked", "OTHER", mockFile)
        );
        assertTrue(ex.getMessage().contains("invalid relative path sequence"));
        verifyNoInteractions(userDocumentRepository);
    }

    @Test
    void getUserDocuments_filtersByUser() {
        UserDocument doc1 = new UserDocument(1L, "Doc 1", "IDENTITY", "d1.pdf", "uuid1.pdf", "application/pdf", 100L);
        doc1.setId(1L);

        when(userDocumentRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(doc1));

        List<UserDocumentDto> list = userDocumentService.getUserDocuments(1L, null);

        assertEquals(1, list.size());
        assertEquals("Doc 1", list.get(0).getDocumentName());
        verify(userDocumentRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getDocumentForUser_otherUser_throwsException() {
        when(userDocumentRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userDocumentService.getDocumentForUser(1L, 99L)
        );
    }

    @Test
    void loadFileAsResource_success() throws IOException {
        String storedName = "test_uuid.pdf";
        Path diskFile = tempUploadDir.resolve(storedName);
        Files.writeString(diskFile, "sample content");

        UserDocument doc = new UserDocument(1L, "Doc", "IDENTITY", "doc.pdf", storedName, "application/pdf", 14L);
        doc.setId(10L);

        when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(doc));

        Resource resource = userDocumentService.loadFileAsResource(1L, 10L);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void deleteDocument_success() throws IOException {
        String storedName = "to_delete.png";
        Path diskFile = tempUploadDir.resolve(storedName);
        Files.writeString(diskFile, "image content");

        UserDocument doc = new UserDocument(1L, "Doc", "IDENTITY", "doc.png", storedName, "image/png", 13L);
        doc.setId(20L);

        when(userDocumentRepository.findByIdAndUserId(20L, 1L)).thenReturn(Optional.of(doc));

        userDocumentService.deleteDocument(1L, 20L);

        verify(userDocumentRepository).delete(doc);
        assertFalse(Files.exists(diskFile), "File should be deleted from disk");
    }
}
