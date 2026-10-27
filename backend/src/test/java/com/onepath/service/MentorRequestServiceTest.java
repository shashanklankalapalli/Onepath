package com.onepath.service;

import com.onepath.dto.MentorRequestCreateDto;
import com.onepath.dto.MentorRequestResponseDto;
import com.onepath.dto.MentorStatusUpdateDto;
import com.onepath.entity.MentorRequest;
import com.onepath.entity.MentorRequestStatus;
import com.onepath.entity.Role;
import com.onepath.entity.User;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.MentorRequestRepository;
import com.onepath.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorRequestServiceTest {

    @Mock
    private MentorRequestRepository mentorRequestRepository;

    @InjectMocks
    private MentorRequestService mentorRequestService;

    private User studentUser;
    private UserPrincipal studentPrincipal;
    private User adminUser;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        studentUser = new User("John Doe", "john@example.com", "hash", Role.USER);
        studentUser.setId(10L);
        studentPrincipal = new UserPrincipal(studentUser);

        adminUser = new User("Admin", "admin@onepath.org", "hash", Role.SUPER_ADMIN);
        adminUser.setId(1L);
        adminPrincipal = new UserPrincipal(adminUser);
    }

    @Test
    @DisplayName("createRequest successfully creates a request with MNT- formatted ID")
    void testCreateRequest_Success() {
        MentorRequestCreateDto dto = new MentorRequestCreateDto(
                "John Doe",
                "john@example.com",
                "Passport",
                "Need guidance on police verification with hostel address",
                "Complete tatkaal passport application successfully",
                "WhatsApp",
                "9876543210",
                "Living in university hostel"
        );

        when(mentorRequestRepository.existsByRequestId(anyString())).thenReturn(false);
        when(mentorRequestRepository.save(any(MentorRequest.class))).thenAnswer(invocation -> {
            MentorRequest r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        MentorRequestResponseDto result = mentorRequestService.createRequest(dto, studentPrincipal);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("Passport", result.getDepartmentOrProcess());
        assertEquals(MentorRequestStatus.NEW, result.getStatus());
        assertEquals(10L, result.getUserId());
        assertNotNull(result.getRequestId());
        assertTrue(result.getRequestId().startsWith("MNT-"), "Request ID must start with 'MNT-'");
        assertEquals(10, result.getRequestId().length(), "MNT- + 6 chars = 10 length");

        verify(mentorRequestRepository).save(any(MentorRequest.class));
    }

    @Test
    @DisplayName("createRequest fails when phone is missing for Phone call contact method")
    void testCreateRequest_MissingPhone_ThrowsBadRequest() {
        MentorRequestCreateDto dto = new MentorRequestCreateDto(
                "John Doe",
                "john@example.com",
                "Driving Licence",
                "Need help booking test slot",
                "Clear driving licence test",
                "Phone call",
                "", // Blank phone
                null
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                mentorRequestService.createRequest(dto, studentPrincipal)
        );
        assertTrue(ex.getMessage().contains("Phone number is required"));
        verify(mentorRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("createRequest succeeds without phone when contact method is Email")
    void testCreateRequest_EmailContactMethod_NoPhoneRequired() {
        MentorRequestCreateDto dto = new MentorRequestCreateDto(
                "Jane Doe",
                "jane@example.com",
                "JoSAA / College Admissions",
                "Need help reviewing seat acceptance documents",
                "Verify certificates before reporting date",
                "Email",
                null,
                null
        );

        when(mentorRequestRepository.existsByRequestId(anyString())).thenReturn(false);
        when(mentorRequestRepository.save(any(MentorRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MentorRequestResponseDto result = mentorRequestService.createRequest(dto, null);

        assertNotNull(result);
        assertEquals("jane@example.com", result.getEmail());
        assertNull(result.getPhoneNumber());
        assertNull(result.getUserId()); // Unauthenticated guest submission
        verify(mentorRequestRepository).save(any());
    }

    @Test
    @DisplayName("updateStatus successfully changes status and updates admin notes")
    void testUpdateStatus_Success() {
        MentorRequest request = new MentorRequest(
                "MNT-ABC123", 10L, "John", "john@example.com",
                "PAN Card", "Issue with minor PAN card update",
                "Update parent signature", "Email", null, null
        );
        request.setId(50L);

        when(mentorRequestRepository.findById(50L)).thenReturn(Optional.of(request));
        when(mentorRequestRepository.save(any(MentorRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MentorStatusUpdateDto updateDto = new MentorStatusUpdateDto(MentorRequestStatus.CONTACTED, "Spoke with applicant via email");
        MentorRequestResponseDto updated = mentorRequestService.updateStatus(50L, updateDto);

        assertEquals(MentorRequestStatus.CONTACTED, updated.getStatus());
        assertEquals("Spoke with applicant via email", updated.getAdminNotes());
    }

    @Test
    @DisplayName("getRequestById denies access if another student attempts to view a private request")
    void testGetRequestById_UnauthorizedStudent_ThrowsAccessDenied() {
        MentorRequest request = new MentorRequest(
                "MNT-XYZ999", 99L, "Other Student", "other@example.com",
                "Aadhaar", "Need biometric update", "Update biometric", "Email", null, null
        );
        request.setId(200L);

        when(mentorRequestRepository.findById(200L)).thenReturn(Optional.of(request));

        // studentPrincipal has ID 10L, but request belongs to 99L
        assertThrows(AccessDeniedException.class, () ->
                mentorRequestService.getRequestById(200L, studentPrincipal)
        );
    }

    @Test
    @DisplayName("getRequestById allows admin to view any student's request")
    void testGetRequestById_AdminAccess_Succeeds() {
        MentorRequest request = new MentorRequest(
                "MNT-XYZ999", 99L, "Other Student", "other@example.com",
                "Aadhaar", "Need biometric update", "Update biometric", "Email", null, null
        );
        request.setId(200L);

        when(mentorRequestRepository.findById(200L)).thenReturn(Optional.of(request));

        MentorRequestResponseDto result = mentorRequestService.getRequestById(200L, adminPrincipal);
        assertNotNull(result);
        assertEquals("MNT-XYZ999", result.getRequestId());
    }

    @Test
    @DisplayName("getAllRequests delegates to searchRequests in repository")
    void testGetAllRequests() {
        MentorRequest req = new MentorRequest(
                "MNT-111222", 10L, "Alice", "alice@example.com",
                "Passport", "Need emergency renewal", "Get passport renewed", "WhatsApp", "9998887776", null
        );
        when(mentorRequestRepository.searchRequests("Alice", MentorRequestStatus.NEW, "Passport"))
                .thenReturn(List.of(req));

        List<MentorRequestResponseDto> list = mentorRequestService.getAllRequests("Alice", MentorRequestStatus.NEW, "Passport");
        assertEquals(1, list.size());
        assertEquals("MNT-111222", list.get(0).getRequestId());
    }
}
