package chores;

import java.util.function.Consumer;
import entities.Chore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReportScreen {

    private final Chore chore;
    private final Consumer<String> onReport;

    public ReportScreen(Chore chore, Consumer<String> onReport) {
        this.chore = chore;
        this.onReport = onReport;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Report Issue");
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white;");

        Label header = new Label("Report Issue");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Label subTitle = new Label("For chore: " + chore.getName());
        subTitle.setTextFill(Color.web("#4F46E5"));

        TextArea descField = new TextArea();
        descField.setPromptText("Describe what went wrong...");
        descField.setWrapText(true); // Αυτό αρκεί για το σωστό text wrapping!
        descField.setStyle("-fx-control-inner-background: #F9FAFB; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-background-radius: 6;");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());

        Button reportBtn = new Button("Submit");
        reportBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;");
        reportBtn.setOnAction(e -> {
            if (!descField.getText().trim().isEmpty()) {
                onReport.accept(descField.getText().trim());
                stage.close();
            }
        });

        btnRow.getChildren().addAll(cancelBtn, reportBtn);
        root.getChildren().addAll(header, subTitle, descField, btnRow);

        stage.setScene(new Scene(root, 380, 320));
        stage.showAndWait();
    }
}