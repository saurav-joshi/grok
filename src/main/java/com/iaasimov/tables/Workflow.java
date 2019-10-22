package com.iaasimov.tables;

import com.iaasimov.entity.ResultSet;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="workflow")
public class Workflow {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "workflow_id")
    private long workflowId;
    @Column(name = "conversation_id")
    private long conversationId;
    @Column(name = "library_id")
    private long libraryId;
    @Column(name = "entity_list")
    private String entityList;
    @Column(name = "question")
    private String question;
    @Column(name = "original_question")
    private String originalQuestion;
    @Column(name = "answer")
    private String answer;
    @Column(name = "user_email", unique = true)
    private String userEmail;
    @Column(name = "time_stamp", unique = true)
    private String timeStamp;
    @Column(name = "states")
    private String states ;
    @Column(name = "suggestion")
    private String suggestion;
    @Column(name = "city")
    private String city;
    @Column(name = "action_words")
    private String actionWords;
    @Column(name = "location")
    private String location;
    @Column(name = "geo")
    private String geo;
    @Column(name = "recommendations")
    private String resultSet;

    public String getResultSet() {
        return resultSet;
    }

    public void setResultSet(String resultSet) {
        this.resultSet = resultSet;
    }

    public long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(long workflowId) {
        this.workflowId = workflowId;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public long getLibraryId() {
        return libraryId;
    }

    public void setLibraryId(long libraryId) {
        this.libraryId = libraryId;
    }

    public String getEntityList() {
        return entityList;
    }

    public void setEntityList(String entityList) {
        this.entityList = entityList;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getOriginalQuestion() {
        return originalQuestion;
    }

    public void setOriginalQuestion(String originalQuestion) {
        this.originalQuestion = originalQuestion;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStates() {
        return states;
    }

    public void setStates(List<String> states) {
        this.states = String.join(",", states);
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getActionWords() {
        return actionWords;
    }

    public void setActionWords(String actionWords) {
        this.actionWords = actionWords;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }
}