package com.iaasimov.data.repo;

import com.iaasimov.data.model.UserProfile;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserProfileRepo extends PagingAndSortingRepository<UserProfile, String> {
    public UserProfile findByUserId(String userId);
}
