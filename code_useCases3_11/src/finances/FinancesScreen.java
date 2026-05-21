package finances;

import entities.Bill;
import ui.ConfirmationScreen;
import ui.ErrorScreen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.sql.*;

public class FinancesScreen extends VBox {

    private TableView<Bill> pendingBillsTable;
    private TableView<Bill> paidBillsTable;
    
    private final Runnable onBackToHub;
    private final Runnable onNavigateToCreate;

    public static final ObservableList<Bill> allBills = FXCollections.observableArrayList();

    public FinancesScreen(Runnable onBackToHub, Runnable onNavigateToCreate) {
      
        loadDataFromDatabase();
        
        this.onBackToHub = onBackToHub;
        this.onNavigateToCreate = onNavigateToCreate;
        
       

        this.pendingBillsTable = createStyledTable();
        this.paidBillsTable = createStyledTable();
        setupTableData();
        
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #F3F4F6;");
        buildUI();
    }

    private void buildUI() {
        // 1. Header
        HBox header = new HBox(15);
        header.setStyle("-fx-background-color: #1e293b; -fx-padding: 15 25;");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f8fafc; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand;");
        backBtn.setOnAction(e -> { if (onBackToHub != null) onBackToHub.run(); });

        Label headerTitle = new Label("Utility Finances");
        headerTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 18px; -fx-font-weight: bold;");
        header.getChildren().addAll(backBtn, headerTitle);

        // 2. Main Content
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        VBox leftSection = new VBox(20);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        leftSection.getChildren().addAll(
            createTableCard("  Pending Bills", "#f97316", pendingBillsTable),
            createTableCard("  Paid Bills History", "#10b981", paidBillsTable)
        );

        VBox rightSection = createMetricsCard();
        rightSection.setMinWidth(250);
        rightSection.setMaxWidth(250);
        mainLayout.getChildren().addAll(leftSection, rightSection);

        // 3. Bottom Bar
        HBox bottomArea = new HBox(12);
        bottomArea.setPadding(new Insets(15, 30, 15, 30));
        bottomArea.setAlignment(Pos.CENTER_RIGHT);
        bottomArea.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1;");

        Button payBtn = new Button("Pay Selected Bill");
        payBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;");
        payBtn.setOnAction(e -> handlePayBill());

        Button navigateCreateBtn = new Button("+ Create New Bill");
        navigateCreateBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;");
        navigateCreateBtn.setOnAction(e -> { if (onNavigateToCreate != null) onNavigateToCreate.run(); });

        bottomArea.getChildren().addAll(payBtn, navigateCreateBtn);

        this.getChildren().addAll(header, mainLayout, bottomArea);
    }

    private VBox createTableCard(String titleText, String color, TableView<Bill> table) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 15;");
        
        Rectangle pill = new Rectangle(8, 20);
        pill.setFill(Color.web(color));
        pill.setArcWidth(8);
        pill.setArcHeight(8);

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        
        HBox titleBox = new HBox(8, pill, title);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        card.getChildren().addAll(titleBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return card;
    }

    private VBox createMetricsCard() {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-padding: 20;");
        
        Rectangle pill = new Rectangle(8, 20);
        pill.setFill(Color.web("#6366f1"));
        pill.setArcWidth(8);
        pill.setArcHeight(8);

        Label title = new Label(" Metrics");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        
        Label statLabel = new Label("Active Pending Bills");
        statLabel.setStyle("-fx-text-fill: #64748b;");
        
        Label count = new Label(String.valueOf(pendingBillsTable.getItems().size()));
        count.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(new HBox(8, pill, title), statLabel, count);
        return card;
    }

    private TableView<Bill> createStyledTable() {
        TableView<Bill> table = new TableView<>();
        table.setStyle("-fx-border-color: #e2e8f0;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Bill, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Bill, Double> amountCol = new TableColumn<>("Amount (€)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        TableColumn<Bill, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        table.getColumns().addAll(typeCol, amountCol, dateCol);
        return table;
    }

    private void setupTableData() {
        pendingBillsTable.setItems(new FilteredList<>(allBills, b -> "Pending".equalsIgnoreCase(b.getStatus())));
        paidBillsTable.setItems(new FilteredList<>(allBills, b -> "Paid".equalsIgnoreCase(b.getStatus())));
    }

    private void loadDataFromDatabase() {
        allBills.clear();
        String sql = "SELECT * FROM bills";
        // REPLACE 'DatabaseManager.getConnection()' WITH YOUR ACTUAL DB CONNECTION
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://bv6dtpgk7dfpjgusrjtc-mysql.services.clever-cloud.com:3306/bv6dtpgk7dfpjgusrjtc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "u4ezo6ujbhl2ltqc", "ODL7dkZjMVMENPqKgOoe");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allBills.add(new Bill(
                    rs.getString("bill_type"),
                    rs.getDouble("amount"),
                    rs.getString("bill_date"),
                    rs.getString("payers"),
                    rs.getString("bill_status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handlePayBill() {
    Bill selected = pendingBillsTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
        ErrorScreen.show("Please select a pending bill.");
        return;
    }

    new ConfirmationScreen("Pay Bill", "Mark as paid?", () -> {
        // We use a transaction to ensure both DBs update or none do
        String updateBillSql = "UPDATE bills SET bill_status = 'Paid' WHERE bill_type = ? AND bill_date = ?";
        // Assuming your calendar events are linked by date and name
        String deleteCalendarSql = "DELETE FROM calendar_events WHERE event_name = ? AND event_date = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://bv6dtpgk7dfpjgusrjtc-mysql.services.clever-cloud.com:3306/bv6dtpgk7dfpjgusrjtc?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", "u4ezo6ujbhl2ltqc", "ODL7dkZjMVMENPqKgOoe")) {
            conn.setAutoCommit(false); // Start transaction

            // 1. Update status in bills table
            try (PreparedStatement pstmt1 = conn.prepareStatement(updateBillSql)) {
                pstmt1.setString(1, selected.getType());
                pstmt1.setString(2, selected.getDate());
                pstmt1.executeUpdate();
            }

            // 2. Remove from calendar table
            try (PreparedStatement pstmt2 = conn.prepareStatement(deleteCalendarSql)) {
                pstmt2.setString(1, "Bill: " + selected.getType());
                pstmt2.setString(2, selected.getDate());
                pstmt2.executeUpdate();
            }

            conn.commit(); // Finalize both changes
            
            // Update UI Memory
            allBills.remove(selected);
            selected.setStatus("Paid");
            allBills.add(selected);
            
        } catch (SQLException ex) {
            ErrorScreen.show("Database error: Could not complete payment.");
            ex.printStackTrace();
        }
    }, () -> {}).show();
}
}