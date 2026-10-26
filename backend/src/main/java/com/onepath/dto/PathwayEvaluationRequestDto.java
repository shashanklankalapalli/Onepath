package com.onepath.dto;

import java.util.HashMap;
import java.util.Map;

public class PathwayEvaluationRequestDto {
    private Map<String, String> answers = new HashMap<>();

    public PathwayEvaluationRequestDto() {
    }

    public PathwayEvaluationRequestDto(Map<String, String> answers) {
        this.answers = answers;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, String> answers) {
        this.answers = answers;
    }
}
