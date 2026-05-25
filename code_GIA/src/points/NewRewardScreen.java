package points;

import ui.ErrorScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NewRewardScreen {

    public interface RewardAddCallback {
        void onAdd(String name, int cost);
    }

    private final RewardAddCallback callback;

    public NewRewardScreen(RewardAddCallback callback) {
        this.callback = callback;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("New Reward");
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");

        VBox headerBox = new VBox(5);
        Label header = new Label("Add New Reward");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.setTextFill(Color.web("#111827"));
        Label subTitle = new Label("Propose a new pass for the apartment.");
        subTitle.setFont(Font.font("Segoe UI", 13));
        subTitle.setTextFill(Color.web("#6B7280"));
        headerBox.getChildren().addAll(header, subTitle);

        VBox form = new VBox(15);
        TextField nameField = createStyledTextField("e.g., No Chores Weekend");
        TextField costField = createStyledTextField("e.g., 300");

        form.getChildren().addAll(
            createFieldGroup("REWARD NAME", nameField),
            createFieldGroup("COST IN POINTS", costField)
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());

        Button saveBtn = new Button("Propose");
        saveBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;");
        
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String costText = costField.getText().trim();

            if (name.isEmpty()) { 
                errorLabel.setText("Reward name cannot be empty."); 
                return; 
            }
            
            int cost;
            try { 
                cost = Integer.parseInt(costText); 
                if (cost <= 0) throw new NumberFormatException(); 
            } catch (NumberFormatException ex) { 
                errorLabel.setText("Cost must be a positive number."); 
                return; 
            }

            callback.onAdd(name, cost);
            stage.close();
        });

        btnRow.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(headerBox, form, errorLabel, btnRow);

        stage.setScene(new Scene(root, 380, 350));
        stage.showAndWait();
    }

    private VBox createFieldGroup(String labelText, Control inputControl) {
        VBox box = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#4B5563"));
        box.getChildren().addAll(lbl, inputControl);
        return box;
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8;");
        return tf;
    }
}