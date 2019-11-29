package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.ResultSet;
import com.iaasimov.entity.SemanticStoreQuery;
import com.iaasimov.recommender.Recommender;

import java.util.List;
import java.util.Set;

public class AirlinesState extends State {

    public void setStateType() {this.stateType = "AirlinesState"; }

    public void allowedInputStateTypes() {
        allowedInputStateTypes.add("StartState");
    }

    public String process(Conversation con) {

        Set<String>questionType =  con.getLatestQA().getMatchedPattern().getQuestionType();

        SemanticStoreQuery query = Recommender.createRecommenderQuery(questionType, con.getLatestEntities(), null, 1,false, " ");
        List<ResultSet> result = Recommender.getInstance().getRecommendationResults(query, null);
        con.getLatestQA().getAnswer().setResultIaaSimov(result);

        con.getLatestQA().getAnswer().setHighlightTerms(con.getLatestQA().highlightTerms());

        return "ResultState";
    }

    public boolean guardCheck(Conversation con){

        return true;
    }
}
