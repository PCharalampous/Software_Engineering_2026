package ui;
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

    public ConfirmationScreen(String title, String message, Runnable onConfirm) {
        this.title = title;
        this.message = message;
        this.onConfirm = onConfirm;
        this.btnText = "Yes, Delete";
        this.btnStyle = "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;";
    }

    public ConfirmationScreen(String title, String message, String btnText, String btnStyle, Runnable onConfirm) {
        this.title = title;
        this.message = message;
        this.btnText = btnText;
        this.btnStyle = btnStyle;
        this.onConfirm = onConfirm;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
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