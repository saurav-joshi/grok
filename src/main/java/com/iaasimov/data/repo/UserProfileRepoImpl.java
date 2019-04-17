package com.iaasimov.data.repo;

import com.iaasimov.data.model.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class UserProfileRepoImpl implements UserProfileRepoCustom {
    @Autowired
    UserProfileRepo userProfileRepo;

    @Override
    public com.iaasimov.entity.UserProfile findByUserIdCustom(String userId){
        com.iaasimov.entity.UserProfile userProfile = new com.iaasimov.entity.UserProfile();
        try{
            UserProfile originalUserProfile = userProfileRepo.findByUserId(userId);
            userProfile.setUserId(originalUserProfile.getUserId());
            userProfile.setName(originalUserProfile.getName());

        }catch (Exception e){
            return null;
        }
        return userProfile;
    }

    @Override
    public void saveUserProfile(com.iaasimov.entity.UserProfile userProfile){
        UserProfile userProfileFinal = new UserProfile();
        userProfileFinal.setUserId(userProfile.getUserId());
        userProfileFinal.setName(userProfile.getName());

        userProfileRepo.save(userProfileFinal);
    }

    @Override
    public String getUserName(String userId){
        UserProfile originalUserProfile = userProfileRepo.findByUserId(userId);
        return originalUserProfile.getName();
    }

    private String listToString(List<String> alist){
        return String.join(",", alist);
    }

    private String mapToString(Object map2Convert){
        String resultStr = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            resultStr = mapper.writeValueAsString(map2Convert);
        } catch (IOException e) {
            System.out.println("Exception when converting map to string");
            e.printStackTrace();
        }
        return resultStr;
    }

    private Object stringToMap(String strToConvert){
        Object resultObj = null;
        if(strToConvert != null && strToConvert.trim().length() != 0){
            ObjectMapper mapper1 = new ObjectMapper();
            try {
                resultObj = mapper1.readValue(strToConvert, Map.class);
            } catch (Exception e) {
                System.out.println("Exception when converting string to map");
                e.printStackTrace();
            }
        }
        return resultObj;
    }


}
