package notifications;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import entities.Notification;
import entities.UnreadCounter;

public class ManageNotificationsClass {
    private List<Notification> notifications;
    private UnreadCounter unreadCounter;

    public ManageNotificationsClass(List<Notification> notifications, UnreadCounter unreadCounter) {
        this.notifications = notifications;
        this.unreadCounter = unreadCounter;
    }

    public List<Notification> queryPendingEvents() { return notifications; }
    public int getCount() { return unreadCounter.getCount(); }

//    public void markAsRead(Notification n) {
//        if (!n.read) {
//            n.read = true;
//            unreadCounter.decrement();
//        }
//    }

    
    //-----------------------------------------------------------------------------------------------
    public void markAllAsRead() {
        // 1. Update the local memory state
        notifications.forEach(n -> n.read = true);
        unreadCounter.reset();

        // 2. Extract current user and room context safely from Authentication
        if (entities.Authentication.getCurrentUser() != null) {
            int userId = entities.Authentication.getCurrentUser().getId();
            
            // Query updates all unread notifications belonging to this specific user
            String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
            
            try (java.sql.Connection conn = util.DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
                
            } catch (SQLException e) {
                System.err.println("Failed to bulk update notifications to read in DB: " + e.getMessage());
            }
        }
    }
    //---
    public void markAsRead(Notification n) {
        if (!n.read) {
            n.read = true;
            unreadCounter.decrement();

            // PERSIST CHANGE TO DATABASE:
            String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_text = ? AND category = ?"; 
            try (java.sql.Connection conn = util.DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, n.getText());
                pstmt.setString(2, n.getCategory());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Failed to update notification read status in DB: " + e.getMessage());
            }
        }
    }
  //-----------------------------------------------------------------------------------------------

    public void deleteNotification(Notification n) {
        notifications.remove(n);
        unreadCounter.update(notifications);
    }
}