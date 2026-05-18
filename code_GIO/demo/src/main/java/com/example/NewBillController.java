package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class NewBillController {

    @FXML private TextField typeField;
    @FXML private TextField amountField;
    @FXML private DatePicker datePicker;
    @FXML private TextField payersField;

    @FXML
    private void handleCreateBill() throws IOException {
        String type = typeField.getText().trim();
        String amountRaw = amountField.getText().trim();
        String payers = payersField.getText().trim();

        // Handles validation logic and redirects to the error screen view
        if (type.isEmpty() || amountRaw.isEmpty() || datePicker.getValue() == null || payers.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Form Validation Exception");
            alert.setHeaderText("Missing Form Properties Detected");
            alert.setContentText("All fields are mandatory. Please fill in the details fully before hitting create.");
            alert.showAndWait();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountRaw);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Amount entry type formats. Please verify numeric input contents parsing values.");
            alert.showAndWait();
            return;
        }

        String formattedDate = datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Instantiate structural model and assign reference tracking pointers
        Bill newBill = new Bill(type, amount, formattedDate, payers, "Pending");
        DataRepository.getBills().add(newBill);

        // Redirect runtime layout execution sequences back towards home space dashboard elements
        App.setRoot("finances");
    }

    @FXML
    private void handleCancel() throws IOException {
        App.setRoot("finances");
    }
}