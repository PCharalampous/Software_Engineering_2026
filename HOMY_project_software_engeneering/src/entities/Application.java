package entities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import util.DatabaseManager;

public class Application {
    private int id;
    private String title;
    private String location;
    private String address;
    private double rent;
    private int roommates;
    private String description;
    private String status;
    private int ownerId; // Το user_id του χρήστη που δημιούργησε την αγγελία

    // Constructor για τη φόρτωση από τη βάση δεδομένων
    public Application(int id, String title, String description, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    // Constructor πλήρης με το ownerId
    public Application(int id, String title, String location, String address, double rent, int roommates, String description, String status, int ownerId) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.address = address;
        this.rent = rent;
        this.roommates = roommates;
        this.description = description;
        this.status = status;
        this.ownerId = ownerId;
    }

    // Constructor για τη δημιουργία νέας αίτησης από τη NewApplicationScreen
    public Application(String title, String location, String address, double rent, int roommates, String description, String status) {
        this.title = title;
        this.location = location;
        this.address = address;
        this.rent = rent;
        this.roommates = roommates;
        this.description = description;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { 
        return this.description; 
    }
    
    public String getHomeScreenDescription() { 

        return this.address.concat("\n").concat(this.title).concat("\n").concat(this.description); 
    }
    
    public String getStatus() { return status; }
    public String getLocation() { return location; }
    public String getAddress() { return address; }
    public double getRent() { return rent; }
    public int getRoommates() { return roommates; }
    public int getOwnerId() { return ownerId; }

    /**
     * Φορτώνει όλες τις αγγελίες της βάσης δεδομένων μαζί με το user_id του δημιουργού (owner).
     */
    public static List<Application> loadAllApplications() {
        List<Application> apps = new ArrayList<>();
        String sql = "SELECT application_id, title, location, house_address, rent, " +
                     "roommates_wanted, description, application_status, user_id FROM applications";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                apps.add(new Application(
                    rs.getInt("application_id"),
                    rs.getString("title"),
                    rs.getString("location"),
                    rs.getString("house_address"),
                    rs.getDouble("rent"),
                    rs.getInt("roommates_wanted"),
                    rs.getString("description"),
                    rs.getString("application_status"),
                    rs.getInt("user_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά τη φόρτωση όλων των αγγελιών:");
            e.printStackTrace();
        }
        return apps;
    }

    /**
     * Φορτώνει τις αγγελίες του χρήστη (Owner) ΚΑΙ τις αγγελίες στις οποίες έκανε Connect.
     */
    public static List<Application> loadUserApplications(int userId) {
        List<Application> apps = new ArrayList<>();
        
        String sql = 
            "SELECT a.application_id, a.title, a.description, a.application_status AS display_status " +
            "FROM applications a " +
            "WHERE a.user_id = ? " +
            "UNION " +
            "SELECT a.application_id, a.title, a.description, rr.request_status AS display_status " +
            "FROM room_requests rr " +
            "JOIN rooms r ON r.room_id = rr.room_id " +
            "JOIN users u ON u.room_id = r.room_id " +
            "JOIN applications a ON a.user_id = u.user_id " +
            "WHERE rr.sender_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apps.add(new Application(
                        rs.getInt("application_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("display_status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά τη φόρτωση των applications (Owner + Connect Requests):");
            e.printStackTrace();
        }
        return apps;
    }

    /**
     * Δημιουργεί ένα εκκρεμές αίτημα (room_request) για το δωμάτιο του ιδιοκτήτη της αγγελίας.
     * Αν ο ιδιοκτήτης δεν έχει δωμάτιο, δημιουργεί αυτόματα ένα προσωρινό για να περάσει το Foreign Key.
     */
    public boolean sendRoomRequest(int senderId) {
        Connection conn = DatabaseManager.getConnection();
        int targetRoomId = 0;

        // 1. Έλεγχος αν ο ιδιοκτήτης της αγγελίας έχει ήδη room_id
        String findRoomSql = "SELECT room_id FROM users WHERE user_id = ?";
        try (PreparedStatement roomStmt = conn.prepareStatement(findRoomSql)) {
            roomStmt.setInt(1, this.ownerId);
            try (ResultSet rs = roomStmt.executeQuery()) {
                if (rs.next()) {
                    targetRoomId = rs.getInt("room_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // 2. Αν ΔΕΝ έχει δωμάτιο, δημιουργούμε αυτόματα ένα προσωρινό δωμάτιο στη βάση
        if (targetRoomId == 0) {
            String createRoomSql = "INSERT INTO rooms (room_code, room_flat_name, max_roommates, rent_value) VALUES (?, ?, ?, ?)";
            String generatedCode = "ROOM-" + this.ownerId + "-" + (int)(Math.random() * 10000);
            
            try (PreparedStatement createStmt = conn.prepareStatement(createRoomSql, Statement.RETURN_GENERATED_KEYS)) {
                createStmt.setString(1, generatedCode);
                createStmt.setString(2, this.title);
                createStmt.setInt(3, this.roommates);
                createStmt.setDouble(4, this.rent);
                createStmt.executeUpdate();

                try (ResultSet generatedKeys = createStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        targetRoomId = generatedKeys.getInt(1);
                    }
                }

                // Ενημέρωση του πίνακα users ώστε ο ιδιοκτήτης να συνδεθεί με αυτό το νέο δωμάτιο
                String updateUserRoomSql = "UPDATE users SET room_id = ? WHERE user_id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateUserRoomSql)) {
                    updateStmt.setInt(1, targetRoomId);
                    updateStmt.setInt(2, this.ownerId);
                    updateStmt.executeUpdate();
                }

            } catch (SQLException e) {
                System.err.println("Σφάλμα κατά την αυτόματη δημιουργία προσωρινού δωματίου:");
                e.printStackTrace();
                return false;
            }
        }

        // 3. Εισαγωγή του αιτήματος στον πίνακα room_requests (Πλέον το targetRoomId είναι 100% υπαρκτό)
        String insertSql = "INSERT INTO room_requests (room_id, sender_id, request_time, request_status) VALUES (?, ?, ?, 'PENDING')";
        String currentTimeString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, targetRoomId);
            insertStmt.setInt(2, senderId);
            insertStmt.setString(3, currentTimeString);
            
            int rowsInserted = insertStmt.executeUpdate();
            if(rowsInserted > 0) {
            	Notification.createNotification(conn,this.ownerId, "requests", "someone wants to join your room", "", "PROFILE_SCREEN", "#14B8A6");
            	return true;
            }else
            	return false;
            
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την εισαγωγή του room request:");
            e.printStackTrace();
            return false;
        }
    }

    public void saveApplication(int userId) {
        String sql = "INSERT INTO applications (user_id, title, location, house_address, rent, roommates_wanted, description, application_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, this.title);
            stmt.setString(3, this.location);
            stmt.setString(4, this.address);
            stmt.setDouble(5, this.rent);
            stmt.setInt(6, this.roommates);
            stmt.setString(7, this.description);
            stmt.setString(8, this.status);
            
            stmt.executeUpdate();
            System.out.println("Η νέα αίτηση αποθηκεύτηκε επιτυχώς στη βάση!");
            
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την αποθήκευση της αίτησης:");
            e.printStackTrace();
        }
    }

    public static boolean deleteApplication(int applicationId) {
        String sql = "DELETE FROM applications WHERE application_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, applicationId);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
            
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά τη διαγραφή της αίτησης:");
            e.printStackTrace();
            return false;
        }
    }
}
