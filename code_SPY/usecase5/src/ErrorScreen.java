import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorScreen {
    
    public static void show(Stage ownerStage, String title, String message) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(ownerStage);
        dlg.setTitle(title);
        dlg.setResizable(false);

        VBox body = new VBox(15);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding:25; -fx-background-color: white;");

        Label lblMessage = new Label(message);
        lblMessage.setStyle("-fx-font-weight:bold; -fx-text-alignment:center; -fx-text-fill:#1A1A1A;");
        lblMessage.setWrapText(true);

        Button btnOk = new Button("OK");
        btnOk.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 20; -fx-cursor:hand;");
        btnOk.setOnAction(e -> dlg.close());

        body.getChildren().addAll(lblMessage, btnOk);
        
        Scene scene = new Scene(body, 300, 160);
        dlg.setScene(scene);
        dlg.showAndWait();
    }
}