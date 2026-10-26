package com.onepath.service.evaluator;

import com.onepath.dto.PathwayEvaluationResultDto;
import com.onepath.dto.QuestionnaireDto;
import com.onepath.entity.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PanCardPathwayEvaluatorTest {

    private PanCardPathwayEvaluator evaluator;
    private Service mockService;

    @BeforeEach
    void setUp() {
        evaluator = new PanCardPathwayEvaluator();
        mockService = new Service();
        mockService.setId(7L);
        mockService.setName("PAN Card Application & Correction");
        mockService.setSlug("pan-card-application-correction");
    }

    @Test
    @DisplayName("Should support pan-card-application-correction and related slugs")
    void testSupports() {
        assertTrue(evaluator.supports("pan-card-application-correction"));
        assertTrue(evaluator.supports("PAN-CARD-APPLICATION-CORRECTION"));
        assertTrue(evaluator.supports("pan-card"));
        assertTrue(evaluator.supports("pan"));
        assertTrue(evaluator.supports("pan-card-correction"));
        assertFalse(evaluator.supports("passport-application-renewal"));
        assertFalse(evaluator.supports(null));
    }

    @Test
    @DisplayName("Should return 5 structured questionnaire questions")
    void testGetQuestionnaire() {
        QuestionnaireDto questionnaire = evaluator.getQuestionnaire(mockService);
        assertNotNull(questionnaire);
        assertEquals("pan-card-application-correction", questionnaire.getServiceSlug());
        assertEquals(5, questionnaire.getQuestions().size());

        assertEquals("applicationType", questionnaire.getQuestions().get(0).getId());
        assertEquals("hasExistingPan", questionnaire.getQuestions().get(1).getId());
        assertEquals("correctionFields", questionnaire.getQuestions().get(2).getId());
        assertEquals("applicantCategory", questionnaire.getQuestions().get(3).getId());
        assertEquals("submissionMode", questionnaire.getQuestions().get(4).getId());
    }

    @Test
    @DisplayName("Should detect multiple PAN holding and trigger Section 272B penalty warning")
    void testDuplicatePanPenaltyWarning() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "CORRECTION_UPDATE");
        answers.put("hasExistingPan", "YES_MULTIPLE_PAN");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("PAN_SURRENDER_SECTION_272B", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("10,000"));
        assertTrue(result.getMatchReason().contains("Section 272B"));
        assertEquals(4, result.getSteps().size());
    }

    @Test
    @DisplayName("Should evaluate Instant e-PAN as 100% FREE in minutes")
    void testInstantEPanPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "INSTANT_EPAN");
        answers.put("hasExistingPan", "NO_FIRST_TIME");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("INSTANT_EPAN_INCOME_TAX", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("FREE"));
        assertTrue(result.getEstimatedTimeline().contains("10 to 30 Minutes"));
        assertEquals(5, result.getSteps().size());
        assertTrue(result.getDocuments().size() >= 1);
    }

    @Test
    @DisplayName("Should evaluate Paperless Form 49A New PAN at ₹107")
    void testPaperlessNewPanPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "NEW_PAN_INDIVIDUAL");
        answers.put("hasExistingPan", "NO_FIRST_TIME");
        answers.put("applicantCategory", "INDIVIDUAL_CITIZEN");
        answers.put("submissionMode", "PAPERLESS_EKYC");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("NEW_PAN_FORM_49A_INDIVIDUAL", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("₹107.00"));
        assertTrue(result.getEstimatedTimeline().contains("2 to 4 Working Days"));
        assertEquals(7, result.getSteps().size());
        assertTrue(result.getDocuments().size() >= 1);
    }

    @Test
    @DisplayName("Should evaluate PAN Correction (CSF Form) with required proof")
    void testPanCorrectionPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "CORRECTION_UPDATE");
        answers.put("hasExistingPan", "YES_SINGLE_PAN");
        answers.put("correctionFields", "NAME_OR_DOB");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("PAN_CORRECTION_CSF_FORM", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("₹107.00"));
        assertEquals(6, result.getSteps().size());
        boolean hasExistingPanDoc = result.getDocuments().stream()
                .anyMatch(d -> d.getDocumentName().contains("Existing"));
        assertTrue(hasExistingPanDoc);
    }

    @Test
    @DisplayName("Should evaluate Reprint of Lost PAN Card at ₹50")
    void testReprintPanPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "REPRINT_EXISTING");
        answers.put("hasExistingPan", "YES_SINGLE_PAN");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("PAN_REPRINT_PHYSICAL_CARD", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("₹50.00"));
        assertEquals(5, result.getSteps().size());
    }

    @Test
    @DisplayName("Should evaluate Foreign Citizen Form 49AA at ₹1,017")
    void testForeignApplicantPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "FOREIGN_APPLICANT");
        answers.put("applicantCategory", "FOREIGN_NATIONAL");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("FOREIGN_PAN_FORM_49AA", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("₹1,017.00"));
        assertTrue(result.getEstimatedTimeline().contains("15 to 25 Working Days"));
        assertEquals(5, result.getSteps().size());
    }
}
