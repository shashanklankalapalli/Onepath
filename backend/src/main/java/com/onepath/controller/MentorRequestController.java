package com.onepath.controller;

import com.onepath.dto.MentorRequestCreateDto;
import com.onepath.dto.MentorRequestResponseDto;
import com.onepath.dto.MentorStatusUpdateDto;
import com.onepath.entity.MentorRequestStatus;
import com.onepath.security.UserPrincipal;
import com.onepath.service.MentorRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/mentor-requests", "/mentor-requests"})
public class MentorRequestController {

    private final MentorRequestService mentorRequestService;

    public MentorRequestController(MentorRequestService mentorRequestService) {
        this.mentorRequestService = mentorRequestService;
    }

    @PostMapping
    public ResponseEntity<MentorRequestResponseDto> submitRequest(
            @Valid @RequestBody MentorRequestCreateDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        MentorRequestResponseDto created = mentorRequestService.createRequest(dto, userPrincipal);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<MentorRequestResponseDto>> getAllRequests(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) MentorRequestStatus status,
            @RequestParam(required = false) String process) {
        List<MentorRequestResponseDto> list = mentorRequestService.getAllRequests(search, status, process);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MentorRequestResponseDto>> getMyRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<MentorRequestResponseDto> list = mentorRequestService.getMyRequests(userPrincipal.getId());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorRequestResponseDto> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        MentorRequestResponseDto request = mentorRequestService.getRequestById(id, userPrincipal);
        return ResponseEntity.ok(request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<MentorRequestResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody MentorStatusUpdateDto dto) {
        MentorRequestResponseDto updated = mentorRequestService.updateStatus(id, dto);
        return ResponseEntity.ok(updated);
    }
}
