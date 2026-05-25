package notifications;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import entities.Notification;
import entities.UnreadCounter;
import entities.Authentication;
import entities.User;
import util.DatabaseManager;

public class ManageNotificationsClass {
    private UnreadCounter unreadCounter;

    // Πλέον ο Manager δεν παίρνει List στον constructor
    public ManageNotificationsClass(UnreadCounter unreadCounter) {
        this.unreadCounter = unreadCounter;
        syncUnreadCount();
    }

    // Συγχρονίζει τον τοπικό μετρητή με τα πραγματικά δεδομένα της βάσης
    public void syncUnreadCount() {
        User user = Authentication.getCurrentUser();
        if (user == null) return;
        
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    unreadCounter.setCount(rs.getInt(1));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Αντλεί LIVE τη λίστα ειδοποιήσεων του χρήστη
    public List<Notification> queryPendingEvents() { 
        List<Notification> list = new ArrayList<>();
        User user = Authentication.getCurrentUser();
        if (user == null) return list;

        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                        rs.getInt("notification_id"),
                        rs.getString("category"),
                        rs.getString("notification_text"),
                        rs.getString("detail"),
                        rs.getString("target_screen"),
                        rs.getString("tag_color"),
                        rs.getBoolean("is_read")
                    ));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    
    public int getCount() { return unreadCounter.getCount(); }

    public void markAsRead(Notification n) {
        if (!n.read) {
            String query = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, n.getNotificationId());
                ps.executeUpdate();
                n.read = true;
                syncUnreadCount();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void markAllAsRead() {
        User user = Authentication.getCurrentUser();
        if (user == null) return;
        
        String query = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, user.getId());
            ps.executeUpdate();
            syncUnreadCount();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void deleteNotification(Notification n) {
        String query = "DELETE FROM notifications WHERE notification_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, n.getNotificationId());
            ps.executeUpdate();
            syncUnreadCount();
        } catch (Exception e) { e.printStackTrace(); }
    }
}