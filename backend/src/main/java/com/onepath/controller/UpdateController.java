package com.onepath.controller;

import com.onepath.entity.Update;
import com.onepath.service.UpdateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/updates", "/updates"})
public class UpdateController {

    private final UpdateService updateService;

    public UpdateController(UpdateService updateService) {
        this.updateService = updateService;
    }

    @GetMapping
    public ResponseEntity<List<Update>> getUpdates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean urgent) {
        List<Update> list = updateService.getUpdates(category, urgent);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COLLEGE_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Update> createUpdate(@Valid @RequestBody Update update) {
        Update created = updateService.createUpdate(update);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
