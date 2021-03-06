package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.ResultSet;
import com.iaasimov.entity.SemanticStoreQuery;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.recommender.Recommender;
import com.iaasimov.workflow.GlobalConstantsNew;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SalesHelpState extends State {

    public void setStateType() {this.stateType = "SalesHelpState"; }

    public void allowedInputStateTypes() {
        allowedInputStateTypes.add("StartState");
    }

    private boolean isManager(Conversation con){
        List<String > managersList = GlobalConstantsNew.getInstance().salesHelpManagers;
        boolean isManager=true;

        for(EntityExtractionUtil.EntityExtractionResult e : con.getLatestEntities()){
            String temp = String.join(" ", e.getEntityValue());
            if(temp.equalsIgnoreCase("assign service request")
                    || temp.equalsIgnoreCase("assign sales request")
                    && !managersList.contains(con.getUserEmail()))
                isManager =false;
        }
        return isManager;

    }

    private String nonManagerResponse(Conversation con){
        ResultSet r = new ResultSet("default");
        r.setDocBody("I am sorry but you are not a sales manager yet. You can however check your sales opportunities by clicking");
        //r.setUrlList(Collections.singletonList("https://eeho.fa.us2.oraclecloud.com/crmUI/faces/FuseOverview?_afrLoop=3704321858615609&fndGlobalItemNodeId=c_345c1f032aae4c5b99e3dc8294504608&fnd=%3B%3B%3B%3Bfalse%3B256%3B%3B%3B&_adf.ctrl-state=sgq52cv54_183\\"));
        r.setUrlList(Collections.singletonList("{\n\t\t\t\"url\": \"https://eeho.fa.us2.oraclecloud.com/crmUI/faces/FuseOverview?_afrLoop=3704321858615609&fndGlobalItemNodeId=c_345c1f032aae4c5b99e3dc8294504608&fnd=%3B%3B%3B%3Bfalse%3B256%3B%3B%3B&_adf.ctrl-state=sgq52cv54_183\",\n\"name\": \"Sales opportunities\",\n\"additionalInfo\": \"Click on <img src=\\\"img/func_fieldsfilter_16_ena.png\\\"> after login.\"\n}"));
        con.getLatestQA().getAnswer().setResultIaaSimov(Collections.singletonList(r));
        return "ResultState";

    }

    public String process(Conversation con) {
        if(!isManager(con))
            return nonManagerResponse(con);

        Set<String>questionType =  con.getLatestQA().getMatchedPattern().getQuestionType();
        SemanticStoreQuery query = Recommender.createRecommenderQuery(questionType, con.getLatestEntities(), null, 1,false, " ");
        List<ResultSet> result = Recommender.getInstance().getRecommendationResults(query, null);
        con.getLatestQA().getAnswer().setResultIaaSimov(result);

        return "ResultState";
    }

    public boolean guardCheck(Conversation con){

        return true;
    }
}
