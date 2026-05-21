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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewBillScreen extends VBox {

    private TextField typeField;
    private TextField amountField;
    private DatePicker datePicker;
    
    // Υβριδικά πεδία για τους Payers (ίδιο design με το NewIssueScreen)
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

        // --- Multi-Select Payers Component (IDIO DESIGN ME NEWISSUESCREEN) ---
        payersField = new TextField();
        payersField.setEditable(false);
        payersField.setPromptText("Click + to select payers...");
        payersField.setStyle("-fx-padding: 8; -fx-background-radius: 4 0 0 4; -fx-border-color: #D1D5DB; -fx-background-color: #f9fafb;");
        HBox.setHgrow(payersField, Priority.ALWAYS);

        payersMenuButton = new MenuButton("+");
        payersMenuButton.setStyle("-fx-background-color: #14B8A6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0 4 4 0; -fx-padding: 7 14; -fx-cursor: hand;");
        
        CheckMenuItem itemAll = new CheckMenuItem("All Roommates");
        CheckMenuItem itemAlex = new CheckMenuItem("Alex");
        CheckMenuItem itemJohn = new CheckMenuItem("John");
        CheckMenuItem itemSarah = new CheckMenuItem("Sarah");
        CheckMenuItem itemEmma = new CheckMenuItem("Emma");

        payersMenuButton.getItems().addAll(itemAll, itemAlex, itemJohn, itemSarah, itemEmma);

        Runnable updatePayersText = () -> {
            if (itemAll.isSelected()) {
                payersField.setText("All Roommates");
                itemAlex.setSelected(false); itemJohn.setSelected(false);
                itemSarah.setSelected(false); itemEmma.setSelected(false);
                return;
            }
            List<String> selectedPayers = new ArrayList<>();
            if (itemAlex.isSelected()) selectedPayers.add("Alex");
            if (itemJohn.isSelected()) selectedPayers.add("John");
            if (itemSarah.isSelected()) selectedPayers.add("Sarah");
            if (itemEmma.isSelected()) selectedPayers.add("Emma");
            payersField.setText(String.join(", ", selectedPayers));
        };

        itemAll.setOnAction(e -> updatePayersText.run());
        itemAlex.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });
        itemJohn.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });
        itemSarah.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });
        itemEmma.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });

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

        // SQL queries
        String billSql = "INSERT INTO bills (room_id, bill_type, amount, bill_date, payers, bill_status) VALUES (1, ?, ?, ?, ?, 'Pending')";
        String calendarSql = "INSERT INTO calendar_events (room_id, event_name, event_description, event_date, event_time, event_type) VALUES (1, ?, ?, ?, 0900, 'BILL')";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction (Ensure both succeed)

            // 1. Save to Bills table
            try (PreparedStatement pstmt1 = conn.prepareStatement(billSql)) {
                pstmt1.setString(1, type);
                pstmt1.setDouble(2, amount);
                pstmt1.setString(3, date);
                pstmt1.setString(4, payers);
                pstmt1.executeUpdate();
            }

            // 2. Save to Calendar table
            try (PreparedStatement pstmt2 = conn.prepareStatement(calendarSql)) {
                pstmt2.setString(1, "Bill: " + type);
                pstmt2.setString(2, "Amount: " + amount + "€ | Payers: " + payers);
                pstmt2.setString(3, date);
                pstmt2.executeUpdate();
            }

            conn.commit(); // Save both changes
            
            // Update UI list (Memory)
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