package com.iaasimov.entity;

import java.util.List;

public class Answer {
    private String message;

    private List<ResultSet> resultIaaSimov;
    private String explainerMessage;
    private Suggestion suggestion;
    private String warningMessage;
    private List<ResultSet> similarQuestions;
    private String highlightTerms;

    public String getHighlightTerms() {
        return highlightTerms;
    }

    public void setHighlightTerms(String highlightEntities) {
        this.highlightTerms = highlightEntities;
    }



    public List<ResultSet> getSimilarQuestions() {
        return similarQuestions;
    }

    public void setSimilarQuestions(List<ResultSet> similarQuestions) {
        this.similarQuestions = similarQuestions;
    }



    public Answer(){

    }

    public Answer(String message,
            List<ResultSet> result,
            String explainerMessage){
        this.message = message;

        this.resultIaaSimov = result;
        this.explainerMessage = explainerMessage;
    }

    public Answer(String message,
                  List<ResultSet> result,
                  String explainerMessage,
                  Suggestion suggestion){
        this.message = message;
        this.resultIaaSimov = result;
        this.explainerMessage = explainerMessage;
        this.suggestion = suggestion;
    }

    public String getExtractedEntity() {
        return extractedEntity;
    }


    public void setExtractedEntity(String extractedEntity) {
        this.extractedEntity = extractedEntity;
    }


    public String extractedEntity;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResultSet> getResultIaaSimov() {
        return resultIaaSimov;
    }

    public void setResultIaaSimov(List<ResultSet> resultRestaurants) {
        this.resultIaaSimov = resultRestaurants;
    }

    public String getExplainerMessage() {
        return explainerMessage;
    }

    public void setExplainerMessage(String explainerMessage) {
        this.explainerMessage = explainerMessage;
    }


    public Suggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }
}
