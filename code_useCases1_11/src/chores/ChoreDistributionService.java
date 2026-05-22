package chores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import util.DatabaseManager;

public class ChoreDistributionService {

    private final int roomId;
    private final List<String> members;

    public ChoreDistributionService(int roomId, List<String> members) {
        this.roomId = roomId;
        this.members = members;
    }

    public void distributeWeeklyChores() {
        if (members == null || members.isEmpty()) return;

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();

            // 1. Έλεγχος αν υπάρχουν ακόμα εκκρεμότητες
            String checkPendingSql = "SELECT COUNT(*) FROM chores WHERE room_id = ? AND chore_status IN ('Pending', 'Suspended')";
            try (PreparedStatement psCheck = conn.prepareStatement(checkPendingSql)) {
                psCheck.setInt(1, roomId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Cannot shuffle! Active tasks still exist.");
                        return;
                    }
                }
            }

            // 2. Ανάκτηση των IDs των ολοκληρωμένων chores
            List<Integer> choreIds = new ArrayList<>();
            String getChoresSql = "SELECT chore_id FROM chores WHERE room_id = ?";
            try (PreparedStatement psGet = conn.prepareStatement(getChoresSql)) {
                psGet.setInt(1, roomId);
                try (ResultSet rs = psGet.executeQuery()) {
                    while (rs.next()) {
                        choreIds.add(rs.getInt("chore_id"));
                    }
                }
            }

            if (choreIds.isEmpty()) return;

            conn.setAutoCommit(false);

            String updateChoreSql = "UPDATE chores SET assignee = ?, chore_status = 'Pending', approve_votes = 0, reject_votes = 0 WHERE chore_id = ?";
            String insertNotificationSql = "INSERT INTO notifications (user_id, room_id, category, notification_text, detail, target_screen, tag_color, is_read) VALUES (?, ?, 'CHORES', ?, ?, 'ChoreScreen', '#D4EDDA', FALSE)";

            List<String> shuffledMembers = new ArrayList<>(members);
            Collections.shuffle(shuffledMembers);

            int memberIndex = 0;
            try (PreparedStatement psUpdate = conn.prepareStatement(updateChoreSql);
                 PreparedStatement psNotify = conn.prepareStatement(insertNotificationSql)) {

                for (int choreId : choreIds) {
                    String assignedMember = shuffledMembers.get(memberIndex);

                    psUpdate.setString(1, assignedMember);
                    psUpdate.setInt(2, choreId);
                    psUpdate.addBatch();

                    int realUserId = getRealUserIdFromDatabase(conn, assignedMember);
                    psNotify.setInt(1, realUserId);
                    psNotify.setInt(2, roomId);
                    psNotify.setString(3, "Chores Shuffled!");
                    psNotify.setString(4, "A new chore round has started! Check your tasks.");
                    psNotify.addBatch();

                    memberIndex = (memberIndex + 1) % shuffledMembers.size();
                }

                psUpdate.executeBatch();
                psNotify.executeBatch();
                conn.commit();
            } catch (Exception ex) {
                if (conn != null) conn.rollback();
                throw ex;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getRealUserIdFromDatabase(Connection conn, String name) {
        String query = "SELECT user_id FROM users WHERE username = ? OR display_name = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("user_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}