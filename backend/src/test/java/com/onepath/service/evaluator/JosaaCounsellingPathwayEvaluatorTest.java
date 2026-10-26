package com.onepath.service.evaluator;

import com.onepath.dto.PathwayEvaluationResultDto;
import com.onepath.dto.QuestionnaireDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JosaaCounsellingPathwayEvaluatorTest {

    private JosaaCounsellingPathwayEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new JosaaCounsellingPathwayEvaluator();
    }

    @Test
    @DisplayName("Should support JoSAA counselling service slugs")
    void testSupports() {
        assertTrue(evaluator.supports("josaa-state-admissions-counselling"));
        assertTrue(evaluator.supports("josaa-counselling"));
        assertTrue(evaluator.supports("josaa"));
        assertTrue(evaluator.supports("JOSAA-STATE-COLLEGE-ADMISSIONS-COUNSELLING"));
        assertFalse(evaluator.supports("passport"));
        assertFalse(evaluator.supports("driving-licence"));
    }

    @Test
    @DisplayName("Should generate complete questionnaire with essential questions")
    void testGetQuestionnaire() {
        QuestionnaireDto q = evaluator.getQuestionnaire(null);
        assertNotNull(q);
        assertEquals(JosaaCounsellingPathwayEvaluator.JOSAA_SLUG, q.getServiceSlug());
        assertFalse(q.getQuestions().isEmpty());

        assertTrue(q.getQuestions().stream().anyMatch(qn -> qn.getId().equals("targetExam")));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> qn.getId().equals("category")));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> qn.getId().equals("boardScore")));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> qn.getId().equals("homeState")));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> qn.getId().equals("preferredRoute")));
    }

    @Test
    @DisplayName("Should evaluate JEE Advanced qualified candidate into IIT Pathway")
    void testEvaluateIitPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("targetExam", "JEE_ADVANCED");
        answers.put("category", "GEN");
        answers.put("boardScore", "ABOVE_75");
        answers.put("homeState", "ANDHRA_PRADESH");
        answers.put("preferredRoute", "JOSAA_CENTRAL");

        PathwayEvaluationResultDto result = evaluator.evaluate(null, answers);

        assertNotNull(result);
        assertEquals("JOSAA_IIT_ADVANCED", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("IIT"));
        assertTrue(result.getCalculatedFee().contains("35,000"));
        assertTrue(result.getFeeBreakdown().contains("Seat Acceptance Fee"));
        assertFalse(result.getSteps().isEmpty());
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("Willingness Selection")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Annexure-7")));
        assertTrue(result.getCriticalTips().stream().anyMatch(t -> t.contains("MANDATORY REPORTING SLA")));
    }

    @Test
    @DisplayName("Should evaluate Andhra Pradesh EAPCET state candidate")
    void testEvaluateApEapcetPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("targetExam", "AP_EAPCET");
        answers.put("category", "OBC_NCL");
        answers.put("boardScore", "BETWEEN_45_65");
        answers.put("homeState", "ANDHRA_PRADESH");
        answers.put("preferredRoute", "AP_EAPCET_STATE");

        PathwayEvaluationResultDto result = evaluator.evaluate(null, answers);

        assertNotNull(result);
        assertEquals("AP_EAPCET_STATE", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Andhra Pradesh"));
        assertTrue(result.getCalculatedFee().contains("1,200"));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("OBC-NCL")));
    }

    @Test
    @DisplayName("Should evaluate CSAB Special Round spot allocation pathway")
    void testEvaluateCsabPathway() {
        Map<String, String> answers = new HashMap<>();
        answers.put("targetExam", "JEE_MAIN");
        answers.put("category", "GEN_EWS");
        answers.put("boardScore", "ABOVE_75");
        answers.put("homeState", "ANDHRA_PRADESH");
        answers.put("preferredRoute", "CSAB_SPECIAL");

        PathwayEvaluationResultDto result = evaluator.evaluate(null, answers);

        assertNotNull(result);
        assertEquals("CSAB_SPECIAL_SPOT", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("CSAB"));
        assertTrue(result.getCalculatedFee().contains("45,000"));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDescription().contains("April 1")));
    }

    @Test
    @DisplayName("Should evaluate Dual Track (JoSAA Central & AP EAPCET)")
    void testEvaluateDualTrack() {
        Map<String, String> answers = new HashMap<>();
        answers.put("targetExam", "BOTH_JEE_AND_EAPCET");
        answers.put("category", "SC");
        answers.put("boardScore", "BETWEEN_65_75");
        answers.put("homeState", "ANDHRA_PRADESH");
        answers.put("preferredRoute", "DUAL_COUNSELLING");

        PathwayEvaluationResultDto result = evaluator.evaluate(null, answers);

        assertNotNull(result);
        assertEquals("DUAL_JOSAA_AP_EAPCET", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("15,000")); // Reserved category SAF
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("SC / ST")));
    }
}
