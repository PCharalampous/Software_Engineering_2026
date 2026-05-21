package profile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import entities.Request;
import entities.UserProfile;
import util.DatabaseManager;
import java.sql.ResultSet;

public class ManageProfileClass {
    private UserProfile currentUser;
    private List<Request> incomingRequests;

    public ManageProfileClass(UserProfile user, List<Request> requests) {
        this.currentUser = user;
        this.incomingRequests = requests;
    }

    public UserProfile queryProfile() { return currentUser; }

    public boolean validateChanges(String name) {
        return name != null && !name.trim().isEmpty();
    }

    public void save(String name, String bio, String prefs) {
        // Safe check για Null values ώστε να αποφευχθεί το NullPointerException
        currentUser.name = (name != null) ? name.trim() : "";
        currentUser.bio = (bio != null) ? bio.trim() : "";
        currentUser.preferences = (prefs != null) ? prefs.trim() : "";

        String sql = "UPDATE users SET display_name = ?, bio = ?, preferences = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currentUser.name);
            stmt.setString(2, currentUser.bio);
            stmt.setString(3, currentUser.preferences);
            stmt.setInt(4, currentUser.user_id);
            stmt.executeUpdate();
            System.out.println("Profile updated στη βάση για user_id: " + currentUser.user_id);

        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά το update του profile:");
            e.printStackTrace();
        }
    }

    public void choseACCEPT(Request req, String justification) {
        incomingRequests.remove(req);
        updateRequestStatus(req.request_id, "ACCEPTED", justification);
    }

    public void choseDECLINE(Request req, String justification) {
        incomingRequests.remove(req);
        updateRequestStatus(req.request_id, "DECLINED", justification);
    }

    private void updateRequestStatus(int requestId, String status, String justification) {
        String sql = "UPDATE room_requests SET request_status = ?, justification = ? WHERE request_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, justification.trim().isEmpty() ? null : justification.trim());
            stmt.setInt(3, requestId);
            stmt.executeUpdate();
            System.out.println("Request " + requestId + " -> " + status + 
                               (justification.trim().isEmpty() ? "" : " | justification: " + justification));

        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά το update του request:");
            e.printStackTrace();
        }
    }

    public static UserProfile loadFromDatabase(int userId) {
        String sql = 
            "SELECT u.user_id, u.display_name, u.username, u.bio, u.preferences, u.room_id, " +
            "       COALESCE(up.current_balance, 0) AS points, " +
            "       COALESCE(r.room_flat_name, 'N/A') AS flat_name, " +
            "       COALESCE((SELECT COUNT(*) FROM users u2 WHERE u2.room_id = u.room_id), 0) AS members " +
            "FROM users u " +
            "LEFT JOIN user_points up ON up.user_id = u.user_id " +
            "LEFT JOIN rooms r ON r.room_id = u.room_id " +
            "WHERE u.user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new UserProfile(
                    rs.getInt("user_id"),
                    rs.getString("display_name"),
                    rs.getString("username"),
                    rs.getString("bio"),
                    rs.getString("preferences"),
                    rs.getInt("points"),
                    rs.getString("flat_name"),
                    rs.getInt("members"),
                    rs.getInt("room_id")
                );
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά το φόρτωμα του profile από τη βάση:");
            e.printStackTrace();
        }
        return null;
    }

    public static List<Request> loadRequestsFromDatabase(int roomId) {
        List<Request> requests = new ArrayList<>();
        String sql = 
            "SELECT rr.request_id, u.display_name, u.username, rr.request_time " +
            "FROM room_requests rr " +
            "JOIN users u ON u.user_id = rr.sender_id " +
            "WHERE rr.room_id = ? AND rr.request_status = 'PENDING'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, roomId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("display_name");
                String[] parts = name.split(" ");
                String initials = parts.length >= 2
                    ? String.valueOf(parts[0].charAt(0)) + String.valueOf(parts[1].charAt(0))
                    : name.substring(0, Math.min(2, name.length()));

                requests.add(new Request(
                    rs.getInt("request_id"),
                    name,
                    initials.toUpperCase(),
                    rs.getString("request_time")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Σφάλμα κατά το φόρτωμα των requests:");
            e.printStackTrace();
        }
        return requests;
    }
}