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
    List<String> urlList;
    String usecase;
    String businessUsecase;
    String docHandle;
    String similarDocs;
    String docBody;
    String dealSize;
    String advantages;
    String keyObjections;
    String customerBackground;
    String businessPainPoints;
    String useCaseType;
    String displayCustomerName;

    public String getDisplayCustomerName() {
        return displayCustomerName;
    }

    public void setDisplayCustomerName(String detailedCustomerName) {
        this.displayCustomerName = detailedCustomerName;
    }

    public String getDealSize() {
        return dealSize;
    }

    public void setDealSize(String dealSize) {
        this.dealSize = dealSize;
    }

    public String getAdvantages() {
        return advantages;
    }

    public void setAdvantages(String advantages) {
        this.advantages = advantages;
    }

    public String getKeyObjections() {
        return keyObjections;
    }

    public void setKeyObjections(String keyObjections) {
        this.keyObjections = keyObjections;
    }

    public String getCustomerBackground() {
        return customerBackground;
    }

    public void setCustomerBackground(String customerBackground) {
        this.customerBackground = customerBackground;
    }

    public String getBusinessPainPoints() {
        return businessPainPoints;
    }

    public void setBusinessPainPoints(String businessPainPoints) {
        this.businessPainPoints = businessPainPoints;
    }

    public String getUseCaseType() {
        return useCaseType;
    }

    public void setUseCaseType(String useCaseType) {
        this.useCaseType = useCaseType;
    }


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

    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> url) {
        this.urlList = url;
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
                     String csm,String customer,String detailedCustomerName,
                     String url, List<String> urlList,String usecase, String businessUsecase,
                     String docHandle, String similarResult,String docBody,
                     String dealSize, String advantages, String keyObjections,
                     String customerBackground, String businessPainPoints, String useCaseType) {
        this.id = id;
        this.product = product;
        this.country = country;
        this.region = region;
        this.industry = industry;
        this.emotionalConnect = ec;
        this.csm = csm;
        this.customer = customer;
        this.displayCustomerName = detailedCustomerName;
        this.url =url;
        this.urlList =urlList;
        this.usecase = usecase;
        this.businessUsecase =businessUsecase;
        this.docHandle= docHandle;
        this.similarDocs = similarResult;
        this.docBody =docBody;
        this.dealSize = dealSize;
        this.advantages = advantages;
        this.keyObjections = keyObjections;
        this.customerBackground = customerBackground;
        this.businessPainPoints = businessPainPoints;
        this.useCaseType = useCaseType;

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
        System.out.println("-Customer: " + displayCustomerName);
        System.out.println("-URL: " + url);
        System.out.println("-URList: " + urlList);
        System.out.println("-UseCase: " + usecase);
        System.out.println("-UseCase: " + businessUsecase);
        System.out.println("-docHandle: " + docHandle);
        System.out.println("-similarDocs: " + similarDocs);
        System.out.println("-docBody: " + docBody);

        System.out.println("-dealSize: " + dealSize);
        System.out.println("-Advantages: " + advantages);
        System.out.println("-keyObjections: " + keyObjections);
        System.out.println("-customerBackground: " + customerBackground);
        System.out.println("-businessPainPoints: " + businessPainPoints);
        System.out.println("-useCaseType: " + useCaseType);

    }
}
