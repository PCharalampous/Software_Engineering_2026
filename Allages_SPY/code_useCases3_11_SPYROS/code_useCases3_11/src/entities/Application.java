package entities;

import util.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Application {
    private int applicationId;
    private String title;
    private String location;
    private String houseAddress;
    private double rent;
    private int roommatesWanted;
    private String description;
    private String status;

    // Constructor για δημιουργία νέας αγγελίας (πριν αποθηκευτεί, χωρίς ID)
    public Application(String title, String location, String houseAddress, double rent, int roommatesWanted, String description, String status) {
        this.title = title;
        this.location = location;
        this.houseAddress = houseAddress;
        this.rent = rent;
        this.roommatesWanted = roommatesWanted;
        this.description = description;
        this.status = status;
    }

    // Constructor για ανάγνωση από τη βάση (με ID)
    public Application(int applicationId, String title, String location, String houseAddress, double rent, int roommatesWanted, String description, String status) {
        this.applicationId = applicationId;
        this.title = title;
        this.location = location;
        this.houseAddress = houseAddress;
        this.rent = rent;
        this.roommatesWanted = roommatesWanted;
        this.description = description;
        this.status = status;
    }

    // Αποθήκευση νέας αγγελίας στη βάση
    public void saveApplication(int userId) {
        String sql = "INSERT INTO applications (user_id, title, location, house_address, rent, roommates_wanted, description, application_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, this.title);
            stmt.setString(3, this.location);
            stmt.setString(4, this.houseAddress);
            stmt.setDouble(5, this.rent);
            stmt.setInt(6, this.roommatesWanted);
            stmt.setString(7, this.description);
            stmt.setString(8, this.status);
            stmt.executeUpdate();
            System.out.println("Η αγγελία αποθηκεύτηκε επιτυχώς στη βάση!");
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την αποθήκευση της αγγελίας:");
            e.printStackTrace();
        }
    }

    // Ακύρωση της αγγελίας στη βάση
    public void cancelApplication() {
        String sql = "UPDATE applications SET application_status = 'CANCELED' WHERE application_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.applicationId);
            stmt.executeUpdate();
            this.status = "CANCELED"; // Ενημέρωση και του τοπικού αντικειμένου
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά την ακύρωση της αγγελίας:");
            e.printStackTrace();
        }
    }

    // Φόρτωση όλων των αγγελιών ενός συγκεκριμένου χρήστη
    public static List<Application> loadUserApplications(int userId) {
        List<Application> apps = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                apps.add(new Application(
                    rs.getInt("application_id"),
                    rs.getString("title"),
                    rs.getString("location"),
                    rs.getString("house_address"),
                    rs.getDouble("rent"),
                    rs.getInt("roommates_wanted"),
                    rs.getString("description"),
                    rs.getString("application_status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά τη φόρτωση των αγγελιών:");
            e.printStackTrace();
        }
        return apps;
    }

    // Getters
    public int getApplicationId() { return applicationId; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public double getRent() { return rent; }
    public int getRoommatesWanted() { return roommatesWanted; }
    public String getStatus() { return status; }
}