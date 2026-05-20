package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorScreen {

    private final String title;
    private final String message;
    private Stage ownerStage;       // Για τη σύνδεση με το γονικό παράθυρο
    private Runnable onCloseAction; // ΝΕΟ: Ενέργεια που εκτελείται μετά το κλείσιμο (από το usecase9)

    // --- Constructor 1: Ο αρχικός για απλή κλήση μέσω αντικειμένου (π.χ. NewApplicationScreen) ---
    public ErrorScreen(String title, String message) {
        this.title = title;
        this.message = message;
    }

    // --- Constructor 2: Για αντικειμενοστραφή κλήση ΜΑΖΙ με Owner Stage ---
    public ErrorScreen(Stage ownerStage, String title, String message) {
        this.ownerStage = ownerStage;
        this.title = title;
        this.message = message;
    }

    // --- Constructor 3: Ο ΝΕΟΣ Constructor που ενώνει τη λειτουργικότητα του usecase9 ---
    // (Χρησιμοποιεί Runnable αντί για EventScreen για πλήρη αποσύνδεση/decoupling των πακέτων)
    public ErrorScreen(String errorMessage, Runnable onCloseAction) {
        this.title = "Error";
        this.message = errorMessage;
        this.onCloseAction = onCloseAction;
    }

    // --- Μέθοδος show() για το αντικείμενο (Χρησιμοποιεί το αναβαθμισμένο CSS στυλ σου) ---
    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        
        // Αν έχει οριστεί γονικό παράθυρο, το κλειδώνουμε φιλικά
        if (ownerStage != null) {
            stage.initOwner(ownerStage);
        }
        
        stage.setTitle(title);
        stage.setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white; -fx-border-color: #EF4444; -fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12;");
        root.setAlignment(Pos.CENTER);

        Label iconLabel = new Label("⚠️");
        iconLabel.setFont(Font.font("Segoe UI", 24));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #EF4444;"); 
        
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 13));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setStyle("-fx-text-fill: #4B5563;");

        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-background-color: #64748B; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 30; -fx-cursor: hand;");
        
        // Όταν πατηθεί το OK, κλείνει το παράθυρο και εκτελείται η ενέργεια επιστροφής (αν υπάρχει)
        okBtn.setOnAction(e -> {
            stage.close();
            if (onCloseAction != null) {
                onCloseAction.run(); // Εκτελεί τη goBack() του usecase9
            }
        });

        root.getChildren().addAll(iconLabel, titleLabel, msgLabel, okBtn);

        stage.setScene(new Scene(root, 360, 220));
        stage.showAndWait();
    }

    // --- Static Μέθοδος 1: Από το 2ο Project για άμεση κλήση με Owner, Τίτλο και Μήνυμα ---
    public static void show(Stage ownerStage, String title, String message) {
        ErrorScreen errorScreen = new ErrorScreen(ownerStage, title, message);
        errorScreen.show(); 
    }

    // --- Static Μέθοδος 2: Από το usecase7 για γρήγορη κλήση ΜΟΝΟ με το μήνυμα σφάλματος ---
    public static void show(String message) {
        ErrorScreen errorScreen = new ErrorScreen("Σφάλμα εισαγωγής", message);
        errorScreen.show(); 
    }
}