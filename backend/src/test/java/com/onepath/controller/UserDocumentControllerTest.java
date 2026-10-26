package com.onepath.controller;

import com.onepath.dto.UserDocumentDto;
import com.onepath.entity.Role;
import com.onepath.entity.User;
import com.onepath.entity.UserDocument;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.UserDocumentRepository;
import com.onepath.security.UserPrincipal;
import com.onepath.service.UserDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

@ExtendWith(MockitoExtension.class)
class UserDocumentControllerTest {

    @Mock
    private UserDocumentRepository userDocumentRepository;

    private UserDocumentService userDocumentService;
    private UserDocumentController userDocumentController;

    private UserPrincipal testUser;
    private UserPrincipal otherUser;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        userDocumentService = new UserDocumentService(userDocumentRepository);
        ReflectionTestUtils.setField(userDocumentService, "uploadDir", tempUploadDir.toString());
        userDocumentService.init();

        userDocumentController = new UserDocumentController(userDocumentService);

        User u1 = new User("Test Student", "student@onepath.org", "password", Role.USER);
        u1.setId(1L);
        testUser = new UserPrincipal(u1);

        User u2 = new User("Other User", "other@onepath.org", "password", Role.USER);
        u2.setId(2L);
        otherUser = new UserPrincipal(u2);
    }

    @Test
    void uploadDocument_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "aadhaar.pdf",
                "application/pdf",
                "%PDF-1.4 test".getBytes()
        );

        when(userDocumentRepository.save(any(UserDocument.class))).thenAnswer(invocation -> {
            UserDocument doc = invocation.getArgument(0);
            doc.setId(10L);
            return doc;
        });

        ResponseEntity<UserDocumentDto> response = userDocumentController.uploadDocument(
                file, "Aadhaar Card", "IDENTITY", testUser
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("Aadhaar Card", response.getBody().getDocumentName());
        assertEquals("aadhaar.pdf", response.getBody().getOriginalFileName());
    }

    @Test
    void uploadDocument_invalidType_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "bad bytes".getBytes()
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userDocumentController.uploadDocument(file, "Malware", "OTHER", testUser)
        );
        assertTrue(ex.getMessage().contains("Unsupported file type"));
    }

    @Test
    void getUserDocuments_success() {
        UserDocument doc = new UserDocument(1L, "Aadhaar Card", "IDENTITY", "aadhaar.pdf", "uuid.pdf", "application/pdf", 14L);
        doc.setId(10L);

        when(userDocumentRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(doc));

        ResponseEntity<List<UserDocumentDto>> response = userDocumentController.getUserDocuments(null, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Aadhaar Card", response.getBody().get(0).getDocumentName());
    }

    @Test
    void getDocumentFile_inlineView_success() throws IOException {
        String storedName = "uuid_view.pdf";
        Path diskFile = tempUploadDir.resolve(storedName);
        Files.writeString(diskFile, "%PDF-1.4 sample content");

        UserDocument doc = new UserDocument(1L, "Aadhaar", "IDENTITY", "aadhaar.pdf", storedName, "application/pdf", 14L);
        doc.setId(10L);

        when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(doc));

        ResponseEntity<Resource> response = userDocumentController.getDocumentFile(10L, false, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("inline"));
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("aadhaar.pdf"));
    }

    @Test
    void getDocumentFile_download_success() throws IOException {
        String storedName = "uuid_download.pdf";
        Path diskFile = tempUploadDir.resolve(storedName);
        Files.writeString(diskFile, "%PDF-1.4 sample content");

        UserDocument doc = new UserDocument(1L, "Aadhaar", "IDENTITY", "aadhaar.pdf", storedName, "application/pdf", 14L);
        doc.setId(10L);

        when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(doc));

        ResponseEntity<Resource> response = userDocumentController.getDocumentFile(10L, true, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("attachment"));
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("aadhaar.pdf"));
    }

    @Test
    void getDocumentFile_unauthorizedAccess_throwsException() {
        // User 2 trying to access user 1's document
        when(userDocumentRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userDocumentController.getDocumentFile(10L, false, otherUser)
        );
    }

    @Test
    void deleteDocument_success() throws IOException {
        String storedName = "uuid_del.pdf";
        Path diskFile = tempUploadDir.resolve(storedName);
        Files.writeString(diskFile, "%PDF-1.4 sample content");

        UserDocument doc = new UserDocument(1L, "Aadhaar", "IDENTITY", "aadhaar.pdf", storedName, "application/pdf", 14L);
        doc.setId(10L);

        when(userDocumentRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(doc));

        ResponseEntity<Void> response = userDocumentController.deleteDocument(10L, testUser);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userDocumentRepository).delete(doc);
        assertFalse(Files.exists(diskFile), "Physical file should be deleted from disk");
    }
}
