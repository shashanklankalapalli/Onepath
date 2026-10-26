package com.onepath.service;

import com.onepath.dto.*;
import com.onepath.entity.*;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final CollegeProcessRepository processRepository;
    private final CollegeProcessStepRepository stepRepository;
    private final CollegeRequiredDocumentRepository documentRepository;
    private final CollegeProcessUpdateRepository updateRepository;

    public CollegeService(
            CollegeRepository collegeRepository,
            CollegeProcessRepository processRepository,
            CollegeProcessStepRepository stepRepository,
            CollegeRequiredDocumentRepository documentRepository,
            CollegeProcessUpdateRepository updateRepository) {
        this.collegeRepository = collegeRepository;
        this.processRepository = processRepository;
        this.stepRepository = stepRepository;
        this.documentRepository = documentRepository;
        this.updateRepository = updateRepository;
    }

    @Transactional(readOnly = true)
    public List<CollegeDto> getAllColleges(String query) {
        List<College> colleges;
        if (query != null && !query.trim().isEmpty()) {
            colleges = collegeRepository.searchColleges(query.trim());
        } else {
            colleges = collegeRepository.findByActiveTrueOrderByNameAsc();
        }

        return colleges.stream().map(this::toCollegeDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollegeDto getCollegeById(Long id) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));

        CollegeDto dto = toCollegeDto(college);
        List<CollegeProcessSummaryDto> processes = processRepository.findByCollegeIdOrderByIdAsc(id)
                .stream().map(this::toProcessSummaryDto).collect(Collectors.toList());
        dto.setProcesses(processes);
        return dto;
    }

    @Transactional
    public CollegeDto createCollege(CollegeDto dto) {
        College college = new College();
        college.setName(dto.getName().trim());
        college.setShortName(dto.getShortName());
        college.setLocation(dto.getLocation());
        college.setWebsite(dto.getWebsite());
        college.setLogoUrl(dto.getLogoUrl());
        college.setDescription(dto.getDescription());
        college.setActive(dto.isActive());

        College saved = collegeRepository.save(college);
        return toCollegeDto(saved);
    }

    @Transactional
    public CollegeDto updateCollege(Long id, CollegeDto dto) {
        College college = collegeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + id));

        college.setName(dto.getName().trim());
        college.setShortName(dto.getShortName());
        college.setLocation(dto.getLocation());
        college.setWebsite(dto.getWebsite());
        college.setLogoUrl(dto.getLogoUrl());
        college.setDescription(dto.getDescription());
        college.setActive(dto.isActive());

        College saved = collegeRepository.save(college);
        return toCollegeDto(saved);
    }

    @Transactional
    public void deleteCollege(Long id) {
        if (!collegeRepository.existsById(id)) {
            throw new ResourceNotFoundException("College not found with id: " + id);
        }
        collegeRepository.deleteById(id);
    }

    // Process methods
    @Transactional(readOnly = true)
    public List<CollegeProcessSummaryDto> getProcessesByCollege(Long collegeId, String category) {
        if (!collegeRepository.existsById(collegeId)) {
            throw new ResourceNotFoundException("College not found with id: " + collegeId);
        }

        List<CollegeProcess> list;
        if (category != null && !category.trim().isEmpty()) {
            list = processRepository.findByCollegeIdAndCategoryIgnoreCase(collegeId, category.trim());
        } else {
            list = processRepository.findByCollegeIdOrderByIdAsc(collegeId);
        }

        return list.stream().map(this::toProcessSummaryDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollegeProcessDetailDto getProcessDetail(Long collegeId, Long processId) {
        CollegeProcess process = processRepository.findByIdAndCollegeId(processId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Process not found with id " + processId + " for college " + collegeId));

        return toProcessDetailDto(process);
    }

    @Transactional
    public CollegeProcessDetailDto createProcess(Long collegeId, CollegeProcessDetailDto dto) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("College not found with id: " + collegeId));

        CollegeProcess process = new CollegeProcess();
        process.setCollege(college);
        process.setTitle(dto.getTitle().trim());
        process.setDescription(dto.getDescription());
        process.setCategory(dto.getCategory());
        String eligibilityVal = dto.getEligibilityCriteria() != null ? dto.getEligibilityCriteria() : dto.getEligibility();
        process.setEligibility(eligibilityVal);
        process.setTimeline(dto.getTimeline());
        process.setFees(dto.getFees());
        process.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");
        process.setImportantNotes(dto.getImportantNotes());
        process.setOfficialPortalUrl(dto.getOfficialPortalUrl());

        if (dto.getSteps() != null) {
            for (CollegeProcessStepDto stepDto : dto.getSteps()) {
                CollegeProcessStep step = new CollegeProcessStep(
                        stepDto.getStepNumber(),
                        stepDto.getTitle(),
                        stepDto.getDescription(),
                        stepDto.getEstimatedTime(),
                        stepDto.getNotes()
                );
                process.addStep(step);
            }
        }

        if (dto.getRequiredDocuments() != null) {
            for (CollegeRequiredDocumentDto docDto : dto.getRequiredDocuments()) {
                CollegeRequiredDocument doc = new CollegeRequiredDocument(
                        docDto.getDocumentName(),
                        docDto.getDescription(),
                        docDto.isMandatory(),
                        docDto.getAcceptedFormats(),
                        docDto.getHowToObtain()
                );
                process.addDocument(doc);
            }
        }

        if (dto.getUpdates() != null) {
            for (CollegeProcessUpdateDto updateDto : dto.getUpdates()) {
                String uTitle = updateDto.getUpdateTitle() != null ? updateDto.getUpdateTitle() : updateDto.getTitle();
                String uDesc = updateDto.getAnnouncementDetails() != null ? updateDto.getAnnouncementDetails() : updateDto.getDescription();
                String uType = updateDto.getUpdateType() != null ? updateDto.getUpdateType() : "GENERAL";
                if (uTitle != null && !uTitle.trim().isEmpty()) {
                    CollegeProcessUpdate update = new CollegeProcessUpdate(
                            uTitle.trim(),
                            uDesc,
                            uType,
                            updateDto.getEffectiveDate()
                    );
                    process.addUpdate(update);
                }
            }
        }

        CollegeProcess saved = processRepository.saveAndFlush(process);
        return toProcessDetailDto(saved);
    }

    @Transactional
    public CollegeProcessDetailDto updateProcess(Long collegeId, Long processId, CollegeProcessDetailDto dto) {
        CollegeProcess process = processRepository.findByIdAndCollegeId(processId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Process not found with id " + processId + " for college " + collegeId));

        if (dto.getTitle() != null && !dto.getTitle().trim().isEmpty()) {
            process.setTitle(dto.getTitle().trim());
        }
        if (dto.getDescription() != null) {
            process.setDescription(dto.getDescription());
        }
        if (dto.getCategory() != null) {
            process.setCategory(dto.getCategory());
        }
        String updatedEligibility = dto.getEligibilityCriteria() != null ? dto.getEligibilityCriteria() : dto.getEligibility();
        if (updatedEligibility != null) {
            process.setEligibility(updatedEligibility);
        }
        if (dto.getTimeline() != null) {
            process.setTimeline(dto.getTimeline());
        }
        if (dto.getFees() != null) {
            process.setFees(dto.getFees());
        }
        if (dto.getStatus() != null) {
            process.setStatus(dto.getStatus());
        }
        if (dto.getImportantNotes() != null) {
            process.setImportantNotes(dto.getImportantNotes());
        }
        if (dto.getOfficialPortalUrl() != null) {
            process.setOfficialPortalUrl(dto.getOfficialPortalUrl());
        }

        if (dto.getSteps() != null) {
            process.getSteps().clear();
            for (CollegeProcessStepDto stepDto : dto.getSteps()) {
                CollegeProcessStep step = new CollegeProcessStep(
                        stepDto.getStepNumber(),
                        stepDto.getTitle(),
                        stepDto.getDescription(),
                        stepDto.getEstimatedTime(),
                        stepDto.getNotes()
                );
                process.addStep(step);
            }
        }

        if (dto.getRequiredDocuments() != null) {
            process.getRequiredDocuments().clear();
            for (CollegeRequiredDocumentDto docDto : dto.getRequiredDocuments()) {
                CollegeRequiredDocument doc = new CollegeRequiredDocument(
                        docDto.getDocumentName(),
                        docDto.getDescription(),
                        docDto.isMandatory(),
                        docDto.getAcceptedFormats(),
                        docDto.getHowToObtain()
                );
                process.addDocument(doc);
            }
        }

        if (dto.getUpdates() != null) {
            process.getUpdates().clear();
            for (CollegeProcessUpdateDto updateDto : dto.getUpdates()) {
                String uTitle = updateDto.getUpdateTitle() != null ? updateDto.getUpdateTitle() : updateDto.getTitle();
                String uDesc = updateDto.getAnnouncementDetails() != null ? updateDto.getAnnouncementDetails() : updateDto.getDescription();
                String uType = updateDto.getUpdateType() != null ? updateDto.getUpdateType() : "GENERAL";
                if (uTitle != null && !uTitle.trim().isEmpty()) {
                    CollegeProcessUpdate update = new CollegeProcessUpdate(
                            uTitle.trim(),
                            uDesc,
                            uType,
                            updateDto.getEffectiveDate()
                    );
                    process.addUpdate(update);
                }
            }
        }

        CollegeProcess saved = processRepository.saveAndFlush(process);
        return toProcessDetailDto(saved);
    }

    @Transactional
    public void deleteProcess(Long collegeId, Long processId) {
        CollegeProcess process = processRepository.findByIdAndCollegeId(processId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Process not found with id " + processId + " for college " + collegeId));
        processRepository.delete(process);
    }

    // Process Steps
    @Transactional
    public CollegeProcessStepDto addStepToProcess(Long collegeId, Long processId, CollegeProcessStepDto stepDto) {
        CollegeProcess process = processRepository.findByIdAndCollegeId(processId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Process not found"));

        CollegeProcessStep step = new CollegeProcessStep(
                stepDto.getStepNumber(),
                stepDto.getTitle(),
                stepDto.getDescription(),
                stepDto.getEstimatedTime(),
                stepDto.getNotes()
        );
        process.addStep(step);
        CollegeProcessStep saved = stepRepository.save(step);

        return new CollegeProcessStepDto(saved.getId(), processId, saved.getStepNumber(), saved.getTitle(), saved.getDescription(), saved.getEstimatedTime(), saved.getNotes());
    }

    // Process Documents
    @Transactional
    public CollegeRequiredDocumentDto addDocumentToProcess(Long collegeId, Long processId, CollegeRequiredDocumentDto docDto) {
        CollegeProcess process = processRepository.findByIdAndCollegeId(processId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Process not found"));

        CollegeRequiredDocument doc = new CollegeRequiredDocument(
                docDto.getDocumentName(),
                docDto.getDescription(),
                docDto.isMandatory(),
                docDto.getAcceptedFormats(),
                docDto.getHowToObtain()
        );
        process.addDocument(doc);
        CollegeRequiredDocument saved = documentRepository.save(doc);

        return new CollegeRequiredDocumentDto(saved.getId(), processId, saved.getDocumentName(), saved.getDescription(), saved.isMandatory(), saved.getAcceptedFormats(), saved.getHowToObtain());
    }

    // Process Updates
    @Transactional
    public CollegeProcessUpdateDto addUpdateToProcess(Long collegeId, Long processId, CollegeProcessUpdateDto updateDto) {
        CollegeProcess process = processRepository.findByIdAndCollegeId(processId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Process not found"));

        String uTitle = updateDto.getUpdateTitle() != null ? updateDto.getUpdateTitle() : updateDto.getTitle();
        String uDesc = updateDto.getAnnouncementDetails() != null ? updateDto.getAnnouncementDetails() : updateDto.getDescription();
        String uType = updateDto.getUpdateType() != null ? updateDto.getUpdateType() : "GENERAL";
        CollegeProcessUpdate update = new CollegeProcessUpdate(
                uTitle.trim(),
                uDesc,
                uType,
                updateDto.getEffectiveDate()
        );
        process.addUpdate(update);
        CollegeProcessUpdate saved = updateRepository.save(update);

        return new CollegeProcessUpdateDto(saved.getId(), processId, saved.getTitle(), saved.getDescription(), saved.getUpdateType(), saved.getEffectiveDate());
    }

    @Transactional(readOnly = true)
    public List<CollegeProcessUpdateDto> getUpdatesByProcess(Long collegeId, Long processId) {
        CollegeProcess process = processRepository.findByIdAndCollegeId(processId, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Process not found with id " + processId + " for college " + collegeId));

        List<CollegeProcessUpdate> list = updateRepository.findByProcessIdOrderByEffectiveDateDesc(processId);
        return list.stream().map(u -> new CollegeProcessUpdateDto(
                u.getId(), processId, u.getTitle(), u.getDescription(), u.getUpdateType(), u.getEffectiveDate()
        )).collect(Collectors.toList());
    }

    // Mappers
    private CollegeDto toCollegeDto(College c) {
        CollegeDto dto = new CollegeDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setShortName(c.getShortName());
        dto.setLocation(c.getLocation());
        dto.setWebsite(c.getWebsite());
        dto.setLogoUrl(c.getLogoUrl());
        dto.setDescription(c.getDescription());
        dto.setActive(c.isActive());
        dto.setProcessCount(c.getProcesses() != null ? c.getProcesses().size() : 0);
        return dto;
    }

    private CollegeProcessSummaryDto toProcessSummaryDto(CollegeProcess p) {
        CollegeProcessSummaryDto dto = new CollegeProcessSummaryDto();
        dto.setId(p.getId());
        dto.setCollegeId(p.getCollege().getId());
        dto.setTitle(p.getTitle());
        dto.setCategory(p.getCategory());
        dto.setTimeline(p.getTimeline());
        dto.setFees(p.getFees());
        dto.setStatus(p.getStatus());
        dto.setStepCount(p.getSteps() != null ? p.getSteps().size() : 0);
        dto.setDocumentCount(p.getRequiredDocuments() != null ? p.getRequiredDocuments().size() : 0);
        return dto;
    }

    private CollegeProcessDetailDto toProcessDetailDto(CollegeProcess p) {
        CollegeProcessDetailDto dto = new CollegeProcessDetailDto();
        dto.setId(p.getId());
        dto.setCollegeId(p.getCollege().getId());
        dto.setCollegeName(p.getCollege().getName());
        dto.setCollegeShortName(p.getCollege().getShortName());
        dto.setTitle(p.getTitle());
        dto.setDescription(p.getDescription());
        dto.setCategory(p.getCategory());
        dto.setEligibility(p.getEligibility());
        dto.setTimeline(p.getTimeline());
        dto.setFees(p.getFees());
        dto.setStatus(p.getStatus());
        dto.setImportantNotes(p.getImportantNotes());
        dto.setOfficialPortalUrl(p.getOfficialPortalUrl());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        if (p.getSteps() != null) {
            dto.setSteps(p.getSteps().stream().map(s -> new CollegeProcessStepDto(
                    s.getId(), p.getId(), s.getStepNumber(), s.getTitle(), s.getDescription(), s.getEstimatedTime(), s.getNotes()
            )).collect(Collectors.toList()));
        }

        if (p.getRequiredDocuments() != null) {
            dto.setRequiredDocuments(p.getRequiredDocuments().stream().map(d -> new CollegeRequiredDocumentDto(
                    d.getId(), p.getId(), d.getDocumentName(), d.getDescription(), d.isMandatory(), d.getAcceptedFormats(), d.getHowToObtain()
            )).collect(Collectors.toList()));
        }

        if (p.getUpdates() != null) {
            dto.setUpdates(p.getUpdates().stream().map(u -> new CollegeProcessUpdateDto(
                    u.getId(), p.getId(), u.getTitle(), u.getDescription(), u.getUpdateType(), u.getEffectiveDate()
            )).collect(Collectors.toList()));
        }

        return dto;
    }
}
