package com.iaasimov.data.repo;

import com.iaasimov.dao.WorkflowDao;
import com.iaasimov.entity.Conversation;
import com.iaasimov.entity.QA;
import com.iaasimov.entityextraction.EntityExtractionUtil;
import com.iaasimov.repository.WorkflowRepository;
import com.iaasimov.tables.Workflow;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConversationRepoImpl implements ConversationRepoCustom {
    @Autowired
    ConversationRepo conversationRepo;
    @Autowired
    UserProfileRepoCustom userProfileRepoCustom;
    @Autowired
    WorkflowRepository workFlowRepo;

    @Override
    public Conversation findCustomConByUserId(String userId) {
        Conversation conversation = new Conversation();
        try {
            int conversationId = (int) conversationRepo.findByUserProfileUserId(userId).getConversationId();
            conversation.setQaList(WorkflowDao.getInstance().getQAByConversation(conversationId));
            conversation.setId(conversationId);
            conversation.setUserId(userId);
            conversation.setUserName(userProfileRepoCustom.getUserName(userId));
        } catch (Exception e) {

            return null;
        }
        return conversation;
    }

    public Conversation findConversationbyEmail(String email) {
        List<Workflow> lwFlow = workFlowRepo.findByuserEmail(email);
        if (lwFlow.isEmpty()){
            return null;
        }

        Conversation con = new Conversation();
        List<QA> qaList = new ArrayList<QA>();
        int id = 0;

        for (Workflow w : lwFlow) {
            QA q = populateQA(w);
            qaList.add(q);
            id = (int) w.getConversationId();

        }
        con.setId(id);
        con.setQaList(qaList);
        return con;

    }

    private QA populateQA(Workflow w) {
        QA qa = new QA();
        qa.setQuestion(w.getQuestion());
        qa.setOriginalQuestion(w.getOriginalQuestion());
        qa.setAnswer(qa.getAnswer());
        qa.setId((int) w.getWorkflowId());
        qa.setConversation_id((int) w.getConversationId());
        qa.setCity(w.getCity());
        //qa.setTime(w.getTimeStamp());
        qa.setStatePaths(w.getStates());
        List<EntityExtractionUtil.EntityExtractionResult> entities = populateEntities(w);
        qa.setEntities(entities);

        return qa;
    }

    private List<EntityExtractionUtil.EntityExtractionResult> populateEntities(Workflow w) {

        String str = w.getEntityList();
        List<EntityExtractionUtil.EntityExtractionResult> entities = new ArrayList<>();
//        EntityExtractionUtil.EntityExtractionResult e = ((EntityExtractionUtil.EntityExtractionResult) Arrays.stream(str.split(" ")).map(s -> new EntityExtractionUtil.EntityExtractionResult(s.split("-")[0], s.split("-")[1])
//        ).collect(Collectors.toList()));
//        entities.add(e);

        String[] entityArr = str.split(" ");
        for(String s : entityArr){
            if (!s.contains("-"))
                continue;
            String[] arr = s.split("-");
            EntityExtractionUtil.EntityExtractionResult ee = new EntityExtractionUtil.EntityExtractionResult(arr[0], arr[1]);
            entities.add(ee);
        }
        return entities;
    }
}
