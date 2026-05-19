import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmationScreen {
    private Stage detailStage;
    private Stage ownerStage;
    private Notification notification;
    private ManageNotificationsClass manager;
    private NotificationsScreen parentScreen;

    public ConfirmationScreen(Stage detailStage, Stage ownerStage, Notification notification, ManageNotificationsClass manager, NotificationsScreen parentScreen) {
        this.detailStage = detailStage;
        this.ownerStage = ownerStage;
        this.notification = notification;
        this.manager = manager;
        this.parentScreen = parentScreen;
    }

    public void show() {
        Stage dlg = new Stage(); 
        dlg.initModality(Modality.APPLICATION_MODAL); 
        dlg.initOwner(ownerStage);
        dlg.setTitle("ConfirmationScreen");
        
        VBox body = new VBox(15); 
        body.setAlignment(Pos.CENTER); 
        body.setStyle("-fx-padding:20; -fx-background-color: white;");
        
        Label msg = new Label("Επιβεβαίωση Διαγραφής;"); 
        msg.setStyle("-fx-font-weight:bold;");
        
        Button btnConfirm = new Button("CONFIRM");
        btnConfirm.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        
        Button btnCancel = new Button("CANCEL");
        btnCancel.setStyle("-fx-background-color:white; -fx-text-fill:#1A1A1A; -fx-border-color:#1A1A1A; -fx-border-width:1; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        
        HBox row = new HBox(10, btnConfirm, btnCancel); 
        row.setAlignment(Pos.CENTER);
        
        // [Confirm Delete] -> deleteNotification() -> return()
        btnConfirm.setOnAction(e -> {
            manager.deleteNotification(notification);
            dlg.close(); 
            detailStage.close(); 
            parentScreen.refreshList(); // Ανανέωση της λίστας στην οθόνη
        });
        
        // [Decline Delete] -> cancel delete
        btnCancel.setOnAction(e -> dlg.close());
        
        body.getChildren().addAll(msg, row);
        dlg.setScene(new Scene(body, 250, 130)); 
        dlg.showAndWait();
    }
}