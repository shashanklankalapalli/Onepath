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

class MarriageRegistrationPathwayEvaluatorTest {

    private MarriageRegistrationPathwayEvaluator evaluator;
    private Service mockService;

    @BeforeEach
    void setUp() {
        evaluator = new MarriageRegistrationPathwayEvaluator();
        mockService = new Service();
        mockService.setId(5L);
        mockService.setName("Marriage Registration");
        mockService.setSlug("marriage-registration");
    }

    @Test
    @DisplayName("Should support marriage-registration and related slugs")
    void testSupports() {
        assertTrue(evaluator.supports("marriage-registration"));
        assertTrue(evaluator.supports("MARRIAGE-REGISTRATION"));
        assertTrue(evaluator.supports("marriage"));
        assertTrue(evaluator.supports("marriage-certificate"));
        assertFalse(evaluator.supports("passport-application-renewal"));
        assertFalse(evaluator.supports(null));
    }

    @Test
    @DisplayName("Should return 6 structured questionnaire questions")
    void testGetQuestionnaire() {
        QuestionnaireDto questionnaire = evaluator.getQuestionnaire(mockService);
        assertNotNull(questionnaire);
        assertEquals("marriage-registration", questionnaire.getServiceSlug());
        assertEquals(6, questionnaire.getQuestions().size());

        assertEquals("ceremonyStatus", questionnaire.getQuestions().get(0).getId());
        assertEquals("applicableAct", questionnaire.getQuestions().get(1).getId());
        assertEquals("legalAge", questionnaire.getQuestions().get(2).getId());
        assertEquals("residenceState", questionnaire.getQuestions().get(3).getId());
        assertEquals("maritalStatus", questionnaire.getQuestions().get(4).getId());
        assertEquals("specialCircumstances", questionnaire.getQuestions().get(5).getId());
    }

    @Test
    @DisplayName("Should evaluate Hindu Marriage Act (HMA 1955) pathway for solemnized marriage")
    void testEvaluateHmaPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("ceremonyStatus", "ALREADY_SOLEMNIZED");
        answers.put("applicableAct", "HMA_1955");
        answers.put("legalAge", "LEGAL_AGE_MET");
        answers.put("residenceState", "ANDHRA_PRADESH");
        answers.put("maritalStatus", "BOTH_UNMARRIED");
        answers.put("specialCircumstances", "NONE");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("HINDU_MARRIAGE_ACT_1955", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Hindu Marriage Act"));
        assertTrue(result.getCalculatedFee().contains("₹100"));
        assertEquals(6, result.getSteps().size());
        assertTrue(result.getDocuments().size() >= 7);
    }

    @Test
    @DisplayName("Should evaluate Special Marriage Act (SMA 1954) pathway with 30-day notice")
    void testEvaluateSmaPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("ceremonyStatus", "INTENDED_FUTURE");
        answers.put("applicableAct", "SMA_1954");
        answers.put("legalAge", "LEGAL_AGE_MET");
        answers.put("residenceState", "ANDHRA_PRADESH");
        answers.put("maritalStatus", "BOTH_UNMARRIED");
        answers.put("specialCircumstances", "INTER_FAITH");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("SPECIAL_MARRIAGE_ACT_1954", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Special Marriage Act"));
        assertTrue(result.getEstimatedTimeline().contains("30"));
        assertEquals(6, result.getSteps().size());
        assertTrue(result.getDocuments().size() >= 6);
    }

    @Test
    @DisplayName("Should detect underage statutory impediment and raise bar alert")
    void testUnderageAlert() {
        Map<String, String> answers = new HashMap<>();
        answers.put("ceremonyStatus", "ALREADY_SOLEMNIZED");
        answers.put("applicableAct", "HMA_1955");
        answers.put("legalAge", "UNDERAGE_ALERT");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("UNDERAGE_STATUTORY_BAR", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("21 years"));
    }

    @Test
    @DisplayName("Should detect living spouse / bigamy impediment if separated without decree")
    void testBigamyImpediment() {
        Map<String, String> answers = new HashMap<>();
        answers.put("ceremonyStatus", "ALREADY_SOLEMNIZED");
        answers.put("legalAge", "LEGAL_AGE_MET");
        answers.put("maritalStatus", "SEPARATED_WITHOUT_DECREE");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("LEGAL_IMPEDIMENT_BIGAMY", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("Divorce Decree Absolute"));
    }

    @Test
    @DisplayName("Should include Divorce Decree for divorced applicants")
    void testDivorceeRequirement() {
        Map<String, String> answers = new HashMap<>();
        answers.put("ceremonyStatus", "ALREADY_SOLEMNIZED");
        answers.put("applicableAct", "HMA_1955");
        answers.put("legalAge", "LEGAL_AGE_MET");
        answers.put("maritalStatus", "DIVORCEE");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        boolean hasDivorceDoc = result.getDocuments().stream()
                .anyMatch(doc -> doc.getDocumentName().contains("Divorce Decree"));
        assertTrue(hasDivorceDoc, "Should contain Divorce Decree Absolute requirement");
    }
}
