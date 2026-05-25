package finances;

import entities.Bill;
import ui.ErrorScreen;
import util.DatabaseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewBillScreen extends VBox {

    private TextField typeField;
    private TextField amountField;
    private DatePicker datePicker;
    
    private TextField payersField;            
    private MenuButton payersMenuButton;      
    private final Runnable onCancelAction;

    public NewBillScreen(Runnable onCancelAction) {
        this.onCancelAction = onCancelAction;
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #F3F4F6;");
        buildUI();
    }

    private void buildUI() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        Label headerTitle = new Label("Add New House Bill");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        headerTitle.setTextFill(Color.web("#1F2937"));
        header.getChildren().add(headerTitle);
        this.getChildren().add(header);

        VBox formCard = new VBox(15);
        formCard.setPadding(new Insets(25));
        formCard.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        VBox.setMargin(formCard, new Insets(30, 50, 30, 50));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        typeField = new TextField();
        typeField.setPromptText("e.g. Electricity, Water...");
        
        amountField = new TextField();
        amountField.setPromptText("e.g. 50.00");
        
        datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        payersField = new TextField();
        payersField.setEditable(false);
        payersField.setPromptText("Click + to select payers...");
        payersField.setStyle("-fx-padding: 8; -fx-background-radius: 4 0 0 4; -fx-border-color: #D1D5DB; -fx-background-color: #f9fafb;");
        HBox.setHgrow(payersField, Priority.ALWAYS);

        payersMenuButton = new MenuButton("+");
        payersMenuButton.setStyle("-fx-background-color: #14B8A6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0 4 4 0; -fx-padding: 7 14; -fx-cursor: hand;");
        
        setupDynamicPayersDropdown();

        HBox payersContainer = new HBox(0, payersField, payersMenuButton);
        payersContainer.setAlignment(Pos.CENTER_LEFT);

        addFormField(grid, "Bill Type:", typeField, 0);
        addFormField(grid, "Amount (€):", amountField, 1);
        addFormField(grid, "Due Date:", datePicker, 2);
        addFormField(grid, "Payers:", payersContainer, 3);

        formCard.getChildren().add(grid);
        this.getChildren().add(formCard);

        HBox bottomArea = new HBox(12);
        bottomArea.setPadding(new Insets(0, 50, 20, 50));
        bottomArea.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #E5E7EB; -fx-text-fill: #4B5563; -fx-font-weight: bold; -fx-padding: 10 24;");
        cancelBtn.setOnAction(e -> onCancelAction.run());
        Button saveBtn = new Button("Create Bill");
        saveBtn.setStyle("-fx-background-color: #14B8A6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 24;");
        saveBtn.setOnAction(e -> handleCreateBill());
        bottomArea.getChildren().addAll(cancelBtn, saveBtn);
        this.getChildren().add(bottomArea);
    }

    private void addFormField(GridPane grid, String label, Node input, int row) {
        grid.add(new Label(label), 0, row);
        grid.add(input, 1, row);
    }

    private void setupDynamicPayersDropdown() {
        int currentRoomId = 0;
        if (entities.Authentication.getCurrentUser() != null) {
            currentRoomId = entities.Authentication.getCurrentUser().getRoomId();
        }

        List<String> roommates = new ArrayList<>();
        String sql = "SELECT username FROM users WHERE room_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    roommates.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching roommates for dropdown: " + e.getMessage());
            e.printStackTrace();
        }

        List<String> selectedPayers = new ArrayList<>();
        payersMenuButton.getItems().clear();

        CheckMenuItem itemAll = new CheckMenuItem("All Roommates");
        payersMenuButton.getItems().add(itemAll);

        List<CheckMenuItem> userItems = new ArrayList<>();

        for (String name : roommates) {
            CheckMenuItem userItem = new CheckMenuItem(name);
            userItems.add(userItem);
            payersMenuButton.getItems().add(userItem);
        }

        Runnable updatePayersText = () -> {
            if (itemAll.isSelected()) {
                payersField.setText("All Roommates");
                for (CheckMenuItem ui : userItems) {
                    ui.setSelected(false);
                }
                selectedPayers.clear();
                selectedPayers.add("All Roommates");
            } else {
                selectedPayers.clear();
                for (CheckMenuItem ui : userItems) {
                    if (ui.isSelected()) {
                        selectedPayers.add(ui.getText());
                    }
                }
                payersField.setText(String.join(", ", selectedPayers));
            }
        };

        itemAll.setOnAction(e -> updatePayersText.run());
        for (CheckMenuItem ui : userItems) {
            ui.setOnAction(e -> {
                itemAll.setSelected(false);
                updatePayersText.run();
            });
        }
    }

    private void handleCreateBill() {
        if (typeField.getText().trim().isEmpty() || amountField.getText().trim().isEmpty() || payersField.getText().trim().isEmpty()) {
            ErrorScreen.show("All fields are mandatory.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountField.getText());
            String type = typeField.getText().trim();
            String date = datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString();
            String payers = payersField.getText();

            String currentUsername = (entities.Authentication.getCurrentUser() != null) ? entities.Authentication.getCurrentUser().getUsername() : "";
            int currentUserId = (entities.Authentication.getCurrentUser() != null) ? entities.Authentication.getCurrentUser().getId() : 0;        
            String calculatedApprovalStatus = "Pending_Approval";
            if (payers.equalsIgnoreCase("Only Me") || payers.equalsIgnoreCase(currentUsername)) {
                calculatedApprovalStatus = "Accepted";
            }

            int currentRoomId = 0;
            if (entities.Authentication.getCurrentUser() != null) {
                currentRoomId = entities.Authentication.getCurrentUser().getRoomId();
            }

            String billSql = "INSERT INTO bills (room_id, bill_type, amount, bill_date, payers, bill_status, approval_status, approve_votes, reject_votes) VALUES (?, ?, ?, ?, ?, 'Pending', ?, 0, 0)";
            String calendarSql = "INSERT INTO calendar_events (room_id, event_name, event_description, event_date, event_time, event_type) VALUES (?, ?, ?, ?, 2359, 'BILL')";
            String notificationSql = "INSERT INTO notifications (user_id, room_id, category, notification_text, detail, target_screen, tag_color, is_read) VALUES (?, ?, ?, ?, ?, 'FINANCES', '#25880d', 0)";  
            String findUserSql = "SELECT user_id FROM users WHERE username = ? AND room_id = ?";

            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false); 

                try (PreparedStatement pstmt1 = conn.prepareStatement(billSql)) {
                    pstmt1.setInt(1, currentRoomId);
                    pstmt1.setString(2, type);
                    pstmt1.setDouble(3, amount);
                    pstmt1.setString(4, date);
                    pstmt1.setString(5, payers);
                    pstmt1.setString(6, calculatedApprovalStatus); 
                    pstmt1.executeUpdate();
                }

                if (calculatedApprovalStatus.equals("Accepted")) {
                    try (PreparedStatement pstmt2 = conn.prepareStatement(calendarSql)) {
                        pstmt2.setInt(1, currentRoomId);
                        pstmt2.setString(2, "Bill: " + type);
                        pstmt2.setString(3, "Amount: " + amount + "€ | Payers: " + payers);
                        pstmt2.setString(4, date);
                        pstmt2.executeUpdate();
                    }
                } else {
                    String creator = currentUsername.isEmpty() ? "A roommate" : currentUsername;
                    List<Integer> targetUserIds = new ArrayList<>();

                    if (payers.trim().equalsIgnoreCase("All Roommates")) {
                        String findAllRoommatesSql = "SELECT user_id FROM users WHERE room_id = ? AND user_id != ?";
                        try (PreparedStatement pstmtAll = conn.prepareStatement(findAllRoommatesSql)) {
                            pstmtAll.setInt(1, currentRoomId);
                            pstmtAll.setInt(2, currentUserId);
                            try (ResultSet rs = pstmtAll.executeQuery()) {
                                while (rs.next()) {
                                    targetUserIds.add(rs.getInt("user_id"));
                                }
                            }
                        }
                    } else {
                        String[] targetUsers = payers.split(",");
                        for (String userRaw : targetUsers) {
                            String targetUsername = userRaw.trim();
                            if (targetUsername.equalsIgnoreCase(currentUsername) || targetUsername.equalsIgnoreCase("Only Me")) {
                                continue; 
                            }
                            try (PreparedStatement pstmtFind = conn.prepareStatement(findUserSql)) {
                                pstmtFind.setString(1, targetUsername);
                                pstmtFind.setInt(2, currentRoomId);
                                try (ResultSet rs = pstmtFind.executeQuery()) {
                                    if (rs.next()) {
                                        targetUserIds.add(rs.getInt("user_id"));
                                    }
                                }
                            }
                        }
                    }

                    for (int targetUserId : targetUserIds) {
                        try (PreparedStatement pstmtNotif = conn.prepareStatement(notificationSql)) {
                            pstmtNotif.setInt(1, targetUserId);
                            pstmtNotif.setInt(2, currentRoomId);
                            pstmtNotif.setString(3, "FINANCES");
                            
                            // 🌟 MATCHED SCREENSHOT LAYOUT DIRECTLY
                            pstmtNotif.setString(4, "Bill Approval"); // "Bill Approval" text goes back to bottom sub-header
                            
                            String structuralDetails = creator + " added you as a payer for the new bill:\n" +
                                                       "Type: " + type + "\n" +
                                                       "Amount: " + amount + "€";
                            pstmtNotif.setString(5, structuralDetails); // Multi-line block into details popup panel
                            pstmtNotif.executeUpdate();
                        }
                    }
                }

                conn.commit(); 
                
                Bill newlyCreatedBill = new Bill(type, amount, date, payers, "Pending", calculatedApprovalStatus);
                newlyCreatedBill.setCreatorUsername(currentUsername);
                
                FinancesScreen.allBills.add(newlyCreatedBill);
                onCancelAction.run();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                ErrorScreen.show("Database error: " + ex.getMessage());
            }
        } catch (NumberFormatException e) {
            ErrorScreen.show("Invalid amount format.");
        }
    }
}
