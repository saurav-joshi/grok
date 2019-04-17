package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.ResultSet;
import com.iaasimov.entity.SemanticStoreQuery;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.recommender.Recommender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultState extends State {
    public void setStateType() {this.stateType = "DefaultState"; }

    public void allowedInputStateTypes() {
        allowedInputStateTypes.add("SrartState");
    }

    public String process(Conversation con) {


        // as a very simple case query the default failed. Default field itself can be queried in number of ways.
        //dumpy entire metadata to one field and query or apply some ML techniques to query guess the most appropiate field.
        // intelligent guessing may further require extension to existing algorithms..

        String entityName =con.isSimilar() ?"$domain" : "$ucq";
        if(con.isSimilar()){
           String q = Arrays.stream(con.getOriginalQuestion().split(" ")).skip(7).collect(Collectors.joining(" "));
           if(con.getSimilarQuestionsforDomain().get(q) != null && con.getSimilarQuestionsforDomain().get(q).size() >1) {
               List<ResultSet> toClient = new ArrayList<>(con.getSimilarQuestionsforDomain().get(q).subList(0,2));
               con.getSimilarQuestionsforDomain().get(q).subList(0,2).clear();
               List<ResultSet> r = con.getSimilarQuestionsforDomain().get(q);
               //ResultSet similarQ = r.remove(0);
               con.setSimilarQuestionsforDomain(q, r);

               //List<ResultSet> l = new ArrayList<>();
               //l.add(similarQ);
               con.getLatestQA().getAnswer().setResultIaaSimov(toClient);
               return "ResultState";
           }
           con.getLatestQA().setOriginalQuestion(q);
        }
        List<EntityExtractionUtil.EntityExtractionResult> entitySet= Stream.of(new EntityExtractionUtil.EntityExtractionResult(entityName,
                                                                     Stream.of(con.getOriginalQuestion()).toArray(String[]::new),
                                                                     0))
                                                                     .collect(Collectors.toList());
        //SimilarTo functionality requires refactoring and hence not using CustomerQuery.Similarto for
        // the moment.. this file and the functionality itself will undergo serious refractoring as
        //and when we extend the functionality
        String queryType = con.isSimilar()? "CustomerQuery.ClassMember":"CustomerQuery.ClassMember";
        SemanticStoreQuery query = Recommender.createRecommenderQuery(Stream.of(queryType).collect(Collectors.toSet()),
                                                                      entitySet, null,
                                                                      1, false,
                                                                      con.getOriginalQuestion());

        List<ResultSet> r = Recommender.getInstance().getRecommendationResults(query, null);
        List<ResultSet> result = r.stream().filter(x->!x.getId().equals("Question")).collect(Collectors.toList());


        con.getLatestQA().getAnswer().setResultIaaSimov(r);
        String changeState = (result == null || result.isEmpty()) ? "ExpectationManagementState":"ResultState";
        if(result !=null && con.isSimilar()) {
            List<ResultSet> toClient = new ArrayList<>(result.subList(0, 2));
            result.subList(0,2).clear();
//            ResultSet s = result.remove(0);
//            List<ResultSet> l = new ArrayList<>();
//            l.add(s);
            con.getLatestQA().getAnswer().setResultIaaSimov(toClient);
            con.setSimilarQuestionsforDomain(con.getLatestQA().getOriginalQuestion(), result);
        }
        return changeState;
    }

    public boolean guardCheck(Conversation con){

        return true;
    }

}
