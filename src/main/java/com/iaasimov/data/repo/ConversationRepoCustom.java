package com.iaasimov.data.repo;

import com.iaasimov.entity.Conversation;

public interface ConversationRepoCustom {
    public Conversation findCustomConByUserId(String userId);
}
