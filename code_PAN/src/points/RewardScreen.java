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
    private final List<String> members = List.of("Makis", "Manos", "Giannis"); // 3 Μέλη
    private VBox container;

    public RewardScreen(PointScreen sidebar) {
        this.pointSidebar = sidebar;
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        HBox.setHgrow(this, Priority.ALWAYS);

        HBox titleRow = new HBox(10);
        Label title = new Label("🛒 REWARDS & VOTING CENTER");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        Pane space = new Pane(); HBox.setHgrow(space, Priority.ALWAYS);
        
        // Κουμπί εισαγωγής προνομίου (Εναλλακτική Ροή 4)
        Button proposeBtn = new Button("+ Propose Pass");
        proposeBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        proposeBtn.setOnAction(e -> {
            NewRewardScreen form = new NewRewardScreen((name, cost) -> insertProposalIntoDatabase(name, cost));
            form.show();
        });
        
        titleRow.getChildren().addAll(title, space, proposeBtn);
        container = new VBox(10);
        this.getChildren().addAll(titleRow, container);
        
        loadRewardsFromDatabase();
    }

    private void loadRewardsFromDatabase() {
        container.getChildren().clear();
        String query = "SELECT * FROM rewards WHERE room_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, pointSidebar.getMockRoomId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reward r = new Reward(
                        rs.getInt("reward_id"),
                        rs.getString("reward_name"),
                        rs.getInt("cost"),
                        rs.getBoolean("is_available"),
                        rs.getString("ui_color"),
                        0, 0 // Τα votes γίνονται live στο runtime
                    );
                    container.getChildren().add(createCard(r));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        // Αν είναι εγκεκριμένο, εμφανίζεται το κουμπί αγοράς
        if (r.isAvailable()) {
            Button buyBtn = new Button("BUY");
            buyBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
            buyBtn.setOnAction(e -> handlePurchase(r));
            card.getChildren().add(buyBtn);
        } 
        // Αλλιώς εμφανίζονται τα κουμπιά ψηφοφορίας (Εναλλακτική Ροή 4.α.5)
        else {
            Label voteLbl = new Label("VOTE: ");
            voteLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #4F46E5; -fx-font-weight: bold;");
            
            Button yesBtn = new Button("✓");
            yesBtn.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #059669; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
            yesBtn.setOnAction(e -> handleVote(r, true));
            
            Button noBtn = new Button("✕");
            noBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
            noBtn.setOnAction(e -> handleVote(r, false));
            
            HBox voteBox = new HBox(5, voteLbl, yesBtn, noBtn);
            voteBox.setAlignment(Pos.CENTER);
            card.getChildren().add(voteBox);
        }
        return card;
    }

    private void handlePurchase(Reward r) {
        Point model = pointSidebar.getPointModel();
        String user = pointSidebar.getCurrentUser();
        
        if (!model.checkPoints(user, r.getCost())) {
            ErrorScreen.show("Your current point balance is too low to buy this reward.");
            return;
        }
        
        new ConfirmationScreen("CONFIRM PURCHASE", "Redeem " + r.getName() + " for " + r.getCost() + " pts?", "Yes, Buy", 
            "-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;", () -> {
                model.pointDeduction(user, r.getCost(), r.getRewardId());
                pointSidebar.updateUI();
        }).show();
    }

    private void handleVote(Reward r, boolean approve) {
        boolean majorityReached = r.vote(approve, members.size());
        
        if (majorityReached) {
            // Ροή 4.α.6: Η πλειοψηφία συμφωνεί -> Ενεργοποίηση
            String updateSql = "UPDATE rewards SET is_available = TRUE WHERE reward_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, r.getRewardId());
                ps.executeUpdate();
                System.out.println("Reward approved by majority vote!");
            } catch (Exception e) { e.printStackTrace(); }
            loadRewardsFromDatabase();
        } else if (!approve && r.getRejectVotes() >= (members.size() / 2) + 1) {
            // Ροή 4.1: Η πλειοψηφία διαφωνεί -> Ακύρωση / Διαγραφή
            String deleteSql = "DELETE FROM rewards WHERE reward_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, r.getRewardId());
                ps.executeUpdate();
                System.out.println("Reward rejected and canceled.");
            } catch (Exception e) { e.printStackTrace(); }
            loadRewardsFromDatabase();
        }
    }

    private void insertProposalIntoDatabase(String name, int cost) {
        // Τυχαία επιλογή χρώματος για το card icon
        String[] colors = {"#FEF08A", "#A7F3D0", "#BBF7D0", "#FED7AA", "#E9D5FF"};
        String chosenColor = colors[(int) (Math.random() * colors.length)];

        String query = "INSERT INTO rewards (room_id, reward_name, cost, is_available, ui_color) VALUES (?, ?, ?, FALSE, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, pointSidebar.getMockRoomId());
            ps.setString(2, name);
            ps.setInt(3, cost);
            ps.setString(4, chosenColor);
            ps.executeUpdate();
            
            loadRewardsFromDatabase(); // Φρεσκάρισμα
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}