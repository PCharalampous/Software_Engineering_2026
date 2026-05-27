package finances;

import entities.Bill;
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

public class FinancesScreen extends VBox {

    private TableView<Bill> pendingBillsTable;
    private TableView<Bill> paidBillsTable;
    
    private final Runnable onBackToHub;
    private final Runnable onNavigateToCreate;

    // 🌟 PERSISTENT SESSION TRACKING: Survives screen switching, works for 3+ roommates!
    private static final Set<String> votedBillKeys = new HashSet<>();

    public static final ObservableList<Bill> allBills = FXCollections.observableArrayList();

    public FinancesScreen(Runnable onBackToHub, Runnable onNavigateToCreate) {
        this.onBackToHub = onBackToHub;
        this.onNavigateToCreate = onNavigateToCreate;

        this.pendingBillsTable = createStyledTable(true); 
        this.paidBillsTable = createStyledTable(false);
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

        Label headerTitle = new Label("Utility Finances");
        headerTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().addAll(backBtn, headerTitle);

        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(25));
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        VBox leftSection = new VBox(20);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        leftSection.getChildren().addAll(
            createTableCard("Pending Bills", "#f97316", pendingBillsTable),
            createTableCard("Paid Bills History", "#10b981", paidBillsTable)
        );

        VBox rightSection = createMetricsCard();
        rightSection.setPrefWidth(260);
        mainLayout.getChildren().addAll(leftSection, rightSection);

        HBox bottomArea = new HBox(15);
        bottomArea.setPadding(new Insets(15, 25, 15, 25));
        bottomArea.setAlignment(Pos.CENTER_RIGHT);
        bottomArea.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0 transparent transparent transparent;");

        Button payBtn = new Button("Pay Selected Bill");
        payBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 6; -fx-cursor: hand;");
        payBtn.setOnAction(e -> handlePayBill());

        Button navigateCreateBtn = new Button("+ Create New Bill");
        navigateCreateBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;");
        navigateCreateBtn.setOnAction(e -> { if (onNavigateToCreate != null) onNavigateToCreate.run(); });

