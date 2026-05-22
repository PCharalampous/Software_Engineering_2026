package chores;

import ui.ErrorScreen; // Εισαγωγή του custom ErrorScreen σου
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

import java.util.List;

public class NewChoreScreen {

    public interface ChoreAddCallback {
        void onAdd(String name, int points, String duty);
    }

    private final List<String> members;
    private final ChoreAddCallback callback;

    public NewChoreScreen(List<String> members, ChoreAddCallback callback) {
        this.members = members;
        this.callback = callback;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("New Chore");
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: white;");

        VBox headerBox = new VBox(5);
        Label header = new Label("Add New Chore");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        header.setTextFill(Color.web("#111827"));
        Label subTitle = new Label("Fill in the details of the new chore.");
        subTitle.setFont(Font.font("Segoe UI", 13));
        subTitle.setTextFill(Color.web("#6B7280"));
        headerBox.getChildren().addAll(header, subTitle);

        VBox form = new VBox(15);
        
        TextField nameField = createStyledTextField("e.g., Living room sweeping");
        TextField pointsField = createStyledTextField("e.g., 500");
        
        ComboBox<String> dutyCombo = new ComboBox<>();
        // ΔΙΟΡΘΩΣΗ 1: Προσθήκη της επιλογής "All Roommates" στην κορυφή του Dropdown
        dutyCombo.getItems().add("All Roommates");
        dutyCombo.getItems().addAll(members);
        
        dutyCombo.setPromptText("Select roommate...");
        dutyCombo.setMaxWidth(Double.MAX_VALUE);
        dutyCombo.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 4;");

        form.getChildren().addAll(
            createFieldGroup("CHORE NAME", nameField),
            createFieldGroup("REWARD POINTS", pointsField),
            createFieldGroup("ASSIGN TO", dutyCombo)
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4B5563; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> stage.close());

        Button addBtn = new Button("Save");
        addBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String ptsText = pointsField.getText().trim();
            String duty = dutyCombo.getValue();

            if (name.isEmpty()) { errorLabel.setText("Chore name cannot be empty."); return; }
            int pts;
            try { pts = Integer.parseInt(ptsText); if (pts <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { errorLabel.setText("Points must be a positive number."); return; }

            // ΔΙΟΡΘΩΣΗ 2: Έλεγχος αν έχει επιλεγεί υποχρεωτικά Assignee
            if (duty == null || duty.trim().isEmpty()) {
                // Χρήση της έτοιμης static μεθόδου του ErrorScreen σου
                ErrorScreen.show(stage, "Missing Assignee", "You must select a responsible roommate or 'All Roommates' before saving.");
                return; // Διακόπτει την εκτέλεση χωρίς να κλείνει τη φόρμα
            }

            callback.onAdd(name, pts, duty);
            stage.close();
        });

        btnRow.getChildren().addAll(cancelBtn, addBtn);
        root.getChildren().addAll(headerBox, form, errorLabel, btnRow);

        stage.setScene(new Scene(root, 400, 450));
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