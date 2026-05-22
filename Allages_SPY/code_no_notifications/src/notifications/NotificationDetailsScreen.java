package notifications;

import ui.ConfirmationScreen;
import entities.Notification;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.HOMYApp; // Απαραίτητο για να καλέσουμε τον δρομολογητή

public class NotificationDetailsScreen {
    private Stage ownerStage;
    private Notification notification;
    private ManageNotificationsClass manager;
    private NotificationsScreen parentScreen;

    public NotificationDetailsScreen(Stage ownerStage, Notification notification, ManageNotificationsClass manager, NotificationsScreen parentScreen) {
        this.ownerStage = ownerStage;
        this.notification = notification;
        this.manager = manager;
        this.parentScreen = parentScreen;
    }

    public void show() {
        Stage dlg = new Stage(); 
        dlg.initModality(Modality.APPLICATION_MODAL); 
        dlg.initOwner(ownerStage);
        dlg.setTitle("NOTIFICATION DETAIL");

        VBox body = new VBox(12); 
        body.setStyle("-fx-padding:20; -fx-background-color: white;");
        
        Label title = new Label(notification.category); 
        title.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        
        Label det = new Label(notification.detail); 
        det.setWrapText(true);
        
        Button btnGoTo = new Button("GO TO " + notification.target); 
        btnGoTo.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        btnGoTo.setMaxWidth(Double.MAX_VALUE);
        
        Button btnDel = new Button("DELETE"); 
        btnDel.setStyle("-fx-background-color:white; -fx-text-fill:#1A1A1A; -fx-border-color:#1A1A1A; -fx-border-width:1; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        btnDel.setMaxWidth(Double.MAX_VALUE);
        
        // --- ΕΔΩ ΕΓΙΝΕ Η ΔΙΟΡΘΩΣΗ ΓΙΑ ΤΟ GO TO ---
        btnGoTo.setOnAction(e -> {
            // 1. Κλείνει το μικρό popup των λεπτομερειών
            dlg.close();
            
            // 2. Κλείνει και την από πίσω λίστα ειδοποιήσεων
            if (ownerStage != null) {
                ownerStage.close();
            }
            
            // 3. Δρομολογεί τον χρήστη στη σωστή οθόνη μέσω του HOMYApp
            HOMYApp.routeToScreen(notification.target);
        });
        
        btnDel.setOnAction(e -> {
            // Μετατρέπουμε το parentScreen σε Runnable Lambda () -> parentScreen.refreshList()
            ConfirmationScreen confScreen = new ConfirmationScreen(dlg, ownerStage, notification, manager, () -> parentScreen.refreshList());
            confScreen.show();
        });
        
        body.getChildren().addAll(title, det, btnGoTo, btnDel);
        dlg.setScene(new Scene(body, 320, 240)); 
        dlg.showAndWait();
    }
}