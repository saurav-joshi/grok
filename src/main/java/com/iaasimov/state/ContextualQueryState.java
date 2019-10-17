package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.QA;
import com.iaasimov.entity.ResultSet;
import com.iaasimov.entity.SemanticStoreQuery;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.recommender.Recommender;
import com.iaasimov.workflow.LibraryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ContextualQueryState extends State {

    public void setStateType() {this.stateType = "ContextualQueryState"; }

    public void allowedInputStateTypes() {
        allowedInputStateTypes.add("CustomerQueryState");
    }

    public void merrgeEntities(Conversation con){
        //Entity consolidation
        QA lastQ = con.getLastQA();
        List<EntityExtractionUtil.EntityExtractionResult> newEntitiesToAdd = new ArrayList<>();
        List<EntityExtractionUtil.EntityExtractionResult> currentEntities = con.getLatestQA().getEntities();
        for(EntityExtractionUtil.EntityExtractionResult oldEntity : lastQ.getEntities()){
            if(currentEntities.stream().filter(x->x.getEntityName().equals(oldEntity.getEntityName())).count() <= 0){
                newEntitiesToAdd.add(oldEntity);
            }
        }
        currentEntities.addAll(newEntitiesToAdd);
        con.getLatestQA().setEntities(currentEntities);
        LibraryUtil.Pattern lastPattern = lastQ.getMatchedPattern();
        con.getLatestQA().setMatchedPattern(lastPattern);
    }

    public String process(Conversation con) {

        Set<String>questionType =  con.getLatestQA().getMatchedPattern().getQuestionType();

        QA lastQ = con.getLastQA();
        //if (lastQ.getMatchedPattern() == null || lastQ.getMatchedPattern().getLibraryName() != "CustomerQueryState"){
        if (lastQ.getMatchedPattern() == null || !lastQ.getMatchedPattern().getLibraryName().equalsIgnoreCase( "CustomerQueryState")){
            return "ExpectationManagementState";
        }

        // To Do :: This may be done while assigning states Perform a partial match when customer names are too big

        //Check for targetFields before merging Entity...
        String targetFields = con.getLatestQA().highlightTerms();

        merrgeEntities(con);

        SemanticStoreQuery query = Recommender.createRecommenderQuery(questionType, con.getLatestEntities(), null, 1,false, " ");
        List<ResultSet> result = Recommender.getInstance().getRecommendationResults(query, null);
        con.getLatestQA().getAnswer().setResultIaaSimov(result);

        String highlightTerms = targetFields !=null ? targetFields:"customerBackground";
        con.getLatestQA().getAnswer().setHighlightTerms(targetFields);

        return "ResultState";
    }

    public boolean guardCheck(Conversation con){

        return true;
    }
}
