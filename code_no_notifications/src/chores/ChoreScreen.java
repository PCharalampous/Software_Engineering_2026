package chores;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import entities.Point;
import entities.Chore;
import entities.Authentication;
import entities.User;
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
    
    private final List<String> members = new ArrayList<>();
    private int currentRoomId = 0; 

    private final List<Integer> votedChoreIdsInSession = new ArrayList<>();

    private VBox rootLayout; 
    private VBox listContainer;
    private ListView<String> historyListView;
    
    private Stage primaryStage;
    private Runnable backAction;

    public ChoreScreen() {
        this.backAction = null;
        initSessionData();
    }

    public ChoreScreen(Runnable backAction) {
        this.backAction = backAction;
        initSessionData();
    }

    private void initSessionData() {
        User sessionUser = Authentication.getCurrentUser();
        if (sessionUser == null) {
            System.err.println("[DEBUG] ChoreScreen: No authenticated user found!");
            return; 
        }

        int userId = sessionUser.getId(); 
        System.out.println("[DEBUG] Initializing ChoreScreen for UserID: " + userId);

        String roomQuery = "SELECT room_id FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(roomQuery)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    this.currentRoomId = rs.getInt("room_id");
                    System.out.println("[DEBUG] Found RoomID: " + this.currentRoomId);
                } else {
                    System.err.println("[DEBUG] No room assigned to this user.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (this.currentRoomId <= 0) {
            System.err.println("[DEBUG] ChoreScreen: Cannot load data, currentRoomId is 0 or NULL.");
            return;
        }

        members.clear();
        String membersQuery = "SELECT username FROM users WHERE room_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(membersQuery)) {
            ps.setInt(1, this.currentRoomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(rs.getString("username"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        loadChoresFromDatabase();
        loadHistoryFromDatabase();
        loadVotesFromDatabase();

        rootLayout = new VBox();
        rootLayout.setSpacing(0);
        rootLayout.setStyle("-fx-background-color: #F3F4F6;");
        display();
        
        Scene scene = new Scene(rootLayout, 850, 650);
        primaryStage.setTitle("HOMY - Chores Module");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    
    private void loadChoresFromDatabase() {
        choresList.clear();
        String query = "SELECT * FROM chores WHERE room_id = ? AND chore_status IN ('Pending', 'Suspended', 'Completed')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, this.currentRoomId);
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadHistoryFromDatabase() {
        historyList.clear();
        String query = "SELECT completed_by, chore_name, points FROM chore_history " +
                       "WHERE room_id = ? ORDER BY completed_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, this.currentRoomId);
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
        } catch (Exception e) { e.printStackTrace(); }
        
        if (historyListView != null) {
            refreshHistoryUI();
        }
    }
    
    private void loadVotesFromDatabase() {
        votedChoreIdsInSession.clear();
        User sessionUser = Authentication.getCurrentUser();
        if (sessionUser == null) return;
        
        String currentUsername = sessionUser.getUsername().trim();
        String query = "SELECT chore_id FROM chore_reports WHERE room_id = ? AND (title = ? OR title = ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, this.currentRoomId);
            ps.setString(2, "VOTE_APPROVE_" + currentUsername);
            ps.setString(3, "VOTE_REJECT_" + currentUsername);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    votedChoreIdsInSession.add(rs.getInt("chore_id"));
                }
            }
            System.out.println("[DEBUG] Loaded " + votedChoreIdsInSession.size() + " existing votes for " + currentUsername);
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void insertChoreIntoDatabase(Chore chore) {
        String query = "INSERT INTO chores (room_id, chore_name, points, assignee, chore_status, approve_votes, reject_votes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, this.currentRoomId);
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

    private void logVoteToDatabase(int choreId, String username, boolean approve) {
        String query = "INSERT INTO chore_reports (chore_id, room_id, title, description) VALUES (?, ?, ?, 'Chore Vote Log')";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, choreId);
            ps.setInt(2, this.currentRoomId);
            ps.setString(3, (approve ? "VOTE_APPROVE_" : "VOTE_REJECT_") + username.trim());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void archiveChoreToHistory(Chore chore) {
        String insertHistorySql = "INSERT INTO chore_history (room_id, chore_id, completed_by, chore_name, points) VALUES (?, ?, ?, ?, ?)";
        
        // 1. Βρίσκουμε το user_id με βάση το username ή το display_name
        String findUserIdSql = "SELECT user_id FROM users WHERE TRIM(username) = ? OR TRIM(display_name) = ? LIMIT 1";
        
        // 2. Safe Upsert: Αν υπάρχει ήδη το user_id (που είναι Primary Key), κάνει UPDATE προσθέτοντας τους πόντους
        String upsertUserPointsSql = "INSERT INTO user_points (user_id, room_id, current_balance) VALUES (?, ?, ?) " +
                                     "ON DUPLICATE KEY UPDATE current_balance = current_balance + VALUES(current_balance)";
        
        String resetChoreSql = "UPDATE chores SET chore_status = 'Pending', assignee = 'Unassigned', " +
                               "approve_votes = 0, reject_votes = 0 WHERE chore_id = ?";
        String clearVotesSql = "DELETE FROM chore_reports WHERE chore_id = ? AND (title LIKE 'VOTE_APPROVE_%' OR title LIKE 'VOTE_REJECT_%')";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Εισαγωγή στο ιστορικό
            try (PreparedStatement psHistory = conn.prepareStatement(insertHistorySql)) {
                psHistory.setInt(1, this.currentRoomId);
                psHistory.setInt(2, chore.getChoreId());
                psHistory.setString(3, chore.getAssignee());
                psHistory.setString(4, chore.getName());
                psHistory.setInt(5, chore.getPoints());
                psHistory.executeUpdate();
            }

            // 2. Εύρεση του σωστού user_id από τον πίνακα users
            int targetUserId = -1;
            String rawAssignee = chore.getAssignee().trim();
            try (PreparedStatement psFindId = conn.prepareStatement(findUserIdSql)) {
                psFindId.setString(1, rawAssignee);
                psFindId.setString(2, rawAssignee);
                try (ResultSet rs = psFindId.executeQuery()) {
                    if (rs.next()) {
                        targetUserId = rs.getInt("user_id");
                    }
                }
            }

            // 3. Εκτέλεση του Upsert για τους πόντους (Δεν θα ξαναχτυπήσει ποτέ Duplicate Entry)
            if (targetUserId != -1) {
                try (PreparedStatement psUpsert = conn.prepareStatement(upsertUserPointsSql)) {
                    psUpsert.setInt(1, targetUserId);
                    psUpsert.setInt(2, this.currentRoomId);
                    psUpsert.setInt(3, chore.getPoints()); 
                    psUpsert.executeUpdate();
                }
            } else {
                System.err.println("[WARNING] ChoreScreen: Could not find user_id for assignee: " + rawAssignee);
            }

            // 4. Επαναφορά της αγγαρείας σε κατάσταση Pending / Unassigned για τον επόμενο γύρο
            try (PreparedStatement psReset = conn.prepareStatement(resetChoreSql)) {
                psReset.setInt(1, chore.getChoreId());
                psReset.executeUpdate();
            }

            // 5. Διαγραφή των ψήφων (VOTE_APPROVE / VOTE_REJECT) από τον πίνακα reports
            try (PreparedStatement psClearVotes = conn.prepareStatement(clearVotesSql)) {
                psClearVotes.setInt(1, chore.getChoreId());
                psClearVotes.executeUpdate();
            }

            // Οριστικοποίηση αλλαγών στη βάση (Commit)
            conn.commit();
            choresList.remove(chore);

            // Επαναφόρτωση δεδομένων και συγχρονισμός της RAM με τη βάση
            loadChoresFromDatabase();
            loadVotesFromDatabase(); 
            refreshChoresUI();
            loadHistoryFromDatabase();
            refreshHistoryUI();

            if (points.PointScreen.getActiveInstance() != null) {
                javafx.application.Platform.runLater(() -> points.PointScreen.getActiveInstance().updateUI());
            }

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Transaction rolled back in archiveChoreToHistory!");
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

    public void display() {
        rootLayout.getChildren().clear();

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
            ChoreDistributionService service = new ChoreDistributionService(this.currentRoomId, members);
            service.distributeWeeklyChores();
            
            // ΚΑΘΑΡΙΣΜΟΣ ΚΑΙ ΕΠΑΝΑΦΟΡΤΩΣΗ ΓΙΑ ΝΑ ΜΗΝ ΕΠΙΤΡΕΠΕΙ ΔΙΠΛΟ VOTE
            votedChoreIdsInSession.clear();
            loadChoresFromDatabase();
            loadVotesFromDatabase(); // <-- ΕΔΩ: Διαβάζει τις σβησμένες ψήφους από τη βάση και ξεκλειδώνει τα κουμπιά σωστά
            refreshChoresUI();
            loadHistoryFromDatabase();
            refreshHistoryUI();
        });

        header.getChildren().addAll(backBtn, headerTitle, headerSpacer, weeklyBtn);
        rootLayout.getChildren().add(header);

        listContainer = new VBox(15);
        listContainer.setPadding(new Insets(25));
        refreshChoresUI();

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #F3F4F6; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        rootLayout.getChildren().add(scrollPane);

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
        if (historyListView == null) return; 
        historyListView.getItems().clear();
        for (Point p : historyList) {
            historyListView.getItems().add("💎 " + p.toString());
        }
    }
    
    private void resetChoreToPending(Chore chore) {
        chore.setStatus("Pending");
        String resetChoreSql = "UPDATE chores SET chore_status = 'Pending', approve_votes = 0, reject_votes = 0 WHERE chore_id = ?";
        String clearVotesSql = "DELETE FROM chore_reports WHERE chore_id = ? AND (title LIKE 'VOTE_APPROVE_%' OR title LIKE 'VOTE_REJECT_%')";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(resetChoreSql);
                 PreparedStatement ps2 = conn.prepareStatement(clearVotesSql)) {
                ps1.setInt(1, chore.getChoreId());
                ps1.executeUpdate();
                ps2.setInt(1, chore.getChoreId());
                ps2.executeUpdate();
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        
        loadChoresFromDatabase();
        refreshChoresUI(); // <--- ΠΡΟΣΘΗΚΗ: Σχεδιάζει ξανά το UI με τα φρέσκα δεδομένα από τη βάση!
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

        User sessionUser = Authentication.getCurrentUser();
        String currentUsername = (sessionUser != null) ? sessionUser.getUsername().trim() : "2"; 
        String choreAssignee = (chore.getAssignee() != null) ? chore.getAssignee().trim() : "";

        if (chore.getStatus().equals("Pending")) {
            Button delBtn = createStyledButton("Delete", "#FEE2E2", "#DC2626");
            delBtn.setOnAction(e -> selectDeleteChore(chore));
            btnBox.getChildren().add(delBtn);

            if (currentUsername.equalsIgnoreCase(choreAssignee)) {
                Button completeBtn = createStyledButton("Complete", "#10B981", "white");
                completeBtn.setOnAction(e -> {
                    chore.setStatus("Completed");
                    updateChoreInDatabase(chore);
                    refreshChoresUI();
                });
                btnBox.getChildren().add(completeBtn);
            }
        } 
        else if (chore.getStatus().equals("Completed")) {
            Button approveBtn = createCircleButton("✓", "#10B981");
            Button rejectBtn  = createCircleButton("✕", "#EF4444");

            final int totalExpectedVoters = members.size() - 1;
            final int majorityNeeded = (totalExpectedVoters / 2) + 1;

            boolean isAssignee = currentUsername.equalsIgnoreCase(choreAssignee);
            boolean alreadyVoted = votedChoreIdsInSession.contains(chore.getChoreId());

            if (isAssignee || alreadyVoted) {
                approveBtn.setDisable(true);
                rejectBtn.setDisable(true);
            }

            approveBtn.setOnAction(e -> {
                if (isAssignee) return;
                votedChoreIdsInSession.add(chore.getChoreId());
                approveBtn.setDisable(true);
                rejectBtn.setDisable(true);
                logVoteToDatabase(chore.getChoreId(), currentUsername, true);
                chore.vote(true);
                updateChoreInDatabase(chore);
                
                int currentTotalVotes = chore.getApproveVotes() + chore.getRejectVotes();
                if (chore.getApproveVotes() >= majorityNeeded) {
                    archiveChoreToHistory(chore);
                    return; // Σταματάει εδώ, το UI ανανεώθηκε από την archive
                } else if (currentTotalVotes >= totalExpectedVoters) {
                    resetChoreToPending(chore);
                    return; // <--- ΝΕΑ ΠΡΟΣΘΗΚΗ: Σταματάει εδώ
                }
                refreshChoresUI();
            });

            rejectBtn.setOnAction(e -> {
                if (isAssignee) return;
                votedChoreIdsInSession.add(chore.getChoreId());
                approveBtn.setDisable(true);
                rejectBtn.setDisable(true);
                logVoteToDatabase(chore.getChoreId(), currentUsername, false);
                chore.vote(false);
                updateChoreInDatabase(chore);
                
                int currentTotalVotes = chore.getApproveVotes() + chore.getRejectVotes();
                if (chore.getRejectVotes() >= majorityNeeded) {
                    resetChoreToPending(chore);
                    return; // <--- ΝΕΑ ΠΡΟΣΘΗΚΗ: Σταματάει εδώ
                } else if (currentTotalVotes >= totalExpectedVoters) {
                    if (chore.getApproveVotes() > chore.getRejectVotes()) {
                        archiveChoreToHistory(chore);
                        return; // Σταματάει εδώ
                    } else {
                        resetChoreToPending(chore);
                        return; // <--- ΝΕΑ ΠΡΟΣΘΗΚΗ: Σταματάει εδώ
                    }
                }
                refreshChoresUI();
            });

            btnBox.getChildren().addAll(rejectBtn, approveBtn);
        }
        else if (chore.getStatus().equals("Suspended")) {
            Button restoreBtn = createStyledButton("Restore", "#D1FAE5", "#059669");
            restoreBtn.setOnAction(e -> {
                chore.setStatus("Pending"); 
                updateChoreInDatabase(chore); 
                refreshChoresUI();      
            });
            Button delBtn = createStyledButton("Delete", "#FEE2E2", "#DC2626");
            delBtn.setOnAction(e -> selectDeleteChore(chore));
            btnBox.getChildren().addAll(restoreBtn, delBtn);
        }

        card.getChildren().addAll(infoBox, arrow, assigneeBox, statusBadge, spacer, btnBox);
        return card;
    }

    private void selectAddChore() {
        List<String> formOptions = new ArrayList<>(members);
        NewChoreScreen form = new NewChoreScreen(formOptions, (name, pts, duty) -> {
            Chore newChore = new Chore(name, pts, duty, members.size());
            insertChoreIntoDatabase(newChore); 
            refreshChoresUI();
        });
        form.show();
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
        User sessionUser = Authentication.getCurrentUser();
        String currentUsername = (sessionUser != null) ? sessionUser.getUsername() : "1";
        if (currentUsername.equals(chore.getAssignee())) {
            assignLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #4F46E5;");
        }
    }
}