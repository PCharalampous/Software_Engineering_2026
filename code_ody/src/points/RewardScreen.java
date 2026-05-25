package points;

import entities.Reward;
import entities.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import ui.ConfirmationScreen;
import ui.ErrorScreen;
import util.DatabaseManager;

public class RewardScreen extends VBox {
    private final PointScreen pointSidebar;
    private VBox container;
    private int totalRoommatesCount = 3; 

    private final List<Integer> votedRewardIdsInSession = new ArrayList<>();

    public RewardScreen(PointScreen sidebar) {
        this.pointSidebar = sidebar;
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        HBox.setHgrow(this, Priority.ALWAYS);

        HBox titleRow = new HBox(10);
        Label title = new Label("🛒 REWARDS & VOTING CENTER");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        Pane space = new Pane(); HBox.setHgrow(space, Priority.ALWAYS);
        
        Button proposeBtn = new Button("+ Propose Pass");
        proposeBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        proposeBtn.setOnAction(e -> {
            NewRewardScreen form = new NewRewardScreen((name, cost) -> 
                insertProposalIntoDatabase(name, cost)); 
            form.show();
        });
        
        titleRow.getChildren().addAll(title, space, proposeBtn);
        container = new VBox(10);
        this.getChildren().addAll(titleRow, container);
        
        updateRoommatesCount();
        loadRewardsFromDatabase();
    }