        bottomArea.getChildren().addAll(payBtn, navigateCreateBtn);
        this.getChildren().addAll(header, mainLayout, bottomArea);
    }

    private VBox createTableCard(String titleText, String color, TableView<Bill> table) {
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

    private VBox createMetricsCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0;");
        
        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setStyle("-fx-background-color: #6366f1; -fx-pref-width: 8; -fx-pref-height: 8; -fx-background-radius: 4;");
        Label title = new Label("Metrics");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");
        titleBox.getChildren().addAll(dot, title);

        VBox activeCard = new VBox(8);
        activeCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 6;");
        
        Label statLabel = new Label("Active Pending Bills");
        statLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #64748b;");
        
        Label count = new Label("0");
        count.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        count.textProperty().bind(javafx.beans.binding.Bindings.size(pendingBillsTable.getItems()).asString());
        
        activeCard.getChildren().addAll(statLabel, count);
        card.getChildren().addAll(titleBox, activeCard);
        return card;
    }

    private TableView<Bill> createStyledTable(boolean isPendingTable) {
        TableView<Bill> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0;");
        table.setPrefHeight(200);
        
        TableColumn<Bill, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell -> cell.getValue().typeProperty());
        
        TableColumn<Bill, Double> amountCol = new TableColumn<>("Amount (€)");
        amountCol.setCellValueFactory(cell -> cell.getValue().amountProperty().asObject());
        
        TableColumn<Bill, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());

        TableColumn<Bill, String> payersCol = new TableColumn<>("Payers");
        payersCol.setCellValueFactory(cell -> cell.getValue().payersProperty());
        
        table.getColumns().addAll(typeCol, amountCol, dateCol, payersCol);

        if (isPendingTable) {
            TableColumn<Bill, Void> actionCol = new TableColumn<>("Voting Actions");
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
                        Bill bill = getTableRow().getItem();
                        String currentUsername = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getUsername() : "";
                        String uniqueKey = currentUsername + "_" + bill.getType() + "_" + bill.getDate();

                        if ("Pending_Approval".equalsIgnoreCase(bill.getApprovalStatus())) {
                            
                            if (bill.getPayers().equalsIgnoreCase("Only Me")) {
                                Label lbl = new Label("Auto-Accepted");
                                lbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            if (currentUsername.equalsIgnoreCase(bill.getCreatorUsername())) {
                                Label lbl = new Label("Waiting for Roommates");
                                lbl.setStyle("-fx-text-fill: #f97316; -fx-font-style: italic; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            // 🌟 RESTORED ORIGINAL FLOW: Shows your exact label text if the specific logged-in session has voted!
                            if (votedBillKeys.contains(uniqueKey)) {
                                Label lbl = new Label("Waiting for Roommates");
                                lbl.setStyle("-fx-text-fill: #f97316; -fx-font-style: italic; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            boolean isTargetPayer = bill.getPayers().toLowerCase().contains("all roommates") 
                                                 || bill.getPayers().toLowerCase().contains(currentUsername.toLowerCase());
                            
                            if (!isTargetPayer) {
                                Label lbl = new Label("Waiting for Roommates");
                                lbl.setStyle("-fx-text-fill: #f97316; -fx-font-style: italic; -fx-font-weight: bold;");
                                centerContainer.getChildren().setAll(lbl);
                                setGraphic(centerContainer);
                                return;
                            }

                            btnCheck.setOnAction(e -> {
                                votedBillKeys.add(uniqueKey);
                                handleVote(bill, true);
                            });
                            btnCross.setOnAction(e -> {
                                votedBillKeys.add(uniqueKey);
                                handleVote(bill, false);
                            });
                            centerContainer.getChildren().setAll(pane);
                            setGraphic(centerContainer);
                            
                        } else {
                            Label statusLbl = new Label(bill.getApprovalStatus());
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
        pendingBillsTable.setItems(new FilteredList<>(allBills, b -> "Pending".equalsIgnoreCase(b.getStatus())));
        paidBillsTable.setItems(new FilteredList<>(allBills, b -> "Paid".equalsIgnoreCase(b.getStatus())));
    }

    public void loadDataFromDatabase() {
        allBills.clear();
        int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;

        String billsSql = "SELECT * FROM bills WHERE room_id = ? AND (approval_status = 'Accepted' OR approval_status = 'Pending_Approval')";
        String notesSql = "SELECT detail FROM notifications WHERE room_id = ? AND category = 'FINANCES'";

        java.util.List<Bill> loadedBills = new java.util.ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            
            try (PreparedStatement pstmt = conn.prepareStatement(billsSql)) {
                pstmt.setInt(1, currentRoomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Bill bill = new Bill(
                            rs.getString("bill_type"),
                            rs.getDouble("amount"),
                            rs.getString("bill_date"),
                            rs.getString("payers"),
                            rs.getString("bill_status"),
                            rs.getString("approval_status")
                        );
                        loadedBills.add(bill);
                    }
                }
            }

            try (PreparedStatement pstmtNotes = conn.prepareStatement(notesSql)) {
                pstmtNotes.setInt(1, currentRoomId);
                try (ResultSet rsNotes = pstmtNotes.executeQuery()) {
                    while (rsNotes.next()) {
                        String detailText = rsNotes.getString("detail");

                        if (detailText != null && detailText.contains(" added you")) {
                            String parsedCreator = detailText.split(" added you")[0].trim();

                            for (Bill b : loadedBills) {
                                if (detailText.contains("Type: " + b.getType())) {
                                    b.setCreatorUsername(parsedCreator);
                                }
                            }
                        }
                    }
                }
            }
            
            allBills.addAll(loadedBills);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleVote(Bill bill, boolean approved) {
        int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;
        String currentUsername = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getUsername() : "";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            if (approved) {
                String updateVoteSql = "UPDATE bills SET approve_votes = approve_votes + 1 WHERE room_id = ? AND bill_type = ? AND bill_date = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateVoteSql)) {
                    pstmt.setInt(1, currentRoomId);
                    pstmt.setString(2, bill.getType());
                    pstmt.setString(3, bill.getDate());
                    pstmt.executeUpdate();
                }

                int requiredApproveVotes = 1; 
                String payersStr = bill.getPayers().trim();

                if (payersStr.equalsIgnoreCase("All Roommates")) {
                    int totalHouseRoommates = 1;
                    String countRoommatesSql = "SELECT COUNT(*) FROM users WHERE room_id = ?";
                    try (PreparedStatement pstmtCount = conn.prepareStatement(countRoommatesSql)) {
                        pstmtCount.setInt(1, currentRoomId);
                        try (ResultSet rs = pstmtCount.executeQuery()) {
                            if (rs.next()) totalHouseRoommates = rs.getInt(1);
                        }
                    }
                    requiredApproveVotes = totalHouseRoommates - 1; 
                } else {
                    String[] targetedUsers = payersStr.split(",");
                    requiredApproveVotes = targetedUsers.length;

                    // 🌟 FIX: If the creator explicitly included themselves in the text list, 
                    // subtract 1 because creators are blocked from voting.
                    String creator = bill.getCreatorUsername() != null ? bill.getCreatorUsername().toLowerCase().trim() : "";
                    for (String user : targetedUsers) {
                        if (user.toLowerCase().trim().equalsIgnoreCase(creator)) {
                            requiredApproveVotes--;
                            break;
                        }
                    }
                }

                if (requiredApproveVotes < 1) {
                    requiredApproveVotes = 1;
                }

                int currentApproveVotes = 0;
                String checkVotesSql = "SELECT approve_votes FROM bills WHERE room_id = ? AND bill_type = ? AND bill_date = ?";
                try (PreparedStatement pstmtVotes = conn.prepareStatement(checkVotesSql)) {
                    pstmtVotes.setInt(1, currentRoomId);
                    pstmtVotes.setString(2, bill.getType());
                    pstmtVotes.setString(3, bill.getDate());
                    try (ResultSet rs = pstmtVotes.executeQuery()) {
                        if (rs.next()) currentApproveVotes = rs.getInt(1);
                    }
                }

                if (currentApproveVotes >= requiredApproveVotes) {
                    String finalizeSql = "UPDATE bills SET approval_status = 'Accepted' WHERE room_id = ? AND bill_type = ? AND bill_date = ?";
                    try (PreparedStatement pstmtFinal = conn.prepareStatement(finalizeSql)) {
                        pstmtFinal.setInt(1, currentRoomId);
                        pstmtFinal.setString(2, bill.getType());
                        pstmtFinal.setString(3, bill.getDate());
                        pstmtFinal.executeUpdate();
                    }

                    String calendarSql = "INSERT INTO calendar_events (room_id, event_name, event_description, event_date, event_time, event_type) VALUES (?, ?, ?, ?, 2359, 'BILL')";
                    try (PreparedStatement pstmtCal = conn.prepareStatement(calendarSql)) {
                        pstmtCal.setInt(1, currentRoomId);
                        pstmtCal.setString(2, "Bill: " + bill.getType());
                        pstmtCal.setString(3, "Amount: " + bill.getAmount() + "€ | Payers: " + bill.getPayers());
                        pstmtCal.setString(4, bill.getDate());
                        pstmtCal.executeUpdate();
                    }
                    
                    entities.Notification.createNotificationToRoom(
                            conn, 
                            "CALENDAR", 
                            "Νέο event στο ημερολόγιο", 
                            "Προστέθηκε ο λογαριασμός: '" + bill.getType() + "' για τις " + bill.getDate() + ".", 
                            "CALENDAR_SCREEN", 
                            "#06B6D4"
                        );
                }
            } else {
                String rejectSql = "UPDATE bills SET reject_votes = reject_votes + 1, approval_status = 'Declined' WHERE room_id = ? AND bill_type = ? AND bill_date = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(rejectSql)) {
                    pstmt.setInt(1, currentRoomId);
                    pstmt.setString(2, bill.getType());
                    pstmt.setString(3, bill.getDate());
                    pstmt.executeUpdate();
                }

                String targetCreator = bill.getCreatorUsername();
                if (targetCreator != null && !targetCreator.isEmpty()) {
                    int creatorUserId = 0;
                    String findCreatorIdSql = "SELECT user_id FROM users WHERE username = ? AND room_id = ?";
                    try (PreparedStatement pstmtFind = conn.prepareStatement(findCreatorIdSql)) {
                        pstmtFind.setString(1, targetCreator);
                        pstmtFind.setInt(2, currentRoomId);
                        try (ResultSet rs = pstmtFind.executeQuery()) {
                            if (rs.next()) {
                                creatorUserId = rs.getInt("user_id");
                            }
                        }
                    }

                    if (creatorUserId > 0) {
                        String pushNotifSql = "INSERT INTO notifications (user_id, room_id, category, notification_text, detail, target_screen, tag_color, is_read) VALUES (?, ?, 'FINANCES', 'Bill Rejected', ?, 'FINANCES', '#25880d', 0)";
                        try (PreparedStatement pstmtNotif = conn.prepareStatement(pushNotifSql)) {
                            pstmtNotif.setInt(1, creatorUserId);
                            pstmtNotif.setInt(2, currentRoomId);
                            
                            String messageDetails = currentUsername + " rejected your bill request:\n" +
                                                   "Type: " + bill.getType() + "\n" +
                                                   "Amount: " + bill.getAmount() + "€";
                            pstmtNotif.setString(3, messageDetails);
                            pstmtNotif.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            loadDataFromDatabase(); 
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void handlePayBill() {
        Bill selected = pendingBillsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            ErrorScreen.show("Please select a pending bill.");
            return;
        }
        
        if ("Pending_Approval".equalsIgnoreCase(selected.getApprovalStatus())) {
            ErrorScreen.show("This bill cannot be settled yet because it hasn't passed unanimous voting!");
            return;
        }

        new ConfirmationScreen("Pay Bill", "Mark as paid?", () -> {
            int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;

            String updateBillSql = "UPDATE bills SET bill_status = 'Paid' WHERE bill_type = ? AND bill_date = ? AND room_id = ?";
            String deleteCalendarSql = "DELETE FROM calendar_events WHERE event_name = ? AND event_date = ? AND room_id = ?";

            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false);

                try (PreparedStatement pstmt1 = conn.prepareStatement(updateBillSql)) {
                    pstmt1.setString(1, selected.getType());
                    pstmt1.setString(2, selected.getDate());
                    pstmt1.setInt(3, currentRoomId);
                    pstmt1.executeUpdate();
                }

                try (PreparedStatement pstmt2 = conn.prepareStatement(deleteCalendarSql)) {
                    pstmt2.setString(1, "Bill: " + selected.getType());
                    pstmt2.setString(2, selected.getDate());
                    pstmt2.setInt(3, currentRoomId);
                    pstmt2.executeUpdate();
                }

                conn.commit();
                loadDataFromDatabase();
                
            } catch (SQLException ex) {
                ErrorScreen.show("Database error: Could not complete payment.");
                ex.printStackTrace();
            }
        }, () -> {}).show();
    }
}