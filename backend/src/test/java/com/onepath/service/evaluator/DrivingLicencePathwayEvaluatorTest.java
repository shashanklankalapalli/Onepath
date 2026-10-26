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

public class DrivingLicencePathwayEvaluatorTest {

    private DrivingLicencePathwayEvaluator evaluator;
    private Service sampleService;

    @BeforeEach
    void setUp() {
        evaluator = new DrivingLicencePathwayEvaluator();
        sampleService = new Service();
        sampleService.setName("Driving Licence & Vehicle Registration");
        sampleService.setSlug("driving-licence-vehicle-registration");
    }

    @Test
    void testSupports() {
        assertTrue(evaluator.supports("driving-licence-vehicle-registration"));
        assertTrue(evaluator.supports("driving-licence"));
        assertTrue(evaluator.supports("driving-license"));
        assertTrue(evaluator.supports("vehicle-registration"));
        assertFalse(evaluator.supports("passport-application-renewal"));
    }

    @Test
    void testGetQuestionnaire() {
        QuestionnaireDto q = evaluator.getQuestionnaire(sampleService);
        assertNotNull(q);
        assertEquals("driving-licence-vehicle-registration", q.getServiceSlug());
        assertEquals(7, q.getQuestions().size());
        assertTrue(q.getQuestions().stream().anyMatch(question -> "serviceNeed".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "vehicleCategory".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "applicantAge".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "stateCode".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "learnerLicenceStatus".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "rcSubService".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "aadhaarAuthAvailable".equals(question.getId())));
    }

