package com.iaasimov.dao;

import com.iaasimov.entity.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.LinkedMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class UserProfileDao {
    private static UserProfileDao userProfileDao = null;
    String tableName = "userprofile";
    public static UserProfileDao getInstance() {
        if (userProfileDao == null) {
            userProfileDao = new UserProfileDao();
        }
        return userProfileDao;
    }

    public void updateUserProfile(UserProfile userProfile){
        if(userProfile == null){
            return;
        }
        String userId = userProfile.getUserId();
        try{
            Connection connection = MySQL.getConnection();
            PreparedStatement ps = connection.prepareStatement("UPDATE " + tableName + " SET name=?, likedRests=?, dislikedRests=?, "+
            "likedDishes=?, dislikedDishes=?, likedCuisines=?, dislikedCuisines=?, likedLocations=?, dislikedLocations=?, contextPreference=?, likedRestAssociations=? " + "WHERE user_id='" + userId +"'");
            ps.setString(1, userProfile.getName());

            ps.setString(2, userProfile.listToString(userProfile.getLikedRests()));
            ps.setString(3, userProfile.listToString(userProfile.getDislikedRests()));

            ps.setString(4, userProfile.listToString(userProfile.getLikedDishes()));
            ps.setString(5, userProfile.listToString(userProfile.getDislikedDishes()));

            ps.setString(6, userProfile.listToString(userProfile.getLikedCuisines()));
            ps.setString(7, userProfile.listToString(userProfile.getDislikedCuisines()));

            ps.setString(8, userProfile.listToString(userProfile.getLikedLocations()));
            ps.setString(9, userProfile.listToString(userProfile.getDislikedLocations()));

            ps.setString(10, userProfile.mapToString(userProfile.getContextPreference()));
            ps.setString(11, userProfile.mapToString(userProfile.getLikedRestAssociations()));
            ps.executeUpdate();
            ps.close();
            connection.close();
        }catch (Exception e){
            System.out.println("Error updating user profile: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isExisted(String userId){
        boolean existed = false;

        try{
            Connection connection = MySQL.getConnection();
            String query = "SELECT EXISTS(SELECT * FROM " + tableName + " WHERE user_id ='" + userId + "') AS existed";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                existed = rs.getInt("existed")==1;
            }
            ps.close();
            //connection.close();
        }catch (Exception e){
            System.out.println("Exception while checking profile exists: " + e.getMessage());
            e.printStackTrace();
        }
        return existed;
    }

    public void insertUserProfile(UserProfile userProfile) throws SQLException {
        if(userProfile == null){
            return;
        }
//        try{
            String userId = userProfile.getUserId();
            if(isExisted(userId)){
                //update an existing profile
                updateUserProfile(userProfile);
            }else{
                // insert a new profile
                Connection connection = MySQL.getConnection();
                PreparedStatement ps = connection.prepareStatement("INSERT INTO " + tableName +
                        " VALUES( " + "?,?)");
                Map<String, Map<String, Map<String, Long>>> contexts = userProfile.getContextPreference();
//                try{
                    ps.setString(1, userId);
                    ps.setString(2, userProfile.getName());

                    ps.execute();
                    ps.close();
                    connection.close();
//                }catch (SQLException e){
//                    System.out.println("Error updating user profile: " + e.getMessage());
//                    e.printStackTrace();
//                }
            }
//        }catch (Exception e){
//            System.out.println("Error updating user profile: " + e.getMessage());
//            e.printStackTrace();
//        }
    }

    public UserProfile selectUserProfile(String userId){
        UserProfile userProfile = null;
        Map<String, Map<String, Map<String, Long>>> contextPreference = new LinkedMap();
        try {
            String query = "Select * from " + tableName + " where user_id = '" + userId + "'";
            Connection connection = MySQL.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
            	userProfile = new UserProfile(userId);
                userProfile.setName(rs.getString("name"));

                userProfile.setContextPreference(contextPreference);

                Map<String,Map<String,List<String>>> likedRestAssociations = new HashedMap();

            }
            ps.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error getting user profile: " + e.getMessage());
            e.printStackTrace();
        }
        return userProfile;
    }

    public String getUserName(String userId){
        try {
            String query = "Select name from " + tableName + " where user_id = '" + userId + "'";
            Connection connection = MySQL.getConnection();
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                return rs.getString("name");
            }
            ps.close();
            connection.close();
        } catch (Exception e) {
            System.out.println("Error getting user name: " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }

}
