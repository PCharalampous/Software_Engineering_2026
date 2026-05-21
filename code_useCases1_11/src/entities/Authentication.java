package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import ui.ErrorScreen;
import util.DatabaseManager;
import main.HOMYApp;

public class Authentication {
    
    private String email;
    private String password;
    private Boolean isCorrect;
    private Integer id;
    private Connection conn;
    
    private static User currentUser;

    public Authentication(Connection conn) {
        this.conn = conn;
    }
    
    public Boolean userLogIn(String usrEmail, String usrPass) throws SQLException {
        isCorrect = false;
        
        // Λήψη της static, έγκυρης σύνδεσης
        this.conn = DatabaseManager.getConnection();
        
        // ΑΛΛΑΓΗ: Χρήση * για να αποφύγουμε το σφάλμα με το όνομα της στήλης ID
        String sql = "SELECT * FROM users";
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = this.conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                // Διαβάζουμε το email (στήλη 4) και το password (στήλη 5) βάσει θέσης
                String dbEmail = rs.getString(4);
                String dbPass = rs.getString(5);
                
                System.out.println("!!!!!!user from database: " + dbEmail + " and user from log in ui: " + usrEmail);
                System.out.println("!!!!!!password from database: " + dbPass + " and password from log in ui: " + usrPass);
                
                if (dbEmail != null && dbPass != null && dbEmail.trim().equalsIgnoreCase(usrEmail.trim()) && dbPass.equals(usrPass)) {
                    System.out.println("user found");
                    isCorrect = true;
                    
                    // rs.getInt(1): παίρνει το ID από την πρώτη στήλη, rs.getString(2): το username
                    currentUser = new User(rs.getInt(1), rs.getString(2), rs.getString(4), rs.getInt(8));
                    break;
                } else {
                    System.out.println("user NOT found");
                }
            }
        } finally {
            if (rs != null) try { rs.close(); } catch(Exception e) {}
            if (stmt != null) try { stmt.close(); } catch(Exception e) {}
        }
        
        return isCorrect;
    }
    
    public void createAcc(String usrEmail, String usrPass, String userName, String dispFirstName, String dispSecondName) throws Exception {
        
        this.conn = DatabaseManager.getConnection();
        
        String checkSql = "SELECT * FROM users WHERE email = ?";
        PreparedStatement checkStmt = this.conn.prepareStatement(checkSql);
        checkStmt.setString(1, usrEmail);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            ErrorScreen errScr = new ErrorScreen("Sign Up failed", "User email already exists");
            errScr.show();
            rs.close();
            checkStmt.close();
            return;
        }
        rs.close();
        checkStmt.close();
            
        String sql = "INSERT INTO users (username, display_name, email, password_hash) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            stmt = this.conn.prepareStatement(sql);
            String displayName = dispFirstName + " " + dispSecondName;
    
            stmt.setString(1, userName);
            stmt.setString(2, displayName);
            stmt.setString(3, usrEmail);
            stmt.setString(4, usrPass);
    
            int rows = stmt.executeUpdate();
            System.out.println("Inserted rows: " + rows);
        } finally {
            if (stmt != null) stmt.close();
        }
        
        System.out.println("usrEmail: " + usrEmail + "\nusrPass: " + usrPass + "\nuserName: " + userName + "\ndispFirstName: " + dispFirstName + "\ndispSecondName: " + dispSecondName);
    }
    
    public void userLogOut() {
        currentUser = null;
        System.out.println("User logged out");
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public void setUserEmail(String email) { this.email = email; }
    public void setUserPassword(String password) { this.password = password; }
}