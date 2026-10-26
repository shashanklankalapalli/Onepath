package com.onepath.service.evaluator;

import com.onepath.dto.PathwayEvaluationResultDto;
import com.onepath.dto.QuestionnaireDto;
import com.onepath.entity.Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyRegistrationPathwayEvaluatorTest {

    private PropertyRegistrationPathwayEvaluator evaluator;
    private Service sampleService;

    @BeforeEach
    void setUp() {
        evaluator = new PropertyRegistrationPathwayEvaluator();
        sampleService = new Service();
        sampleService.setName("Property / Land Revenue Registration");
        sampleService.setSlug("property-land-revenue-registration");
        sampleService.setCategory("Government & Civic Services");
    }

    @Test
    void testSupports() {
        assertTrue(evaluator.supports("property-land-revenue-registration"));
        assertTrue(evaluator.supports("property-registration"));
        assertTrue(evaluator.supports("land-registration"));
        assertTrue(evaluator.supports("property-land-revenue"));
        assertFalse(evaluator.supports("passport-application-renewal"));
        assertFalse(evaluator.supports("driving-licence"));
    }

    @Test
    void testGetQuestionnaire() {
        QuestionnaireDto q = evaluator.getQuestionnaire(sampleService);
        assertNotNull(q);
        assertEquals("property-land-revenue-registration", q.getServiceSlug());
        assertEquals(5, q.getQuestions().size());
        assertTrue(q.getQuestions().stream().anyMatch(question -> "serviceRequirement".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "isAndhraPradesh".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "propertyType".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "existingDocumentsStatus".equals(question.getId())));
        assertTrue(q.getQuestions().stream().anyMatch(question -> "isSaleDeedRegistered".equals(question.getId())));
    }

    @Test
    void testScenario_UrbanSalePurchase_AP() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "SALE_PURCHASE_REGISTRATION");
        answers.put("isAndhraPradesh", "AP_YES");
        answers.put("propertyType", "URBAN");
        answers.put("existingDocumentsStatus", "DOCS_AVAILABLE");
        answers.put("isSaleDeedRegistered", "DEED_NOT_REGISTERED");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_SALE_REGISTRATION_URBAN_AP", result.getPathwayCode());
        assertEquals("Market Value (Circle Rate) Based Duty", result.getCalculatedFee());
        assertTrue(result.getOfficialPortalUrl().contains("registration.ap.gov.in"));
        assertEquals(9, result.getSteps().size());

        // Urban documents check
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Building Sanction Plan")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Property Tax Receipts")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Parent / Link Deeds")));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Aadhaar & PAN")));

        // Tips check
        assertTrue(result.getCriticalTips().stream().anyMatch(t -> t.contains("Section 22A")));
        assertTrue(result.getCriticalTips().stream().anyMatch(t -> t.contains("Section 194-IA")));
    }

    @Test
    void testScenario_RuralAgriculturalSale_AP() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "SALE_PURCHASE_REGISTRATION");
        answers.put("isAndhraPradesh", "AP_YES");
        answers.put("propertyType", "RURAL_AGRICULTURAL");
        answers.put("existingDocumentsStatus", "DOCS_AVAILABLE");
        answers.put("isSaleDeedRegistered", "DEED_NOT_REGISTERED");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_SALE_REGISTRATION_RURAL_AP", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Agricultural Land"));
        assertTrue(result.getDocuments().stream().anyMatch(d -> d.getDocumentName().contains("Pattadar Passbook")));
    }

    @Test
    void testScenario_EncumbranceCertificateSearch_AP() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "ENCUMBRANCE_CERTIFICATE");
        answers.put("isAndhraPradesh", "AP_YES");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_ENCUMBRANCE_CERTIFICATE_AP", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Form 15"));
        assertTrue(result.getEstimatedTimeline().contains("Instant"));
        assertTrue(result.getFeeBreakdown().contains("Form 15/16"));
        assertTrue(result.getCriticalTips().stream().anyMatch(t -> t.contains("30-Year Rule")));
    }

    @Test
    void testScenario_LandRecordsSearch_AP() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "LAND_RECORD_SEARCH");
        answers.put("isAndhraPradesh", "AP_YES");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_LAND_RECORDS_SEARCH_AP", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Meebhoomi"));
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("Meebhoomi")));
    }

    @Test
    void testScenario_MutationRecordUpdate_AP() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "MUTATION_RECORD_UPDATE");
        answers.put("isAndhraPradesh", "AP_YES");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_MUTATION_AP", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Webland / RoR-1B"));
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("15-Day Public Notice")));
    }

    @Test
    void testScenario_AlreadyRegisteredDeed_DirectsToMutation() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "SALE_PURCHASE_REGISTRATION");
        answers.put("isAndhraPradesh", "AP_YES");
        answers.put("isSaleDeedRegistered", "DEED_REGISTERED_NEED_MUTATION");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_MUTATION_AP", result.getPathwayCode());
        assertTrue(result.getMatchReason().contains("already been registered"));
    }

    @Test
    void testScenario_LandRecordCorrection_AP() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "LAND_RECORD_CORRECTION");
        answers.put("isAndhraPradesh", "AP_YES");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_RECORD_CORRECTION_AP", result.getPathwayCode());
        assertTrue(result.getSteps().stream().anyMatch(s -> s.getTitle().contains("Mandal Surveyor")));
    }

    @Test
    void testScenario_OtherPropertyDeed_AP() {
        Map<String, String> answers = new HashMap<>();
        answers.put("serviceRequirement", "OTHER_PROPERTY_SERVICE");
        answers.put("isAndhraPradesh", "AP_YES");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("PROPERTY_OTHER_DEED_AP", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("Gift Deed"));
        assertTrue(result.getFeeBreakdown().contains("concessional stamp duty"));
    }

    @Test
    void testScenario_OutOfStateProperty() {
        Map<String, String> answers = new HashMap<>();
        answers.put("isAndhraPradesh", "AP_NO");

        PathwayEvaluationResultDto result = evaluator.evaluate(sampleService, answers);

        assertNotNull(result);
        assertEquals("OUT_OF_STATE_PROPERTY", result.getPathwayCode());
        assertTrue(result.getPathwayTitle().contains("State Jurisdiction Requirement"));
        assertTrue(result.getMatchReason().contains("Section 28"));
    }
}