    private void updateRoommatesCount() {
        // ΔΙΟΡΘΩΣΗ: Χρήση του getCurrentRoomId() για σίγουρη ανάκτηση
        int roomId = pointSidebar.getCurrentRoomId();
        String query = "SELECT COUNT(*) as total FROM users WHERE room_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("total");
                    // Αν δεν βρει χρήστες, βάζουμε 1 για να μην έχουμε διαίρεση με το μηδέν
                    this.totalRoommatesCount = (count > 0) ? count : 1;
                    System.out.println("[DEBUG] Roommates count for Room " + roomId + ": " + this.totalRoommatesCount);
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    private void loadRewardsFromDatabase() {
        container.getChildren().clear();
        updateRoommatesCount(); // Ενημέρωση του count για το σωστό δωμάτιο
        
        List<Reward> tempRewards = new ArrayList<>();
        // Φιλτράρουμε τα rewards ΜΟΝΟ για το τρέχον δωμάτιο
        String query = "SELECT * FROM rewards WHERE room_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, pointSidebar.getCurrentRoomId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reward r = new Reward(
                        rs.getInt("reward_id"),
                        rs.getString("reward_name"),
                        rs.getInt("cost"),
                        rs.getBoolean("is_available"),
                        rs.getString("ui_color"),
                        rs.getInt("approve_votes"), 
                        rs.getInt("reject_votes")
                    );
                    tempRewards.add(r);
                }
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        for (Reward r : tempRewards) {
            container.getChildren().add(createCard(r));
        }
    }

    private HBox createCard(Reward r) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #E2E8F0;");
        
        StackPane icon = new StackPane();
        icon.setPrefSize(40, 40);
        icon.setStyle("-fx-background-color: " + r.getColor() + "; -fx-background-radius: 8;");
        
        VBox txt = new VBox(2);
        Label name = new Label(r.getName()); name.setStyle("-fx-font-weight: bold;");
        Label cost = new Label(r.getCost() + " points"); cost.setStyle("-fx-font-size: 11; -fx-text-fill: #64748B;");
        txt.getChildren().addAll(name, cost);
        
        Pane spacer = new Pane(); HBox.setHgrow(spacer, Priority.ALWAYS);
        card.getChildren().addAll(icon, txt, spacer);

        if (r.isAvailable()) {
            Button buyBtn = new Button("BUY");
            buyBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
            buyBtn.setOnAction(e -> handlePurchase(r));
            
            Button deleteBtn = new Button("🗑️");
            deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-size: 14px; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> selectDeleteProposal(r));

            HBox activeActionsBox = new HBox(10, buyBtn, deleteBtn);
            activeActionsBox.setAlignment(Pos.CENTER_RIGHT);
            card.getChildren().add(activeActionsBox);
        } 
        else {
            Label voteLbl = new Label("VOTE (" + (r.getApproveVotes() + r.getRejectVotes()) + "/" + totalRoommatesCount + "): ");
            voteLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #4F46E5; -fx-font-weight: bold;");
            
            Button yesBtn = new Button("✓");
            yesBtn.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
            
            Button noBtn = new Button("✕");
            noBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");

            if (votedRewardIdsInSession.contains(r.getRewardId())) {
                yesBtn.setDisable(true);
                noBtn.setDisable(true);
            } else {
                yesBtn.setDisable(false);
                noBtn.setDisable(false);
            }

            yesBtn.setOnAction(e -> {
                votedRewardIdsInSession.add(r.getRewardId()); 
                yesBtn.setDisable(true);
                noBtn.setDisable(true);
                handleVote(r, true);
            });
            
            noBtn.setOnAction(e -> {
                votedRewardIdsInSession.add(r.getRewardId()); 
                yesBtn.setDisable(true);
                noBtn.setDisable(true);
                handleVote(r, false);
            });
            
            HBox voteBox = new HBox(5, voteLbl, yesBtn, noBtn);
            voteBox.setAlignment(Pos.CENTER);
            card.getChildren().add(voteBox);
        }
        return card;
    }

    private void selectDeleteProposal(Reward r) {
        ConfirmationScreen confirm = new ConfirmationScreen(
            "DELETE REWARD", 
            "Are you sure you want to permanently delete the reward: " + r.getName() + "?", 
            "Yes, Delete", 
            "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;", 
            () -> deleteProposalFromDatabase(r)
        );
        confirm.show(); 
    }

    private void deleteProposalFromDatabase(Reward r) {
        String deleteSql = "DELETE FROM rewards WHERE reward_id = ?";
        String deleteLogsSql = "DELETE FROM chore_reports WHERE title LIKE ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDel = conn.prepareStatement(deleteSql);
                 PreparedStatement psLogs = conn.prepareStatement(deleteLogsSql)) {
                
                psDel.setInt(1, r.getRewardId());
                psDel.executeUpdate();
                
                psLogs.setString(1, "%RewardID:" + r.getRewardId() + "%");
                psLogs.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) { e.printStackTrace(); }
        
        loadRewardsFromDatabase();
    }

    private void handlePurchase(Reward r) {
        Point model = pointSidebar.getPointModel();
        String user = pointSidebar.getCurrentUser().trim(); 
        
        System.out.println("[PURCHASE DEBUG] Buyer Username/Name: [" + user + "] | Reward Cost: " + r.getCost());

        // Έλεγχος επάρκειας πόντων
        if (!model.checkPoints(user, r.getCost())) {
            ErrorScreen.show("Your current point balance is too low to buy this reward.");
            return;
        }

        new ConfirmationScreen("CONFIRM PURCHASE", "Redeem " + r.getName() + " for " + r.getCost() + " pts?", "Yes, Buy", 
            "-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;", () -> {
                
                String logRedeem = "INSERT INTO user_redeemed_rewards (user_id, reward_id) VALUES " +
                                   "((SELECT user_id FROM users WHERE TRIM(username) = ? OR TRIM(display_name) = ? LIMIT 1), ?)";
                                   
                String deductPoints = "UPDATE user_points up " +
                                      "JOIN users u ON up.user_id = u.user_id " +
                                      "SET up.current_balance = up.current_balance - ? " +
                                      "WHERE (TRIM(u.username) = ? OR TRIM(u.display_name) = ?)";

                try (Connection conn = DatabaseManager.getConnection()) {
                    conn.setAutoCommit(false);
                    
                    try (PreparedStatement ps1 = conn.prepareStatement(logRedeem)) {
                        ps1.setString(1, user);
                        ps1.setString(2, user);
                        ps1.setInt(3, r.getRewardId());
                        ps1.executeUpdate();
                    }
                    
                    try (PreparedStatement ps2 = conn.prepareStatement(deductPoints)) {
                        ps2.setInt(1, r.getCost());
                        ps2.setString(2, user);
                        ps2.setString(3, user);
                        int rowsUpdated = ps2.executeUpdate();
                        System.out.println("[PURCHASE] Points deducted. Rows affected: " + rowsUpdated);
                    }
                    
                    conn.commit();
                } catch (Exception ex) { 
                    System.err.println("Purchase transaction failed:");
                    ex.printStackTrace(); 
                }
                
                pointSidebar.updateUI();
                loadRewardsFromDatabase();
        }).show();
    }

    private void handleVote(Reward r, boolean approve) {
        String currentUsername = pointSidebar.getCurrentUser().trim();
        int roomId = pointSidebar.getCurrentRoomId(); // Ανάκτηση του σωστού room ID

        // ΔΙΟΡΘΩΘΗΚΕ: Προσθήκη room_id στο INSERT
        String logVoteSql = "INSERT INTO chore_reports (chore_id, room_id, title, description) " +
                            "VALUES ((SELECT chore_id FROM chores WHERE room_id = ? LIMIT 1), ?, ?, ?)";
                            
        String updateRewardSql = approve ? 
                "UPDATE rewards SET approve_votes = approve_votes + 1 WHERE reward_id = ? AND room_id = ?" :
                "UPDATE rewards SET reject_votes = reject_votes + 1 WHERE reward_id = ? AND room_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psLog = conn.prepareStatement(logVoteSql)) {
                psLog.setInt(1, roomId); // Για το subquery του chore_id ώστε να βρει chore του σωστού δωματίου
                psLog.setInt(2, roomId); // ΔΙΟΡΘΩΣΗ: Το room_id για το report
                psLog.setString(3, (approve ? "REWARD_APPROVE_" : "REWARD_REJECT_") + currentUsername);
                psLog.setString(4, "RewardID:" + r.getRewardId()); 
                psLog.executeUpdate();
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateRewardSql)) {
                psUpdate.setInt(1, r.getRewardId());
                psUpdate.setInt(2, roomId); // Ασφάλεια εδώ!
                psUpdate.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }

        checkMajorityStatus(r.getRewardId());
        loadRewardsFromDatabase();
        pointSidebar.updateUI();
    }

    private void checkMajorityStatus(int rewardId) {
        String selectSql = "SELECT approve_votes, reject_votes FROM rewards WHERE reward_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setInt(1, rewardId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int app = rs.getInt("approve_votes");
                    int rej = rs.getInt("reject_votes");
                    int totalVotesLogged = app + rej;

                    if (totalVotesLogged >= totalRoommatesCount) {
                        int majorityNeeded = (totalRoommatesCount / 2) + 1; 

                        if (app >= majorityNeeded) {
                            String approveSql = "UPDATE rewards SET is_available = TRUE WHERE reward_id = ?";
                            try (PreparedStatement psApp = conn.prepareStatement(approveSql)) {
                                psApp.setInt(1, rewardId);
                                psApp.executeUpdate();
                                System.out.println("Reward approved by strict majority of all members!");
                            }
                            
                            // ΔΙΟΡΘΩΣΗ: Αφαιρούμε το ID από τα voted της τρέχουσας συνεδρίας 
                            // για να εμφανιστεί αμέσως το κουμπί BUY καθαρό για όλους!
                            votedRewardIdsInSession.remove(Integer.valueOf(rewardId));
                            
                        } else {
                            String deleteSql = "DELETE FROM rewards WHERE reward_id = ?";
                            String deleteLogs = "DELETE FROM chore_reports WHERE description = ?";
                            try (PreparedStatement psDel = conn.prepareStatement(deleteSql);
                                 PreparedStatement psLogs = conn.prepareStatement(deleteLogs)) {
                                psDel.setInt(1, rewardId);
                                psDel.executeUpdate();
                                psLogs.setString(1, "RewardID:" + rewardId);
                                psLogs.executeUpdate();
                                System.out.println("Reward proposal failed and was deleted automatically.");
                            }
                            votedRewardIdsInSession.remove(Integer.valueOf(rewardId));
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void insertProposalIntoDatabase(String name, int cost) {
        String[] colors = {"#FEF08A", "#A7F3D0", "#BBF7D0", "#FED7AA", "#E9D5FF"};
        String chosenColor = colors[(int) (Math.random() * colors.length)];

        // Χρησιμοποιούμε το ID από το sidebar που είναι πλέον σωστό!
        int roomId = pointSidebar.getCurrentRoomId(); 

        String query = "INSERT INTO rewards (room_id, reward_name, cost, is_available, approve_votes, reject_votes, ui_color) VALUES (?, ?, ?, FALSE, 0, 0, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roomId);
            ps.setString(2, name);
            ps.setInt(3, cost);
            ps.setString(4, chosenColor);
            ps.executeUpdate();
            
            System.out.println("[DB] New reward proposal inserted for room: " + roomId);
            loadRewardsFromDatabase(); 
        } catch (Exception e) { 
            System.err.println("Database error during insertProposalIntoDatabase:");
            e.printStackTrace(); 
        }
    }
}