package myapplications;

import entities.Application;
import profile.ProfileScreen;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ui.ConfirmationScreen;
import ui.ErrorScreen;

public class NewApplicationScreen {

    public void fillForm() {
        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F8FAFC;");

        Label titleLabel = new Label("Application Form");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Label typeLabel = new Label("Property Type");
        typeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B5563;");
        ComboBox<String> cmbType = new ComboBox<>();
        cmbType.getItems().addAll("Flat", "Studio", "Apartment");
        cmbType.setValue("Flat");
        cmbType.setMaxWidth(Double.MAX_VALUE);
        cmbType.setStyle("-fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-padding: 6; -fx-background-color: white;");

        TextField txtLocation = createStyledField("Location (e.g., Kypseli)");
        TextField txtAddress = createStyledField("Address");
        TextField txtRent = createStyledField("Rent (€)");
        TextField txtRoommates = createStyledField("Roommates");
        
        TextArea txtDesc = new TextArea(); 
        txtDesc.setPromptText("Description...");
        txtDesc.setStyle("-fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-padding: 4;");
        txtDesc.setPrefHeight(100);

        Button previewBtn = new Button("Preview & Submit");
        previewBtn.setMaxWidth(Double.MAX_VALUE);
        previewBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");

        previewBtn.setOnAction(e -> {
            if(txtLocation.getText().isEmpty() || txtAddress.getText().isEmpty() || txtRent.getText().isEmpty() || txtRoommates.getText().isEmpty()) {
                new ErrorScreen("Input Error", "All fields are required!").show();
                return;
            }

            try {
                double rent = Double.parseDouble(txtRent.getText());
                int roommates = Integer.parseInt(txtRoommates.getText());
                String selectedType = cmbType.getValue();

                String summaryMessage = String.format(
                    "Property Type: %s\nLocation: %s\nAddress: %s\nRent: %.2f €\nRoommates: %d\n\nDescription: %s",
                    selectedType, txtLocation.getText(), txtAddress.getText(), rent, roommates, txtDesc.getText()
                );

                String confirmStyle = "-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;";

                ConfirmationScreen confirmDialog = new ConfirmationScreen(
                    "Confirm Listing Details",
                    summaryMessage,
                    "Confirm",
                    confirmStyle,
                    () -> {
                        String adTitle = selectedType + " " + txtLocation.getText();
                        
                        Application newApp = new Application(adTitle, txtLocation.getText(), txtAddress.getText(), rent, roommates, txtDesc.getText(), "PENDING");
                        
                        // --- ΑΛΛΑΓΗ ΕΔΩ: Αποθήκευση στη βάση για τον χρήστη με user_id = 1 ---
                        newApp.saveApplication(1); 
                        
                        SuccessScreen.display("Your ad was successfully created and is pending approval!");
                        
                        new MyApplicationScreen().display();
                    }
                );
                confirmDialog.show();

            } catch (NumberFormatException ex) {
                new ErrorScreen("Invalid Format", "Rent and Roommates must be valid numbers!").show();
            }
        });

        Button cancelBtn = new Button("Back");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> new MyApplicationScreen().display());

        root.getChildren().addAll(titleLabel, typeLabel, cmbType, 
                new Label("Location"), txtLocation, 
                new Label("Address"), txtAddress, 
                new Label("Rent"), txtRent, 
                new Label("Roommates"), txtRoommates, 
                new Label("Description"), txtDesc, 
                previewBtn, cancelBtn);
                
        ProfileScreen.getStage().setScene(new Scene(root, 420, 750));
    }

    private TextField createStyledField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-radius: 6; -fx-border-color: #CBD5E1; -fx-border-radius: 6; -fx-padding: 8; -fx-background-color: white;");
        return f;
    }
}