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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class FinancesScreen extends VBox {

    private TableView<Bill> pendingBillsTable;
    private TableView<Bill> paidBillsTable;

    private final Runnable onBackToHub; // Callback για επιστροφή στο αρχικό Hub
    private final Runnable onNavigateToCreate; // Callback για μετάβαση στη φόρμα

    public static final ObservableList<Bill> allBills = FXCollections.observableArrayList();

    static {
        if (allBills.isEmpty()) {
            allBills.add(new Bill("Electricity", 120.50, "2026-05-10", "Giorgos, Alex", "Pending"));
            allBills.add(new Bill("Internet", 35.00, "2026-05-01", "All Roommates", "Paid"));
            allBills.add(new Bill("Water", 45.20, "2026-05-14", "Giorgos", "Pending"));
        }
    }

    // Ο Constructor δέχεται πλέον τα Callbacks πλοήγησης
    public FinancesScreen(Runnable onBackToHub, Runnable onNavigateToCreate) {
        this.onBackToHub = onBackToHub;
        this.onNavigateToCreate = onNavigateToCreate;
        
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #F3F4F6;"); 
        
        buildUI();
    }

    private void buildUI() {
        // --- 1. Header Section ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1F2937; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
        
        // Χρήση του Callback αντί για απευθείας κλήση στη main
        backBtn.setOnAction(e -> {
            if (onBackToHub != null) onBackToHub.run();
        });

        Label headerTitle = new Label("Utility Finances");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        headerTitle.setTextFill(Color.web("#1F2937"));

        header.getChildren().addAll(backBtn, headerTitle);
        this.getChildren().add(header);

        // --- 2. Tables Section ---
        VBox centerContainer = new VBox(20);
        centerContainer.setPadding(new Insets(20));
        VBox.setVgrow(centerContainer, Priority.ALWAYS);

        Label pendingTitle = new Label("⏳ Pending Bills");
        pendingTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        pendingTitle.setTextFill(Color.web("#374151"));

        pendingBillsTable = createStyledTable();
        VBox.setVgrow(pendingBillsTable, Priority.ALWAYS);

        Label paidTitle = new Label("✅ Paid Bills History");
        paidTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        paidTitle.setTextFill(Color.web("#374151"));

        paidBillsTable = createStyledTable();
        VBox.setVgrow(paidBillsTable, Priority.ALWAYS);

        centerContainer.getChildren().addAll(pendingTitle, pendingBillsTable, paidTitle, paidBillsTable);
        this.getChildren().add(centerContainer);

        // --- 3. Bottom Action Bar ---
        HBox bottomArea = new HBox(15);
        bottomArea.setPadding(new Insets(15, 20, 15, 20));
        bottomArea.setAlignment(Pos.CENTER_RIGHT);
        bottomArea.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, -2);");

        Button payBtn = new Button("Mark as Paid");
        payBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        payBtn.setOnAction(e -> handlePayBill());

        Button navigateCreateBtn = new Button("New Bill +");
        navigateCreateBtn.setStyle("-fx-background-color: #14B8A6; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand;");
        
        // Χρήση του Callback για μετάβαση στη φόρμα
        navigateCreateBtn.setOnAction(e -> {
            if (onNavigateToCreate != null) onNavigateToCreate.run();
        });

        bottomArea.getChildren().addAll(payBtn, navigateCreateBtn);
        this.getChildren().add(bottomArea);

        setupTableData();
    }

    private TableView<Bill> createStyledTable() {
        TableView<Bill> table = new TableView<>();
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #E5E7EB;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Bill, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Bill, Double> amountCol = new TableColumn<>("Amount (€)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Bill, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Bill, String> payersCol = new TableColumn<>("Payers");
        payersCol.setCellValueFactory(new PropertyValueFactory<>("payers"));

        table.getColumns().addAll(typeCol, amountCol, dateCol, payersCol);
        return table;
    }

    private void setupTableData() {
        FilteredList<Bill> pendingBills = new FilteredList<>(
            allBills, bill -> bill.getStatus() != null && bill.getStatus().equalsIgnoreCase("Pending")
        );

        FilteredList<Bill> paidBills = new FilteredList<>(
            allBills, bill -> bill.getStatus() != null && bill.getStatus().equalsIgnoreCase("Paid")
        );

        pendingBillsTable.setItems(pendingBills);
        paidBillsTable.setItems(paidBills);
    }

    private void handlePayBill() {
        Bill selectedBill = pendingBillsTable.getSelectionModel().getSelectedItem();
        
        if (selectedBill == null) {
            Bill paidSelection = paidBillsTable.getSelectionModel().getSelectedItem();
            if (paidSelection != null && "Paid".equalsIgnoreCase(paidSelection.getStatus())) {
                ErrorScreen.show("This bill instance has already been marked as paid.");
                paidBillsTable.getSelectionModel().clearSelection();
                return;
            }
            ErrorScreen.show("Please select a specific pending bill row entry from the table first.");
            return;
        }

        // ΔΙΟΡΘΩΣΗ: Χρήση του Constructor 6 για custom Τίτλο και Μήνυμα Πληρωμής
        ConfirmationScreen confirmDialog = new ConfirmationScreen(
            "Confirm Bill Payment", 
            "Are you sure you want to mark the bill '" + selectedBill.getType() + "' (" + selectedBill.getAmount() + "€) as paid?",
            () -> {
                // Live μεταφορά στο History Log (αφαίρεση και επανεισαγωγή για ακαριαίο UI trigger)
                allBills.remove(selectedBill);
                selectedBill.setStatus("Paid");
                
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
                selectedBill.setDate(today.format(formatter));
                
                allBills.add(selectedBill);
                
                pendingBillsTable.getSelectionModel().clearSelection();
                paidBillsTable.getSelectionModel().clearSelection();
            },
            () -> pendingBillsTable.getSelectionModel().clearSelection()
        );
        confirmDialog.show();
    }
}