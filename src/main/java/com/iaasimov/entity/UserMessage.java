package com.iaasimov.entity;

public class UserMessage {

    String question;
    String original_question;
    String token;
    String type;
    String latitude;
    String longitude;
    String eMailName;
    String domain;
    String userEmail;
    Boolean isSimilar;

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String geteMail() {
        return eMailName;
    }

    public void seteMail(String eMail) {
        setUserEmail(eMail);
        this.eMailName = eMail.split("@")[0].replace(".", " ");
    }

    public Boolean isSimilar() {
        return isSimilar;
    }

    public void setisSimilar(Boolean similar) {
        isSimilar = similar;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
    public void setOriginalQuestion(String question) {
        this.original_question = question;
    }

    public String getOriginalQuestion() {
        return original_question;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
