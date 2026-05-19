import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmationScreen {
    private Stage ownerStage;
    private Application application;
    private java.util.List<Application> myApplications;
    private Runnable onSuccess;

    public ConfirmationScreen(Stage ownerStage, Application application, java.util.List<Application> myApplications, Runnable onSuccess) {
        this.ownerStage = ownerStage;
        this.application = application;
        this.myApplications = myApplications;
        this.onSuccess = onSuccess;
    }

    public void show() {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(ownerStage);
        dlg.setTitle("Cancel Application");
        dlg.setResizable(false);

        VBox body = new VBox(16);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding:24; -fx-background-color: white;");

        Label msg = new Label("Ακύρωση αίτησης για:\n" + application.title + ";");
        msg.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-alignment:center;");
        msg.setWrapText(true);

        Button btnConfirm = new Button("CONFIRM");
        btnConfirm.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        
        Button btnCancel = new Button("CANCEL");
        btnCancel.setStyle("-fx-background-color:white; -fx-text-fill:#1A1A1A; -fx-border-color:#1A1A1A; -fx-border-width:1; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");

        HBox btnRow = new HBox(10, btnConfirm, btnCancel);
        btnRow.setAlignment(Pos.CENTER);

        // Επιβεβαίωση ακύρωσης αίτησης
        btnConfirm.setOnAction(e -> {
            myApplications.remove(application);
            dlg.close();
            if (onSuccess != null) {
                onSuccess.run(); // Ανανέωση του UI της λίστας
            }
        });

        // Ακύρωση ενέργειας
        btnCancel.setOnAction(e -> dlg.close());

        body.getChildren().addAll(msg, btnRow);
        dlg.setScene(new Scene(body, 300, 160));
        dlg.showAndWait();
    }
}