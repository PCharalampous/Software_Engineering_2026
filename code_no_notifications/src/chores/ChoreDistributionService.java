package chores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import util.DatabaseManager;

public class ChoreDistributionService {

    private final int roomId;
    private final List<String> members;

    public ChoreDistributionService(int roomId, List<String> members) {
        this.roomId = roomId;
        this.members = members;
    }

    public void distributeWeeklyChores() {
        if (members == null || members.isEmpty() || roomId == 0) return;

        try (Connection conn = DatabaseManager.getConnection()) {

            // 1. Έλεγχος αν υπάρχουν chores που δεν έχουν επιστρέψει σε Unassigned (δηλαδή δεν ολοκληρώθηκαν)
            String checkPendingSql = "SELECT COUNT(*) FROM chores WHERE room_id = ? " +
                                     "AND chore_status IN ('Pending', 'Completed', 'Suspended') " +
                                     "AND assignee != 'Unassigned'";
            
            boolean dynamicProceed = true;
            
            try (PreparedStatement psCheck = conn.prepareStatement(checkPendingSql)) {
                psCheck.setInt(1, roomId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        dynamicProceed = false;
                    }
                }
            }

            // Αν βρέθηκαν chores που εκκρεμούν, ρωτάμε τον χρήστη αν θέλει Force Redistribution
         // Αν βρέθηκαν chores που εκκρεμούν, εμφανίζουμε Alert με OK και σταματάμε
            if (!dynamicProceed) {
                System.out.println("Cannot shuffle! Not all chores are completed yet.");
                
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(
                        Alert.AlertType.WARNING,
                        "Not all chores are completed yet! All roommates must complete and vote on their chores before redistributing.",
                        ButtonType.OK
                    );
                    alert.setHeaderText("Cannot Redistribute Yet");
                    alert.showAndWait();
                });
                
                return; // Σταματάει αμέσως τη διανομή και δεν σβήνει τίποτα
            }

            // 2. Μάζεψε όλα τα διαθέσιμα Chore IDs του δωματίου
            List<Integer> choreIds = new ArrayList<>();
            String getChoresSql = "SELECT chore_id FROM chores WHERE room_id = ?";
            try (PreparedStatement psGet = conn.prepareStatement(getChoresSql)) {
                psGet.setInt(1, roomId);
                try (ResultSet rs = psGet.executeQuery()) {
                    while (rs.next()) choreIds.add(rs.getInt("chore_id"));
                }
            }

            if (choreIds.isEmpty()) {
                System.out.println("No chores to distribute for room: " + roomId);
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(
                        Alert.AlertType.INFORMATION,
                        "No chores found! Please add chores first using the '+' button.",
                        ButtonType.OK
                    );
                    alert.setHeaderText("No Chores Available");
                    alert.showAndWait();
                });
                return;
            }

            conn.setAutoCommit(false);

            String updateChoreSql = "UPDATE chores SET assignee = ?, chore_status = 'Pending', approve_votes = 0, reject_votes = 0 WHERE chore_id = ?";
            String deleteHistorySql = "DELETE FROM chore_history WHERE room_id = ?";
            // Καθαρισμός ΟΛΩΝ των ψήφων του δωματίου
            String deleteVotesSql = "DELETE FROM chore_reports WHERE room_id = ? AND (title LIKE 'VOTE_APPROVE_%' OR title LIKE 'VOTE_REJECT_%')";
            String insertNotificationSql = "INSERT INTO notifications (user_id, room_id, category, notification_text, detail, target_screen, tag_color, is_read) VALUES (?, ?, 'CHORES', ?, ?, 'ChoreScreen', '#D4EDDA', FALSE)";

            // Διαγραφή ιστορικού οπτικών logs
            try (PreparedStatement psDelHistory = conn.prepareStatement(deleteHistorySql)) {
                psDelHistory.setInt(1, roomId);
                psDelHistory.executeUpdate();
                System.out.println("[HOMY DB] Chore history cleared for room: " + roomId);
            }
            
            // ΚΡΙΣΙΜΟ: Διαγραφή όλων των καταγεγραμμένων ψήφων του δωματίου στη βάση
            try (PreparedStatement psDelVotes = conn.prepareStatement(deleteVotesSql)) {
                psDelVotes.setInt(1, roomId);
                psDelVotes.executeUpdate();
                System.out.println("[HOMY DB] All old chore votes cleared from database reports for room: " + roomId);
            }
            
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
                System.out.println("Chores successfully redistributed among real house members!");
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
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