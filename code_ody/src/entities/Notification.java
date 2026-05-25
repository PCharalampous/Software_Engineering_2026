package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Alert;

public class Notification {
    // Πεδία δεδομένων (τα κρατάμε public για συμβατότητα, αλλά προσθέτουμε και getters)
    public String category;
    public String text;
    public String detail;
    public String target;
    public String tagColor;
    public boolean read;

    // --- Constructor: Δημιουργία αντικειμένου ειδοποίησης ---
    public Notification(String cat, String txt, String det, String tgt, String tc) {
        this.category = cat;
        this.text = txt;
        this.detail = det;
        this.target = tgt;
        this.tagColor = tc;
        this.read = false; // Αρχικά κάθε νέα ειδοποίηση είναι μη διαβασμένη
    }
    //-----------------------------------------------------------------------------------------------
    public static boolean createNotification(Connection conn, String category, String text, 
            String detail, String targetScreen, String tagColor) {

		// Automatically retrieve the user ID from the active Authentication context
		if (entities.Authentication.getCurrentUser() == null) {
			System.err.println("Error creating notification: No authenticated user found.");
			return false;
		}
		int userId = entities.Authentication.getCurrentUser().getId();
		
		// Forward the parameters straight to Method A to avoid code duplication!
		return createNotification(conn, userId, category, text, detail, targetScreen, tagColor);
	}
    //------
    public static boolean createNotification(Connection conn, int userId ,String category, String text, 
            String detail, String targetScreen, String tagColor) {

		// 1. Automatically retrieve the user ID from the active Authentication context
		if (entities.Authentication.getCurrentUser() == null) {
			System.err.println("Error creating notification: No authenticated user found.");
			return false;
		}
		
		Integer roomId = null;
		
		// 2. Query the 'users' table to fetch this user's current room_id
		String roomSql = "SELECT room_id FROM users WHERE user_id = ?";
			try (PreparedStatement roomStmt = conn.prepareStatement(roomSql)) {
				roomStmt.setInt(1, userId);
			try (var rs = roomStmt.executeQuery()) {
				if (rs.next()) {
					// getObject allows handling NULL values safely if a user isn't in a room
					roomId = (Integer) rs.getObject("room_id"); 
				}
			}
		} catch (SQLException e) {
			System.err.println("Error fetching room_id for user " + userId + ": " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		// 3. Prepare the notification insertion string
		String insertSql = "INSERT INTO notifications (user_id, room_id, category, notification_text, " +
		"detail, target_screen, tag_color, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)";
		
		try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
		
		pstmt.setInt(1, userId);
		
		// Handle cases where room_id might be NULL in the database safely
		if (roomId != null) {
			pstmt.setInt(2, roomId);
		} else {
			pstmt.setNull(2, java.sql.Types.INTEGER);
		}
		
		pstmt.setString(3, category.toUpperCase());
		pstmt.setString(4, text);
		pstmt.setString(5, detail);
		pstmt.setString(6, targetScreen);
		pstmt.setString(7, tagColor);
		
		int rowsAffected = pstmt.executeUpdate();
		return rowsAffected > 0;
		
		} catch (SQLException e) {
			System.err.println("Error creating notification: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
    //----------
    /**
     * Overloaded Method C: Sends a notification to ALL users belonging to a specific room.
     * It looks up the current user's room, finds all members, and loops over them.
     */
    public static boolean createNotificationToRoom(Connection conn, String category, String text, 
                                                   String detail, String targetScreen, String tagColor) {
        
        // 1. Get current logged-in user to identify which room we are targeting
        if (entities.Authentication.getCurrentUser() == null) {
            System.err.println("Error: No authenticated user found to identify the room context.");
            return false;
        }
        int currentUserId = entities.Authentication.getCurrentUser().getId();
        Integer roomId = null;

        // 2. Step A: Find the room_id of the current user
        String getRoomSql = "SELECT room_id FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(getRoomSql)) {
            stmt.setInt(1, currentUserId);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    roomId = (Integer) rs.getObject("room_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching current user's room context: " + e.getMessage());
            return false;
        }

        // If the user isn't assigned to any room, we can't blast a notification
        if (roomId == null || roomId == 0) {
            System.err.println("Error: Current user does not belong to any room.");
            return false;
        }

        // 3. Step B: Fetch all user_ids belonging to this specific room
        List<Integer> roommateIds = new ArrayList<>();
        String getMembersSql = "SELECT user_id FROM users WHERE room_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(getMembersSql)) {
            stmt.setInt(1, roomId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    roommateIds.add(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching roommates for room " + roomId + ": " + e.getMessage());
            return false;
        }

        // 4. Step C: Loop through every user ID found and call your single-user method
        boolean allSucceeded = true;
        for (int memberId : roommateIds) {
            // Reuses your existing 7-parameter overload!
            boolean individualSuccess = createNotification(conn, memberId, category, text, detail, targetScreen, tagColor);
            
            if (!individualSuccess) {
                allSucceeded = false;
                System.err.println("Warning: Failed to deliver notification to user ID: " + memberId);
            }
        }

        return allSucceeded;
    }
    //----------
    public static List<Notification> fetchNotificationsForUser(Connection conn, int userId, int roomId) {
        List<Notification> list = new ArrayList<>();
        
        String sql = "SELECT category, notification_text, detail, target_screen, tag_color, is_read " +
                     "FROM notifications WHERE user_id = ? AND room_id = ? " +
                     "ORDER BY created_at DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roomId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create the Java object using data from the database row
                    Notification n = new Notification(
                        rs.getString("category"),
                        rs.getString("notification_text"),
                        rs.getString("detail"),
                        rs.getString("target_screen"),
                        rs.getString("tag_color")
                    );
                    
                    // Set the read state based on the database column value
                    n.setRead(rs.getBoolean("is_read"));
                    
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    //-----------------------------------------------------------------------------------------------
    

    /**
     * Εμφανίζει ένα JavaFX Alert χρησιμοποιώντας τα δεδομένα 
     * αυτού του συγκεκριμένου αντικειμένου Notification.
     */
    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification - " + this.category);
        alert.setHeaderText(this.text);
        alert.setContentText(this.detail);
        alert.show();
    }

    /**
     * Static μέθοδος για γρήγορη εμφάνιση 
     * ενός απλού μηνύματος σε JavaFX Alert χωρίς τη δημιουργία αντικειμένου.
     */
    public static void makeNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); 
    }

    /**
     * ΝΕΑ Static μέθοδος (από το usecase7) για την προσομοίωση/καταγραφή
     * αποστολής μιας ειδοποίησης στο σύστημα μέσω της κονσόλας.
     */
    public static void makeNotification(String title, String message) {
        System.out.println("====== [NOTIFICATION TRIGGERED] ======");
        System.out.println("Title: " + title);
        System.out.println("Message: " + message);
        System.out.println("=======================================");
    }

    // --- Getters: Απαραίτητοι για τη μεταφορά δεδομένων σε screens άλλων πακέτων ---
    public String getCategory() { return category; }
    public String getText() { return text; }
    public String getDetail() { return detail; }
    public String getTarget() { return target; }
    public String getTagColor() { return tagColor; }
    public boolean isRead() { return read; }
    
    public void setRead(boolean read) { this.read = read; }
}