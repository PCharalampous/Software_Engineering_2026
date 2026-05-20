package chores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import entities.Point;
import entities.Chore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import ui.ConfirmationScreen;
import util.DatabaseManager;

public class ChoreScreen extends VBox {
    
    private final ObservableList<Chore> choresList = FXCollections.observableArrayList();
    private final ObservableList<Point> historyList = FXCollections.observableArrayList();
    
    // Εναρμόνιση με τα πραγματικά ονόματα της βάσης σου
    private final List<String> members = new ArrayList<>(List.of("makis99", "anna_dev", "george21"));
    private final int mockRoomId = 1; 

    private VBox rootLayout; 
    private VBox listContainer;
    private ListView<String> historyListView;
    
    private Stage primaryStage;
    private Runnable backAction;

    public ChoreScreen() {
        this.backAction = null;
    }

    public ChoreScreen(Runnable backAction) {
        this.backAction = backAction;
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        rootLayout = new VBox();
        rootLayout.setSpacing(0);
        rootLayout.setStyle("-fx-background-color: #F3F4F6;");
        
        // Φόρτωση και των δύο λιστών live από το Clever Cloud
        loadChoresFromDatabase();
        loadHistoryFromDatabase();
        
        display(); 

        Scene scene = new Scene(rootLayout, 850, 650);
        primaryStage.setTitle("HOMY - Chores Module");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // --- SQL DATABASE OPERATIONS ---
    
    private void loadChoresFromDatabase() {
        choresList.clear();
        int majority = (members.size() / 2) + 1;
        String query = "SELECT * FROM chores WHERE room_id = ? AND (chore_status IN ('Pending', 'Suspended') OR (chore_status = 'Completed' AND approve_votes < ?))";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, mockRoomId);
            ps.setInt(2, majority);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chore chore = new Chore(
                        rs.getInt("chore_id"),
                        rs.getString("chore_name"),
                        rs.getInt("points"),
                        rs.getString("assignee"),
                        rs.getString("chore_status"),
                        rs.getInt("approve_votes"),
                        rs.getInt("reject_votes"),
                        members.size()
                    );
                    choresList.add(chore);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading chores from Database:");
            e.printStackTrace();
        }
    }

    // ΔΙΟΡΘΩΣΗ: Live ανάκτηση του μόνιμου ιστορικού αγγαρειών από τη βάση δεδομένων
    private void loadHistoryFromDatabase() {
        historyList.clear();
        String query = "SELECT ch.completed_by, c.points, c.chore_name FROM chore_history ch " +
                       "JOIN chores c ON ch.chore_id = c.chore_id WHERE ch.room_id = ? ORDER BY ch.completed_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, mockRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Point p = new Point(
                        rs.getString("completed_by"),
                        rs.getInt("points"),
                        rs.getString("chore_name")
                    );
                    historyList.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading chore history from Database:");
            e.printStackTrace();
        }
    }

    private void insertChoreIntoDatabase(Chore chore) {
        String query = "INSERT INTO chores (room_id, chore_name, points, assignee, chore_status, approve_votes, reject_votes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, mockRoomId);
            ps.setString(2, chore.getName());
            ps.setInt(3, chore.getPoints());
            ps.setString(4, chore.getAssignee());
            ps.setString(5, chore.getStatus());
            ps.setInt(6, chore.getApproveVotes());
            ps.setInt(7, chore.getRejectVotes());
            
            ps.executeUpdate();
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    chore.setChoreId(generatedKeys.getInt(1));
                }
            }
            choresList.add(chore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateChoreInDatabase(Chore chore) {
        String query = "UPDATE chores SET chore_status = ?, approve_votes = ?, reject_votes = ? WHERE chore_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, chore.getStatus());
            ps.setInt(2, chore.getApproveVotes());
            ps.setInt(3, chore.getRejectVotes());
            ps.setInt(4, chore.getChoreId());
            
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void archiveChoreToHistory(Chore chore) {
        String insertHistorySql = "INSERT INTO chore_history (room_id, chore_id, completed_by) VALUES (?, ?, ?)";
        
        String updateUserPointsSql = "UPDATE user_points up JOIN users u ON up.user_id = u.user_id " +
                                     "SET up.current_balance = up.current_balance + ? WHERE u.username = ? AND up.room_id = ?";
        
        String updateAllMembersPointsSql = "UPDATE user_points SET current_balance = current_balance + ? WHERE room_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            // 1. Καταγραφή στον πίνακα ολοκληρωμένων (chore_history)
            try (PreparedStatement psHistory = conn.prepareStatement(insertHistorySql)) {
                psHistory.setInt(1, mockRoomId);
                psHistory.setInt(2, chore.getChoreId());
                psHistory.setString(3, chore.getAssignee());
                psHistory.executeUpdate();
            }
            
            // 2. Πίστωση πόντων
            if ("All Roommates".equals(chore.getAssignee())) {
                int splitPoints = chore.getPoints() / members.size();
                try (PreparedStatement psAllPoints = conn.prepareStatement(updateAllMembersPointsSql)) {
                    psAllPoints.setInt(1, splitPoints);
                    psAllPoints.setInt(2, mockRoomId);
                    psAllPoints.executeUpdate();
                }
            } else {
                try (PreparedStatement psUserPoints = conn.prepareStatement(updateUserPointsSql)) {
                    psUserPoints.setInt(1, chore.getPoints());
                    psUserPoints.setString(2, chore.getAssignee());
                    psUserPoints.setInt(3, mockRoomId);
                    psUserPoints.executeUpdate();
                }
            }
            
            // 3. Ενημέρωση κατάστασης της αγγαρείας σε Completed
            chore.setStatus("Completed");
            String updateStatusSql = "UPDATE chores SET chore_status = ?, approve_votes = ?, reject_votes = ? WHERE chore_id = ?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateStatusSql)) {
                psUpdate.setString(1, chore.getStatus());
                psUpdate.setInt(2, chore.getApproveVotes());
                psUpdate.setInt(3, chore.getRejectVotes());
                psUpdate.setInt(4, chore.getChoreId());
                psUpdate.executeUpdate();
            }
            
            conn.commit();
            choresList.remove(chore); 
            
            // Κάνουμε ξανά φόρτωση του ιστορικού από τη βάση για να ανανεωθεί σωστά το tray
            loadHistoryFromDatabase();
            refreshHistoryUI();
            
            System.out.println("Points successfully credited and chore archived!");
        } catch (Exception e) {
            System.err.println("Transaction failed inside archiveChoreToHistory:");
            e.printStackTrace();
        }
    }

    private void deleteChoreFromDatabase(Chore chore) {
        String query = "DELETE FROM chores WHERE chore_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, chore.getChoreId());
            ps.executeUpdate();
            choresList.remove(chore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------

    public void display() {
        rootLayout.getChildren().clear();

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
        backBtn.setOnAction(e -> {
            if (primaryStage != null) primaryStage.close();
            if (backAction != null) backAction.run();
        });
        
        Label headerTitle = new Label("Chore Management");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        headerTitle.setTextFill(Color.web("#1F2937"));
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button weeklyBtn = new Button("🔄 Distribute Chores");
        weeklyBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;");
        weeklyBtn.setOnAction(e -> {
            ChoreDistributionService service = new ChoreDistributionService(mockRoomId, members);
            service.distributeWeeklyChores();
            loadChoresFromDatabase();
            refreshChoresUI();
        });

        header.getChildren().addAll(backBtn, headerTitle, headerSpacer, weeklyBtn);
        rootLayout.getChildren().add(header);

        // Scrollable List Area
        listContainer = new VBox(15);
        listContainer.setPadding(new Insets(25));
        refreshChoresUI();

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #F3F4F6; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        rootLayout.getChildren().add(scrollPane);

        // Bottom History Tray
        HBox bottomArea = new HBox(20);
        bottomArea.setPadding(new Insets(20, 25, 20, 25));
        bottomArea.setAlignment(Pos.CENTER_LEFT);
        bottomArea.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, -2);");

        VBox historyBox = new VBox(8);
        historyBox.setPrefWidth(350);
        Label historyTitle = new Label("History (Points)");
        historyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        historyListView = new ListView<>();
        historyListView.setPrefHeight(100);
        historyListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #E5E7EB;");
        refreshHistoryUI();
        historyBox.getChildren().addAll(historyTitle, historyListView);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+");
        addBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 65px; -fx-min-height: 65px; -fx-font-size: 28px; -fx-font-weight: bold; -fx-cursor: hand;");
        addBtn.setOnAction(e -> selectAddChore());

        bottomArea.getChildren().addAll(historyBox, spacer, addBtn);
        rootLayout.getChildren().add(bottomArea);
    }

    private void refreshChoresUI() {
        listContainer.getChildren().clear();
        for (Chore chore : choresList) {
            listContainer.getChildren().add(createChoreCard(chore));
        }
    }

    private void refreshHistoryUI() {
        historyListView.getItems().clear();
        for (Point p : historyList) {
            historyListView.getItems().add("💎 " + p.toString());
        }
    }

    private HBox createChoreCard(Chore chore) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E5E7EB;");

        VBox infoBox = new VBox(5);
        Label nameLbl = new Label(chore.getName());
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        Label pointsLbl = new Label(chore.getPoints() + " Points");
        pointsLbl.setTextFill(Color.web("#6B7280"));
        infoBox.getChildren().addAll(nameLbl, pointsLbl);
        infoBox.setPrefWidth(160);

        Label arrow = new Label("➡");

        VBox assigneeBox = new VBox(2);
        Label assignLbl = new Label(chore.getAssignee());
        assignLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        assignNoticeStyle(chore, assignLbl);
        assigneeBox.getChildren().addAll(new Label("Assignee"), assignLbl);
        assigneeBox.setPrefWidth(120); 

        Label statusBadge = new Label(chore.getStatus());
        statusBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        statusBadge.setPadding(new Insets(4, 10, 4, 10));
        
        String badgeStyle = switch (chore.getStatus()) {
            case "Pending" -> "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
            case "Completed" -> "-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB;";
            case "Suspended" -> "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
            default -> "-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563;";
        };
        statusBadge.setStyle(badgeStyle + " -fx-background-radius: 20;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button reportBtn = createStyledButton("Report", "#F3F4F6", "#4B5563");
        reportBtn.setOnAction(e -> selectReportChore(chore));

        if (chore.getStatus().equals("Pending")) {
            Button completeBtn = createStyledButton("Complete", "#10B981", "white");
            completeBtn.setOnAction(e -> {
                chore.setStatus("Completed");
                updateChoreInDatabase(chore);
                refreshChoresUI();
            });
            
            Button delBtn = createStyledButton("Delete", "#FEE2E2", "#DC2626");
            delBtn.setOnAction(e -> selectDeleteChore(chore));
            
            btnBox.getChildren().addAll(reportBtn, delBtn, completeBtn);
        } 
        else if (chore.getStatus().equals("Completed")) {
            Button approveBtn = createCircleButton("✓", "#10B981");
            approveBtn.setOnAction(e -> {
                boolean isApproved = chore.vote(true);
                if (isApproved) {
                    archiveChoreToHistory(chore); 
                    
                    if (points.PointScreen.getActiveInstance() != null) {
                        points.PointScreen.getActiveInstance().updateUI();
                    }
                } else {
                    updateChoreInDatabase(chore);
                }
                refreshChoresUI();
            });
            
            Button rejectBtn = createCircleButton("✕", "#EF4444");
            rejectBtn.setOnAction(e -> {
                chore.vote(false);
                updateChoreInDatabase(chore);
                refreshChoresUI();
            });
            
            btnBox.getChildren().addAll(reportBtn, rejectBtn, approveBtn);
        }
        else if (chore.getStatus().equals("Suspended")) {
            Button restoreBtn = createStyledButton("Restore", "#D1FAE5", "#059669");
            restoreBtn.setOnAction(e -> {
                chore.setStatus("Pending"); 
                updateChoreInDatabase(chore); 
                
                String deleteReportQuery = "DELETE FROM chore_reports WHERE chore_id = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(deleteReportQuery)) {
                    ps.setInt(1, chore.getChoreId());
                    ps.executeUpdate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                refreshChoresUI();      
            });
            
            Button delBtn = createStyledButton("Delete", "#FEE2E2", "#DC2626");
            delBtn.setOnAction(e -> selectDeleteChore(chore));
            
            btnBox.getChildren().addAll(reportBtn, restoreBtn, delBtn);
        }

        card.getChildren().addAll(infoBox, arrow, assigneeBox, statusBadge, spacer, btnBox);
        return card;
    }

    private void selectAddChore() {
        NewChoreScreen form = new NewChoreScreen(members, (name, pts, duty) -> {
            Chore newChore = new Chore(name, pts, duty, members.size());
            insertChoreIntoDatabase(newChore); 
            refreshChoresUI();
        });
        form.show();
    }

    private void selectReportChore(Chore chore) {
        ReportScreen report = new ReportScreen(chore, (description) -> {
            chore.setStatus("Suspended"); 
            updateChoreInDatabase(chore); 
            refreshChoresUI();            

            String query = "INSERT INTO chore_reports (chore_id, title, description) VALUES (?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, chore.getChoreId());
                ps.setString(2, "Chore Issue: " + chore.getName()); 
                ps.setString(3, description);
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        report.show();
    }

    private void selectDeleteChore(Chore chore) {
        ConfirmationScreen confirm = new ConfirmationScreen(
            "DELETE CHORE", 
            "Are you sure you want to delete: " + chore.getName() + "?", 
            "Yes, Delete", 
            "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;", 
            () -> {
                deleteChoreFromDatabase(chore); 
                refreshChoresUI();
            }
        );
        confirm.show();
    }

    private Button createStyledButton(String text, String bg, String textFill) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + textFill + "; -fx-background-radius: 6; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;");
        return btn;
    }

    private Button createCircleButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 35px; -fx-min-height: 35px; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        return btn;
    }

    private void assignNoticeStyle(Chore chore, Label assignLbl) {
        if ("makis99".equals(chore.getAssignee())) {
            assignLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #4F46E5;");
        } else if ("All Roommates".equals(chore.getAssignee())) {
            assignLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #14B8A6;");
        }
    }
}