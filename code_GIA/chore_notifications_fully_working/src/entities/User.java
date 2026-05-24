package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import ui.ErrorScreen;
import util.DatabaseManager;

public class User {
    private int id;
    private String username;
    private String email;
    private int roomId; // ← ΠΡΟΣΘΗΚΗ

    public User(int id, String username, String email, int roomId) { // ← ΕΝΗΜΕΡΩΣΗ
        this.id = id;
        this.username = username;
        this.email = email;
        this.roomId = roomId;
    }

    public int getId() { return this.id; }
    public String getUsername() { return this.username; }
    public String getEmail() { return this.email; }
    public int getRoomId() { return this.roomId; } // ← ΠΡΟΣΘΗΚΗ
    
    public boolean joinRoomInstantly(int targetRoomId) {
        // 1. Check max roommates limit
        String checkSql = "SELECT " +
                          "  (SELECT max_roommates FROM rooms WHERE room_id = ?) AS max_seats, " +
                          "  (SELECT COUNT(*) FROM users WHERE room_id = ?) AS current_occupants";
        
        String updateSql = "UPDATE users SET room_id = ? WHERE user_id = ?";
        
        // SQL targeting the applications table linked to the room's owner
        String sqlDecreaseApps = "UPDATE applications a " +
                                 "JOIN users u ON a.user_id = u.user_id " +
                                 "SET a.roommates_wanted = a.roommates_wanted - 1 " +
                                 "WHERE u.room_id = ? AND a.roommates_wanted > 0";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            // Start unified transaction block
            conn.setAutoCommit(false); 
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, targetRoomId);
                checkStmt.setInt(2, targetRoomId);
                
                try (var rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int maxSeats = rs.getInt("max_seats");
                        int currentOccupants = rs.getInt("current_occupants");
                        
                        if (rs.wasNull() || maxSeats == 0) {
                            System.err.println("Error: Room " + targetRoomId + " does not exist.");
                            conn.rollback();
                            return false;
                        }
                        
                        if (currentOccupants >= maxSeats) {
                            System.out.println("Join failed: Room is full! (" + currentOccupants + "/" + maxSeats + ")");
                            conn.rollback();
                            return false; 
                        }
                    }
                }
            }
            
            // 2. Put the user in the room
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, targetRoomId);
                updateStmt.setInt(2, this.id);
                updateStmt.executeUpdate();
            }
            
            // 3. Decrease roommates_wanted on the owner's application for this room frame
            try (PreparedStatement decreaseStmt = conn.prepareStatement(sqlDecreaseApps)) {
                decreaseStmt.setInt(1, targetRoomId);
                decreaseStmt.executeUpdate();
            }
            
            // ONLY NOW we safely commit everything together!
            conn.commit();
            
            // Update local memory state
            this.roomId = targetRoomId;
            System.out.println("Instant join and listing update successful for room: " + targetRoomId);
            
            // Trigger visual layout switch
            main.HOMYApp.showCentralHub();
            
            return true; // Return occurs seamlessly at the very end
            
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την απευθείας είσοδο στο δωμάτιο:");
            e.printStackTrace();
            return false;
        }
    }
}