package issues;

import util.DatabaseManager;
import entities.Issue;
import entities.Authentication; // ← ΠΡΟΣΘΗΚΗ
import ui.ErrorScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        reportedComboBox.setMaxWidth(Double.MAX_VALUE);
        reportedComboBox.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");

        payersField = new TextField();
        payersField.setEditable(false);
        payersField.setPromptText("Click + to select payers...");
        payersField.setStyle("-fx-padding: 8; -fx-background-radius: 4 0 0 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4 0 0 4; -fx-background-color: #f8fafc;");
        HBox.setHgrow(payersField, Priority.ALWAYS);

        payersMenuButton = new MenuButton("+");
        payersMenuButton.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0 4 4 0; -fx-padding: 7 14; -fx-cursor: hand;");
        
        // ΔΥΝΑΜΙΚΟ: Φόρτωση συγκατοίκων αντί για hardcoded λίστα
        loadRoommatesFromDatabase();

        HBox payersContainer = new HBox(0, payersField, payersMenuButton);
        payersContainer.setAlignment(Pos.CENTER_LEFT);

        datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        addFormField(grid, "Issue Type:", typeField, 0);
        addFormField(grid, "Reported By:", reportedComboBox, 1);
        addFormField(grid, "Payers:", payersContainer, 2); 
        addFormField(grid, "Date Logged:", datePicker, 3);

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

    private void loadRoommatesFromDatabase() {
        int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;
        String currentUsername = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getUsername() : "";

        CheckMenuItem itemAll = new CheckMenuItem("All Roommates");
        payersMenuButton.getItems().add(itemAll);
        
        List<CheckMenuItem> roommateCheckItems = new ArrayList<>();

        String sql = "SELECT username FROM users WHERE room_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentRoomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    
                    // Προσθήκη στο ComboBox
                    reportedComboBox.getItems().add(username);
                    
                    // Δημιουργία CheckMenuItem για τους Payers
                    CheckMenuItem itemRoommate = new CheckMenuItem(username);
                    roommateCheckItems.add(itemRoommate);
                    payersMenuButton.getItems().add(itemRoommate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Default τιμή στο dropdown ο τρέχων χρήστης
        if (!currentUsername.isEmpty() && reportedComboBox.getItems().contains(currentUsername)) {
            reportedComboBox.setValue(currentUsername);
        } else if (!reportedComboBox.getItems().isEmpty()) {
            reportedComboBox.setValue(reportedComboBox.getItems().get(0));
        }

        // Runnable για την ανανέωση του κειμένου των Payers
        Runnable updatePayersText = () -> {
            if (itemAll.isSelected()) {
                payersField.setText("All Roommates");
                for (CheckMenuItem item : roommateCheckItems) {
                    item.setSelected(false);
                }
                return;
            }
            List<String> selectedPayers = new ArrayList<>();
            for (CheckMenuItem item : roommateCheckItems) {
                if (item.isSelected()) selectedPayers.add(item.getText());
            }
            payersField.setText(String.join(", ", selectedPayers));
        };

        itemAll.setOnAction(e -> updatePayersText.run());
        for (CheckMenuItem item : roommateCheckItems) {
            item.setOnAction(e -> { 
                itemAll.setSelected(false); 
                updatePayersText.run(); 
            });
        }
    }

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

        if (typeField.getText().trim().isEmpty() || payersText.isEmpty()) {
            ErrorScreen.show("All fields are required before creating an entry. Make sure to select payers using the + button.");
            return;
        }

        Issue newIssue = new Issue(
            typeField.getText().trim(),
            reportedComboBox.getValue(),
            payersText, 
            datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString()
        );
        
        // ΔΥΝΑΜΙΚΟ: Εισαγωγή με το πραγματικό room_id του συνδεδεμένου χρήστη
        int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;
        String sql = "INSERT INTO issues (room_id, issue_type, reported_by, payers, issue_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, currentRoomId);
            pstmt.setString(2, typeField.getText().trim());
            pstmt.setString(3, reportedComboBox.getValue());
            pstmt.setString(4, payersField.getText().trim());
            pstmt.setString(5, datePicker.getValue().toString());
            
            pstmt.executeUpdate();
            
        } catch (Exception e) {
            ErrorScreen.show("Error saving to database: " + e.getMessage());
        }
        HomeIssueScreen.allIssues.add(newIssue);
        onCancel.run();
    }
}