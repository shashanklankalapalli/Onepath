package com.onepath.controller;

import com.onepath.dto.ChecklistItemDto;
import com.onepath.security.UserPrincipal;
import com.onepath.service.ChecklistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/checklists", "/checklists"})
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping
    public ResponseEntity<List<ChecklistItemDto>> getUserChecklist(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ChecklistItemDto> list = checklistService.getUserChecklist(userPrincipal.getId(), targetType, targetId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/missing")
    public ResponseEntity<List<ChecklistItemDto>> getUserMissingDocuments(
            @RequestParam(required = false) String targetType,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ChecklistItemDto> list = checklistService.getUserMissingDocuments(userPrincipal.getId(), targetType);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<ChecklistItemDto> saveChecklistItem(
            @Valid @RequestBody ChecklistItemDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ChecklistItemDto saved = checklistService.saveChecklistItem(userPrincipal.getId(), dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChecklistItemDto> updateChecklistItem(
            @PathVariable Long id,
            @RequestBody ChecklistItemDto dto,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ChecklistItemDto updated = checklistService.updateChecklistItem(userPrincipal.getId(), id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChecklistItem(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        checklistService.deleteChecklistItem(userPrincipal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
