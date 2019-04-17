package com.iaasimov.dao;

import com.iaasimov.workflow.GlobalConstants;
import com.iaasimov.workflow.GlobalConstantsNew;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQL {
    private static Connection connection = null;
//    static String dbUrl = "jdbc:mysql://" + GlobalConstants.mysqlIP + ":" + GlobalConstants.mysqlPORT + "/" + GlobalConstants.mysqlDB;
      static String dbClass = "com.mysql.jdbc.Driver";
//    static String username = GlobalConstants.mysqlUser;
//    static String password = GlobalConstants.mysqlPass;

    static {
        try {
            Class.forName(dbClass);
            connection = DriverManager.getConnection(GlobalConstantsNew.getInstance().dbUrl,
                                                      GlobalConstantsNew.getInstance().dbUsername,
                                                      GlobalConstantsNew.getInstance().dbPassword);
            System.out.println("Connect database successfully.");
        } catch (Exception e) {
            System.out.println("Error connect database! " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        try{
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(GlobalConstantsNew.getInstance().dbUrl,
                                                          GlobalConstantsNew.getInstance().dbUsername,
                                                         GlobalConstantsNew.getInstance().dbPassword);
            }
        }catch (Exception e){
            System.out.println("Error connect database! " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}
