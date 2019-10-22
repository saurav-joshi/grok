package com.iaasimov.entity;

import com.iaasimov.dao.WorkflowDao;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.workflow.GeoCalculator;
import com.iaasimov.entityextraction.LocationFromAddress;
import com.iaasimov.workflow.GlobalConstants;
import org.apache.commons.collections.CollectionUtils;
import scala.Tuple2;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Conversation {

    int id;
    List<QA> qaList = new ArrayList<>();
    String userId;
    int numOfShowMore;
    int numOfShowMoreHasResult;
    int bookingRestaurantId =0;
    boolean isActivateBooking = false;
    String eMail;

    public String getUserEmail() {
        return eMail;
    }

    public void setUserEmail(String eMail) {
        this.eMail = eMail;
    }

    static Map<String, List<ResultSet>> similarDomain = new HashMap<String, List<ResultSet>>();

    public Map<String, List<ResultSet>> getSimilarQuestionsforDomain() {
        return similarDomain;
    }

    public void setSimilarQuestionsforDomain(String k, List<ResultSet> s) {
        this.similarDomain.put(k, s);

    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    private String domain;

    public boolean isSimilar() {
        return isSimilar;
    }

    public void setSimilar(boolean similar) {
        isSimilar = similar;
    }

    boolean isSimilar;

    public boolean isPreProcess() {
        return isPreProcess;
    }

    public void setPreProcess(boolean preProcess) {
        isPreProcess = preProcess;
    }

    boolean isPreProcess = false;

    public boolean isActivateBooking() {
        return isActivateBooking;
    }

    public void setActivateBooking(boolean activateBooking) {
        isActivateBooking = activateBooking;
    }

    public int getNumOfShowMoreHasResult() {
        return numOfShowMoreHasResult;
    }

    public void setNumOfShowMoreHasResult(int numOfShowMoreHasResult) {
        this.numOfShowMoreHasResult = numOfShowMoreHasResult;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    String userName;

    String eMailName;

    boolean isLocationExpand;

    boolean isBookingExpired;

    public Conversation() {

    }

    public String getEMailName() {
        return eMailName;
    }

    public void setEMailName(String eMailName) {
        this.eMailName = "<span class=userName>" + eMailName + "</span>";
    }

    public String getLatestQueston() {
        return getLatestQA().getQuestion();
    }
    public String getOriginalQuestion() {
        return getLatestQA().getOriginalQuestion();
    }

    public Answer getLatestAnswer() {
        return getLatestQA().getAnswer();
    }

    public List<String> getLatestStatePaths() {
        return getLatestQA().getStatePaths();
    }

    public List<EntityExtractionUtil.EntityExtractionResult> getLatestEntities() {
        return getLatestQA().getEntities();
    }

    public QA getLatestQAWhichIncludeStateTypes(List<String> statetypes) {
        return getLatestQAWhichIncludeStateTypes(statetypes, false, false, null);
    }

    public QA getLatestQAWhichIncludeStateTypes(List<String> statetypes, boolean withinCurrentSession, boolean continous, List<String> skipStates) {
        QA lastQA = null;
        List<QA> qaListToSearchFor = this.getQaList();
        if(withinCurrentSession){
            qaListToSearchFor = WorkflowDao.getInstance().getQACurrentSession(this.getQaList());
        }
        for (int i = qaListToSearchFor.size() - 2; i > -1; i--) {
            if (CollectionUtils.intersection(qaListToSearchFor.get(i).getStatePaths(), statetypes).size() > 0) {
                lastQA = qaListToSearchFor.get(i);
                break;
            }else if(continous){
                if(CollectionUtils.intersection(qaListToSearchFor.get(i).getStatePaths(), skipStates).size() <= 0){
                    break;
                }
            }
        }
        return lastQA;
    }

    public QA getLatestQAWhichIncludeSuggestion(String suggestionType) {
        QA lastQA = null;
        for (int i = this.getQaList().size() - 2; i > -1; i--) {
            Suggestion suggestioni = getQaList().get(i).getAnswer().getSuggestion();
            if(suggestioni != null && suggestioni.getType() != null && suggestioni.getType().equalsIgnoreCase(suggestionType)){
                lastQA = getQaList().get(i);
                break;
            }
        }
        return lastQA;
    }

    public QA getLatestQAByStateTypes(List<String> statetypes) {
        QA lastQA = null;
        for (int i = this.getQaList().size() - 2; i > -1; i--) {
            if(getQaList().get(i).getStatePaths().toString().equalsIgnoreCase(statetypes.toString())){
                lastQA = getQaList().get(i);
                break;
            }
        }
        return lastQA;
    }

    public boolean isLastQAWithinTolerantTime(){
        QA latestQA = this.getQaList().size() >= 1 ? this.getLatestQA() : null;
        QA secondLastQA = this.getQaList().size() >= 2 ? this.getQaList().get(this.getQaList().size()-2) : null;
        return isLastQAWithinTolerantTime(latestQA,secondLastQA);
    }

    public boolean isLastQAWithinTolerantTime(QA qa1, QA qa2){
        if(qa1 == null || qa2 == null){
            return false;
        }
        long duration = qa1.getTime().getTime() - qa2.getTime().getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        if(diffInMinutes < GlobalConstants.refineQuestionToleranceInterval)
            return true;
        else
            return false;
    }

    public List<String> getRecommendRestaurantIdInShowMore(){
        List<String> ids = new ArrayList<>();
        for (int i = this.getQaList().size() - 2; i > -1; i--) {
            QA qa = this.getQaList().get(i);
            if (qa.getStatePaths().contains("ShowMoreState")){
                ids.addAll(qa.getRecommendRestaurantIds());
            }
            else {
                ids.addAll(qa.getRecommendRestaurantIds());
                break;
            }
        }
        return ids;
    }

    public Conversation(String question) {
        QA qa = new QA();
        qa.setQuestion(question);
        qa.setTime(new Date());
        addQA(qa);
    }

    public List<QA> getQaList() {
        return qaList;
    }

    public void setQaList(List<QA> qaList) {
        this.qaList = qaList;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public QA getLatestQA() {
        return qaList.get(qaList.size() - 1);
    }
    public QA getLastQA() {
        return qaList.get(qaList.size() - 2);
    }
    public String currentState(){
        int currStateCount = getLatestQA().getStatePaths().size() - 1 ;
        return getLatestQA().getStatePaths().get(currStateCount);
    }

    public String getLastKnownCityByGeo() {
        String city = "";
        for(int i= qaList.size()-1 ; i >= 0; i--){
            QA qa = qaList.get(i);
            if(qa.getGeo()!=null){
                city = GeoCalculator.getCityFromLatLongOpenMap(qa.getGeo().split(",")[0], qa.getGeo().split(",")[1]);
                break;
            }
        }
        return city;
    }

    public Conversation addQA(QA qa) {
        this.getQaList().add(qa);
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumOfShowMore() {
        return numOfShowMore;
    }

    public void setNumOfShowMore(int numOfShowMore) {
        this.numOfShowMore = numOfShowMore;
    }

    public boolean getLocationExpand() {
        return isLocationExpand;
    }

    public void setLocationExpand(boolean locationExpand) {
        isLocationExpand = locationExpand;
    }

    public int getBookingRestaurantId() {
        return bookingRestaurantId;
    }

    public void setBookingRestaurantId(int bookingRestaurantId) {
        this.bookingRestaurantId = bookingRestaurantId;
    }

    public boolean isLocationExpand() {
        return isLocationExpand;
    }

    public boolean isBookingExpired() {
        return isBookingExpired;
    }

    public void setBookingExpired(boolean bookingExpired) {
        isBookingExpired = bookingExpired;
    }

    public String getCurrentCityOfUser(){
        String city = "";
        try{
            List<EntityExtractionUtil.EntityExtractionResult> latestEntities = getLatestEntities();
            //1st priority is city was mentioned explicitly, even more than one, choose the first one, open for disambiguous optimization
            if(latestEntities.stream().filter(x -> x.getEntityName().contains("city")).count()>0){
                EntityExtractionUtil.EntityExtractionResult cityEntity = latestEntities.stream().filter(x -> x.getEntityName().contains("city")).findFirst().get();
                return String.join(" ", cityEntity.getEntityValue());
            }
            String geo = getLatestQA().getGeo();
            boolean validGeo = (geo != null && geo.split(",").length == 2);
            if (validGeo) {
                city = GeoCalculator.getCityFromLatLongOpenMap(getLatestQA().getGeo().split(",")[0], getLatestQA().getGeo().split(",")[1]);
            }

            //2nd priority is to infer from mentioned location, even more than one, choose the first one, open for disambiguous optimization
            List<EntityExtractionUtil.EntityExtractionResult> locationEntities = latestEntities.stream().filter(x -> (x.getEntityName().equals("#location")
                    || x.getEntityName().equals("$location"))).collect(Collectors.toList());//ignore $refinelocation entities like "closer", "nearer"...
            if (locationEntities != null && locationEntities.size() > 0) {
                EntityExtractionUtil.EntityExtractionResult locationEntity = locationEntities.stream().findFirst().get();
                //remove preposition like in/near/at...
                String mentionedLocation = String.join(" ", locationEntity.getEntityValue()).replaceAll("^\\b(in|on|near|within|along|close to|at|around|next to|across|to)\\b", "").trim();
                List<Tuple2<String, String>> cityCountries = LocationFromAddress.getCityAndCountryByLocation(mentionedLocation);
                if (cityCountries != null ) {
                    if(cityCountries.size()==1 || cityCountries.stream().allMatch(x -> x._1().equalsIgnoreCase(cityCountries.get(0)._1())))
                        return cityCountries.get(0)._1();

                    if(cityCountries.size()>1){
                        if (validGeo) {
                            String geoCity = city;
                            if(cityCountries.stream().anyMatch(x -> x._1().equalsIgnoreCase(geoCity))){
                                return cityCountries.stream().filter(x -> x._1().equalsIgnoreCase(geoCity)).findFirst().get()._1();
                            }else{
                                return String.join("||",cityCountries.stream().map(x -> x._1()).distinct().collect(Collectors.toList()));
                            }
                        }else{
                            return String.join("||",cityCountries.stream().map(x -> x._1()).distinct().collect(Collectors.toList()));
                        }
                    }
                }
            }
            if(city.isEmpty()){//try last known geo
                return getLastKnownCityByGeo();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return city.toLowerCase();
    }
}


