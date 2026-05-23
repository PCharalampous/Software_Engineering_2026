package notifications;
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

    public void markAsRead(Notification n) {
        if (!n.read) {
            n.read = true;
            unreadCounter.decrement();
        }
    }

    public void markAllAsRead() {
        notifications.forEach(n -> n.read = true);
        unreadCounter.reset();
    }

    public void deleteNotification(Notification n) {
        notifications.remove(n);
        unreadCounter.update(notifications);
    }
}