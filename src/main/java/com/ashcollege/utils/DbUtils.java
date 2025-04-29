package com.ashcollege.utils;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
public class DbUtils {

    private Connection connection;

    @PostConstruct
    public void init() {
        createDbConnection("postgres", "Mj40340550!");
    }

    private void createDbConnection(String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            String jdbcUrl = "jdbc:postgresql://db.ntjpvjlippedoxlnjmmo.supabase.co:5432/postgres";
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("✅ PostgreSQL connection successful!");
        } catch (Exception e) {
            System.out.println("❌ Cannot connect to PostgreSQL DB!");
            e.printStackTrace();
        }
    }

    // את יכולה להוסיף כאן פונקציות להרצת שאילתות או בדיקות אם צריך
}
