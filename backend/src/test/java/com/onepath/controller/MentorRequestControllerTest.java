package com.onepath.controller;

import com.onepath.dto.MentorRequestCreateDto;
import com.onepath.dto.MentorRequestResponseDto;
import com.onepath.dto.MentorStatusUpdateDto;
import com.onepath.entity.MentorRequest;
import com.onepath.entity.MentorRequestStatus;
import com.onepath.entity.Role;
import com.onepath.entity.User;
import com.onepath.security.UserPrincipal;
import com.onepath.service.MentorRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentorRequestControllerTest {

    @Mock
    private MentorRequestService mentorRequestService;

    @InjectMocks
    private MentorRequestController mentorRequestController;

    private UserPrincipal studentPrincipal;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        User student = new User("Student", "student@onepath.org", "pass", Role.USER);
        student.setId(5L);
        studentPrincipal = new UserPrincipal(student);

        User admin = new User("Admin", "admin@onepath.org", "pass", Role.SUPER_ADMIN);
        admin.setId(1L);
        adminPrincipal = new UserPrincipal(admin);
    }

    @Test
    @DisplayName("submitRequest returns HTTP 201 with generated request ID")
    void testSubmitRequest() {
        MentorRequestCreateDto dto = new MentorRequestCreateDto(
                "Student", "student@onepath.org", "Passport",
                "Need passport guidance", "Get passport", "WhatsApp", "9876543210", null
        );

        MentorRequest req = new MentorRequest(
                "MNT-123456", 5L, "Student", "student@onepath.org",
                "Passport", "Need passport guidance", "Get passport", "WhatsApp", "9876543210", null
        );
        req.setId(1L);
        MentorRequestResponseDto responseDto = new MentorRequestResponseDto(req);

        when(mentorRequestService.createRequest(any(MentorRequestCreateDto.class), eq(studentPrincipal)))
                .thenReturn(responseDto);

        ResponseEntity<MentorRequestResponseDto> response = mentorRequestController.submitRequest(dto, studentPrincipal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MNT-123456", response.getBody().getRequestId());
        assertEquals(MentorRequestStatus.NEW, response.getBody().getStatus());
    }

    @Test
    @DisplayName("getAllRequests returns filtered request list")
    void testGetAllRequests() {
        MentorRequest req = new MentorRequest(
                "MNT-999888", 5L, "Student", "student@onepath.org",
                "Voter ID", "Need voter card address update", "Update address", "Email", null, null
        );
        req.setId(2L);
        when(mentorRequestService.getAllRequests("Student", MentorRequestStatus.NEW, "Voter ID"))
                .thenReturn(List.of(new MentorRequestResponseDto(req)));

        ResponseEntity<List<MentorRequestResponseDto>> response =
                mentorRequestController.getAllRequests("Student", MentorRequestStatus.NEW, "Voter ID");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("MNT-999888", response.getBody().get(0).getRequestId());
    }

    @Test
    @DisplayName("getMyRequests returns authenticated student's requests")
    void testGetMyRequests() {
        MentorRequest req = new MentorRequest(
                "MNT-555666", 5L, "Student", "student@onepath.org",
                "Driving Licence", "LLR slot booking", "Get LLR", "WhatsApp", "9876543210", null
        );
        req.setId(3L);
        when(mentorRequestService.getMyRequests(5L)).thenReturn(List.of(new MentorRequestResponseDto(req)));

        ResponseEntity<List<MentorRequestResponseDto>> response =
                mentorRequestController.getMyRequests(studentPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("MNT-555666", response.getBody().get(0).getRequestId());
    }

    @Test
    @DisplayName("updateStatus returns updated response with new status")
    void testUpdateStatus() {
        MentorRequest req = new MentorRequest(
                "MNT-123456", 5L, "Student", "student@onepath.org",
                "Passport", "Need passport guidance", "Get passport", "WhatsApp", "9876543210", null
        );
        req.setId(1L);
        req.setStatus(MentorRequestStatus.CONTACTED);
        req.setAdminNotes("Contacted via WhatsApp");

        MentorStatusUpdateDto updateDto = new MentorStatusUpdateDto(MentorRequestStatus.CONTACTED, "Contacted via WhatsApp");
        when(mentorRequestService.updateStatus(1L, updateDto)).thenReturn(new MentorRequestResponseDto(req));

        ResponseEntity<MentorRequestResponseDto> response =
                mentorRequestController.updateStatus(1L, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MentorRequestStatus.CONTACTED, response.getBody().getStatus());
        assertEquals("Contacted via WhatsApp", response.getBody().getAdminNotes());
    }
}
