package com.onepath.service;

import com.onepath.dto.CollegeProcessDetailDto;
import com.onepath.dto.CollegeProcessUpdateDto;
import com.onepath.entity.College;
import com.onepath.entity.CollegeProcess;
import com.onepath.entity.CollegeProcessUpdate;
import com.onepath.repository.CollegeProcessRepository;
import com.onepath.repository.CollegeProcessStepRepository;
import com.onepath.repository.CollegeProcessUpdateRepository;
import com.onepath.repository.CollegeRepository;
import com.onepath.repository.CollegeRequiredDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollegeProcessUpdateTest {

    @Mock
    private CollegeRepository collegeRepository;

    @Mock
    private CollegeProcessRepository processRepository;

    @Mock
    private CollegeProcessStepRepository stepRepository;

    @Mock
    private CollegeRequiredDocumentRepository documentRepository;

    @Mock
    private CollegeProcessUpdateRepository updateRepository;

    @InjectMocks
    private CollegeService collegeService;

    private College college;

    @BeforeEach
    void setUp() {
        college = new College();
        college.setId(1L);
        college.setName("Test College of Engineering");
    }

    @Test
    void testProcessCreationSeparatesEligibilityNotesAndUpdates() {
        Long collegeId = 1L;
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.of(college));

        CollegeProcessDetailDto dto = new CollegeProcessDetailDto();
        dto.setTitle("B.Tech Admissions Process");
        dto.setCategory("Admissions");
        dto.setEligibility("Minimum 60% in 10+2 with PCM");
        dto.setImportantNotes("Original certificates must be verified at the desk");

        CollegeProcessUpdateDto updateDto = new CollegeProcessUpdateDto();
        updateDto.setTitle("Reporting Window Extended");
        updateDto.setDescription("Reporting deadline extended till July 31");
        updateDto.setUpdateType("DEADLINE_EXTENSION");
        updateDto.setEffectiveDate(LocalDate.of(2026, 7, 31));
        dto.setUpdates(Collections.singletonList(updateDto));

        when(processRepository.saveAndFlush(any(CollegeProcess.class))).thenAnswer(invocation -> {
            CollegeProcess savedProcess = invocation.getArgument(0);
            savedProcess.setId(10L);
            return savedProcess;
        });

        CollegeProcessDetailDto result = collegeService.createProcess(collegeId, dto);

        assertNotNull(result);
        assertEquals("Minimum 60% in 10+2 with PCM", result.getEligibility());
        assertEquals("Original certificates must be verified at the desk", result.getImportantNotes());
        assertNotNull(result.getUpdates());
        assertEquals(1, result.getUpdates().size());

        CollegeProcessUpdateDto savedUpdate = result.getUpdates().get(0);
        assertEquals("Reporting Window Extended", savedUpdate.getTitle());
        assertEquals("Reporting Window Extended", savedUpdate.getUpdateTitle());
        assertEquals("Reporting deadline extended till July 31", savedUpdate.getDescription());
        assertEquals("Reporting deadline extended till July 31", savedUpdate.getAnnouncementDetails());
        assertEquals("DEADLINE_EXTENSION", savedUpdate.getUpdateType());
        assertEquals(LocalDate.of(2026, 7, 31), savedUpdate.getEffectiveDate());
        assertEquals("Minimum 60% in 10+2 with PCM", result.getEligibilityCriteria());

        // Verify that update title and description do NOT leak into eligibility or importantNotes
        assertFalse(result.getEligibility().contains("Reporting Window Extended"));
        assertFalse(result.getImportantNotes().contains("Reporting Window Extended"));
    }

    @Test
    void testProcessCreationWithUpdateTitleAndAnnouncementDetails() {
        Long collegeId = 1L;
        when(collegeRepository.findById(collegeId)).thenReturn(Optional.of(college));

        CollegeProcessDetailDto dto = new CollegeProcessDetailDto();
        dto.setTitle("M.Tech Admissions Process");
        dto.setCategory("Admissions");
        dto.setEligibilityCriteria("B.Tech with 65%");
        dto.setImportantNotes("Bring original degree certificates");

        CollegeProcessUpdateDto updateDto = new CollegeProcessUpdateDto();
        updateDto.setUpdateTitle("GATE Score Cutoff Revised");
        updateDto.setAnnouncementDetails("Cutoff percentile updated to 85");
        updateDto.setUpdateType("RULE_REVISION");
        updateDto.setEffectiveDate(LocalDate.of(2026, 8, 15));
        dto.setUpdates(Collections.singletonList(updateDto));

        when(processRepository.saveAndFlush(any(CollegeProcess.class))).thenAnswer(invocation -> {
            CollegeProcess savedProcess = invocation.getArgument(0);
            savedProcess.setId(20L);
            return savedProcess;
        });

        CollegeProcessDetailDto result = collegeService.createProcess(collegeId, dto);

        assertNotNull(result);
        assertEquals("B.Tech with 65%", result.getEligibility());
        assertEquals("B.Tech with 65%", result.getEligibilityCriteria());
        assertEquals("Bring original degree certificates", result.getImportantNotes());
        assertNotNull(result.getUpdates());
        assertEquals(1, result.getUpdates().size());

        CollegeProcessUpdateDto savedUpdate = result.getUpdates().get(0);
        assertEquals("GATE Score Cutoff Revised", savedUpdate.getTitle());
        assertEquals("GATE Score Cutoff Revised", savedUpdate.getUpdateTitle());
        assertEquals("Cutoff percentile updated to 85", savedUpdate.getDescription());
        assertEquals("Cutoff percentile updated to 85", savedUpdate.getAnnouncementDetails());
    }

    @Test
    void testGetUpdatesByProcessReturnsOnlyUpdates() {
        Long collegeId = 1L;
        Long processId = 10L;

        CollegeProcess process = new CollegeProcess();
        process.setId(processId);
        process.setCollege(college);
        process.setTitle("Hostel Allotment");
        process.setEligibility("Enrolled regular students only");
        process.setImportantNotes("Keep fee receipt handy");

        when(processRepository.findByIdAndCollegeId(processId, collegeId)).thenReturn(Optional.of(process));

        CollegeProcessUpdate update = new CollegeProcessUpdate(
                "Room Allocation Notice",
                "Phase 2 room allocations published",
                "GENERAL",
                LocalDate.of(2026, 8, 1)
        );
        update.setId(101L);
        update.setProcess(process);

        when(updateRepository.findByProcessIdOrderByEffectiveDateDesc(processId))
                .thenReturn(Collections.singletonList(update));

        List<CollegeProcessUpdateDto> updates = collegeService.getUpdatesByProcess(collegeId, processId);

        assertNotNull(updates);
        assertEquals(1, updates.size());
        assertEquals(101L, updates.get(0).getId());
        assertEquals("Room Allocation Notice", updates.get(0).getTitle());
        assertEquals("Room Allocation Notice", updates.get(0).getUpdateTitle());
        assertEquals("Phase 2 room allocations published", updates.get(0).getDescription());
        assertEquals("Phase 2 room allocations published", updates.get(0).getAnnouncementDetails());
        assertEquals(LocalDate.of(2026, 8, 1), updates.get(0).getEffectiveDate());
    }
}
