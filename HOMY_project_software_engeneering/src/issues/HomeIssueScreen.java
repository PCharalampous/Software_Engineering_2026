package issues;

import entities.Issue;
import ui.ConfirmationScreen;
import ui.ErrorScreen;
import util.DatabaseManager;
import entities.Authentication;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class HomeIssueScreen extends VBox {

    private TableView<Issue> pendingIssuesTable;
    private TableView<Issue> resolvedIssuesTable;
    
    private final Runnable onBackToHub;
    private final Runnable onNavigateToCreate;
    private final Runnable onNavigateToSchedule;

    private static final Set<String> votedIssueKeys = new HashSet<>();

    public static final ObservableList<Issue> allIssues = FXCollections.observableArrayList();

    public HomeIssueScreen(Runnable onBackToHub, Runnable onNavigateToCreate, Runnable onNavigateToSchedule) {
        this.onBackToHub = onBackToHub;
        this.onNavigateToCreate = onNavigateToCreate;
        this.onNavigateToSchedule = onNavigateToSchedule;

        this.pendingIssuesTable = createStyledTable(true);
        this.resolvedIssuesTable = createStyledTable(false);
        setupTableData();

        this.setSpacing(0);
        this.setStyle("-fx-background-color: #f1f5f9;");
        buildUI();
        loadDataFromDatabase();
    }

    private void buildUI() {
        HBox header = new HBox(15);
        header.setStyle("-fx-background-color: #1e293b; -fx-padding: 15 20;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand;");
        backBtn.setOnAction(e -> { if (onBackToHub != null) onBackToHub.run(); });

        Label headerTitle = new Label("House Issues & Repairs");
        headerTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().addAll(backBtn, headerTitle);

        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(25));
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        VBox leftSection = new VBox(20);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        leftSection.getChildren().addAll(
            createTableCard("Active Issues", "#f97316", pendingIssuesTable),
            createTableCard("Resolved History", "#10b981", resolvedIssuesTable)
        );

        mainLayout.getChildren().add(leftSection);

        HBox bottomArea = new HBox(15);
        bottomArea.setPadding(new Insets(15, 25, 15, 25));
        bottomArea.setAlignment(Pos.CENTER_RIGHT);
        bottomArea.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0 transparent transparent transparent;");

        Button scheduleBtn = new Button("🔧 Schedule Technician");
        scheduleBtn.setStyle("-fx-background-color: #0284c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        scheduleBtn.setOnAction(e -> { if (onNavigateToSchedule != null) onNavigateToSchedule.run(); });

        // 🌟 FIXED: Color reverted from purple to the identical emerald green (#10b981)
        Button navigateCreateBtn = new Button("+ Report New Issue");
        navigateCreateBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");
        navigateCreateBtn.setOnAction(e -> { if (onNavigateToCreate != null) onNavigateToCreate.run(); });

        bottomArea.getChildren().addAll(scheduleBtn, navigateCreateBtn);
        this.getChildren().addAll(header, mainLayout, bottomArea);
    }

    private VBox createTableCard(String titleText, String color, TableView<Issue> table) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0;");
        
        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + color + "; -fx-pref-width: 8; -fx-pref-height: 8; -fx-background-radius: 4;");
        
        Label title = new Label(titleText);
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        titleBox.getChildren().addAll(dot, title);
        
        card.getChildren().addAll(titleBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private TableView<Issue> createStyledTable(boolean isPendingTable) {
        TableView<Issue> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0;");
        table.setPrefHeight(250);
        
        TableColumn<Issue, String> typeCol = new TableColumn<>("Issue Type");
        typeCol.setCellValueFactory(cell -> cell.getValue().typeProperty());
        
        TableColumn<Issue, String> reporterCol = new TableColumn<>("Reported By");
        reporterCol.setCellValueFactory(cell -> cell.getValue().reportedByProperty());
        
        TableColumn<Issue, String> dateCol = new TableColumn<>("Date Logged");
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());

        TableColumn<Issue, String> payersCol = new TableColumn<>("Assigned Roommates");
        payersCol.setCellValueFactory(cell -> cell.getValue().payersProperty());
        
        table.getColumns().addAll(typeCol, reporterCol, dateCol, payersCol);

        if (isPendingTable) {
            TableColumn<Issue, Void> actionCol = new TableColumn<>("Approval Actions");
            actionCol.setMinWidth(160);
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button btnCheck = new Button("✓");
                private final Button btnCross = new Button("✕");
                private final HBox pane = new HBox(10, btnCheck, btnCross);
                private final StackPane centerContainer = new StackPane();

                {
                    btnCheck.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 4;");
                    btnCross.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10; -fx-background-radius: 4;");
                    pane.setAlignment(Pos.CENTER);
                    centerContainer.setAlignment(Pos.CENTER);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                        setGraphic(null);
                    } else {
                        Issue issue = getTableRow().getItem();
                        String currentUsername = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getUsername() : "";
                        String uniqueKey = currentUsername + "_" + issue.getType() + "_" + issue.getDate();

                        if ("Pending_Approval".equalsIgnoreCase(issue.getApprovalStatus())) {
                            
                            if (issue.getPayers().equalsIgnoreCase("Only Me")) {
                                Label lbl = new Label("Auto-Accepted");
                                lbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            if (currentUsername.equalsIgnoreCase(issue.getReportedBy())) {
                                Label lbl = new Label("Waiting for Roommates");
                                lbl.setStyle("-fx-text-fill: #f97316; -fx-font-style: italic; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            if (votedIssueKeys.contains(uniqueKey)) {
                                Label lbl = new Label("Waiting for Roommates");
                                lbl.setStyle("-fx-text-fill: #f97316; -fx-font-style: italic; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            boolean isTargetPayer = issue.getPayers().toLowerCase().contains("all roommates") 
                                                 || issue.getPayers().toLowerCase().contains(currentUsername.toLowerCase());
                            
                            if (!isTargetPayer) {
                                Label lbl = new Label("Waiting for Roommates");
                                lbl.setStyle("-fx-text-fill: #f97316; -fx-font-style: italic; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            btnCheck.setOnAction(e -> {
                                votedIssueKeys.add(uniqueKey);
                                handleVote(issue, true);
                            });
                            btnCross.setOnAction(e -> {
                                votedIssueKeys.add(uniqueKey);
                                handleVote(issue, false);
                            });
                            centerContainer.getChildren().setAll(pane);
                            setGraphic(centerContainer);
                            
                        } else {
                            Label statusLbl = new Label(issue.getApprovalStatus());
                            statusLbl.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
                            centerContainer.getChildren().setAll(statusLbl);
                            setGraphic(centerContainer);
                        }
                    }
                }
            });
            table.getColumns().add(actionCol);
        }
        return table;
    }

    private void setupTableData() {
        pendingIssuesTable.setItems(new FilteredList<>(allIssues, i -> !"Resolved".equalsIgnoreCase(i.getApprovalStatus()) && !"Declined".equalsIgnoreCase(i.getApprovalStatus())));
        resolvedIssuesTable.setItems(new FilteredList<>(allIssues, i -> "Resolved".equalsIgnoreCase(i.getApprovalStatus()) || "Declined".equalsIgnoreCase(i.getApprovalStatus())));
    }

    public void loadDataFromDatabase() {
        allIssues.clear();
        int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;

        String issuesSql = "SELECT * FROM issues WHERE room_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(issuesSql)) {
            
            pstmt.setInt(1, currentRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Issue issue = new Issue(
                        rs.getInt("issue_id"), 
                        rs.getString("issue_type"),
                        rs.getString("reported_by"),
                        rs.getString("payers"),
                        rs.getString("issue_date"),
                        rs.getString("approval_status")
                    );
                    allIssues.add(issue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleVote(Issue issue, boolean approved) {
        int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            if (approved) {
                String updateVoteSql = "UPDATE issues SET approve_votes = approve_votes + 1 WHERE room_id = ? AND issue_type = ? AND issue_date = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateVoteSql)) {
                    pstmt.setInt(1, currentRoomId);
                    pstmt.setString(2, issue.getType());
                    pstmt.setString(3, issue.getDate());
                    pstmt.executeUpdate();
                }

                int totalRoommates = 1;
                String countRoommatesSql = "SELECT COUNT(*) FROM users WHERE room_id = ?";
                try (PreparedStatement pstmtCount = conn.prepareStatement(countRoommatesSql)) {
                    pstmtCount.setInt(1, currentRoomId);
                    try (ResultSet rs = pstmtCount.executeQuery()) {
                        if (rs.next()) totalRoommates = rs.getInt(1);
                    }
                }

                int currentApproveVotes = 0;
                String checkVotesSql = "SELECT approve_votes FROM issues WHERE room_id = ? AND issue_type = ? AND issue_date = ?";
                try (PreparedStatement pstmtVotes = conn.prepareStatement(checkVotesSql)) {
                    pstmtVotes.setInt(1, currentRoomId);
                    pstmtVotes.setString(2, issue.getType());
                    pstmtVotes.setString(3, issue.getDate());
                    try (ResultSet rs = pstmtVotes.executeQuery()) {
                        if (rs.next()) currentApproveVotes = rs.getInt(1);
                    }
                }

                if (currentApproveVotes >= (totalRoommates - 1)) {
                    String finalizeSql = "UPDATE issues SET approval_status = 'Accepted' WHERE room_id = ? AND issue_type = ? AND issue_date = ?";
                    try (PreparedStatement pstmtFinal = conn.prepareStatement(finalizeSql)) {
                        pstmtFinal.setInt(1, currentRoomId);
                        pstmtFinal.setString(2, issue.getType());
                        pstmtFinal.setString(3, issue.getDate());
                        pstmtFinal.executeUpdate();
                    }
                }
            } else {
                String rejectSql = "UPDATE issues SET reject_votes = reject_votes + 1, approval_status = 'Declined' WHERE room_id = ? AND issue_type = ? AND issue_date = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(rejectSql)) {
                    pstmt.setInt(1, currentRoomId);
                    pstmt.setString(2, issue.getType());
                    pstmt.setString(3, issue.getDate());
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            loadDataFromDatabase();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}