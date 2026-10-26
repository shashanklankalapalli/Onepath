package com.onepath.dto;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireDto {
    private String serviceSlug;
    private String serviceName;
    private String title;
    private String description;
    private List<QuestionnaireQuestionDto> questions = new ArrayList<>();

    public QuestionnaireDto() {
    }

    public QuestionnaireDto(String serviceSlug, String serviceName, String title, String description, List<QuestionnaireQuestionDto> questions) {
        this.serviceSlug = serviceSlug;
        this.serviceName = serviceName;
        this.title = title;
        this.description = description;
        this.questions = questions;
    }

    public String getServiceSlug() {
        return serviceSlug;
    }

    public void setServiceSlug(String serviceSlug) {
        this.serviceSlug = serviceSlug;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<QuestionnaireQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionnaireQuestionDto> questions) {
        this.questions = questions;
    }
}
