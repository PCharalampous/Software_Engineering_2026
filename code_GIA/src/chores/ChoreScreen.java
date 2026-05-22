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
        
        // 1. Έλεγχος αν ο χρήστης είναι πράγματι συνδεδεμένος
        if (sessionUser == null) {
            System.err.println("[DEBUG] ChoreScreen: No authenticated user found!");
            return; 
        }

        int userId = sessionUser.getId(); 
        System.out.println("[DEBUG] Initializing ChoreScreen for UserID: " + userId);

        // 2. Ανάκτηση του σωστού room_id από τη βάση
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

        // 3. Αν δεν βρέθηκε room_id, σταματάμε εδώ (μην φορτώσεις άσχετα μέλη)
        if (this.currentRoomId <= 0) {
            System.err.println("[DEBUG] ChoreScreen: Cannot load data, currentRoomId is 0 or NULL.");
            return;
        }

        // 4. Φόρτωση μελών ΜΟΝΟ αν έχουμε έγκυρο room_id
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
        
        // ΠΡΟΣΩΡΙΝΟ DEBUG: Δες ΟΛΑ τα chores στη βάση
        String debugQuery = "SELECT chore_id, room_id, chore_name, chore_status FROM chores LIMIT 20";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(debugQuery);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("[DEBUG] ALL chores in DB:");
            while (rs.next()) {
                System.out.println("  chore_id=" + rs.getInt("chore_id") + 
                                   " room_id=" + rs.getInt("room_id") + 
                                   " name=" + rs.getString("chore_name") + 
                                   " status=" + rs.getString("chore_status"));
            }
        } catch (Exception e) { e.printStackTrace(); }

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
            ps.setInt(2, this.currentRoomId);  // ← προσθήκη room_id
            ps.setString(3, (approve ? "VOTE_APPROVE_" : "VOTE_REJECT_") + username.trim());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void archiveChoreToHistory(Chore chore) {
        String insertHistorySql = "INSERT INTO chore_history (room_id, chore_id, completed_by, chore_name, points) VALUES (?, ?, ?, ?, ?)";
        String updateUserPointsSql = "UPDATE user_points up " +
                                     "JOIN users u ON up.user_id = u.user_id " +
                                     "SET up.current_balance = up.current_balance + ? " +
                                     "WHERE (TRIM(u.username) = ? OR TRIM(u.display_name) = ?) AND up.room_id = ?";
        String findUserIdSql = "SELECT user_id FROM users WHERE TRIM(username) = ? OR TRIM(display_name) = ? LIMIT 1";
        String insertUserPointsSql = "INSERT INTO user_points (user_id, room_id, current_balance) VALUES (?, ?, ?)";
        // ΑΛΛΑΓΗ: Αντί για DELETE, επαναφορά σε Pending/Unassigned για επόμενη διανομή
        String resetChoreSql = "UPDATE chores SET chore_status = 'Pending', assignee = 'Unassigned', " +
                               "approve_votes = 0, reject_votes = 0 WHERE chore_id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Insert στο History
            try (PreparedStatement psHistory = conn.prepareStatement(insertHistorySql)) {
                psHistory.setInt(1, this.currentRoomId);
                psHistory.setInt(2, chore.getChoreId());
                psHistory.setString(3, chore.getAssignee());
                psHistory.setString(4, chore.getName());
                psHistory.setInt(5, chore.getPoints());
                psHistory.executeUpdate();
            }

            // 2. Update Points
            String rawAssignee = chore.getAssignee().trim();
            int rowsAffected = 0;
            try (PreparedStatement psUserPoints = conn.prepareStatement(updateUserPointsSql)) {
                psUserPoints.setInt(1, chore.getPoints());
                psUserPoints.setString(2, rawAssignee);
                psUserPoints.setString(3, rawAssignee);
                psUserPoints.setInt(4, this.currentRoomId);
                rowsAffected = psUserPoints.executeUpdate();
            }

            if (rowsAffected == 0) {
                int targetUserId = -1;
                try (PreparedStatement psFindId = conn.prepareStatement(findUserIdSql)) {
                    psFindId.setString(1, rawAssignee);
                    psFindId.setString(2, rawAssignee);
                    try (ResultSet rs = psFindId.executeQuery()) {
                        if (rs.next()) targetUserId = rs.getInt("user_id");
                    }
                }
                if (targetUserId != -1) {
                    try (PreparedStatement psInsertPoints = conn.prepareStatement(insertUserPointsSql)) {
                        psInsertPoints.setInt(1, targetUserId);
                        psInsertPoints.setInt(2, this.currentRoomId);
                        psInsertPoints.setInt(3, chore.getPoints());
                        psInsertPoints.executeUpdate();
                    }
                }
            }

            // 3. ΑΛΛΑΓΗ: Reset σε Pending/Unassigned αντί για διαγραφή
            // Έτσι το chore παραμένει στη βάση για την επόμενη διανομή
            try (PreparedStatement psReset = conn.prepareStatement(resetChoreSql)) {
                psReset.setInt(1, chore.getChoreId());
                psReset.executeUpdate();
            }

            conn.commit();
            choresList.remove(chore);

            // ΑΛΛΑΓΗ: Reload από βάση ώστε το Unassigned να εμφανιστεί αμέσως
            loadChoresFromDatabase();
            refreshChoresUI();

            loadHistoryFromDatabase();
            refreshHistoryUI();;

            if (points.PointScreen.getActiveInstance() != null) {
                javafx.application.Platform.runLater(() -> points.PointScreen.getActiveInstance().updateUI());
            }

        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Transaction rolled back in archiveChoreToHistory!");
            e.printStackTrace();
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
                System.out.println("[HOMY] Chore reset to Pending due to majority rejection/tie. Logs cleared.");
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        loadChoresFromDatabase();
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
            
            votedChoreIdsInSession.clear();
            
            loadChoresFromDatabase();
            refreshChoresUI();
            
            loadHistoryFromDatabase();
            refreshHistoryUI();
            
            System.out.println("[HOMY UI] Chores redistributed and History visual logs cleared!");
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
        // Αν το UI δεν έχει χτιστεί ακόμα, απλά βγαίνουμε
        if (historyListView == null) return; 
        
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

        User sessionUser = Authentication.getCurrentUser();
        String currentUsername = (sessionUser != null) ? sessionUser.getUsername().trim() : "2"; 
        String choreAssignee = (chore.getAssignee() != null) ? chore.getAssignee().trim() : "";

        Button reportBtn = createStyledButton("Report", "#F3F4F6", "#4B5563");
        reportBtn.setOnAction(e -> selectReportChore(chore));

        if (chore.getStatus().equals("Pending")) {
            Button delBtn = createStyledButton("Delete", "#FEE2E2", "#DC2626");
            delBtn.setOnAction(e -> selectDeleteChore(chore));
            btnBox.getChildren().addAll(reportBtn, delBtn);

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

            // Οι voters είναι ΟΛΟΙ εκτός από τον assignee
            final int totalExpectedVoters = members.size() - 1;
            final int majorityNeeded = (totalExpectedVoters / 2) + 1;

            // Ο assignee ΔΕΝ ψηφίζει για τη δική του αγγαρεία
            boolean isAssignee = currentUsername.equalsIgnoreCase(choreAssignee);
            boolean alreadyVoted = votedChoreIdsInSession.contains(chore.getChoreId());

            if (isAssignee || alreadyVoted) {
                approveBtn.setDisable(true);
                rejectBtn.setDisable(true);
            }

            approveBtn.setOnAction(e -> {
                if (isAssignee) return; // Double-check
                
                votedChoreIdsInSession.add(chore.getChoreId());
                approveBtn.setDisable(true);
                rejectBtn.setDisable(true);

                logVoteToDatabase(chore.getChoreId(), currentUsername, true);
                chore.vote(true);
                updateChoreInDatabase(chore);

                int currentTotalVotes = chore.getApproveVotes() + chore.getRejectVotes();
                System.out.println("[VOTE APPROVE] " + chore.getApproveVotes() + "/" + majorityNeeded + 
                                   " needed, total votes: " + currentTotalVotes + "/" + totalExpectedVoters);

                if (chore.getApproveVotes() >= majorityNeeded) {
                    // Πλειοψηφία APPROVE → πάει στο History
                    archiveChoreToHistory(chore);
                } else if (currentTotalVotes >= totalExpectedVoters) {
                    // Ψήφισαν όλοι αλλά δεν μαζεύτηκε πλειοψηφία → Pending
                    resetChoreToPending(chore);
                }
                refreshChoresUI();
            });

            rejectBtn.setOnAction(e -> {
                if (isAssignee) return; // Double-check

                votedChoreIdsInSession.add(chore.getChoreId());
                approveBtn.setDisable(true);
                rejectBtn.setDisable(true);

                logVoteToDatabase(chore.getChoreId(), currentUsername, false);
                chore.vote(false);
                updateChoreInDatabase(chore);

                int currentTotalVotes = chore.getApproveVotes() + chore.getRejectVotes();
                System.out.println("[VOTE REJECT] " + chore.getRejectVotes() + "/" + majorityNeeded + 
                                   " needed, total votes: " + currentTotalVotes + "/" + totalExpectedVoters);

                if (chore.getRejectVotes() >= majorityNeeded) {
                    // Πλειοψηφία REJECT → πίσω σε Pending
                    resetChoreToPending(chore);
                } else if (currentTotalVotes >= totalExpectedVoters) {
                    // Ψήφισαν όλοι → κρίνε βάσει αποτελέσματος
                    if (chore.getApproveVotes() > chore.getRejectVotes()) {
                        archiveChoreToHistory(chore);
                    } else {
                        resetChoreToPending(chore);
                    }
                }
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
                } catch (Exception ex) { ex.printStackTrace(); }
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
        // ΔΙΟΡΘΩΘΗΚΕ: Περνάμε τη λίστα των συγκατοίκων αυτούσια χωρίς να κολλάμε πλέον το "All Roommates"
        List<String> formOptions = new ArrayList<>(members);

        NewChoreScreen form = new NewChoreScreen(formOptions, (name, pts, duty) -> {
            Chore newChore = new Chore(name, pts, duty, members.size());
            insertChoreIntoDatabase(newChore); 
            refreshChoresUI();
        });
        form.show();
    }

    private void selectReportChore(Chore chore) {
        // Περνάμε το this.currentRoomId στον constructor
        ReportScreen report = new ReportScreen(chore, this.currentRoomId, (description) -> {
            chore.setStatus("Suspended"); 
            updateChoreInDatabase(chore); 
            refreshChoresUI();            

            // ΕΔΩ ΠΡΟΣΘΕΤΟΥΜΕ ΤΟ room_id ΣΤΟ QUERY
            String query = "INSERT INTO chore_reports (chore_id, room_id, title, description) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, chore.getChoreId());
                ps.setInt(2, this.currentRoomId); // ΤΟ ROOM_ID ΕΔΩ
                ps.setString(3, "Chore Issue: " + chore.getName()); 
                ps.setString(4, description);
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
        User sessionUser = Authentication.getCurrentUser();
        String currentUsername = (sessionUser != null) ? sessionUser.getUsername() : "1";

        if (currentUsername.equals(chore.getAssignee())) {
            assignLbl.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #4F46E5;");
        }
    }
}