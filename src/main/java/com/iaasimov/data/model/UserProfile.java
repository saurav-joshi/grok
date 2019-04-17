package com.iaasimov.data.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "userprofile")
public class UserProfile {
	@Id
	@Column(name = "user_id")
	private String userId;
	private String name;
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "userProfile",cascade={CascadeType.ALL})
	private Set<Conversation> conversations;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Set<Conversation> getConversations() {
		return conversations;
	}
	public void setConversations(Set<Conversation> conversations) {
		this.conversations = conversations;
	}


}
