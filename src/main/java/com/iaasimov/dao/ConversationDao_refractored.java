package com.iaasimov.dao;

import com.iaasimov.data.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Saurav on 03-06-2017.
 */
public interface ConversationDao_refractored extends JpaRepository<Conversation, Long> {

}
