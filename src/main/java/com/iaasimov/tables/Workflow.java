package com.iaasimov.tables;

import javax.persistence.*;

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
    private String states;
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

}