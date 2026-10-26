package com.onepath.service.evaluator;

import com.onepath.dto.PathwayEvaluationResultDto;
import com.onepath.dto.QuestionnaireDto;
import com.onepath.entity.Service;

import java.util.Map;

public interface ServicePathwayEvaluator {
    boolean supports(String serviceSlug);
    QuestionnaireDto getQuestionnaire(Service service);
    PathwayEvaluationResultDto evaluate(Service service, Map<String, String> answers);
}
