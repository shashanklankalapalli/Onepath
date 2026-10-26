package com.onepath.dto;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireQuestionDto {
    private String id;
    private String title;
    private String subtitle;
    private String dependsOn;
    private List<QuestionOptionDto> options = new ArrayList<>();

    public QuestionnaireQuestionDto() {
    }

    public QuestionnaireQuestionDto(String id, String title, String subtitle, String dependsOn, List<QuestionOptionDto> options) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.dependsOn = dependsOn;
        this.options = options;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }
}