    // Scenario 1: Fresh Learner's Licence for Dual Class (MCWG + LMV) with Aadhaar Contactless
    @Test
    void testScenario1_LearnerLicence_Combo_AadhaarContactless() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "LEARNER_LICENCE");
        answers.put("vehicleCategory", "BOTH_TWO_FOUR");
        answers.put("applicantAge", "AGE_18_TO_39");
        answers.put("stateCode", "ANDHRA_PRADESH");
        answers.put("aadhaarAuthAvailable", "AADHAAR_OTP_AVAILABLE");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("LL_COMBO_TWO_FOUR", result.getPathwayCode());
        assertEquals("₹350", result.getCalculatedFee());
        assertTrue(result.getEstimatedTimeline().contains("Instant / Same Day"));
        assertTrue(result.getPoliceVerificationMode().contains("Faceless / Contactless Online Test"));
        assertTrue(result.getOfficialPortalUrl().contains("sarathi.parivahan.gov.in"));

        // Documents must include DOB, Address, Form 1
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Date of Birth")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Residential Address")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 1")));
        // Must NOT require Learner's Licence or old DL
        assertFalse(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 3 Learner's Licence")));
        assertFalse(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 1A"))); // Not required for under 40
    }

    // Scenario 2: Permanent Driving Licence for LMV Car after 30+ days practice
    @Test
    void testScenario2_PermanentDL_LMV_Eligible() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "PERMANENT_DL");
        answers.put("vehicleCategory", "FOUR_WHEELER_LMV");
        answers.put("applicantAge", "AGE_18_TO_39");
        answers.put("learnerLicenceStatus", "LL_ACTIVE_OVER_30_DAYS");
        answers.put("stateCode", "TELANGANA");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("DL_PERMANENT_SINGLE", result.getPathwayCode());
        assertEquals("₹700", result.getCalculatedFee());
        assertTrue(result.getEstimatedTimeline().contains("7 – 15 Working Days"));
        assertTrue(result.getPoliceVerificationMode().contains("Practical Driving Competence Test"));

        // Must require Form 3 LL and physical test vehicle
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 3 Learner's Licence")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Original Vehicle for Practical Test")));
    }

    // Scenario 3: Permanent Driving Licence BLOCKED due to under 30 days practice
    @Test
    void testScenario3_PermanentDL_Blocked_Under30DaysPractice() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "PERMANENT_DL");
        answers.put("vehicleCategory", "FOUR_WHEELER_LMV");
        answers.put("applicantAge", "AGE_18_TO_39");
        answers.put("learnerLicenceStatus", "LL_HELD_UNDER_30_DAYS");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("DL_PERMANENT_BLOCKED_WAITING_PERIOD", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("minimum of 30 days"));
        assertTrue(result.getBadgeText().contains("Practice Period"));
    }

    // Scenario 4: Driving Licence Renewal
    @Test
    void testScenario4_DLRenewal_Standard() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "DL_RENEWAL");
        answers.put("vehicleCategory", "FOUR_WHEELER_LMV");
        answers.put("applicantAge", "AGE_18_TO_39");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("DL_RENEWAL_STANDARD", result.getPathwayCode());
        assertEquals("₹400", result.getCalculatedFee());
        assertTrue(result.getEstimatedTimeline().contains("7 – 10 Working Days"));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Existing Original Driving Licence")));
    }

    // Scenario 5: Duplicate Driving Licence Issue (Lost / Damaged)
    @Test
    void testScenario5_DuplicateDL_Lost() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "DL_DUPLICATE");
        answers.put("vehicleCategory", "TWO_WHEELER_GEAR");
        answers.put("applicantAge", "AGE_18_TO_39");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("DL_DUPLICATE_ISSUE", result.getPathwayCode());
        assertEquals("₹400", result.getCalculatedFee());
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Police Non-Cognizable Report")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form LLD")));
    }

    // Scenario 6: New Vehicle Registration (Car / LMV)
    @Test
    void testScenario6_NewVehicleRegistration_Car() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "NEW_VEHICLE_RC");
        answers.put("vehicleCategory", "FOUR_WHEELER_LMV");
        answers.put("stateCode", "ANDHRA_PRADESH");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("RC_NEW_VEHICLE", result.getPathwayCode());
        assertEquals("₹600 (plus State Road Tax)", result.getCalculatedFee());
        assertTrue(result.getOfficialPortalUrl().contains("vahan.parivahan.gov.in"));
        assertTrue(result.getPoliceVerificationMode().contains("Physical Vehicle Inspection by MVI"));

        // Must require Form 20, 21, 22, Insurance
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 20")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 21")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 22")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Insurance Certificate")));
    }

    // Scenario 7: Vehicle Ownership Transfer
    @Test
    void testScenario7_VehicleOwnershipTransfer() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "RC_SERVICES");
        answers.put("rcSubService", "RC_TRANSFER_OWNERSHIP");
        answers.put("vehicleCategory", "FOUR_WHEELER_LMV");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("RC_TRANSFER_OWNERSHIP", result.getPathwayCode());
        assertEquals("₹300", result.getCalculatedFee());
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 29")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("PUC")));
    }

    // Scenario 8: 15-Year Vehicle Registration Renewal
    @Test
    void testScenario8_RC15YearRenewal_Car() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "RC_SERVICES");
        answers.put("rcSubService", "RC_RENEWAL_15_YEAR");
        answers.put("vehicleCategory", "FOUR_WHEELER_LMV");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("RC_15_YEAR_RENEWAL", result.getPathwayCode());
        assertEquals("₹5,000 (Rule 81 CMVR)", result.getCalculatedFee());
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 25")));
    }

    // Scenario 9: Underage Applicant (Under 16 Years)
    @Test
    void testScenario9_IneligibleUnderage() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "LEARNER_LICENCE");
        answers.put("vehicleCategory", "TWO_WHEELER_NO_GEAR");
        answers.put("applicantAge", "UNDER_16");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("INELIGIBLE_UNDER_AGE", result.getPathwayCode());
        assertEquals("₹0", result.getCalculatedFee());
        assertTrue(result.getMatchReason().contains("Section 4 of the Motor Vehicles Act 1988"));
    }

    // Scenario 10: Age 40+ Requires Medical Form 1A
    @Test
    void testScenario10_Age40Plus_RequiresForm1A() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceNeed", "LEARNER_LICENCE");
        answers.put("vehicleCategory", "FOUR_WHEELER_LMV");
        answers.put("applicantAge", "AGE_40_PLUS");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Form 1A (Medical Certificate")));
    }
}
