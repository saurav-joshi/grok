package com.iaasimov.state;

import com.iaasimov.entity.Conversation;
import com.iaasimov.entityextraction.EntityExtraction;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.workflow.FlowManagement;
import com.iaasimov.entity.QA;
import com.iaasimov.entityextraction.DistanceExtraction;
import com.iaasimov.workflow.LibraryUtil;
import com.iaasimov.recommender.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ResultState extends State {

    @Override
    public void setStateType() {
        this.stateType = "ResultState";
    }

    @Override
    public String process(Conversation con) {

        String numResults = String.valueOf(con.getLatestAnswer().getResultIaaSimov().size());
        String message = " ";

        if(!con.isSimilar() && con.getLatestQA().getMatchedPattern().getQuestionType().contains("CustomerQuery.ClassMember.Count"))
        {

            //List<String> entityList = Arrays.asList("$product", "$region", "$industry" );
            List<String> entityList = Arrays.asList("$product");
            List<EntityExtractionUtil.EntityExtractionResult> u = con.getLatestEntities().stream()
            //List<String> u = con.getLatestEntities().stream()
                               .filter(x->entityList.contains(x.getEntityName()))
                               //.map(EntityExtractionUtil.EntityExtractionResult::getEntityValue)
                               .collect(toList() );

            List<String> s = u.stream().map(x->x.getEntityValue()[0]).collect(toList());


            String messagePattern = LibraryUtil.getRandomPatternByQuestionClass("Result.Count")
                                        .getSystemMessage()
                                        .replace("%Number", numResults);
                                        //.replace("%1", s.toString());
            con.getLatestQA().getAnswer().setMessage(messagePattern);
            con.getLatestQA().getAnswer().setResultIaaSimov(null);

        }

        if (con.getLatestAnswer().getMessage() != null) {
            return "EndState";
        }

        //if (conversation.getLatestAnswer().getResultRestaurants() == null) {
        if (con.getLatestAnswer().getResultIaaSimov() == null) {
            if (con.getNumOfShowMore() > 0) {
                String messagePattern = LibraryUtil.getRandomPatternByQuestionClass("Result.ShowMore.NoMoreResult.4").getSystemMessage();
                int numOfShowMoreNoResult = con.getNumOfShowMore() - con.getNumOfShowMoreHasResult();
                System.out.println("#show more no result" + numOfShowMoreNoResult);
                if (1 <= numOfShowMoreNoResult & numOfShowMoreNoResult <= 3) {
                    messagePattern = LibraryUtil.getRandomPatternByQuestionClass("Result.ShowMore.NoMoreResult." + numOfShowMoreNoResult).getSystemMessage();
                }
                con.getLatestAnswer().setMessage(messagePattern);
            } else return "ExpectationManagementState";
        //} else if (conversation.getLatestAnswer().getResultRestaurants().size() < Constants.DEFAULT_PAGE_SIZE) {
        }   else if (con.getLatestAnswer().getResultIaaSimov().size() < Constants.DEFAULT_PAGE_SIZE) {
            //String numResults = String.valueOf(conversation.getLatestAnswer().getResultRestaurants().size());

            String messagePattern = con.currentState().equalsIgnoreCase("SalesHelpState") ? message: LibraryUtil.getRandomPatternByQuestionClass("Result.Single")
                                                                                               .getSystemMessage()
                                                                                               .replace("%Number", numResults);
            con.getLatestAnswer().setMessage(messagePattern);
        } else if (con.getLatestStatePaths().contains("UserRefineState")) {
            String messagePattern = con.currentState().equalsIgnoreCase("SalesHelpState") ? message : LibraryUtil.getRandomPatternByQuestionClass("Result.Refine")
                                                                                                .getSystemMessage();
            String filledMessage = createMessageForUserRefine(messagePattern, con);
            con.getLatestAnswer().setMessage(String.join(" ", filledMessage));
        } else {

            String preMess = "";
            QA secondLastQA = con.getQaList().size() < 2 ? null : con.getQaList().get(con.getQaList().size() - 2);

            String messagePattern = con.currentState().equalsIgnoreCase("SalesHelpState") ? message : LibraryUtil.getRandomPatternByQuestionClass("Result.General")
                                                                                                .getSystemMessage()
                                                                                                .replaceAll("%Number", numResults);
            if (con.getLocationExpand()) {
                messagePattern = LibraryUtil.getRandomPatternByQuestionClass("Result.ShowMore.LocationBased").getSystemMessage();
                con.setLocationExpand(false);
            }
            con.getLatestQA().getAnswer().setMessage(preMess + messagePattern);
        }
        return "EndState";
    }

    public String createMessageForUserRefine(String messagePattern, Conversation con) {
        List<String> result = new ArrayList<>();
        System.out.println(messagePattern);
        List<EntityExtractionUtil.EntityExtractionResult> entities = con.getLatestEntities();
        System.out.println(entities);
        Arrays.stream(messagePattern.split("\\s+")).forEach(w -> {
            if (w.equals("#RestaurantEntity")) {
                String e = entities.stream().filter(x -> !x.getEntityName().equals("$location") &&
                        !x.getEntityName().equals("#location") &&
                        !x.getEntityName().equals("$refinelocation") &&
                        !x.getEntityName().equals("$actionword") &&
                        !x.getEntityName().equals("$offer") &&
                        !x.getEntityName().equals("$distance"))
                        .map(x -> {
                            if(x.getEntityName().equals("#cuisine") || x.getEntityName().equals("$cuisine")){
                                return String.join(" ", x.getEntityValue()).substring(0, 1).toUpperCase() + String.join(" ", x.getEntityValue()).substring(1);
                            }
                           return String.join(" ", x.getEntityValue());
                        }).collect(Collectors.joining(",")).replace("restaurant", "");
                if (e.indexOf("low") != -1) e = "cheaper";
                if (e.indexOf("high") != -1) e = "nicer";
                //if similar
                if (con.getLatestQA().getMatchedPattern().getQuestionType().contains("ConsumerQuery.SimilarServices")) {
                    e = "similar to " + e;
                }
                result.add(e);
            } else if (w.toLowerCase().contains("#location") || w.toLowerCase().contains("$location")) {
                String location = "";
                if (entities.stream().anyMatch(x -> x.getEntityName().equals("#location"))) {
                    location = entities.stream().filter(x -> x.getEntityName().equals("#location"))
                            .map(x -> {
                                String locationValue = FlowManagement.contextToEntityMapping.getOrDefault(String.join(" ", x.getEntityValue()),String.join(" ", x.getEntityValue()));
                                locationValue = locationValue.substring(0,1).toUpperCase() + locationValue.substring(1);
                                return String.join(" ", x.getEntityValue()).replaceAll(locationValue.toLowerCase(), locationValue);
                            }).collect(Collectors.joining(","));
                } else {
                    if (entities.stream().anyMatch(x -> x.getEntityName().equals("$location"))) {
                        location = "in " + entities.stream().filter(x -> x.getEntityName().equals("$location"))
                                .map(x -> String.join(" ", x.getEntityValue()).substring(0, 1).toUpperCase() + String.join(" ", x.getEntityValue()).substring(1))
                                .collect(Collectors.joining(","));
                    }
                }
                if (location.length() > 1) {
                    result.add(location);
                }

            } else if (w.toLowerCase().contains("$distance")) {
                if (entities.stream().anyMatch(x -> x.getEntityName().equals("$distance"))) {
                    String distanceText = entities.stream().filter(x -> x.getEntityName().equals("$distance"))
                            .map(x -> String.join(" ", x.getEntityValue())).collect(Collectors.joining(","));
                    String distanceNum = DistanceExtraction.getInstance().getFormattedDistanceFromText(distanceText);
                    String distanceValue = null;
                    try {
                        distanceValue = Double.valueOf(distanceNum) >= 1 ? distanceNum + "km." : String.valueOf((int) Math.round(1000.0 * Double.valueOf(distanceNum))) + "m.";
                    } catch (Exception e) {
                        System.out.println("Wrong distance value.");
                        e.printStackTrace();
                    }
                    if (distanceValue != null && distanceValue.length() > 1) {
                        result.add("within " + distanceValue);
                    }
                }else{
                    result.add(".");
                }
            } else {
                result.add(w);
                if (w.contains("restaurant")) {
                    if (entities.stream().anyMatch(x -> x.getEntityName().equals("$offer"))) {
                        result.add(" with offer ");
                    }
                }
            }

        });
        System.out.println(result);
        return result.stream().collect(Collectors.joining(" ")).replaceAll("\\s+\\.",".");
    }

    @Override
    public void allowedInputStateTypes() {
        allowedInputStateTypes.add("StartState");
        allowedInputStateTypes.add("SystemGreetingState");
        allowedInputStateTypes.add("UserGreetingState");
    }

    public boolean guardCheck(Conversation con) {
        return true;
    }
}
