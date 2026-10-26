package com.onepath.service.evaluator;

import com.onepath.dto.PathwayEvaluationResultDto;
import com.onepath.dto.QuestionnaireDto;
import com.onepath.dto.ServiceRequiredDocumentDto;
import com.onepath.entity.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PassportPathwayEvaluatorTest {

    private PassportPathwayEvaluator evaluator;
    private Service sampleService;

    @BeforeEach
    void setUp() {
        evaluator = new PassportPathwayEvaluator();
        sampleService = new Service();
        sampleService.setName("Passport Application & Renewal");
        sampleService.setSlug("passport-application-renewal");
    }

    @Test
    void testSupports() {
        assertTrue(evaluator.supports("passport-application-renewal"));
        assertTrue(evaluator.supports("passport"));
        assertFalse(evaluator.supports("driving-licence"));
    }

    @Test
    void testGetQuestionnaire() {
        QuestionnaireDto q = evaluator.getQuestionnaire(sampleService);
        assertNotNull(q);
        assertEquals("passport-application-renewal", q.getServiceSlug());
        assertEquals(6, q.getQuestions().size());
        assertTrue(q.getQuestions().stream().anyMatch(question -> "applicationType".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "applicantCategory".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "renewalReason".equals(question.getId())));
    }

    @Test
    void testScenarioA_CollegeStudentHostel_Fresh_Normal() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "FRESH");
        answers.put("applicantCategory", "COLLEGE_STUDENT_HOSTEL");
        answers.put("scheme", "NORMAL");
        answers.put("bookletSize", "PAGES_36");
        answers.put("educationLevel", "NON_ECR_QUALIFIED");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("FRESH_STUDENT_HOSTEL", result.getPathwayCode());
        assertEquals("₹2,500", result.getCalculatedFee());
        assertTrue(result.getEstimatedTimeline().contains("7 – 15 Working Days"));
        assertEquals("Pre-Police Verification (Standard Fresh & Changes)", result.getPoliceVerificationMode());
        assertTrue(result.getEmigrationStatus().startsWith("Non-ECR"));

        // Documents must include Student ID, Bonafide, Warden Certificate
        boolean hasStudentId = result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Student ID"));
        boolean hasBonafide = result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Bonafide"));
        boolean hasWardenCert = result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Warden"));
        assertTrue(hasStudentId, "Should require Student ID");
        assertTrue(hasBonafide, "Should require Bonafide Certificate");
        assertTrue(hasWardenCert, "Should require Hostel Warden Certificate");

        // Old passport must NOT be required for fresh
        boolean hasOldPassport = result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Old Passport"));
        assertFalse(hasOldPassport, "Fresh application should NOT require old passport");

        // Must include student dual address tip
        assertTrue(result.getCriticalTips().stream().anyMatch(tip -> tip.contains("Student Dual Address Handling")));
    }

    @Test
    void testScenarioB_AdultRenewal_UnchangedDetails_Tatkaal() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "RENEWAL");
        answers.put("applicantCategory", "ADULT");
        answers.put("scheme", "TATKAAL");
        answers.put("bookletSize", "PAGES_36");
        answers.put("renewalReason", "EXPIRY_PAGES_NO_CHANGE");
        answers.put("educationLevel", "NON_ECR_QUALIFIED");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("RENEWAL_UNCHANGED", result.getPathwayCode());
        assertEquals("₹5,000", result.getCalculatedFee());
        assertTrue(result.getEstimatedTimeline().contains("1 – 3 Working Days"));
        assertEquals("No Police Verification (Exempted)", result.getPoliceVerificationMode());

        // Must require Old Passport and photocopies
        boolean hasOldPassport = result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Old Passport"));
        assertTrue(hasOldPassport, "Renewal MUST require old passport");

        // Address proof should be optional/not mandatory for unchanged renewal
        ServiceRequiredDocumentDto addressDoc = result.getDocuments().stream()
                .filter(d -> d.getDocumentName().contains("Address"))
                .findFirst()
                .orElse(null);
        assertNotNull(addressDoc);
        assertFalse(addressDoc.isMandatory(), "Address proof should NOT be mandatory when address unchanged");

        // Tatkaal verification document should be present
        boolean hasTatkaalDoc = result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Tatkaal"));
        assertTrue(hasTatkaalDoc, "Tatkaal application should require Tatkaal verification docs");
    }

    @Test
    void testScenarioC_MinorApplication_Normal() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "FRESH");
        answers.put("applicantCategory", "MINOR");
        answers.put("scheme", "NORMAL");
        answers.put("bookletSize", "PAGES_36");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("FRESH_MINOR", result.getPathwayCode());
        assertEquals("₹1,750", result.getCalculatedFee());
        assertTrue(result.getValidity().contains("5 Years or until age 18"));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Annexure D")));
    }

    @Test
    void testScenarioD_Renewal_NameChange() {
        Map<String, String> answers = new HashMap<>();
        answers.put("applicationType", "RENEWAL");
        answers.put("applicantCategory", "ADULT");
        answers.put("scheme", "NORMAL");
        answers.put("bookletSize", "PAGES_36");
        answers.put("renewalReason", "NAME_CHANGE");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("RENEWAL_NAME_CHANGE", result.getPathwayCode());
        boolean hasNameProof = result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Name / Surname Change"));
        assertTrue(hasNameProof, "Must require supporting documents for name change");
    }

    // Required tests for official fee structure effective 1 July 2026:
    // 1. Adult Fresh + Normal + 36 pages -> ₹2,500
    @Test
    void testOfficialFee_AdultFreshNormal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "FRESH",
                "applicantCategory", "ADULT",
                "scheme", "NORMAL",
                "bookletSize", "PAGES_36"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹2,500", result.getCalculatedFee());
    }

    // 2. Adult Fresh + Normal + 60 pages -> ₹3,500
    @Test
    void testOfficialFee_AdultFreshNormal60Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "FRESH",
                "applicantCategory", "ADULT",
                "scheme", "NORMAL",
                "bookletSize", "PAGES_60"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹3,500", result.getCalculatedFee());
    }

    // 3. Adult Fresh + Tatkaal + 36 pages -> ₹5,000
    @Test
    void testOfficialFee_AdultFreshTatkaal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "FRESH",
                "applicantCategory", "ADULT",
                "scheme", "TATKAAL",
                "bookletSize", "PAGES_36"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹5,000", result.getCalculatedFee());
    }

    // 4. Adult Fresh + Tatkaal + 60 pages -> ₹6,000
    @Test
    void testOfficialFee_AdultFreshTatkaal60Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "FRESH",
                "applicantCategory", "ADULT",
                "scheme", "TATKAAL",
                "bookletSize", "PAGES_60"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹6,000", result.getCalculatedFee());
    }

    // 5. Minor + Normal + 36 pages -> ₹1,750
    @Test
    void testOfficialFee_MinorNormal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "FRESH",
                "applicantCategory", "MINOR",
                "scheme", "NORMAL",
                "bookletSize", "PAGES_36"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹1,750", result.getCalculatedFee());
    }

    // 6. Minor + Tatkaal + 36 pages -> ₹4,250
    @Test
    void testOfficialFee_MinorTatkaal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "FRESH",
                "applicantCategory", "MINOR",
                "scheme", "TATKAAL",
                "bookletSize", "PAGES_36"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹4,250", result.getCalculatedFee());
    }

    // 7. Renewal/Re-issue + Normal + 36 pages -> ₹2,500
    @Test
    void testOfficialFee_RenewalNormal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "RENEWAL",
                "applicantCategory", "ADULT",
                "scheme", "NORMAL",
                "bookletSize", "PAGES_36",
                "renewalReason", "EXPIRY_PAGES_NO_CHANGE"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹2,500", result.getCalculatedFee());
    }

    // 8. Renewal/Re-issue + Tatkaal + 36 pages -> ₹5,000
    @Test
    void testOfficialFee_RenewalTatkaal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "RENEWAL",
                "applicantCategory", "ADULT",
                "scheme", "TATKAAL",
                "bookletSize", "PAGES_36",
                "renewalReason", "EXPIRY_PAGES_NO_CHANGE"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹5,000", result.getCalculatedFee());
    }

    // Additional official structure tests:
    // Replacement Lost/Damaged 36p Normal -> ₹5,000
    @Test
    void testOfficialFee_LostDamagedNormal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "RENEWAL",
                "applicantCategory", "ADULT",
                "scheme", "NORMAL",
                "bookletSize", "PAGES_36",
                "renewalReason", "LOST_DAMAGED"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹5,000", result.getCalculatedFee());
    }

    // Replacement Lost/Damaged 36p Tatkaal -> ₹7,500
    @Test
    void testOfficialFee_LostDamagedTatkaal36Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "RENEWAL",
                "applicantCategory", "ADULT",
                "scheme", "TATKAAL",
                "bookletSize", "PAGES_36",
                "renewalReason", "LOST_DAMAGED"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹7,500", result.getCalculatedFee());
    }

    // Replacement Lost/Damaged 60p Normal -> ₹6,000
    @Test
    void testOfficialFee_LostDamagedNormal60Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "RENEWAL",
                "applicantCategory", "ADULT",
                "scheme", "NORMAL",
                "bookletSize", "PAGES_60",
                "renewalReason", "LOST_DAMAGED"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹6,000", result.getCalculatedFee());
    }

    // Replacement Lost/Damaged 60p Tatkaal -> ₹8,500
    @Test
    void testOfficialFee_LostDamagedTatkaal60Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "RENEWAL",
                "applicantCategory", "ADULT",
                "scheme", "TATKAAL",
                "bookletSize", "PAGES_60",
                "renewalReason", "LOST_DAMAGED"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("₹8,500", result.getCalculatedFee());
    }

    // Unlisted Category: Police Clearance Certificate (PCC) -> Redirects to official calculator
    @Test
    void testOfficialFee_PoliceClearanceCertificate() {
        Map<String, String> answers = Map.of(
                "applicationType", "PCC"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("Refer to Official Fee Calculator", result.getCalculatedFee());
        assertTrue(result.getFeeBreakdown().contains("https://www.passportindia.gov.in/psp/FeeCalculator"));
    }

    // Unlisted Category: Minor with 60 Pages Jumbo Booklet -> Redirects to official calculator
    @Test
    void testOfficialFee_MinorUnlisted60Pages() {
        Map<String, String> answers = Map.of(
                "applicationType", "FRESH",
                "applicantCategory", "MINOR",
                "scheme", "NORMAL",
                "bookletSize", "PAGES_60"
        );
        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);
        assertEquals("Refer to Official Fee Calculator", result.getCalculatedFee());
        assertTrue(result.getFeeBreakdown().contains("https://www.passportindia.gov.in/psp/FeeCalculator"));
    }
}
