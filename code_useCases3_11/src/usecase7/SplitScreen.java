package usecase7;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitScreen {
    private Allocation resultAllocation = null;
    private final List<String> roommates = List.of("Giannis", "Manos", "Makis");

    public Allocation insertAllocationStatus(Stage owner, File imageFile, Allocation existingAlloc) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Οθόνη Split");

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        // --- 1. ΕΜΦΑΝΙΣΗ ΦΩΤΟΓΡΑΦΙΑΣ ΑΠΟΔΕΙΞΗΣ ---
        if (imageFile != null) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(290); 
                imageView.setFitHeight(110); 
                imageView.setPreserveRatio(true); 
                
                HBox imageBox = new HBox(imageView);
                imageBox.setAlignment(Pos.CENTER);
                imageBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-padding: 5px; -fx-background-color: #f9f9f9;");
                
                root.getChildren().add(imageBox);
            } catch (Exception ex) {
                System.out.println("Σφάλμα φόρτωσης εικόνας: " + ex.getMessage());
            }
        }

        // --- 2. ΣΥΝΟΛΙΚΟ ΠΟΣΟ ΣΥΝΑΛΛΑΓΗΣ ---
        Label totalLabel = new Label("Total Amount (€):");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        String initialTotal = "0";
        if (existingAlloc != null) {
            double tot = existingAlloc.getTotalAmount();
            initialTotal = (tot == (long) tot) ? String.valueOf((long) tot) : String.valueOf(tot);
        }
        TextField totalAmountField = new TextField(initialTotal);
        totalAmountField.setPrefWidth(90);
        
        HBox totalBox = new HBox(10, totalLabel, totalAmountField);
        totalBox.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(totalBox);

        // --- 3. CHOOSE A RECEIVER (Dropdown List) ---
        Label receiverLabel = new Label("Choose a receiver:");
        receiverLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        ComboBox<String> receiverComboBox = new ComboBox<>();
        receiverComboBox.getItems().addAll(roommates);
        receiverComboBox.setMaxWidth(Double.MAX_VALUE);
        receiverComboBox.setPromptText("-");

        VBox receiverSection = new VBox(5, receiverLabel, receiverComboBox);
        root.getChildren().add(receiverSection);

        // --- 4. ΔΥΝΑΜΙΚΑ ΠΕΔΙΑ ΥΠΟΛΟΙΠΩΝ ΣΥΓΚΑΤΟΙΚΩΝ ---
        VBox dynamicFieldsContainer = new VBox(10);
        Map<String, TextField> fieldsMap = new HashMap<>();
        root.getChildren().add(dynamicFieldsContainer);

        // Σύνδεση του Listener με τη νέα μέθοδο δημιουργίας πεδίων
        receiverComboBox.setOnAction(e -> {
            populateRoommateFields(receiverComboBox.getValue(), existingAlloc, dynamicFieldsContainer, fieldsMap);
        });

        // ΔΙΟΡΘΩΣΗ: Αν είμαστε σε Edit mode, επιλέγουμε τον receiver ΚΑΙ καλούμε ρητά 
        // τη μέθοδο για να εμφανιστούν ακαριαία οι συγκατοίκοι από κάτω!
        if (existingAlloc != null) {
            receiverComboBox.setValue(existingAlloc.getReceiver());
            populateRoommateFields(existingAlloc.getReceiver(), existingAlloc, dynamicFieldsContainer, fieldsMap);
        }

        // --- 5. ΚΟΥΜΠΙ CONFIRM ---
        Button confirmBtn = new Button("Confirm");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle("-fx-font-weight: bold;");
        confirmBtn.setOnAction(e -> {
            String selectedReceiver = receiverComboBox.getValue();
            
            if (selectedReceiver == null || selectedReceiver.equals("-")) {
                ErrorScreen.show("Παρακαλώ επιλέξτε ποιος πλήρωσε!");
                return;
            }
            
            try {
                double totalAmt = Double.parseDouble(totalAmountField.getText());
                Map<String, Double> amounts = new HashMap<>();
                
                double othersSum = 0.0;
                for (Map.Entry<String, TextField> entry : fieldsMap.entrySet()) {
                    String name = entry.getKey();
                    double amount = Double.parseDouble(entry.getValue().getText());
                    amounts.put(name, amount);
                    othersSum += amount;
                }

                // Αυτόματος υπολογισμός receiver (Σύνολο - Υπόλοιποι)
                double receiverAmount = totalAmt - othersSum;
                amounts.put(selectedReceiver, receiverAmount);

                LocalDate allocDate = (existingAlloc != null) ? existingAlloc.getDate() : LocalDate.now();
                resultAllocation = new Allocation(allocDate, amounts, imageFile, totalAmt, selectedReceiver);
                
                Notification.makeNotification("SHOPPING", "Δημιουργήθηκε νέος επιμερισμός!");
                stage.close();
            } catch (NumberFormatException ex) {
                ErrorScreen.show("Εισάγετε έγκυρα ποσά!");
            }
        });
        root.getChildren().add(confirmBtn);

        Scene scene = new Scene(root, 340, 440);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        return resultAllocation;
    }

    // ΝΕΑ ΑΥΤΟΝΟΜΗ ΜΕΘΟΔΟΣ: Δημιουργεί και γεμίζει τα πεδία των συγκατοίκων με απόλυτη ασφάλεια
    private void populateRoommateFields(String selectedReceiver, Allocation existingAlloc, VBox dynamicFieldsContainer, Map<String, TextField> fieldsMap) {
        dynamicFieldsContainer.getChildren().clear();
        fieldsMap.clear();
        
        if (selectedReceiver != null && !selectedReceiver.equals("-")) {
            for (String roommate : roommates) {
                if (!roommate.equals(selectedReceiver)) {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    
                    Label label = new Label("- " + roommate + ":");
                    label.setPrefWidth(80); 
                    
                    String defaultVal = "0";
                    // Αν είναι Edit και ο receiver παραμένει ίδιος, φορτώνουμε τις αποθηκευμένες τιμές
                    if (existingAlloc != null && selectedReceiver.equals(existingAlloc.getReceiver())) {
                        if (existingAlloc.getMemberAmounts().containsKey(roommate)) {
                            double amt = existingAlloc.getMemberAmounts().get(roommate);
                            defaultVal = (amt == (long) amt) ? String.valueOf((long) amt) : String.valueOf(amt);
                        }
                    }
                    
                    TextField textField = new TextField(defaultVal); 
                    textField.setPrefWidth(80);
                    
                    Label euroSign = new Label("€"); 
                    
                    row.getChildren().addAll(label, textField, euroSign);
                    dynamicFieldsContainer.getChildren().add(row);
                    
                    fieldsMap.put(roommate, textField);
                }
            }
        }
    }
}