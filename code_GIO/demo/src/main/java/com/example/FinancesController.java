package com.example;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;
import java.util.Optional;

public class FinancesController {


    @FXML
    private TableView<Bill> pendingBillsTable;
    @FXML private TableColumn<Bill, String> pendTypeCol;
    @FXML private TableColumn<Bill, Double> pendAmountCol;
    @FXML private TableColumn<Bill, String> pendDateCol;
    @FXML private TableColumn<Bill, String> pendPayersCol;

    @FXML
    private TableView<Bill> paidBillsTable;
    @FXML private TableColumn<Bill, String> paidTypeCol;
    @FXML private TableColumn<Bill, Double> paidAmountCol;
    @FXML private TableColumn<Bill, String> paidDateCol;
    @FXML private TableColumn<Bill, String> paidPayersCol;
    

   
    
   
    @FXML
    public void initialize() {
        // 1. Set up cell value factories for the PENDING Table
        pendTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        pendAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        pendDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        pendPayersCol.setCellValueFactory(new PropertyValueFactory<>("payers"));

        // 2. Set up cell value factories for the PAID Table
        paidTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        paidAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        paidDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        paidPayersCol.setCellValueFactory(new PropertyValueFactory<>("payers"));

        // 3. Get the complete list of bills
        javafx.collections.ObservableList<Bill> allBills = DataRepository.getBills();

        // 4. Filter the bills automatically into two separate lists based on their status
        javafx.collections.transformation.FilteredList<Bill> pendingBills = new javafx.collections.transformation.FilteredList<>(
            allBills, bill -> bill.getStatus() != null && bill.getStatus().equalsIgnoreCase("Pending")
        );

        javafx.collections.transformation.FilteredList<Bill> paidBills = new javafx.collections.transformation.FilteredList<>(
            allBills, bill -> bill.getStatus() != null && bill.getStatus().equalsIgnoreCase("Paid")
        );

        // 5. Link the filtered lists to your respective tables
        pendingBillsTable.setItems(pendingBills);
        paidBillsTable.setItems(paidBills);
        // Link table variables directly to properties inside your object definitions
        /* 
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        payersCol.setCellValueFactory(new PropertyValueFactory<>("payers"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        billsTable.setItems(DataRepository.getBills());
        recalculateBalances();
        */
    }

  

    @FXML
    private void handlePayBill() {
     
        // 1. Try to find if a bill is selected from the pending table
        Bill selectedBill = pendingBillsTable.getSelectionModel().getSelectedItem();
        
        // 2. If nothing is selected in Pending, check if they accidentally selected an already paid bill
        if (selectedBill == null) {
            Bill paidSelection = paidBillsTable.getSelectionModel().getSelectedItem();
            if (paidSelection != null && "Paid".equalsIgnoreCase(paidSelection.getStatus())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Operation");
                alert.setHeaderText("Action Blocked");
                alert.setContentText("This bill instance has already been marked as paid.");
                alert.showAndWait();
                
                // Clear the selection on the paid table so it doesn't get stuck
                paidBillsTable.getSelectionModel().clearSelection();
                return;
            }
            
            // If truly nothing from either table is selected
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a specific pending bill row entry from the table first.");
            alert.showAndWait();
            return;
        }

        // 3. Double-check guard clause just in case state tracking gets out of sync
        if (selectedBill.getStatus().equalsIgnoreCase("Paid")) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "This bill instance has already been marked as paid.");
            alert.showAndWait();
            pendingBillsTable.getSelectionModel().clearSelection();
            return;
        }

        // 4. Show Confirmation Screen 
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Transaction Action");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to mark " + selectedBill.getType() + " (€" + selectedBill.getAmount() + ") as paid?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            // A. Remove the old pending instance from the main repository data list
            DataRepository.getBills().remove(selectedBill);
            
            // B. Change the status property to Paid
            selectedBill.setStatus("Paid");
            
            // C. CRITICAL CHANGE: Update the bill's date to today's current payment date (yyyy-MM-dd)
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
            selectedBill.setDate(today.format(formatter));
            
            // D. Push back to repository list to trigger FilteredList UI moving transitions
            DataRepository.getBills().add(selectedBill);
            
            // E. Clean clear all remaining table focuses to ensure safe future click triggers
            pendingBillsTable.getSelectionModel().clearSelection();
            paidBillsTable.getSelectionModel().clearSelection();
        }
    }
    

    @FXML
    private void handleNavigateToCreate() throws IOException {
        App.setRoot("new_bill");
    }
}