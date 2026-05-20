package issues;

import entities.Issue;
import ui.ErrorScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewIssueScreen extends VBox {

    private TextField titleField;
    private TextField descField;
    private TextField typeField;
    private ComboBox<String> reportedComboBox; 
    private TextField payersField;            
    private MenuButton payersMenuButton;      
    private DatePicker datePicker;

    private final Runnable onCancel;

    public NewIssueScreen(Runnable onCancel) {
        this.onCancel = onCancel;
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #f1f5f9;");
        buildUI();
    }

    private void buildUI() {
        HBox header = new HBox();
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #1e293b;");
        Label headerTitle = new Label("Create a New Issue");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        headerTitle.setTextFill(Color.WHITE);
        header.getChildren().add(headerTitle);
        this.getChildren().add(header);

        VBox contentWrapper = new VBox(15);
        contentWrapper.setPadding(new Insets(25, 30, 25, 30));
        contentWrapper.setAlignment(Pos.CENTER);

        VBox formContentBlock = new VBox(20);
        formContentBlock.setPrefWidth(450);
        formContentBlock.setMaxWidth(450); 
        formContentBlock.setPadding(new Insets(25));
        formContentBlock.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        
        Label subHeading = new Label("Fill in the issue details:");
        subHeading.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #334155;");
        formContentBlock.getChildren().add(subHeading);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        titleField = new TextField();
        titleField.setPromptText("Enter issue summary title...");
        descField = new TextField();
        descField.setPromptText("Enter detailed description logs...");
        typeField = new TextField();
        typeField.setPromptText("Enter maintenance category...");
        
        reportedComboBox = new ComboBox<>();
        reportedComboBox.getItems().addAll("Alex", "John", "Sarah", "Emma");
        reportedComboBox.setValue("Alex");
        reportedComboBox.setMaxWidth(Double.MAX_VALUE);
        reportedComboBox.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");

        // --- Multi-Select Payers Component ---
        payersField = new TextField();
        payersField.setEditable(false);
        payersField.setPromptText("Click + to select payers...");
        payersField.setStyle("-fx-padding: 8; -fx-background-radius: 4 0 0 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4 0 0 4; -fx-background-color: #f8fafc;");
        HBox.setHgrow(payersField, Priority.ALWAYS);

        payersMenuButton = new MenuButton("+");
        payersMenuButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0 4 4 0; -fx-padding: 7 14; -fx-cursor: hand;");
        
        CheckMenuItem itemAll = new CheckMenuItem("All Roommates");
        CheckMenuItem itemAlex = new CheckMenuItem("Alex");
        CheckMenuItem itemJohn = new CheckMenuItem("John");
        CheckMenuItem itemSarah = new CheckMenuItem("Sarah");
        CheckMenuItem itemEmma = new CheckMenuItem("Emma");

        payersMenuButton.getItems().addAll(itemAll, itemAlex, itemJohn, itemSarah, itemEmma);

        Runnable updatePayersText = () -> {
            if (itemAll.isSelected()) {
                payersField.setText("All Roommates");
                itemAlex.setSelected(false);
                itemJohn.setSelected(false);
                itemSarah.setSelected(false);
                itemEmma.setSelected(false);
                return;
            }
            List<String> selectedPayers = new ArrayList<>();
            if (itemAlex.isSelected()) selectedPayers.add("Alex");
            if (itemJohn.isSelected()) selectedPayers.add("John");
            if (itemSarah.isSelected()) selectedPayers.add("Sarah");
            if (itemEmma.isSelected()) selectedPayers.add("Emma");

            payersField.setText(String.join(", ", selectedPayers));
        };

        itemAll.setOnAction(e -> updatePayersText.run());
        itemAlex.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });
        itemJohn.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });
        itemSarah.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });
        itemEmma.setOnAction(e -> { itemAll.setSelected(false); updatePayersText.run(); });

        HBox payersContainer = new HBox(0, payersField, payersMenuButton);
        payersContainer.setAlignment(Pos.CENTER_LEFT);

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        // Καλούμε κανονικά τη μέθοδο, πλέον δέχεται και το HBox Container
        addFormField(grid, "Issue Title:", titleField, 0);
        addFormField(grid, "Description:", descField, 1);
        addFormField(grid, "Issue Type:", typeField, 2);
        addFormField(grid, "Reported By:", reportedComboBox, 3);
        addFormField(grid, "Payers:", payersContainer, 4); 
        addFormField(grid, "Date Logged:", datePicker, 5);

        formContentBlock.getChildren().add(grid);
        contentWrapper.getChildren().add(formContentBlock);
        this.getChildren().add(contentWrapper);

        HBox actionTray = new HBox(12);
        actionTray.setAlignment(Pos.CENTER_RIGHT);
        actionTray.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15 30; -fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1;");

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 4; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> onCancel.run());

        Button saveBtn = new Button("Create Issue");
        saveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 22; -fx-background-radius: 4; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> handleSave());

        actionTray.getChildren().addAll(cancelBtn, saveBtn);
        this.getChildren().add(actionTray);
    }

    // ΔΙΟΡΘΩΣΗ: Αλλάξαμε την παράμετρο από Control σε Node για να δέχεται layouts (HBox) και controls (TextField, ComboBox κλπ.)
    private void addFormField(GridPane grid, String labelText, Node input, int row) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px;");
        
        if (input instanceof TextField) {
            input.setStyle("-fx-padding: 8; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        } else if (input instanceof DatePicker) {
            input.setStyle("-fx-padding: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        }
        
        grid.add(lbl, 0, row);
        grid.add(input, 1, row);
        GridPane.setHgrow(input, Priority.ALWAYS);
    }

    private void handleSave() {
        String payersText = payersField.getText().trim();

        if (titleField.getText().trim().isEmpty() || typeField.getText().trim().isEmpty() || payersText.isEmpty()) {
            ErrorScreen.show("All fields are required before creating an entry. Make sure to select payers using the + button.");
            return;
        }

        Issue newIssue = new Issue(
            titleField.getText().trim(),
            descField.getText().trim().isEmpty() ? "No description logs" : descField.getText().trim(),
            typeField.getText().trim(),
            reportedComboBox.getValue(),
            payersText, 
            datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString()
        );

        HomeIssueScreen.allIssues.add(newIssue);
        onCancel.run();
    }
}