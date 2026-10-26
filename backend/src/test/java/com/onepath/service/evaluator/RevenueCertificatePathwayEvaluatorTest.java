package com.onepath.service.evaluator;

import com.onepath.dto.PathwayEvaluationResultDto;
import com.onepath.dto.QuestionnaireDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RevenueCertificatePathwayEvaluatorTest {

    private RevenueCertificatePathwayEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new RevenueCertificatePathwayEvaluator();
    }

    @Test
    @DisplayName("Should support correct service slugs")
    void testSupportsSlug() {
        assertTrue(evaluator.supports("ews-income-caste-certificate"));
        assertTrue(evaluator.supports("EWS-INCOME-CASTE-CERTIFICATE"));
        assertTrue(evaluator.supports("ews-certificate"));
        assertTrue(evaluator.supports("income-certificate"));
        assertTrue(evaluator.supports("caste-certificate"));
        assertTrue(evaluator.supports("revenue-certificates"));
        assertFalse(evaluator.supports("passport-application-renewal"));
        assertFalse(evaluator.supports("driving-licence-vehicle-registration"));
        assertFalse(evaluator.supports(null));
    }

    @Test
    @DisplayName("Should return structured questionnaire with 11 questions")
    void testGetQuestionnaire() {
        QuestionnaireDto q = evaluator.getQuestionnaire(null);
        assertNotNull(q);
        assertEquals("ews-income-caste-certificate", q.getServiceSlug());
        assertEquals(11, q.getQuestions().size());

        // Check essential questions exist
        assertTrue(q.getQuestions().stream().anyMatch(qn -> "certificateType".equals(qn.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> "stateJurisdiction".equals(qn.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> "ewsCasteCategory".equals(qn.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> "ewsGrossFamilyIncome".equals(qn.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> "ewsAssetExclusion".equals(qn.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> "casteCommunityType".equals(qn.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(qn -> "incomeCertAnnualRange".equals(qn.getId())));
    }

    @Test
    @DisplayName("Scenario 1: Central EWS Eligible (General, Income < 8L, No Excluded Assets, Central Admissions)")
    void testCentralEwsEligible() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "EWS_CERTIFICATE");
        answers.put("stateJurisdiction", "MAHARASHTRA");
        answers.put("applicationNature", "FIRST_TIME");
        answers.put("applicationPurpose", "CENTRAL_ADMISSIONS_JOBS");
        answers.put("ewsCasteCategory", "GENERAL_UNRESERVED");
        answers.put("ewsGrossFamilyIncome", "INCOME_BELOW_8_LAKH");
        answers.put("ewsAssetExclusion", "NO_EXCLUDED_ASSETS");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("EWS_CENTRAL_ELIGIBLE", res.getPathwayCode());
        assertTrue(res.getPathwayTitle().contains("Central EWS Certificate"));
        assertEquals("Central 10% Quota", res.getBadgeText());
        assertTrue(res.getMatchReason().contains("General/Unreserved"));
        assertTrue(res.getMatchReason().contains("below ₹8,00,000"));
        assertTrue(res.getOfficialPortalUrl().contains("aaplesarkar.mahaonline.gov.in"));
        assertTrue(res.getCalculatedFee().contains("₹20.00"));
        assertTrue(res.getEstimatedTimeline().contains("15 Working Days"));
        assertFalse(res.getDocuments().isEmpty());
        assertFalse(res.getSteps().isEmpty());
    }

    @Test
    @DisplayName("Scenario 2: EWS Ineligible - Category Mismatch (SC / ST / OBC)")
    void testEwsCategoryMismatch() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "EWS_CERTIFICATE");
        answers.put("stateJurisdiction", "UTTAR_PRADESH");
        answers.put("ewsCasteCategory", "OBC_NON_CREAMY");
        answers.put("ewsGrossFamilyIncome", "INCOME_BELOW_8_LAKH");
        answers.put("ewsAssetExclusion", "NO_EXCLUDED_ASSETS");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("EWS_INELIGIBLE_CATEGORY_MISMATCH", res.getPathwayCode());
        assertEquals("Statutory Category Exclusion", res.getBadgeText());
        assertEquals("₹0", res.getCalculatedFee());
        assertTrue(res.getMatchReason().contains("103rd Constitutional Amendment Act"));
        assertTrue(res.getMatchReason().contains("OBC"));
    }

    @Test
    @DisplayName("Scenario 3: EWS Ineligible - Family Income Exceeds ₹8 LPA")
    void testEwsIncomeExceeded() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "EWS_CERTIFICATE");
        answers.put("ewsCasteCategory", "GENERAL_UNRESERVED");
        answers.put("ewsGrossFamilyIncome", "INCOME_ABOVE_8_LAKH");
        answers.put("ewsAssetExclusion", "NO_EXCLUDED_ASSETS");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("EWS_INELIGIBLE_INCOME_EXCEEDED", res.getPathwayCode());
        assertEquals("Income Limit Exceeded", res.getBadgeText());
        assertEquals("₹0", res.getCalculatedFee());
        assertTrue(res.getMatchReason().contains("strictly below ₹8,00,000"));
    }

    @Test
    @DisplayName("Scenario 4: EWS Ineligible - Excluded Assets Disqualification")
    void testEwsAssetExclusion() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "EWS_CERTIFICATE");
        answers.put("ewsCasteCategory", "GENERAL_UNRESERVED");
        answers.put("ewsGrossFamilyIncome", "INCOME_BELOW_8_LAKH");
        answers.put("ewsAssetExclusion", "HAS_EXCLUDED_ASSETS");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("EWS_INELIGIBLE_ASSET_EXCLUDED", res.getPathwayCode());
        assertEquals("Asset Exclusion Disqualification", res.getBadgeText());
        assertEquals("₹0", res.getCalculatedFee());
        assertTrue(res.getMatchReason().contains("5 acres"));
    }

    @Test
    @DisplayName("Scenario 5: EWS Annual Renewal / Re-issue for Current Financial Year")
    void testEwsRenewal() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "EWS_CERTIFICATE");
        answers.put("applicationNature", "RENEWAL_UPDATE");
        answers.put("ewsCasteCategory", "GENERAL_UNRESERVED");
        answers.put("ewsGrossFamilyIncome", "INCOME_BELOW_8_LAKH");
        answers.put("ewsAssetExclusion", "NO_EXCLUDED_ASSETS");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("EWS_RENEWAL_NEW_FY", res.getPathwayCode());
        assertEquals("Annual FY Re-issue", res.getBadgeText());
        assertTrue(res.getValidity().contains("1 Financial Year"));
        assertTrue(res.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Previous Financial Year EWS Certificate Copy")));
    }

    @Test
    @DisplayName("Scenario 6: Caste OBC Non-Creamy Layer (NCL) Eligible")
    void testObcNonCreamyLayer() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "CASTE_CERTIFICATE");
        answers.put("stateJurisdiction", "DELHI");
        answers.put("casteCommunityType", "OBC_COMMUNITY");
        answers.put("castePaternalProof", "PATERNAL_CERTIFICATE_AVAILABLE");
        answers.put("incomeCertAnnualRange", "BETWEEN_2_5_AND_8_LAKH");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("CASTE_OBC_NON_CREAMY", res.getPathwayCode());
        assertEquals("27% OBC Quota", res.getBadgeText());
        assertTrue(res.getOfficialPortalUrl().contains("edistrict.delhigovt.nic.in"));
        assertEquals("NIL (100% Free Service)", res.getCalculatedFee());
        assertTrue(res.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("3-Year Family Income Evidence")));
    }

    @Test
    @DisplayName("Scenario 7: Caste OBC Creamy Layer Advisory (Income >= 8 LPA)")
    void testObcCreamyLayer() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "CASTE_CERTIFICATE");
        answers.put("casteCommunityType", "OBC_COMMUNITY");
        answers.put("castePaternalProof", "PATERNAL_CERTIFICATE_AVAILABLE");
        answers.put("incomeCertAnnualRange", "ABOVE_8_LAKH");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("CASTE_OBC_CREAMY_LAYER", res.getPathwayCode());
        assertEquals("Creamy Layer Exclusion", res.getBadgeText());
        assertTrue(res.getMatchReason().contains("Creamy Layer"));
    }

    @Test
    @DisplayName("Scenario 8: Caste SC Certificate with Paternal Proof")
    void testScCertificate() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "CASTE_CERTIFICATE");
        answers.put("stateJurisdiction", "TAMIL_NADU");
        answers.put("casteCommunityType", "SC_COMMUNITY");
        answers.put("castePaternalProof", "PATERNAL_CERTIFICATE_AVAILABLE");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("CASTE_SC_CERTIFICATE", res.getPathwayCode());
        assertEquals("15% SC Constitutional Quota", res.getBadgeText());
        assertTrue(res.getOfficialPortalUrl().contains("tnesevai.tn.gov.in"));
        assertEquals("₹60.00", res.getCalculatedFee());
        assertTrue(res.getValidity().contains("Lifetime Validity"));
    }

    @Test
    @DisplayName("Scenario 9: Caste Certificate with Missing Paternal Record")
    void testCasteMissingPaternalRecord() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "CASTE_CERTIFICATE");
        answers.put("casteCommunityType", "ST_COMMUNITY");
        answers.put("castePaternalProof", "NO_PATERNAL_RECORD");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertTrue(res.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Genealogical Family Tree")));
    }

    @Test
    @DisplayName("Scenario 10: Income Certificate Priority BPL Tier (< 1 Lakh)")
    void testIncomeCertBpl() {
        Map<String, String> answers = new HashMap<>();
        answers.put("certificateType", "INCOME_CERTIFICATE");
        answers.put("stateJurisdiction", "KARNATAKA");
        answers.put("incomeCertAnnualRange", "BELOW_1_LAKH");
        answers.put("incomeProofType", "VILLAGE_OFFICER_TALATHI");

        PathwayEvaluationResultDto res = evaluator.evaluate(null, answers);

        assertEquals("INCOME_CERT_PRIORITY_BPL", res.getPathwayCode());
        assertEquals("BPL / Priority Tier", res.getBadgeText());
        assertTrue(res.getOfficialPortalUrl().contains("nadakacheri.karnataka.gov.in"));
        assertTrue(res.getCalculatedFee().contains("₹25.00"));
        assertTrue(res.getEstimatedTimeline().contains("21 Working Days"));
        assertTrue(res.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Village Revenue Officer")));
    }

    @Test
    @DisplayName("Scenario 11: Multi-state Fee and SLA verification (Bihar & Telangana)")
    void testStateFeeAndSla() {
        Map<String, String> answersBihar = new HashMap<>();
        answersBihar.put("certificateType", "INCOME_CERTIFICATE");
        answersBihar.put("stateJurisdiction", "BIHAR");
        PathwayEvaluationResultDto resBihar = evaluator.evaluate(null, answersBihar);
        assertTrue(resBihar.getOfficialPortalUrl().contains("serviceonline.bihar.gov.in"));
        assertTrue(resBihar.getCalculatedFee().contains("NIL"));
        assertTrue(resBihar.getEstimatedTimeline().contains("10 to 15"));

        Map<String, String> answersTS = new HashMap<>();
        answersTS.put("certificateType", "INCOME_CERTIFICATE");
        answersTS.put("stateJurisdiction", "TELANGANA_AP");
        PathwayEvaluationResultDto resTS = evaluator.evaluate(null, answersTS);
        assertTrue(resTS.getOfficialPortalUrl().contains("meeseva"));
        assertTrue(resTS.getCalculatedFee().contains("₹45.00"));
        assertTrue(resTS.getEstimatedTimeline().contains("30 Working Days"));
    }
}
