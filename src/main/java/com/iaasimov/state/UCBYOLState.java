package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.QA;
import com.iaasimov.entity.ResultSet;
import com.iaasimov.entity.SemanticStoreQuery;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.recommender.Recommender;

import java.util.ArrayList;
import java.util.List;

public class UCBYOLState extends State {
    public void setStateType() {this.stateType = "UCBYOLState"; }

    public void allowedInputStateTypes() {
        allowedInputStateTypes.add("SrartState");
    }

    public String process(Conversation con) {

        System.out.println("CAME HERE");

        int e = con.getQaList().size() -1;
        String q =  con.getQaList().get(e).getOriginalQuestion();
        SemanticStoreQuery query = Recommender.createRecommenderQuery(con.getLatestQA().getMatchedPattern().getQuestionType(), con.getLatestEntities(), null, 1, true, q);
        List<ResultSet> result = Recommender.getInstance().getRecommendationResults(query, null);



        con.getLatestQA().getAnswer().setResultIaaSimov(result);
        return "ResultState";
    }

    public boolean guardCheck(Conversation con){

        return true;
    }

}
