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
    private Stage ownerStage; // Για τη σύνδεση με το γονικό παράθυρο (από το 2ο project)

    // --- Constructor 1: Ο αρχικός για απλή κλήση μέσω αντικειμένου (π.χ. NewApplicationScreen) ---
    public ErrorScreen(String title, String message) {
        this.title = title;
        this.message = message;
    }

    // --- Constructor 2: Προσθήκη για αντικειμενοστραφή κλήση ΜΑΖΙ με Owner Stage ---
    public ErrorScreen(Stage ownerStage, String title, String message) {
        this.ownerStage = ownerStage;
        this.title = title;
        this.message = message;
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
        okBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(iconLabel, titleLabel, msgLabel, okBtn);

        stage.setScene(new Scene(root, 360, 220));
        stage.showAndWait();
    }

    // --- Static Μέθοδος: Από το 2ο Project για άμεση κλήση χωρίς "new" ---
    public static void show(Stage ownerStage, String title, String message) {
        // Δημιουργούμε εσωτερικά το αντικείμενο χρησιμοποιώντας τον Constructor 2
        ErrorScreen errorScreen = new ErrorScreen(ownerStage, title, message);
        errorScreen.show(); // Ανοίγουμε το παράθυρο με το σωστό UI
    }
}