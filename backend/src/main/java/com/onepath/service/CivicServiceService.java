package com.onepath.service;

import com.onepath.dto.*;
import com.onepath.entity.Service;
import com.onepath.entity.ServiceRequiredDocument;
import com.onepath.entity.ServiceStep;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.ServiceRepository;
import com.onepath.service.evaluator.ServicePathwayEvaluator;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class CivicServiceService {

    private final ServiceRepository serviceRepository;
    private final List<ServicePathwayEvaluator> evaluators;

    public CivicServiceService(ServiceRepository serviceRepository, List<ServicePathwayEvaluator> evaluators) {
        this.serviceRepository = serviceRepository;
        this.evaluators = evaluators;
    }

    @Transactional(readOnly = true)
    public List<ServiceDto> getAllServices(String category, String query) {
        List<Service> list;
        if (query != null && !query.trim().isEmpty()) {
            list = serviceRepository.searchServices(query.trim());
        } else if (category != null && !category.trim().isEmpty()) {
            list = serviceRepository.findByCategoryIgnoreCase(category.trim());
        } else {
            list = serviceRepository.findAll();
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceDto getServiceByIdOrSlug(String identifier) {
        Service service = findServiceEntity(identifier);
        return toDto(service);
    }

    @Transactional(readOnly = true)
    public Service findServiceEntity(String identifier) {
        try {
            Long id = Long.parseLong(identifier);
            return serviceRepository.findById(id)
                    .orElseGet(() -> serviceRepository.findBySlug(identifier)
                            .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + identifier)));
        } catch (NumberFormatException e) {
            return serviceRepository.findBySlug(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found with slug: " + identifier));
        }
    }

    @Transactional(readOnly = true)
    public QuestionnaireDto getQuestionnaire(String identifier) {
        Service service = findServiceEntity(identifier);
        return evaluators.stream()
                .filter(eval -> eval.supports(service.getSlug()))
                .findFirst()
                .map(eval -> eval.getQuestionnaire(service))
                .orElseThrow(() -> new ResourceNotFoundException("Questionnaire not supported for service: " + service.getName()));
    }

    @Transactional(readOnly = true)
    public PathwayEvaluationResultDto evaluatePathway(String identifier, Map<String, String> answers) {
        Service service = findServiceEntity(identifier);
        return evaluators.stream()
                .filter(eval -> eval.supports(service.getSlug()))
                .findFirst()
                .map(eval -> eval.evaluate(service, answers != null ? answers : Map.of()))
                .orElseThrow(() -> new ResourceNotFoundException("Pathway evaluation not supported for service: " + service.getName()));
    }

    @Transactional
    public ServiceDto createService(ServiceDto dto) {
        Service service = new Service();
        service.setName(dto.getName().trim());
        service.setSlug(dto.getSlug() != null ? dto.getSlug().trim() : dto.getName().toLowerCase().replace(" ", "-"));
        service.setCategory(dto.getCategory());
        service.setDescription(dto.getDescription());
        service.setEligibility(dto.getEligibility());
        service.setTimeline(dto.getTimeline());
        service.setFees(dto.getFees());
        service.setImportantNotes(dto.getImportantNotes());
        service.setOfficialPortalUrl(dto.getOfficialPortalUrl());
        service.setHasQuestionnaire(dto.isHasQuestionnaire());

        if (dto.getSteps() != null) {
            for (ServiceStepDto s : dto.getSteps()) {
                ServiceStep step = new ServiceStep(s.getStepNumber(), s.getTitle(), s.getDescription(), s.getEstimatedTime(), s.getNotes());
                service.addStep(step);
            }
        }

        if (dto.getRequiredDocuments() != null) {
            for (ServiceRequiredDocumentDto d : dto.getRequiredDocuments()) {
                ServiceRequiredDocument doc = new ServiceRequiredDocument(d.getDocumentName(), d.getDescription(), d.isMandatory(), d.getAcceptedFormats(), d.getHowToObtain());
                service.addDocument(doc);
            }
        }

        Service saved = serviceRepository.save(service);
        return toDto(saved);
    }

    private ServiceDto toDto(Service s) {
        ServiceDto dto = new ServiceDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setSlug(s.getSlug());
        dto.setCategory(s.getCategory());
        dto.setDescription(s.getDescription());
        dto.setEligibility(s.getEligibility());
        dto.setTimeline(s.getTimeline());
        dto.setFees(s.getFees());
        dto.setImportantNotes(s.getImportantNotes());
        dto.setOfficialPortalUrl(s.getOfficialPortalUrl());
        dto.setHasQuestionnaire(s.isHasQuestionnaire());

        if (s.getSteps() != null) {
            dto.setSteps(s.getSteps().stream().map(st -> new ServiceStepDto(
                    st.getId(), st.getStepNumber(), st.getTitle(), st.getDescription(), st.getEstimatedTime(), st.getNotes()
            )).collect(Collectors.toList()));
        }

        if (s.getRequiredDocuments() != null) {
            dto.setRequiredDocuments(s.getRequiredDocuments().stream().map(rd -> new ServiceRequiredDocumentDto(
                    rd.getId(), rd.getDocumentName(), rd.getDescription(), rd.isMandatory(), rd.getAcceptedFormats(), rd.getHowToObtain()
            )).collect(Collectors.toList()));
        }

        return dto;
    }
}

