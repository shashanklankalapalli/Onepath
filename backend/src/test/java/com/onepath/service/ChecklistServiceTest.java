package com.onepath.service;

import com.onepath.dto.ChecklistItemDto;
import com.onepath.entity.UserDocumentChecklist;
import com.onepath.repository.UserDocumentChecklistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChecklistServiceTest {

    @Mock
    private UserDocumentChecklistRepository checklistRepository;

    @InjectMocks
    private ChecklistService checklistService;

    @Test
    void testGetUserChecklistWithoutTargetTypeReturnsOnlyPersonalDocuments() {
        Long userId = 1L;

        UserDocumentChecklist personalDoc = new UserDocumentChecklist(
                userId, "PERSONAL", 0L, "Birth Certificate", true, false, null
        );
        personalDoc.setId(10L);

        when(checklistRepository.findByUserIdAndTargetType(userId, "PERSONAL"))
                .thenReturn(Collections.singletonList(personalDoc));

        // When requesting without targetType (as My Documents does), it must return only PERSONAL items
        List<ChecklistItemDto> result = checklistService.getUserChecklist(userId, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Birth Certificate", result.get(0).getDocumentName());
        assertEquals("PERSONAL", result.get(0).getTargetType());
        verify(checklistRepository).findByUserIdAndTargetType(userId, "PERSONAL");
        verify(checklistRepository, never()).findByUserId(userId);
    }

    @Test
    void testGetUserChecklistWithTargetTypeReturnsOnlyMatchingProcessRequirements() {
        Long userId = 1L;
        Long processId = 5L;

        UserDocumentChecklist processDoc = new UserDocumentChecklist(
                userId, "COLLEGE_PROCESS", processId, "Identity Proof", false, true, "Needs notary seal"
        );
        processDoc.setId(20L);

        when(checklistRepository.findByUserIdAndTargetTypeAndTargetId(userId, "COLLEGE_PROCESS", processId))
                .thenReturn(Collections.singletonList(processDoc));

        List<ChecklistItemDto> result = checklistService.getUserChecklist(userId, "COLLEGE_PROCESS", processId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Identity Proof", result.get(0).getDocumentName());
        assertEquals("COLLEGE_PROCESS", result.get(0).getTargetType());
        assertEquals(processId, result.get(0).getTargetId());
    }

    @Test
    void testSaveChecklistItemDefaultsToPersonalWhenTargetTypeNotSpecified() {
        Long userId = 1L;

        ChecklistItemDto inputDto = new ChecklistItemDto();
        inputDto.setDocumentName("Aadhaar Card");
        inputDto.setCompleted(true);

        when(checklistRepository.findByUserIdAndTargetTypeAndTargetIdAndDocumentName(userId, "PERSONAL", 0L, "Aadhaar Card"))
                .thenReturn(Optional.empty());

        when(checklistRepository.save(any(UserDocumentChecklist.class))).thenAnswer(inv -> {
            UserDocumentChecklist item = inv.getArgument(0);
            item.setId(50L);
            return item;
        });

        ChecklistItemDto saved = checklistService.saveChecklistItem(userId, inputDto);

        assertNotNull(saved);
        assertEquals("PERSONAL", saved.getTargetType());
        assertEquals(0L, saved.getTargetId());
        assertEquals("Aadhaar Card", saved.getDocumentName());
    }
}
