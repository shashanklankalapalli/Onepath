package com.onepath.controller;

import com.onepath.dto.*;
import com.onepath.security.UserPrincipal;
import com.onepath.service.CollegeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colleges")
public class CollegeController {

    private final CollegeService collegeService;

    public CollegeController(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    @GetMapping
    public ResponseEntity<List<CollegeDto>> getAllColleges(
            @RequestParam(required = false) String search) {
        List<CollegeDto> colleges = collegeService.getAllColleges(search);
        return ResponseEntity.ok(colleges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollegeDto> getCollegeById(@PathVariable Long id) {
        CollegeDto college = collegeService.getCollegeById(id);
        return ResponseEntity.ok(college);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CollegeDto> createCollege(
            @Valid @RequestBody CollegeDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CollegeDto created = collegeService.createCollege(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CollegeDto> updateCollege(
            @PathVariable Long id,
            @Valid @RequestBody CollegeDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyCollegePermission(userPrincipal, id);
        CollegeDto updated = collegeService.updateCollege(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCollege(@PathVariable Long id) {
        collegeService.deleteCollege(id);
        return ResponseEntity.noContent().build();
    }

    // Processes
    @GetMapping("/{collegeId}/processes")
    public ResponseEntity<List<CollegeProcessSummaryDto>> getCollegeProcesses(
            @PathVariable Long collegeId,
            @RequestParam(required = false) String category) {
        List<CollegeProcessSummaryDto> processes = collegeService.getProcessesByCollege(collegeId, category);
        return ResponseEntity.ok(processes);
    }

    @GetMapping("/{collegeId}/processes/{processId}")
    public ResponseEntity<CollegeProcessDetailDto> getCollegeProcessDetail(
            @PathVariable Long collegeId,
            @PathVariable Long processId) {
        CollegeProcessDetailDto process = collegeService.getProcessDetail(collegeId, processId);
        return ResponseEntity.ok(process);
    }

    @PostMapping("/{collegeId}/processes")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CollegeProcessDetailDto> createCollegeProcess(
            @PathVariable Long collegeId,
            @Valid @RequestBody CollegeProcessDetailDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyCollegePermission(userPrincipal, collegeId);
        CollegeProcessDetailDto created = collegeService.createProcess(collegeId, dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{collegeId}/processes/{processId}")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CollegeProcessDetailDto> updateCollegeProcess(
            @PathVariable Long collegeId,
            @PathVariable Long processId,
            @Valid @RequestBody CollegeProcessDetailDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyCollegePermission(userPrincipal, collegeId);
        CollegeProcessDetailDto updated = collegeService.updateProcess(collegeId, processId, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{collegeId}/processes/{processId}")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCollegeProcess(
            @PathVariable Long collegeId,
            @PathVariable Long processId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyCollegePermission(userPrincipal, collegeId);
        collegeService.deleteProcess(collegeId, processId);
        return ResponseEntity.noContent().build();
    }

    // Steps
    @PostMapping("/{collegeId}/processes/{processId}/steps")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CollegeProcessStepDto> addStep(
            @PathVariable Long collegeId,
            @PathVariable Long processId,
            @Valid @RequestBody CollegeProcessStepDto stepDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyCollegePermission(userPrincipal, collegeId);
        CollegeProcessStepDto created = collegeService.addStepToProcess(collegeId, processId, stepDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Required Documents
    @PostMapping("/{collegeId}/processes/{processId}/documents")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CollegeRequiredDocumentDto> addDocument(
            @PathVariable Long collegeId,
            @PathVariable Long processId,
            @Valid @RequestBody CollegeRequiredDocumentDto docDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyCollegePermission(userPrincipal, collegeId);
        CollegeRequiredDocumentDto created = collegeService.addDocumentToProcess(collegeId, processId, docDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // Updates
    @GetMapping("/{collegeId}/processes/{processId}/updates")
    public ResponseEntity<List<CollegeProcessUpdateDto>> getProcessUpdates(
            @PathVariable Long collegeId,
            @PathVariable Long processId) {
        List<CollegeProcessUpdateDto> updates = collegeService.getUpdatesByProcess(collegeId, processId);
        return ResponseEntity.ok(updates);
    }

    @PostMapping("/{collegeId}/processes/{processId}/updates")
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<CollegeProcessUpdateDto> addUpdate(
            @PathVariable Long collegeId,
            @PathVariable Long processId,
            @Valid @RequestBody CollegeProcessUpdateDto updateDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyCollegePermission(userPrincipal, collegeId);
        CollegeProcessUpdateDto created = collegeService.addUpdateToProcess(collegeId, processId, updateDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    private void verifyCollegePermission(UserPrincipal userPrincipal, Long collegeId) {
        if (userPrincipal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        boolean isSuperAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (!isSuperAdmin) {
            if (userPrincipal.getAssignedCollegeId() == null || !userPrincipal.getAssignedCollegeId().equals(collegeId)) {
                throw new AccessDeniedException("You are not authorized to manage processes for this college");
            }
        }
    }
}
