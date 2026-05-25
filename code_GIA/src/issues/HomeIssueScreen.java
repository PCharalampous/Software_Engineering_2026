package issues;

import util.DatabaseManager;
import entities.Issue;
import entities.Authentication; // ← ΠΡΟΣΘΗΚΗ
import ui.ConfirmationScreen;
import ui.ErrorScreen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class HomeIssueScreen extends VBox {

    private TableView<Issue> pendingIssuesTable;
    private TableView<Issue> issuesHistoryTable;
    private Label activeCountLabel;

    public static final ObservableList<Issue> allIssues = FXCollections.observableArrayList();

    private final Runnable onBackToHub;
    private final Runnable onNavigateToCreate;
    private final Runnable onNavigateToSchedule;

    public HomeIssueScreen(Runnable onBackToHub, Runnable onNavigateToCreate, Runnable onNavigateToSchedule) {
        this.onBackToHub = onBackToHub;
        this.onNavigateToCreate = onNavigateToCreate;
        this.onNavigateToSchedule = onNavigateToSchedule;

        this.setSpacing(0);
        this.setStyle("-fx-background-color: #f1f5f9;");
        
        buildUI();
        loadIssuesFromDatabase();
    }

    private void buildUI() {
        // --- 1. Header Section ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #1e293b;");
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            if (onBackToHub != null) onBackToHub.run();
        });

        Label titleLabel = new Label("Home Issue Report");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#f8fafc"));
        header.getChildren().addAll(backBtn, titleLabel);
        this.getChildren().add(header);

        // --- 2. Workspace Layout ---
        HBox workspace = new HBox(20);
        workspace.setPadding(new Insets(25));
        VBox.setVgrow(workspace, Priority.ALWAYS);

        VBox leftColumn = new VBox(20);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        VBox pendingBox = createCardContainer();
        pendingIssuesTable = createIssueTable(true);
        pendingBox.getChildren().addAll(createSectionHeader("#f97316", "Pending Issues"), pendingIssuesTable);

        VBox historyBox = createCardContainer();
        issuesHistoryTable = createIssueTable(false);
        historyBox.getChildren().addAll(createSectionHeader("#10b981", "Issues History Log"), issuesHistoryTable);

        leftColumn.getChildren().addAll(pendingBox, historyBox);

        // Right Metric Sidebar
        VBox rightColumn = new VBox(15);
        rightColumn.setPadding(new Insets(20));
        rightColumn.setPrefWidth(260);
        rightColumn.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0;");

        VBox activeCard = new VBox(8);
        activeCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 6;");
        Label caption = new Label("Active Maintenance Issues");
        caption.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        caption.setTextFill(Color.web("#64748b"));
        activeCountLabel = new Label("0");
        activeCountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        activeCard.getChildren().addAll(caption, activeCountLabel);
        rightColumn.getChildren().addAll(createSectionHeader("#6366f1", "Metrics"), activeCard);

        workspace.getChildren().addAll(leftColumn, rightColumn);
        this.getChildren().add(workspace);

        // --- 3. Bottom Tray Bar ---
        HBox bottomTray = new HBox(15);
        bottomTray.setAlignment(Pos.CENTER_RIGHT);
        bottomTray.setPadding(new Insets(15, 25, 15, 25));
        bottomTray.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0 transparent transparent transparent;");

        Button btnSchedule = new Button("Schedule Technician");
        btnSchedule.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSchedule.setOnAction(e -> { if (onNavigateToSchedule != null) onNavigateToSchedule.run(); });

        Button btnPay = new Button("Pay Selected Issue");
        btnPay.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnPay.setOnAction(e -> handleResolutionAction());

        Button btnCreate = new Button("+ Create New Issue");
        btnCreate.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        btnCreate.setOnAction(e -> { if (onNavigateToCreate != null) onNavigateToCreate.run(); });

        bottomTray.getChildren().addAll(btnSchedule, btnPay, btnCreate);
        this.getChildren().add(bottomTray);

        setupDataBindings();
    }

    private TableView<Issue> createIssueTable(boolean isPending) {
        TableView<Issue> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0;");
        table.setPrefHeight(200);

        TableColumn<Issue, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> cell.getValue().typeProperty());

        TableColumn<Issue, String> reportedCol = new TableColumn<>("Reported By");
        reportedCol.setCellValueFactory(cell -> cell.getValue().reportedByProperty());

        TableColumn<Issue, String> payersCol = new TableColumn<>("Payers");
        payersCol.setCellValueFactory(cell -> cell.getValue().payersProperty());

        TableColumn<Issue, String> dateCol = new TableColumn<>(isPending ? "Date Logged" : "Repaired Date");
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());

        table.getColumns().addAll(typeCol, reportedCol, payersCol, dateCol);
        return table;
    }

    private void setupDataBindings() {
        FilteredList<Issue> pendingFilteredList = new FilteredList<>(allIssues, issue -> 
            "Pending".equals(issue.getStatus())
        );

        FilteredList<Issue> historyFilteredList = new FilteredList<>(allIssues, issue -> 
            "Resolved".equals(issue.getStatus())
        );

        pendingIssuesTable.setItems(pendingFilteredList);
        issuesHistoryTable.setItems(historyFilteredList);

        activeCountLabel.textProperty().bind(
            javafx.beans.binding.Bindings.size(pendingFilteredList).asString()
        );
    }

    public void setupTableDataRefresh() {
        pendingIssuesTable.refresh();
        issuesHistoryTable.refresh();
    }

    private void handleResolutionAction() {
        if (issuesHistoryTable.getSelectionModel().getSelectedItem() != null) {
            ErrorScreen.show("This issue has already been logged as paid and resolved!");
            issuesHistoryTable.getSelectionModel().clearSelection();
            return;
        }

        Issue selected = pendingIssuesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorScreen.show("Please select an unpaid issue from the 'Pending Issues' table above to finalize payment.");
            return;
        }

        ConfirmationScreen confirm = new ConfirmationScreen(
            "Confirm Payment Action", 
            "Are you sure you want to resolve and pay for this issue?",
            () -> {
                String updateSql = "UPDATE issues SET issue_status = 'Resolved' WHERE issue_id = ?";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                    
                    pstmt.setInt(1, selected.getId());
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        allIssues.remove(selected);
                        selected.setStatus("Resolved");
                        allIssues.add(selected);
                        pendingIssuesTable.getSelectionModel().clearSelection();
                    } else {
                        ErrorScreen.show("Database error: Could not find issue with ID " + selected.getId());
                    }
                    
                } catch (Exception e) {
                    ErrorScreen.show("Database exception: " + e.getMessage());
                    e.printStackTrace();
                }
            },
            () -> pendingIssuesTable.getSelectionModel().clearSelection()
        );
        confirm.show();
    }

    private VBox createCardContainer() {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0;");
        return box;
    }

    private HBox createSectionHeader(String color, String text) {
        HBox hbox = new HBox(8);
        hbox.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setStyle("-fx-background-color: " + color + "; -fx-pref-width: 8; -fx-pref-height: 8; -fx-background-radius: 4;");
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        hbox.getChildren().addAll(dot, lbl);
        return hbox;
    }
  
    private void loadIssuesFromDatabase() {
        allIssues.clear(); 

        // ΔΥΝΑΜΙΚΟ: Λήψη του room_id του τρέχοντος συνδεδεμένου χρήστη
        int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;

        String query = "SELECT * FROM issues WHERE room_id = ?"; 
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, currentRoomId);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Issue issue = new Issue(
                        rs.getInt("issue_id"),
                        rs.getString("issue_type"),
                        rs.getString("reported_by"),
                        rs.getString("payers"),
                        rs.getString("issue_date")
                    );
                    
                    issue.setStatus(rs.getString("issue_status")); 
                    allIssues.add(issue);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading issues from database: " + e.getMessage());
        }
    }
}