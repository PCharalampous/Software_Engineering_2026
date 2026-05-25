package ui;

import entities.Application;
import entities.Notification;
import notifications.ManageNotificationsClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmationScreen {

    private final String title;
    private final String message;
    private final Runnable onConfirm;
    private final String btnText;
    private final String btnStyle;
    
    private Stage ownerStage;   // Για το κλείδωμα του γονικού παραθύρου
    private Stage detailStage;  // Ειδικό πεδίο για το αυτόματο κλείσιμο των λεπτομερειών ειδοποίησης

    // --- Constructor 1: Ο αρχικός σου για Chores (Default Red/Delete στυλ) ---
    public ConfirmationScreen(String title, String message, Runnable onConfirm) {
        this.title = title;
        this.message = message;
        this.onConfirm = onConfirm;
        this.btnText = "Yes, Delete";
        this.btnStyle = "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;";
    }

    // --- Constructor 2: Ο πλήρως παραμετροποιήσιμος για το NewApplicationScreen ---
    public ConfirmationScreen(String title, String message, String btnText, String btnStyle, Runnable onConfirm) {
        this.title = title;
        this.message = message;
        this.btnText = btnText;
        this.btnStyle = btnStyle;
        this.onConfirm = onConfirm;
    }

    // --- Constructor 3: Για την ακύρωση αιτήσεων (Applications) ---
    public ConfirmationScreen(Stage ownerStage, Application application, java.util.List<Application> myApplications, Runnable onSuccess) {
        this.ownerStage = ownerStage;
        this.title = "Cancel Application";
        this.message = "Ακύρωση αίτησης για:\n" + application.getTitle() + ";";
        this.btnText = "CONFIRM";
        this.btnStyle = "-fx-background-color: #1A1A1A; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 16; -fx-cursor: hand;";
        
        this.onConfirm = () -> {
            myApplications.remove(application);
            if (onSuccess != null) {
                onSuccess.run();
            }
        };
    }

    // --- Constructor 4: Για τη διαγραφή ειδοποιήσεων (Notifications) ---
    public ConfirmationScreen(Stage detailStage, Stage ownerStage, Notification notification, Object manager, Runnable refreshList) {
        this.detailStage = detailStage;
        this.ownerStage = ownerStage;
        this.title = "ConfirmationScreen";
        this.message = "Επιβεβαίωση Διαγραφής;";
        this.btnText = "CONFIRM";
        
        this.btnStyle = "-fx-background-color: #1A1A1A; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 16; -fx-cursor: hand;";
        
        this.onConfirm = () -> {
            // ΔΙΟΡΘΩΣΗ: Έλεγχος και cast στο σωστό πακέτο (notifications αντί για profile)
            if (manager instanceof ManageNotificationsClass) {
                ((ManageNotificationsClass) manager).deleteNotification(notification);
            } else {
                System.out.println("Notification Deleted: " + notification.text);
            }
            
            // Κλείσιμο του παραθύρου λεπτομερειών αν υπάρχει
            if (this.detailStage != null) {
                this.detailStage.close();
            }
            
            // Ανανέωση της λίστας στο UI
            if (refreshList != null) {
                refreshList.run();
            }
        };
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        
        if (ownerStage != null) {
            stage.initOwner(ownerStage);
        }
        
        stage.setTitle(title);
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white;");
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));

        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);
        
        if (message.contains("Επιβεβαίωση")) {
            msgLabel.setStyle("-fx-font-weight: bold;");
        }

        HBox btnRow = new HBox(15);
        btnRow.setAlignment(Pos.CENTER);

        Button noBtn = new Button("Cancel");
        noBtn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;");
        noBtn.setOnAction(e -> stage.close());

        Button yesBtn = new Button(btnText);
        yesBtn.setStyle(btnStyle);
        yesBtn.setOnAction(e -> {
            stage.close();
            onConfirm.run();
        });

        btnRow.getChildren().addAll(noBtn, yesBtn);
        root.getChildren().addAll(titleLabel, msgLabel, btnRow);

        stage.setScene(new Scene(root, 390, 230));
        stage.showAndWait();
    }
}