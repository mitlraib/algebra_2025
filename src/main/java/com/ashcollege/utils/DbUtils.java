package com.ashcollege.utils;



import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.ashcollege.utils.Constants.SCHEMA;

@Component
public class DbUtils {

    private Connection connection;

    @PostConstruct
    public void init () {
        createDbConnection(Constants.DB_USERNAME, Constants.DB_PASSWORD);
    }

    private void createDbConnection(String username, String password){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+SCHEMA, username, password);
            System.out.println("Connection successful!");
            System.out.println();
        }catch (Exception e){
            System.out.println("Cannot create DB connection!");
        }
    }





}
