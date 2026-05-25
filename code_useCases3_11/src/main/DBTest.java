package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBTest {
    public static void main(String[] args) {
        // Update these credentials with your actual database details
        String url = "jdbc:mysql://bv6dtpgk7dfpjgusrjtc-mysql.services.clever-cloud.com:3306/bv6dtpgk7dfpjgusrjtc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String user = "u4ezo6ujbhl2ltqc";
        String password = "ODL7dkZjMVMENPqKgOoe";

        try {
            // This attempts to establish the connection
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ SUCCESS: Connected to the database!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("❌ FAILURE: Could not connect to the database.");
            System.out.println("Error Message: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            e.printStackTrace(); // This is the most important part!
        }
    }
}