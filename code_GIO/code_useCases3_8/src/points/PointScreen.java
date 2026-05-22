package points;

import entities.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import util.DatabaseManager;

public class PointScreen extends VBox {
    private final Point pointModel;
    private final String currentUser = "Manos"; // Mock τρέχων χρήστης
    private final int mockRoomId = 1;
    private VBox leaderboardList;
    private VBox redeemedList;
    private Stage primaryStage;
    private Runnable backAction;

    // ΔΙΟΡΘΩΣΗ: Στατικό instance για παγκόσμια πρόσβαση realtime ανανέωσης
    private static PointScreen activeInstance;

    public static PointScreen getActiveInstance() {
        return activeInstance;
    }

    // --- Constructor 1: Συνδέεται με το Central Hub ---
    public PointScreen(Point pointModel, Runnable backAction) {
        this.pointModel = pointModel;
        this.backAction = backAction;
        
        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.setPrefWidth(320);
        this.setStyle("-fx-background-color: #F1F5F9;");
        
        activeInstance = this; // Αποθήκευση του ενεργού instance στη μνήμη
        buildSections();
    }

    // --- Constructor 2: Fallback για συμβατότητα ---
    public PointScreen(Point pointModel) {
        this(pointModel, null);
    }

    // Μέθοδος για να περάσουμε το Stage από τον εξωτερικό Launcher
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void buildSections() {
        // Header με κουμπί επιστροφής Back
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E3A5F; -fx-cursor: hand; -fx-padding: 0 5 0 0;");
        backBtn.setOnAction(e -> {
            if (primaryStage != null) {
                primaryStage.close(); 
            }
            if (backAction != null) {
                backAction.run(); 
            }
        });
        
        Label mainTitle = new Label("Points Center");
        mainTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        headerBox.getChildren().addAll(backBtn, mainTitle);
        this.getChildren().add(headerBox);

        // Leaderboard Box
        VBox lbBox = new VBox(10);
        lbBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
        Label lbHeader = new Label("🏆 LEADERBOARD");
        lbHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        leaderboardList = new VBox(8);
        lbBox.getChildren().addAll(lbHeader, leaderboardList);
        
        // Redeemed Box
        VBox rbBox = new VBox(10);
        rbBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
        Label rbHeader = new Label("🎁 REDEEMED LOGS");
        rbHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        redeemedList = new VBox(8);
        rbBox.getChildren().addAll(rbHeader, redeemedList);

        this.getChildren().addAll(lbBox, rbBox);
        updateUI();
    }

    public void updateUI() {
        if (leaderboardList == null) return;
        leaderboardList.getChildren().clear();
        redeemedList.getChildren().clear();

        // 1. Live SQL ανάκτηση του Leaderboard
        String lbQuery = "SELECT u.username, up.current_balance FROM user_points up " +
                         "JOIN users u ON up.user_id = u.user_id WHERE up.room_id = ? ORDER BY up.current_balance DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(lbQuery)) {
            ps.setInt(1, mockRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    String user = rs.getString("username");
                    int bal = rs.getInt("current_balance");
                    leaderboardList.getChildren().add(createRow(rank + ". " + user, bal + " pts"));
                    rank++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Live SQL ανάκτηση των εξαργυρωμένων προνομίων
        String redQuery = "SELECT u.username, r.reward_name FROM user_redeemed_rewards urr " +
                          "JOIN users u ON urr.user_id = u.user_id " +
                          "JOIN rewards r ON urr.reward_id = r.reward_id WHERE r.room_id = ? ORDER BY urr.redeemed_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(redQuery)) {
            ps.setInt(1, mockRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    addRedeemedRow(rs.getString("username") + ": " + rs.getString("reward_name"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createRow(String name, String pts) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(new Circle(12, Color.web("#CBD5E1")), new Label(name), new Pane(), new Label(pts));
        HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
        return row;
    }

    private void addRedeemedRow(String logText) {
        Label l = new Label("✔ " + logText);
        l.setStyle("-fx-font-size: 11; -fx-text-fill: #10B981; -fx-font-weight: bold;");
        redeemedList.getChildren().add(l);
    }

    public Point getPointModel() { return pointModel; }
    public String getCurrentUser() { return currentUser; }
    public int getMockRoomId() { return mockRoomId; }

    // --- Ο ΚΑΘΑΡΟΣ LAUNCHER (Εναρμονισμένος με το Hub) ---
    public static class Launcher {
        public void start(Stage primaryStage) {
            Point pointModel = new Point(null, 0, null); 
            
            PointScreen sidebar = new PointScreen(pointModel, () -> primaryStage.close());
            sidebar.setStage(primaryStage); 
            
            RewardScreen shop = new RewardScreen(sidebar);
            
            HBox dashboard = new HBox(sidebar, shop);
            dashboard.setStyle("-fx-background-color: #F8FAF9;");

            Scene scene = new Scene(dashboard, 950, 600);
            primaryStage.setTitle("HOMY - Points Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        }
    }
}