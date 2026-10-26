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

class AadhaarPathwayEvaluatorTest {

    private AadhaarPathwayEvaluator evaluator;
    private Service mockService;

    @BeforeEach
    void setUp() {
        evaluator = new AadhaarPathwayEvaluator();
        mockService = new Service();
        mockService.setId(6L);
        mockService.setName("Aadhaar Card Update & Correction");
        mockService.setSlug("aadhaar-update-correction");
    }

    @Test
    @DisplayName("Should support aadhaar-update-correction and related slugs")
    void testSupports() {
        assertTrue(evaluator.supports("aadhaar-update-correction"));
        assertTrue(evaluator.supports("AADHAAR-UPDATE-CORRECTION"));
        assertTrue(evaluator.supports("aadhaar-update"));
        assertTrue(evaluator.supports("aadhaar"));
        assertFalse(evaluator.supports("passport-application-renewal"));
        assertFalse(evaluator.supports(null));
    }

    @Test
    @DisplayName("Should return 4 structured questionnaire questions")
    void testGetQuestionnaire() {
        QuestionnaireDto questionnaire = evaluator.getQuestionnaire(mockService);
        assertNotNull(questionnaire);
        assertEquals("aadhaar-update-correction", questionnaire.getServiceSlug());
        assertEquals(4, questionnaire.getQuestions().size());

        assertEquals("updateType", questionnaire.getQuestions().get(0).getId());
        assertEquals("mobileLinked", questionnaire.getQuestions().get(1).getId());
        assertEquals("hasSupportingDoc", questionnaire.getQuestions().get(2).getId());
        assertEquals("lifetimeHistory", questionnaire.getQuestions().get(3).getId());
    }

    @Test
    @DisplayName("Should evaluate online address update when mobile is linked")
    void testEvaluateOnlineAddressUpdate() {
        Map<String, String> answers = new HashMap<>();
        answers.put("updateType", "ADDRESS_ONLINE");
        answers.put("mobileLinked", "YES_LINKED_OTP");
        answers.put("hasSupportingDoc", "HAVE_OWN_DOCS");
        answers.put("lifetimeHistory", "FIRST_TIME");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("ONLINE_MYAADHAAR_ADDRESS", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Online Address Update"));
        assertTrue(result.getCalculatedFee().contains("₹50.00"));
        assertEquals(5, result.getSteps().size());
        assertTrue(result.getDocuments().size() >= 2);
    }

    @Test
    @DisplayName("Should direct to ASK if mobile is not linked for address update")
    void testEvaluateOfflineAddressUpdateWhenNoMobile() {
        Map<String, String> answers = new HashMap<>();
        answers.put("updateType", "ADDRESS_ONLINE");
        answers.put("mobileLinked", "NO_NOT_LINKED");
        answers.put("hasSupportingDoc", "HAVE_OWN_DOCS");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("ASK_ADDRESS_UPDATE_OFFLINE", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("mobile number is not actively linked"));
        assertEquals(7, result.getSteps().size());
    }

    @Test
    @DisplayName("Should evaluate Mobile/Email update requiring centre visit with NO documents")
    void testEvaluateMobileUpdate() {
        Map<String, String> answers = new HashMap<>();
        answers.put("updateType", "MOBILE_OR_EMAIL");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("ASK_MOBILE_EMAIL_UPDATE", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("CANNOT be performed online"));
        assertTrue(result.getCalculatedFee().contains("₹50.00"));
        assertEquals(7, result.getSteps().size());
    }

    @Test
    @DisplayName("Should evaluate Child Mandatory Biometric Update (MBU) as FREE")
    void testEvaluateChildMbu() {
        Map<String, String> answers = new HashMap<>();
        answers.put("updateType", "CHILD_MBU");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("CHILD_MANDATORY_BIOMETRIC_MBU", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("FREE"));
        assertTrue(result.getMatchReason().contains("age 5"));
    }

    @Test
    @DisplayName("Should evaluate Photograph and Biometrics update at ₹100")
    void testEvaluateBiometricsUpdate() {
        Map<String, String> answers = new HashMap<>();
        answers.put("updateType", "PHOTO_BIOMETRICS");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("ASK_BIOMETRIC_UPDATE", result.getPathwayCode());
        assertTrue(result.getCalculatedFee().contains("₹100.00"));
    }

    @Test
    @DisplayName("Should trigger Exception Handling when lifetime limits are exceeded")
    void testEvaluateLifetimeLimitExceeded() {
        Map<String, String> answers = new HashMap<>();
        answers.put("updateType", "NAME_DOB_GENDER");
        answers.put("lifetimeHistory", "EXCEEDED_LIMIT");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("UIDAI_EXCEPTION_RO_APPROVAL", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("Regional Office"));
        boolean hasGazetteDoc = result.getDocuments().stream()
                .anyMatch(d -> d.getDocumentName().contains("Gazette"));
        assertTrue(hasGazetteDoc);
    }
}
