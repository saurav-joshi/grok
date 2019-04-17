package com.iaasimov.data.repo;

import com.iaasimov.entity.UserProfile;

public interface UserProfileRepoCustom {
    public UserProfile findByUserIdCustom(String userId);
    public void saveUserProfile(UserProfile userProfile);
    public String getUserName(String userId);
}
