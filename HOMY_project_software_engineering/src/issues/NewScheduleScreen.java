package issues;

import ui.ErrorScreen;
import util.DatabaseManager;
import entities.Authentication;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class NewScheduleScreen extends VBox {

    private TextField typeField;
    private DatePicker datePicker;
    private ComboBox<String> timePicker;
    private final Runnable onCancel;

    // 🌟 OPTIMIZATION: Generate the 144 items once statically so the UI thread doesn't choke on load
    private static final ObservableList<String> TIMES_LIST = FXCollections.observableArrayList();
    static {
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 10) {
                TIMES_LIST.add(String.format("%02d:%02d", h, m));
            }
        }
    }

    public NewScheduleScreen(Runnable onCancel) {
        this.onCancel = onCancel;
        this.setSpacing(0);
        this.setStyle("-fx-background-color: #f1f5f9;");
        buildUI();
    }

    private void buildUI() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1e293b; -fx-padding: 15 25;");
        header.setAlignment(Pos.CENTER);
        Label formTitle = new Label("Schedule Technician");
        formTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 18px; -fx-font-weight: bold;");
        header.getChildren().add(formTitle);
        this.getChildren().add(header);

        VBox contentWrapper = new VBox(15);
        contentWrapper.setPadding(new Insets(25, 30, 25, 30));
        contentWrapper.setAlignment(Pos.CENTER); 

        VBox formContentBlock = new VBox(20);
        formContentBlock.setPrefWidth(450);
        formContentBlock.setMaxWidth(450); 
        formContentBlock.setPadding(new Insets(25));
        formContentBlock.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1;");
        formContentBlock.setAlignment(Pos.CENTER); 
        
        Label subHeading = new Label("Fill in scheduling window parameters:");
        subHeading.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #334155;");
        subHeading.setAlignment(Pos.CENTER);
        formContentBlock.getChildren().add(subHeading);

        VBox fieldsContainer = new VBox(15);
        fieldsContainer.setAlignment(Pos.CENTER);

        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);
        Label lblType = new Label("Issue Type:");
        lblType.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px;");
        typeField = new TextField();
        typeField.setPromptText("Enter maintenance category...");
        typeField.setStyle("-fx-padding: 8; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        HBox.setHgrow(typeField, Priority.ALWAYS);
        row1.getChildren().addAll(lblType, typeField);

        HBox row2 = new HBox(12);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label lblDate = new Label("Date:");
        lblDate.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px;");
        datePicker = new DatePicker(LocalDate.now());
        
        // 🌟 FIX: Removed custom padding strings which break native click event hitboxes on dropdown controls
        datePicker.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-color: white;");
        datePicker.setEditable(false); 
        datePicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(datePicker, Priority.ALWAYS);
        row2.getChildren().addAll(lblDate, datePicker);

        HBox row3 = new HBox(12);
        row3.setAlignment(Pos.CENTER_LEFT);
        Label lblTime = new Label("Time Window:");
        lblTime.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px;");
        timePicker = new ComboBox<>();
        
        // 🌟 FIX: Clean styling parameters ensures native arrow UI component registers clicks immediately
        timePicker.setStyle("-fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-color: white;");
        timePicker.setMaxWidth(Double.MAX_VALUE);
        timePicker.setVisibleRowCount(8); // Limits viewport rendering calculations to prevent drop lag
        HBox.setHgrow(timePicker, Priority.ALWAYS);
        
        // Use pre-loaded fast reference
        timePicker.setItems(TIMES_LIST);
        timePicker.setValue("09:00");
        row3.getChildren().addAll(lblTime, timePicker);

        fieldsContainer.getChildren().addAll(row1, row2, row3);
        formContentBlock.getChildren().add(fieldsContainer);
        contentWrapper.getChildren().add(formContentBlock);
        this.getChildren().add(contentWrapper);

        HBox actionTray = new HBox(12);
        actionTray.setAlignment(Pos.CENTER_RIGHT);
        actionTray.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15 30; -fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 4; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> onCancel.run());

        Button btnAddSchedule = new Button("Add");
        btnAddSchedule.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 22; -fx-background-radius: 4; -fx-cursor: hand;");
        btnAddSchedule.setOnAction(e -> {
            if (typeField.getText().trim().isEmpty()) {
                ErrorScreen.show("The Issue Type field is required.");
                return;
            }
            
            int currentRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 1;
            String eventName = "Technician: " + typeField.getText().trim();
            String date = datePicker.getValue().toString();
            
            String timeStr = timePicker.getValue().replace(":", "");
            int eventTime = Integer.parseInt(timeStr);

            String sql = "INSERT INTO calendar_events (room_id, event_name, event_date, event_time, event_type) VALUES (?, ?, ?, ?, 'ISSUE')";

            // 🌟 OPTIMIZATION: Database operations now happen cleanly alongside instant close callback response
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, currentRoomId);
                pstmt.setString(2, eventName);
                pstmt.setString(3, date);
                pstmt.setInt(4, eventTime);
                
                pstmt.executeUpdate();
                
                entities.Notification.createNotificationToRoom(
                        conn, 
                        "CALENDAR", 
                        "Νέο event στο ημερολόγιο", 
                        "Προστέθηκε ο τεχνικός: '" + typeField.getText().trim() + "' για τις " + date + ".", 
                        "CALENDAR_SCREEN", 
                        "#06B6D4"
                    );
                
                onCancel.run();
                
            } catch (Exception ex) {
                ErrorScreen.show("Error saving schedule: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        actionTray.getChildren().addAll(btnCancel, btnAddSchedule);
        this.getChildren().add(actionTray);
    }
}
