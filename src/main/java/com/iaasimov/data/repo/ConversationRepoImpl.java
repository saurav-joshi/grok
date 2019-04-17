package com.iaasimov.data.repo;

import com.iaasimov.dao.WorkflowDao;
import com.iaasimov.entity.Conversation;
import org.springframework.beans.factory.annotation.Autowired;

public class ConversationRepoImpl implements ConversationRepoCustom{
    @Autowired
    ConversationRepo conversationRepo;
    @Autowired
    UserProfileRepoCustom userProfileRepoCustom;
    @Override
    public Conversation findCustomConByUserId(String userId) {
        Conversation conversation = new Conversation();
        try{
            int conversationId = (int) conversationRepo.findByUserProfileUserId(userId).getConversationId();
            conversation.setQaList(WorkflowDao.getInstance().getQAByConversation(conversationId));
            conversation.setId(conversationId);
            conversation.setUserId(userId);
            conversation.setUserName(userProfileRepoCustom.getUserName(userId));
        }catch (Exception e){

            return null;
        }
        return conversation;
    }
}
