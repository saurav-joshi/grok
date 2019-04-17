package com.iaasimov.entity;

import java.util.List;

public class ResultSet {

    String id;
    String product;
    String country;
    String region;
    String industry;
    String emotionalConnect;
    String csm;
    String customer;
    String url;
    String usecase;
    String businessUsecase;
    String docHandle;
    String similarDocs;
    String docBody;

    public String getSimilarDocs() {
        return similarDocs;
    }

    public void setSimilarDocs(String similardocs) {
        this.similarDocs = similardocs;
    }

    public String getDocHandle() {
        return docHandle;
    }

    public void setDocHandle(String docHandle) {
        this.docHandle = docHandle;
    }

    public String getDocBody() {
        return docBody;
    }

    public void setDocBody(String docBody) {
        this.docBody = docBody;
    }

    public String getBusinessUsecase() {
        return businessUsecase;
    }

    public void setBusinessUsecase(String businessUsecase) {
        this.businessUsecase = businessUsecase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getEmotionalConnect() {
        return emotionalConnect;
    }

    public void setEmotionalConnect(String emotionalConnect) {
        this.emotionalConnect = emotionalConnect;
    }

    public String getCsm() {
        return csm;
    }

    public void setCsm(String csm) {
        this.csm = csm;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsecase() {
        return usecase;
    }

    public void setUsecase(String usecase) {
        this.usecase = usecase;
    }

    public  ResultSet(String id) {
        this.id = id;
    }

    public ResultSet(String id, String product, String country,
                     String region, String industry, String ec,
                     String csm,String customer,
                     String url, String usecase, String businessUsecase,
                     String docHandle, String similarResult,String docBody) {
        this.id = id;
        this.product = product;
        this.country = country;
        this.region = region;
        this.industry = industry;
        this.emotionalConnect = ec;
        this.csm = csm;
        this.customer = customer;
        this.url =url;
        this.usecase = usecase;
        this.businessUsecase =businessUsecase;
        this.docHandle= docHandle;
        this.similarDocs = similarResult;
        this.docBody =docBody;

    }

    public void print(){
        System.out.println("========================");
        System.out.println("-Id:" + id);
        System.out.println("-Product: " + product);
        System.out.println("-Country: " + country);
        System.out.println("-Region: " + region);
        System.out.println("-Industry: " + industry);
        System.out.println("-Emotional Connect: " + emotionalConnect);
        System.out.println("-CSM: " + csm);
        System.out.println("-Customer: " + customer);
        System.out.println("-URL: " + url);
        System.out.println("-UseCase: " + usecase);
        System.out.println("-UseCase: " + businessUsecase);
        System.out.println("-docHandle: " + docHandle);
        System.out.println("-similarDocs: " + similarDocs);
        System.out.println("-docBody: " + docBody);

    }
}
