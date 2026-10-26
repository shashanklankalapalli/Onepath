package com.onepath.service.evaluator;

import com.onepath.dto.PathwayEvaluationResultDto;
import com.onepath.dto.QuestionnaireDto;
import com.onepath.entity.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class VoterIdPathwayEvaluatorTest {

    private VoterIdPathwayEvaluator evaluator;
    private Service mockService;

    @BeforeEach
    void setUp() {
        evaluator = new VoterIdPathwayEvaluator();
        mockService = new Service();
        mockService.setId(10L);
        mockService.setName("Voter ID Registration & Address Change");
        mockService.setSlug("voter-id-registration-address-change");
    }

    @Test
    void testSupportsSlug() {
        assertTrue(evaluator.supports("voter-id-registration-address-change"));
        assertTrue(evaluator.supports("voter-id"));
        assertTrue(evaluator.supports("VOTER-REGISTRATION"));
        assertTrue(evaluator.supports("voters-service"));
        assertTrue(evaluator.supports("epic-card"));
        assertFalse(evaluator.supports("passport-application-renewal"));
        assertFalse(evaluator.supports(null));
    }

    @Test
    void testGetQuestionnaireReturnsQuestions() {
        QuestionnaireDto questionnaire = evaluator.getQuestionnaire(mockService);
        assertNotNull(questionnaire);
        assertEquals("voter-id-registration-address-change", questionnaire.getServiceSlug());
        assertNotNull(questionnaire.getQuestions());
        assertTrue(questionnaire.getQuestions().size() >= 4);

        // Check that Q1 has the 6 main voter purposes
        var q1 = questionnaire.getQuestions().get(0);
        assertEquals("voterPurpose", q1.getId());
        assertTrue(q1.getOptions().size() >= 6);
    }

    @Test
    void testPathway1_NewVoterRegistrationForm6() {
        Map<String, String> answers = new HashMap<>();
        answers.put("voterPurpose", "NEW_VOTER_REGISTRATION");
        answers.put("qualifyingAgeStatus", "QUALIFIED_18_PLUS");
        answers.put("stateJurisdiction", "ANDHRA_PRADESH");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("NEW_VOTER_REGISTRATION_FORM_6", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Form 6"));
        assertEquals("₹0.00 (Statutory Free of Cost)", result.getCalculatedFee());
        assertTrue(result.getOfficialPortalUrl().contains("voters.eci.gov.in"));
        assertTrue(result.getMatchReason().contains("Section 19 of the Representation of the People Act"));

        // Check required documents
        assertNotNull(result.getDocuments());
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Proof of Date of Birth & Age") && d.isMandatory()));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Proof of Ordinary Residence") && d.isMandatory()));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Photograph") && d.isMandatory()));

        // Check procedural steps
        assertNotNull(result.getSteps());
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("Booth Level Officer (BLO) Physical Field Verification")));
    }

    @Test
    void testPathway2_ShiftingResidenceForm8() {
        Map<String, String> answers = new HashMap<>();
        answers.put("voterPurpose", "SHIFTING_RESIDENCE");
        answers.put("stateJurisdiction", "ANDHRA_PRADESH");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("SHIFTING_RESIDENCE_FORM_8", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Form 8"));
        assertEquals("₹0.00 (Statutory Free of Cost)", result.getCalculatedFee());
        assertTrue(result.getMatchReason().contains("automatically triggers deletion from the old polling station"));

        // Must require existing EPIC and new address proof
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Existing Voter ID (EPIC) Number") && d.isMandatory()));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Proof of New Ordinary Residence") && d.isMandatory()));
    }

    @Test
    void testPathway3_CorrectionDetailsForm8() {
        Map<String, String> answers = new HashMap<>();
        answers.put("voterPurpose", "CORRECTION_DETAILS");
        answers.put("conditionDetails", "CORRECTING_NAME_OR_DOB");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("CORRECTION_ENTRIES_FORM_8", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Correction of Entries"));
        assertEquals("₹0.00 (Statutory Free of Cost)", result.getCalculatedFee());
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Documentary Proof Supporting Requested Correction") && d.isMandatory()));
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("Select Specific Fields to Correct (Max 4 Fields)")));
    }

    @Test
    void testPathway4_ReplacementEpicForm8_LostCard() {
        Map<String, String> answers = new HashMap<>();
        answers.put("voterPurpose", "REPLACEMENT_EPIC");
        answers.put("conditionDetails", "LOST_STOLEN_EPIC");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("REPLACEMENT_EPIC_FORM_8", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Issue of Replacement EPIC"));
        assertEquals("₹0.00 (Statutory Free of Cost)", result.getCalculatedFee());

        // For lost card, FIR/Police report should be mandatory
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Police FIR") && d.isMandatory()));
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("Lodge Police Lost Report")));
    }

    @Test
    void testPathway5_DownloadDigitalEEpic() {
        Map<String, String> answers = new HashMap<>();
        answers.put("voterPurpose", "DOWNLOAD_E_EPIC");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("DOWNLOAD_DIGITAL_E_EPIC", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("e-EPIC PDF"));
        assertEquals("₹0.00 (Statutory 100% Free)", result.getCalculatedFee());
        assertEquals("Instant (2 to 5 Minutes)", result.getEstimatedTimeline());
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Registered Mobile Number")));
    }

    @Test
    void testPathway6_CheckStatusAndElectoralSearch() {
        Map<String, String> answers = new HashMap<>();
        answers.put("voterPurpose", "CHECK_STATUS_SEARCH");

        PathwayEvaluationResultDto result = evaluator.evaluate(mockService, answers);
        assertNotNull(result);
        assertEquals("CHECK_STATUS_ELECTORAL_SEARCH", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Electoral Roll Search"));
        assertTrue(result.getOfficialPortalUrl().contains("electoralsearch.eci.gov.in"));
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("Download Voter Information Slip (VIS)")));
    }
}
