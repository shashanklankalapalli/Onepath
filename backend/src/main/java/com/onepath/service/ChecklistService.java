package com.onepath.service;

import com.onepath.dto.ChecklistItemDto;
import com.onepath.entity.UserDocumentChecklist;
import com.onepath.exception.BadRequestException;
import com.onepath.exception.ResourceNotFoundException;
import com.onepath.repository.UserDocumentChecklistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChecklistService {

    private final UserDocumentChecklistRepository checklistRepository;

    public ChecklistService(UserDocumentChecklistRepository checklistRepository) {
        this.checklistRepository = checklistRepository;
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemDto> getUserChecklist(Long userId, String targetType, Long targetId) {
        List<UserDocumentChecklist> items;
        if (targetType != null && targetId != null) {
            items = checklistRepository.findByUserIdAndTargetTypeAndTargetId(userId, targetType.toUpperCase(), targetId);
        } else if (targetType != null && !targetType.trim().isEmpty()) {
            items = checklistRepository.findByUserIdAndTargetType(userId, targetType.toUpperCase());
        } else {
            // Default to PERSONAL student documents to guarantee institutional process requirements never leak into personal lists
            items = checklistRepository.findByUserIdAndTargetType(userId, "PERSONAL");
        }
        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemDto> getUserMissingDocuments(Long userId) {
        return getUserMissingDocuments(userId, "PERSONAL");
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemDto> getUserMissingDocuments(Long userId, String targetType) {
        String type = (targetType != null && !targetType.trim().isEmpty()) ? targetType.toUpperCase() : "PERSONAL";
        return checklistRepository.findByUserIdAndTargetTypeAndIsMissingTrue(userId, type)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ChecklistItemDto saveChecklistItem(Long userId, ChecklistItemDto dto) {
        if (dto == null || dto.getDocumentName() == null || dto.getDocumentName().trim().isEmpty()) {
            throw new BadRequestException("Document name is required");
        }
        String targetType = (dto.getTargetType() != null && !dto.getTargetType().trim().isEmpty())
                ? dto.getTargetType().toUpperCase()
                : "PERSONAL";
        Long targetId = dto.getTargetId() != null ? dto.getTargetId() : 0L;

        Optional<UserDocumentChecklist> existing = checklistRepository
                .findByUserIdAndTargetTypeAndTargetIdAndDocumentName(userId, targetType, targetId, dto.getDocumentName().trim());

        UserDocumentChecklist item;
        if (existing.isPresent()) {
            item = existing.get();
            item.setCompleted(dto.isCompleted());
            item.setMissing(dto.isMissing());
            if (dto.getUserNotes() != null) {
                item.setUserNotes(dto.getUserNotes());
            }
        } else {
            item = new UserDocumentChecklist(
                    userId,
                    targetType,
                    targetId,
                    dto.getDocumentName().trim(),
                    dto.isCompleted(),
                    dto.isMissing(),
                    dto.getUserNotes()
            );
        }

        UserDocumentChecklist saved = checklistRepository.save(item);
        return toDto(saved);
    }

    @Transactional
    public ChecklistItemDto updateChecklistItem(Long userId, Long id, ChecklistItemDto dto) {
        UserDocumentChecklist item = checklistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found with id: " + id));

        if (!item.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to checklist item");
        }

        item.setCompleted(dto.isCompleted());
        item.setMissing(dto.isMissing());
        if (dto.getUserNotes() != null) {
            item.setUserNotes(dto.getUserNotes());
        }
        if (dto.getDocumentName() != null && !dto.getDocumentName().trim().isEmpty()) {
            item.setDocumentName(dto.getDocumentName().trim());
        }

        UserDocumentChecklist saved = checklistRepository.save(item);
        return toDto(saved);
    }

    @Transactional
    public void deleteChecklistItem(Long userId, Long id) {
        UserDocumentChecklist item = checklistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found with id: " + id));

        if (!item.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to checklist item");
        }

        checklistRepository.delete(item);
    }

    private ChecklistItemDto toDto(UserDocumentChecklist item) {
        ChecklistItemDto dto = new ChecklistItemDto();
        dto.setId(item.getId());
        dto.setUserId(item.getUserId());
        dto.setTargetType(item.getTargetType());
        dto.setTargetId(item.getTargetId());
        dto.setDocumentName(item.getDocumentName());
        dto.setCompleted(item.isCompleted());
        dto.setMissing(item.isMissing());
        dto.setUserNotes(item.getUserNotes());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }
}
