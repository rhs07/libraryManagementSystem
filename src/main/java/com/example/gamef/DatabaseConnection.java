package com.example.gamef;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection databaseLink;

    public static Connection getConnection() {
        String databaseName = "user_info";
        String databaseUser = "root";
        String databasePassword = "Shakib123@";

        // ✅ FIXED URL:
        String url = "jdbc:mysql://localhost:3306/" + databaseName +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try {
            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to database
            databaseLink = DriverManager.getConnection(url, databaseUser, databasePassword);
            System.out.println("✅ Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed.");
            e.printStackTrace();
        }

        return databaseLink;
    }
}
