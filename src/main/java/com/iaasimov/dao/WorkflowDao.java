package com.iaasimov.dao;

import com.iaasimov.entity.*;
import com.iaasimov.entityextraction.EntityExtractionUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iaasimov.workflow.GlobalConstants;
import com.iaasimov.workflow.GlobalConstantsNew;
import org.apache.commons.collections.map.HashedMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class WorkflowDao {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    static int totalColumns = 13;//end index of entities column
    static Map<String, Integer> mappingColumn = new HashedMap();
    private static WorkflowDao workflowDao = null;
    private static final String tableName = "workflow";

    static {
        int index = 7;
        for (GlobalConstants.Entity entity : GlobalConstants.Entity.values()) {
            String columnName = entity.name();
            mappingColumn.put(columnName, index++);
        }
    }

    public static WorkflowDao getInstance() {
        if (workflowDao == null) {
            workflowDao = new WorkflowDao();
        }
        return workflowDao;
    }

    public List<QA> getQAByConversation(int conversation_id) {
        List<QA> qaList = new ArrayList<>();
        String query = "SELECT * FROM " + tableName + " WHERE conversation_id =" + conversation_id;
        try {
            Connection connection = MySQL.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                QA qa = new QA();
                int qaId = rs.getInt("workflow_id");
                String question = rs.getString("question");
                String originalQuestion = rs.getString("originalQuestion");
                qa.setId(qaId);
                qa.setQuestion(question);
                qa.setOriginalQuestion(originalQuestion);
                qa.setConversation_id(conversation_id);
                //city
                String city = rs.getString("city");
                qa.setCity(city);

                String message = rs.getString("answer");
                List<ResultSet> resultSet = new ArrayList<>();
                String choice = rs.getString("choice").trim();
                if(!choice.isEmpty()){
                    String[] choices = choice.split("\\|\\|");
                    for (String choiceId : choices) {//choice id
                      resultSet.add(new ResultSet(choiceId));
                    }
                }

                // get the suggestion here
                String suggestionStr = rs.getString("suggestion");
                Suggestion suggestion = null;
                if(suggestionStr != null && suggestionStr.trim().length() !=0){
                    Map<String, Object> suggestionMap = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        suggestionMap = mapper.readValue(suggestionStr, Map.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String suggestionMessage = (String) suggestionMap.get("message");
                    List<String> suggestionOption = (List<String>) suggestionMap.get("options");
                    String suggestionType = (String) suggestionMap.get("type");
                    suggestion = new Suggestion(suggestionMessage,suggestionOption,suggestionType);
                }

                Answer answer = new Answer();
                answer.setMessage(message);
                answer.setResultIaaSimov(resultSet);
                answer.setSuggestion(suggestion);
                qa.setAnswer(answer);

                String timestamp = rs.getString("timestamp");
                qa.setTime(dateFormat.parse(timestamp));

                List<EntityExtractionUtil.EntityExtractionResult> entityExtractionResults = new ArrayList<>();

                Map<String, List<String>> mapEntites = new HashedMap();
                for (GlobalConstants.Entity entity : GlobalConstants.Entity.values()) {
                    String value = rs.getString(entity.name());
                    String name = entity.name();

                    if(value!=null && !value.isEmpty()){
                        mapEntites.put(name, Arrays.asList(value.split("\\|\\|")));
                    }
                }

                mapEntites.forEach((name,value) -> {
                    if(value!=null && !value.isEmpty()){
                        value.forEach(entity -> {
                            EntityExtractionUtil.EntityExtractionResult entityExtractionResult = new EntityExtractionUtil.EntityExtractionResult(name, entity);
                            entityExtractionResults.add(entityExtractionResult);
                        });
                    }
                });
                if(!entityExtractionResults.isEmpty()){
                    qa.setEntities(entityExtractionResults);
                }

                qa.setGeo(rs.getString("geo"));
                qa.setMatchedPatternIdInLibrary(rs.getInt("library_id"));
                //System.out.println("Library: " + rs.getInt("library_id"));
                //state
                List<String> stateTypes = new ArrayList<>();
                String[] states = rs.getString("states").split(",");
                if(states!=null){
                    for (String state : states) {
                        String stateType = getState(state);
                        if(stateType!=null){
                            stateTypes.add(stateType);
                        }
                    }
                }
                qa.setStatePaths(stateTypes);
                qaList.add(qa);
            }
            ps.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error getting Q/A: " + e.getMessage());
            e.printStackTrace();
        }
        return qaList;
    }

    public List<QA> getQACurrentSession(int conversation_id) {
        List<QA> qaCurrentSession = new ArrayList<>();
        try {
            qaCurrentSession = getQACurrentSession(getQAByConversation(conversation_id));
        } catch (Exception e) {
            System.out.println("Error getting Q/A current session: " + e.getMessage());
            e.printStackTrace();
        }
        return qaCurrentSession;
    }

    public List<QA> getQACurrentSession(List<QA> qaList) {
        List<QA> qaCurrentSession = new ArrayList<>();
        try {
            //get latest session only
            int index = qaList.size()-1;
            for(index= qaList.size()-1; index>0; index=index-1){
                QA q1 = qaList.get(index);
                QA q2 = qaList.get(index-1);
                long duration = q1.getTime().getTime() - q2.getTime().getTime();
                long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
                if(diffInMinutes<GlobalConstants.sessionInterval) continue;
                else {
                    break;
                }
            }
            //get from the index
            for(int i = index; i< qaList.size(); i++){
                qaCurrentSession.add(qaList.get(i));
            }
        } catch (Exception e) {
            System.out.println("Error getting Q/A: " + e.getMessage());
            e.printStackTrace();
        }
        return qaCurrentSession;
    }

    public void insertListQA(Conversation conversation) {
        try {
            Connection connection = MySQL.getConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT INTO " + tableName +
                    " VALUES( " + "?, ?, ?,?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?,?,?)");
            int conversationId = conversation.getId();

            //insert the newest

            QA qa = conversation.getLatestQA();
            int qaId = qa.getId();
            String question = qa.getQuestion();
            String answer = qa.getAnswer().getMessage();
            List<String> resultIds = new ArrayList<>();
            List<ResultSet> results = qa.getAnswer().getResultIaaSimov();
            if(results != null)
              results.forEach(r -> {
                resultIds.add(r.getId());
              });
            String choice = String.join("||", resultIds);
            String suggestion = qa.getAnswer().getSuggestion() == null ? null : qa.getAnswer().getSuggestion().toString();
            String timestamp = dateFormat.format(qa.getTime());

            ps.setInt(1, qaId);
            ps.setInt(2, conversationId);
            ps.setString(3, question);
            ps.setString(4, answer);
            ps.setString(5, choice);
            ps.setString(6, timestamp);

            int index = 7;
            //Default value
            for (int i = 7; i <= totalColumns; i++) {
                ps.setString(i, "");
            }
            List<EntityExtractionUtil.EntityExtractionResult> entities = qa.getEntities();
            Map<String, List<String>> mapEntites = new HashedMap();
            if (entities != null) {
                for (EntityExtractionUtil.EntityExtractionResult entity : entities) {
                    String name = entity.getEntityName().replace("#", "");
                    String value = String.join(" ", entity.getEntityValue());
                    //ps.setString(mappingColumn.get(name), value);
                    List<String> values = mapEntites.get(name);
                    if (values == null) {
                        values = new ArrayList<>();
                    }
                    values.add(value);
                    mapEntites.put(name, values);
                }
            }
            mapEntites.forEach((name, value) ->{
                try{
                    if(mappingColumn.containsKey(name)){
                        ps.setString(mappingColumn.get(name), String.join("||",value));
                    }
                }catch (SQLException e){
                }
            });
            ps.setString(14, qa.getGeo()); //add geo passed via API
            ps.setInt(15, qa.getMatchedPatternIdInLibrary()); //add matched libary id
            ps.setString(16, "");// add state path
            if(!qa.getStatePaths().isEmpty()){
                StringBuilder sb = new StringBuilder();
                qa.getStatePaths().stream().forEach(stateType -> {
                    sb.append(",").append(stateType);
                });
                ps.setString(16, sb.toString().substring(1));
            }

            ps.setString(17, suggestion);
            ps.setString(18, qa.getOriginalQuestion());
            ps.setString(19, qa.getCity());
            ps.execute();
            ps.close();
            connection.close();

        } catch (Exception e) {
            System.out.println("Error updating workflow: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getState(String state){
        for (String stateType : GlobalConstantsNew.getInstance().iaaSimovStates) {
            if(stateType.equalsIgnoreCase(state)){
                return stateType;
            }
        }
        return null;
    }

}
