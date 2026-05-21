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
        // --- Header ---
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

        // --- Multi-Select Payers Component ---
        payersField = new TextField();
        payersField.setEditable(false);
        payersField.setPromptText("Click + to select payers...");
        payersField.setStyle("-fx-padding: 8; -fx-background-radius: 4 0 0 4; -fx-border-color: #D1D5DB; -fx-background-color: #f9fafb;");
        HBox.setHgrow(payersField, Priority.ALWAYS);

        payersMenuButton = new MenuButton("+");
        payersMenuButton.setStyle("-fx-background-color: #14B8A6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0 4 4 0; -fx-padding: 7 14; -fx-cursor: hand;");
        
        // Φόρτωση δυναμικών συγκατοίκων από τη Βάση Δεδομένων
        setupDynamicPayersDropdown();

        HBox payersContainer = new HBox(0, payersField, payersMenuButton);
        payersContainer.setAlignment(Pos.CENTER_LEFT);

        addFormField(grid, "Bill Type:", typeField, 0);
        addFormField(grid, "Amount (€):", amountField, 1);
        addFormField(grid, "Due Date:", datePicker, 2);
        addFormField(grid, "Payers:", payersContainer, 3);

        formCard.getChildren().add(grid);
        this.getChildren().add(formCard);

        // --- Bottom Actions ---
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
        // Φέρνουμε όλους τους χρήστες που ανήκουν στο ίδιο room_id
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

        // 1. Προσθήκη επιλογής "All Roommates"
        CheckMenuItem itemAll = new CheckMenuItem("All Roommates");
        payersMenuButton.getItems().add(itemAll);

        // Λίστα για να κρατάμε αναφορές στα υπόλοιπα CheckMenuItems των χρηστών
        List<CheckMenuItem> userItems = new ArrayList<>();

        // 2. Δημιουργία CheckMenuItem για κάθε πραγματικό συγκατοίκο
        for (String name : roommates) {
            CheckMenuItem userItem = new CheckMenuItem(name);
            userItems.add(userItem);
            payersMenuButton.getItems().add(userItem);
        }

        // Λογική ανανέωσης του κειμένου
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

        // Event Handlers
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
            String date = datePicker.getValue().toString();
            String payers = payersField.getText();

            int currentRoomId = 0;
            if (entities.Authentication.getCurrentUser() != null) {
                currentRoomId = entities.Authentication.getCurrentUser().getRoomId();
            }

            String billSql = "INSERT INTO bills (room_id, bill_type, amount, bill_date, payers, bill_status) VALUES (?, ?, ?, ?, ?, 'Pending')";
            String calendarSql = "INSERT INTO calendar_events (room_id, event_name, event_description, event_date, event_time, event_type) VALUES (?, ?, ?, ?, 0900, 'BILL')";

            try (Connection conn = DatabaseManager.getConnection()) {
                conn.setAutoCommit(false); 

                try (PreparedStatement pstmt1 = conn.prepareStatement(billSql)) {
                    pstmt1.setInt(1, currentRoomId);
                    pstmt1.setString(2, type);
                    pstmt1.setDouble(3, amount);
                    pstmt1.setString(4, date);
                    pstmt1.setString(5, payers);
                    pstmt1.executeUpdate();
                }

                try (PreparedStatement pstmt2 = conn.prepareStatement(calendarSql)) {
                    pstmt2.setInt(1, currentRoomId);
                    pstmt2.setString(2, "Bill: " + type);
                    pstmt2.setString(3, "Amount: " + amount + "€ | Payers: " + payers);
                    pstmt2.setString(4, date);
                    pstmt2.executeUpdate();
                }

                conn.commit(); 
                
                FinancesScreen.allBills.add(new Bill(type, amount, date, payers, "Pending"));
                onCancelAction.run();
                
            } catch (Exception ex) {
                ErrorScreen.show("Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (NumberFormatException e) {
            ErrorScreen.show("Invalid amount format.");
        }
    }
}