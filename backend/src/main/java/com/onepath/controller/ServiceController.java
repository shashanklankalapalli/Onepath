package com.onepath.controller;

import com.onepath.dto.ServiceDto;
import com.onepath.service.CivicServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final CivicServiceService civicServiceService;

    public ServiceController(CivicServiceService civicServiceService) {
        this.civicServiceService = civicServiceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        List<ServiceDto> services = civicServiceService.getAllServices(category, search);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<ServiceDto> getServiceByIdOrSlug(@PathVariable String identifier) {
        ServiceDto service = civicServiceService.getServiceByIdOrSlug(identifier);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/{identifier}/questionnaire")
    public ResponseEntity<com.onepath.dto.QuestionnaireDto> getQuestionnaire(@PathVariable String identifier) {
        com.onepath.dto.QuestionnaireDto questionnaire = civicServiceService.getQuestionnaire(identifier);
        return ResponseEntity.ok(questionnaire);
    }

    @PostMapping("/{identifier}/evaluate")
    public ResponseEntity<com.onepath.dto.PathwayEvaluationResultDto> evaluatePathway(
            @PathVariable String identifier,
            @RequestBody(required = false) com.onepath.dto.PathwayEvaluationRequestDto request) {
        com.onepath.dto.PathwayEvaluationResultDto result = civicServiceService.evaluatePathway(
                identifier,
                request != null ? request.getAnswers() : null
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceDto dto) {
        ServiceDto created = civicServiceService.createService(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
