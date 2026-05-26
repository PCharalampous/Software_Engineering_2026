package shoppinglist;

import entities.Allocation;
import entities.Authentication;
import ui.ErrorScreen;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitScreen {
    private Allocation resultAllocation = null;
    
    // Η λίστα γίνεται δυναμική, χωρίς καρφωμένες τιμές
    private List<String> roommates;

    // Constructor που δέχεται δυναμικά τη λίστα των συγκατοίκων
    public SplitScreen(List<String> roommates) {
        this.roommates = roommates;
    }

    public Allocation insertAllocationStatus(Stage owner, File imageFile, Allocation existingAlloc) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("Split");

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
        receiverComboBox.getItems().addAll(roommates); // Γεμίζει δυναμικά από τη βάση
        receiverComboBox.setMaxWidth(Double.MAX_VALUE);
        receiverComboBox.setPromptText("-");

        VBox receiverSection = new VBox(5, receiverLabel, receiverComboBox);
        root.getChildren().add(receiverSection);

        // --- 4. ΔΥΝΑΜΙΚΑ ΠΕΔΙΑ ΥΠΟΛΟΙΠΩΝ ΣΥΓΚΑΤΟΙΚΩΝ ---
        VBox dynamicFieldsContainer = new VBox(10);
        Map<String, TextField> fieldsMap = new HashMap<>();
        root.getChildren().add(dynamicFieldsContainer);

        receiverComboBox.setOnAction(e -> {
            populateRoommateFields(receiverComboBox.getValue(), existingAlloc, dynamicFieldsContainer, fieldsMap);
        });

        if (existingAlloc != null) {
            receiverComboBox.setValue(existingAlloc.getReceiver());
            populateRoommateFields(existingAlloc.getReceiver(), existingAlloc, dynamicFieldsContainer, fieldsMap);
        }

        // --- 5. ΚΟΥΜΠΙ CONFIRM ΜΕ ΕΛΕΓΧΟΥΣ ΕΓΚΥΡΟΤΗΤΑΣ ---
        Button confirmBtn = new Button("Confirm");
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setStyle("-fx-font-weight: bold;");
        confirmBtn.setOnAction(e -> {
            String selectedReceiver = receiverComboBox.getValue();
            
            if (selectedReceiver == null || selectedReceiver.equals("-")) {
                ErrorScreen.show("Please choose a receiver!");
                return;
            }
            
            try {
                double totalAmt = Double.parseDouble(totalAmountField.getText());
                
                if (totalAmt < 0) {
                    ErrorScreen.show("Total amount can not be negative!");
                    return;
                }

                Map<String, Double> amounts = new HashMap<>();
                double othersSum = 0.0;
                
                for (Map.Entry<String, TextField> entry : fieldsMap.entrySet()) {
                    String name = entry.getKey();
                    double amount = Double.parseDouble(entry.getValue().getText());
                    
                    if (amount < 0) {
                        ErrorScreen.show("The amount for " + name + " can not be negative!");
                        return;
                    }
                    
                    amounts.put(name, amount);
                    othersSum += amount;
                }

                if (othersSum > totalAmt) {
                    ErrorScreen.show("The sum of the payers amounts can not be higher than the total amount!");
                    return;
                }

                double receiverAmount = totalAmt - othersSum;
                amounts.put(selectedReceiver, receiverAmount);

                LocalDate allocDate = (existingAlloc != null) ? existingAlloc.getDate() : LocalDate.now();
                resultAllocation = new Allocation(allocDate, amounts, imageFile, totalAmt, selectedReceiver);
                
                // --- ΔΙΟΡΘΩΣΗ: Αποστολή ειδοποίησης ΜΟΝΟ στους ΥΠΟΛΟΙΠΟΥΣ συγκατοίκους με σωστά ονόματα στηλών ---
                if (Authentication.getCurrentUser() != null) {
                    int currentUserId = Authentication.getCurrentUser().getId();
                    String currentUsername = Authentication.getCurrentUser().getUsername();
                    int userRoomId = Authentication.getCurrentUser().getRoomId();

                    try (Connection conn = util.DatabaseManager.getConnection()) {
                        String notificationText;
                        String notificationDetail;
                        
                        if (existingAlloc == null) {
                            notificationText = "Νέος επιμερισμός Shopping List!";
                            notificationDetail = "Ο/Η " + currentUsername + " πρόσθεσε νέα έξοδα ύψους €" + String.format("%.2f", totalAmt);
                        } else {
                            notificationText = "Τροποποίηση επιμερισμού Shopping List!";
                            notificationDetail = "Ο/Η " + currentUsername + " ενημέρωσε τα έξοδα ενός επιμερισμού στα €" + String.format("%.2f", totalAmt);
                        }

                        // Επιλέγουμε όλα τα μέλη του δωματίου ΕΚΤΟΣ από τον τρέχοντα χρήστη που κάνει το confirm
                        String fetchRoommatesSql = "SELECT user_id FROM users WHERE room_id = ? AND user_id != ?";
                        try (PreparedStatement psRoommates = conn.prepareStatement(fetchRoommatesSql)) {
                            psRoommates.setInt(1, userRoomId);
                            psRoommates.setInt(2, currentUserId);
                            
                            try (ResultSet rsRoommates = psRoommates.executeQuery()) {
                                // ΔΙΟΡΘΩΘΗΚΕ: Τα ονόματα των στηλών άλλαξαν σε room_id, notification_text και target_screen σύμφωνα με το σχήμα της βάσης
                                String insertNotificationSql = "INSERT INTO notifications (user_id, room_id, category, notification_text, detail, target_screen, tag_color, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
                                try (PreparedStatement psInsert = conn.prepareStatement(insertNotificationSql)) {
                                    
                                    while (rsRoommates.next()) {
                                        int targetUserId = rsRoommates.getInt("user_id");
                                        psInsert.setInt(1, targetUserId);
                                        psInsert.setInt(2, userRoomId);
                                        psInsert.setString(3, "SHOPPING");
                                        psInsert.setString(4, notificationText);
                                        psInsert.setString(5, notificationDetail);
                                        psInsert.setString(6, "SHOPPING_SCREEN");
                                        psInsert.setString(7, "#F59E0B");
                                        psInsert.addBatch();
                                    }
                                    psInsert.executeBatch(); // Εκτέλεση μαζικής εισαγωγής για όλους τους υπόλοιπους
                                }
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Σφάλμα κατά την αποστολή της ειδοποίησης SHOPPING στη βάση:");
                        ex.printStackTrace();
                    }
                }
                // --------------------------------------------------------------------------------------------------

                stage.close();
            } catch (NumberFormatException ex) {
                ErrorScreen.show("Wrong input!");
            }
        });
        root.getChildren().add(confirmBtn);

        Scene scene = new Scene(root, 340, 440);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        return resultAllocation;
    }

    private void populateRoommateFields(String selectedReceiver, Allocation existingAlloc, VBox dynamicFieldsContainer, Map<String, TextField> fieldsMap) {
        dynamicFieldsContainer.getChildren().clear();
        fieldsMap.clear();
        
        if (selectedReceiver != null && !selectedReceiver.equals("-")) {
            for (String roommate : roommates) {
                if (!roommate.equals(selectedReceiver)) {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);
                    
                    Label label = new Label("- " + roommate + ":");
                    label.setPrefWidth(100); 
                    
                    String defaultVal = "0";
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