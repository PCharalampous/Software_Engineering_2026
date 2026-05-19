package com.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class MainApp extends Application {

    private TableView<Issue> pendingIssuesTable;
    private TableView<Issue> issuesHistoryTable;
    private Label activeCountLabel;

    private final ObservableList<Issue> pendingIssuesData = FXCollections.observableArrayList();
    private final ObservableList<Issue> issuesHistoryData = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        // Root layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9; -fx-font-family: 'Segoe UI', system-ui, sans-serif;");

        // Top Main Header
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1e293b; -fx-padding: 18 25;");
        header.setAlignment(Pos.CENTER);
        Label titleLabel = new Label("Home Issue Report");
        titleLabel.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 20px; -fx-font-weight: bold;");
        header.getChildren().add(titleLabel);
        root.setTop(header);

        // Workspace split layout
        HBox workspace = new HBox(20);
        workspace.setAlignment(Pos.TOP_CENTER); 
        workspace.setPadding(new Insets(30));
        
        // Form layout constraint container
        HBox contentLimiter = new HBox(20);
        contentLimiter.setMaxWidth(1150); 
        HBox.setHgrow(contentLimiter, Priority.ALWAYS);

        // --- LEFT COLUMN: TABLES ---
        VBox leftColumn = new VBox(25);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);

        // Pending Card
        VBox pendingBox = createCardContainer();
        HBox pendingHeader = createSectionHeader("#f97316", "Pending Issues");
        pendingIssuesTable = createIssueTable("Type", "Reported By", "Payers", "Date Logged");
        pendingBox.getChildren().addAll(pendingHeader, pendingIssuesTable);

        // History Card
        VBox historyBox = createCardContainer();
        HBox historyHeader = createSectionHeader("#10b981", "Issues History Log");
        issuesHistoryTable = createIssueTable("Type", "Reported By", "Payers", "Repaired Date");
        historyBox.getChildren().addAll(historyHeader, issuesHistoryTable);

        leftColumn.getChildren().addAll(pendingBox, historyBox);

        // --- RIGHT COLUMN: SIDEBAR METRICS ---
        VBox rightColumn = new VBox(15);
        rightColumn.setPadding(new Insets(20));
        rightColumn.setPrefWidth(280);
        rightColumn.setMinWidth(280);
        rightColumn.setMaxWidth(280);
        rightColumn.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1;");

        HBox metricsHeader = createSectionHeader("#6366f1", "Maintenance Metrics");
        Separator separator = new Separator();
        separator.setStyle("-fx-opacity: 0.6;");

        VBox activeCard = createMetricCard("Active Maintenance Issues");
        activeCountLabel = new Label("0");
        activeCountLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        activeCard.getChildren().add(activeCountLabel);

        rightColumn.getChildren().addAll(metricsHeader, separator, activeCard);

        // Assemble layouts
        contentLimiter.getChildren().addAll(leftColumn, rightColumn);
        workspace.getChildren().add(contentLimiter);
        
        ScrollPane scrollPane = new ScrollPane(workspace);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scrollPane);

        // Bottom Tray
        HBox bottomTray = new HBox(15);
        bottomTray.setAlignment(Pos.CENTER_RIGHT);
        bottomTray.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15 30; -fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1;");

        Button btnSchedule = createButton("Schedule Technician", "#4b5563");
        Button btnPay = createButton("Pay Selected Issue", "#3b82f6");
        Button btnCreate = createButton("+ Create New Issue", "#10b981");

        // Click Actions
        btnSchedule.setOnAction(e -> showScheduleTechnicianDialog());
        btnPay.setOnAction(e -> handleResolutionAction(stage)); 
        btnCreate.setOnAction(e -> showNewIssueDialog());

        bottomTray.getChildren().addAll(btnSchedule, btnPay, btnCreate);
        root.setBottom(bottomTray);

        pendingIssuesTable.setItems(pendingIssuesData);
        issuesHistoryTable.setItems(issuesHistoryData);
        loadSampleData();
        recalculateMetrics();

        Scene scene = new Scene(root, 1180, 720);

        // Inject dynamic styling to force Table Column Headers to solid bold black text
        try {
            File cssFile = File.createTempFile("app-style", ".css");
            cssFile.deleteOnExit();
            try (FileWriter writer = new FileWriter(cssFile)) {
                writer.write(".table-view .column-header .label { -fx-text-fill: #000000 !important; -fx-font-weight: bold !important; }\n");
                writer.write(".table-view .column-header-background .label { -fx-text-fill: #000000 !important; }\n");
            }
            scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
        } catch (IOException ex) {
            System.out.println("Could not load table header customization style sheet: " + ex.getMessage());
        }

        stage.setTitle("Household Maintenance & Issue Tracker");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Handles payment resolution logic with a validation confirmation pop-up window
     */
    private void handleResolutionAction(Stage ownerStage) {
        if (issuesHistoryTable.getSelectionModel().getSelectedItem() != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(ownerStage);
            alert.setTitle("Action Error");
            alert.setHeaderText("Cannot Process Request");
            alert.setContentText("This issue has already been logged as paid and resolved!");
            alert.showAndWait();
            
            issuesHistoryTable.getSelectionModel().clearSelection();
            return;
        }

        Issue selected = pendingIssuesTable.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            // CONFIRMATION POP-UP SYSTEM INJECTED HERE
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.initOwner(ownerStage);
            confirmationAlert.setTitle("Confirm Payment Action");
            confirmationAlert.setHeaderText("Verify Payment Resolution");
            confirmationAlert.setContentText("Are you sure you want to resolve and pay for this issue?");

            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // If user clicks OK, proceed to transition the row data
                pendingIssuesData.remove(selected);
                issuesHistoryData.add(new Issue(selected.getType(), selected.getReportedBy(), selected.getPayers(), LocalDate.now().toString()));
                recalculateMetrics();
                pendingIssuesTable.getSelectionModel().clearSelection(); 
            } else {
                // If user cancels, clear selection safely and break away cleanly
                pendingIssuesTable.getSelectionModel().clearSelection();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(ownerStage);
            alert.setTitle("Selection Required");
            alert.setHeaderText("No Active Issue Selected");
            alert.setContentText("Please select an unpaid issue from the 'Pending Issues' table above to finalize payment.");
            alert.showAndWait();
        }
    }

    /**
     * Displays the balanced Schedule Technician screen
     */
    private void showScheduleTechnicianDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Schedule Technician Appointment");

        BorderPane dialogRoot = new BorderPane();
        dialogRoot.setStyle("-fx-background-color: #f1f5f9; -fx-font-family: 'Segoe UI', system-ui, sans-serif;");

        HBox formHeader = new HBox();
        formHeader.setStyle("-fx-background-color: #1e293b; -fx-padding: 15 25;");
        formHeader.setAlignment(Pos.CENTER);
        Label formTitle = new Label("Schedule Technician");
        formTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 18px; -fx-font-weight: bold;");
        formHeader.getChildren().add(formTitle);
        dialogRoot.setTop(formHeader);

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

        // Row 1 - Issue Type
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);
        Label lblType = new Label("Issue Type:");
        lblType.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px; -fx-alignment: center-left;");
        TextField txtType = new TextField();
        txtType.setPromptText("Enter maintenance category...");
        txtType.setStyle("-fx-padding: 8; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        HBox.setHgrow(txtType, Priority.ALWAYS);
        row1.getChildren().addAll(lblType, txtType);

        // Row 2 - Date
        HBox row2 = new HBox(12);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label lblDate = new Label("Date:");
        lblDate.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px; -fx-alignment: center-left;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-padding: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(datePicker, Priority.ALWAYS);
        row2.getChildren().addAll(lblDate, datePicker);

        // Row 3 - Time Window
        HBox row3 = new HBox(12);
        row3.setAlignment(Pos.CENTER_LEFT);
        Label lblTime = new Label("Time Window:");
        lblTime.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px; -fx-alignment: center-left;");
        ComboBox<String> timePicker = new ComboBox<>();
        timePicker.setStyle("-fx-padding: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-background-color: white;");
        timePicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(timePicker, Priority.ALWAYS);
        
        ObservableList<String> timesList = FXCollections.observableArrayList();
        for (int h = 0; h < 24; h++) {
            for (int m = 0; m < 60; m += 10) {
                timesList.add(String.format("%02d:%02d", h, m));
            }
        }
        timePicker.setItems(timesList);
        timePicker.setValue("09:00");
        row3.getChildren().addAll(lblTime, timePicker);

        fieldsContainer.getChildren().addAll(row1, row2, row3);
        formContentBlock.getChildren().add(fieldsContainer);
        contentWrapper.getChildren().add(formContentBlock);
        dialogRoot.setCenter(contentWrapper);

        HBox actionTray = new HBox(12);
        actionTray.setAlignment(Pos.CENTER_RIGHT);
        actionTray.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15 30; -fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 4; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnAddSchedule = new Button("Add");
        btnAddSchedule.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 22; -fx-background-radius: 4; -fx-cursor: hand;");
        btnAddSchedule.setOnAction(e -> {
            if (txtType.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "The Issue Type field is required.", ButtonType.OK);
                alert.initOwner(dialog);
                alert.showAndWait();
                return;
            }
            System.out.println("[Scheduled] " + txtType.getText().trim() + " set for " + datePicker.getValue() + " at " + timePicker.getValue());
            dialog.close();
        });

        actionTray.getChildren().addAll(btnCancel, btnAddSchedule);
        dialogRoot.setBottom(actionTray);

        Scene dialogScene = new Scene(dialogRoot, 560, 410);
        dialog.setScene(dialogScene);
        dialog.setResizable(true); 
        dialog.show();
    }

    /**
     * Displays the balanced New Issue creation window
     */
    private void showNewIssueDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Create a New Issue");

        BorderPane dialogRoot = new BorderPane();
        dialogRoot.setStyle("-fx-background-color: #f1f5f9; -fx-font-family: 'Segoe UI', system-ui, sans-serif;");

        HBox formHeader = new HBox();
        formHeader.setStyle("-fx-background-color: #1e293b; -fx-padding: 15 25;");
        formHeader.setAlignment(Pos.CENTER);
        Label formTitle = new Label("New Issue");
        formTitle.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 18px; -fx-font-weight: bold;");
        formHeader.getChildren().add(formTitle);
        dialogRoot.setTop(formHeader);

        VBox contentWrapper = new VBox(15);
        contentWrapper.setPadding(new Insets(25, 30, 25, 30));
        contentWrapper.setAlignment(Pos.CENTER); 

        VBox formContentBlock = new VBox(20);
        formContentBlock.setPrefWidth(450);
        formContentBlock.setMaxWidth(450); 
        formContentBlock.setPadding(new Insets(25));
        formContentBlock.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1;");
        formContentBlock.setAlignment(Pos.CENTER); 
        
        Label subHeading = new Label("Fill in the issue details:");
        subHeading.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #334155;");
        subHeading.setAlignment(Pos.CENTER);
        formContentBlock.getChildren().add(subHeading);

        VBox fieldsContainer = new VBox(15);
        fieldsContainer.setAlignment(Pos.CENTER);

        // Row 1 - Issue Type
        HBox row1 = new HBox(12);
        row1.setAlignment(Pos.CENTER_LEFT);
        Label lblType = new Label("Issue Type:");
        lblType.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px; -fx-alignment: center-left;");
        TextField txtType = new TextField();
        txtType.setPromptText("Enter maintenance category...");
        txtType.setStyle("-fx-padding: 8; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        HBox.setHgrow(txtType, Priority.ALWAYS);
        row1.getChildren().addAll(lblType, txtType);

        // Row 2 - Reported By
        HBox row2 = new HBox(12);
        row2.setAlignment(Pos.CENTER_LEFT);
        Label lblReportedBy = new Label("Reported By:");
        lblReportedBy.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px; -fx-alignment: center-left;");
        TextField txtReportedBy = new TextField();
        txtReportedBy.setPromptText("Enter name...");
        txtReportedBy.setStyle("-fx-padding: 8; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        HBox.setHgrow(txtReportedBy, Priority.ALWAYS);
        row2.getChildren().addAll(lblReportedBy, txtReportedBy);

        // Row 3 - Payers
        HBox row3 = new HBox(12);
        row3.setAlignment(Pos.CENTER_LEFT);
        Label lblPayers = new Label("Payers:");
        lblPayers.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px; -fx-alignment: center-left;");
        TextField txtPayers = new TextField();
        txtPayers.setPromptText("Who covers costs...");
        txtPayers.setStyle("-fx-padding: 8; -fx-background-radius: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        HBox.setHgrow(txtPayers, Priority.ALWAYS);
        row3.getChildren().addAll(lblPayers, txtPayers);

        // Row 4 - Date Logged
        HBox row4 = new HBox(12);
        row4.setAlignment(Pos.CENTER_LEFT);
        Label lblDate = new Label("Date Logged:");
        lblDate.setStyle("-fx-font-weight: 600; -fx-text-fill: #475569; -fx-pref-width: 110px; -fx-alignment: center-left;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setStyle("-fx-padding: 4; -fx-border-color: #cbd5e1; -fx-border-radius: 4;");
        datePicker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(datePicker, Priority.ALWAYS);
        row4.getChildren().addAll(lblDate, datePicker);

        fieldsContainer.getChildren().addAll(row1, row2, row3, row4);
        formContentBlock.getChildren().add(fieldsContainer);
        contentWrapper.getChildren().add(formContentBlock);
        dialogRoot.setCenter(contentWrapper);

        HBox actionTray = new HBox(12);
        actionTray.setAlignment(Pos.CENTER_RIGHT);
        actionTray.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15 30; -fx-border-color: #e2e8f0 transparent transparent transparent; -fx-border-width: 1;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 18; -fx-background-radius: 4; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> dialog.close());

        Button btnCreateIssue = new Button("Create Issue");
        btnCreateIssue.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 22; -fx-background-radius: 4; -fx-cursor: hand;");
        btnCreateIssue.setOnAction(e -> {
            if (txtType.getText().trim().isEmpty() || txtReportedBy.getText().trim().isEmpty() || txtPayers.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "All fields are required before creating an entry.", ButtonType.OK);
                alert.initOwner(dialog);
                alert.showAndWait();
                return;
            }

            String dateString = (datePicker.getValue() != null) ? datePicker.getValue().toString() : LocalDate.now().toString();
            pendingIssuesData.add(new Issue(txtType.getText().trim(), txtReportedBy.getText().trim(), txtPayers.getText().trim(), dateString));
            recalculateMetrics();
            dialog.close();
        });

        actionTray.getChildren().addAll(btnCancel, btnCreateIssue);
        dialogRoot.setBottom(actionTray);

        Scene dialogScene = new Scene(dialogRoot, 560, 440);
        dialog.setScene(dialogScene);
        dialog.setResizable(true); 
        dialog.show();
    }

    private void loadSampleData() {
        pendingIssuesData.add(new Issue("Plumbing - Kitchen Leak", "Alex", "All Roommates", "2026-05-14"));
        pendingIssuesData.add(new Issue("Electrical - HVAC Failure", "John", "John", "2026-05-16"));
        pendingIssuesData.add(new Issue("Structural - Door Lock", "Sarah", "All Roommates", "2026-05-18"));
        issuesHistoryData.add(new Issue("Appliance - Refrigerator Fix", "Emma", "John, Alex", "2026-04-29"));
    }

    private void recalculateMetrics() {
        activeCountLabel.setText(String.valueOf(pendingIssuesData.size()));
    }

    private VBox createCardContainer() {
        VBox box = new VBox(12);
        box.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10; -fx-border-width: 1;");
        return box;
    }

    private HBox createSectionHeader(String dotColorHex, String titleText) {
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Region statusDot = new Region();
        statusDot.setStyle("-fx-background-color: " + dotColorHex + "; -fx-pref-width: 8; -fx-pref-height: 8; -fx-background-radius: 4;");
        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #334155;");
        headerBox.getChildren().addAll(statusDot, title);
        return headerBox;
    }

    private TableView<Issue> createIssueTable(String col1Name, String col2Name, String col3Name, String col4Name) {
        TableView<Issue> table = new TableView<>();
        table.setPrefHeight(210);
        table.setMinHeight(160);
        table.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Issue, String> typeCol = new TableColumn<>(col1Name);
        typeCol.setCellValueFactory(cell -> cell.getValue().typeProperty());
        typeCol.setStyle("-fx-alignment: CENTER;"); 
        typeCol.setPrefWidth(220);

        TableColumn<Issue, String> reportedCol = new TableColumn<>(col2Name);
        reportedCol.setCellValueFactory(cell -> cell.getValue().reportedByProperty());
        reportedCol.setStyle("-fx-alignment: CENTER;"); 
        reportedCol.setPrefWidth(180);

        TableColumn<Issue, String> payersCol = new TableColumn<>(col3Name);
        payersCol.setCellValueFactory(cell -> cell.getValue().payersProperty());
        payersCol.setStyle("-fx-alignment: CENTER;"); 
        payersCol.setPrefWidth(180);

        TableColumn<Issue, String> dateCol = new TableColumn<>(col4Name);
        dateCol.setCellValueFactory(cell -> cell.getValue().dateProperty());
        dateCol.setStyle("-fx-alignment: CENTER;"); 
        dateCol.setPrefWidth(140);

        table.getColumns().addAll(typeCol, reportedCol, payersCol, dateCol);
        return table;
    }

    private VBox createMetricCard(String cardCaption) {
        VBox card = new VBox(6);
        card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6;");
        Label caption = new Label(cardCaption);
        caption.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        card.getChildren().add(caption);
        return card;
    }

    private Button createButton(String label, String bgColorHex) {
        Button btn = new Button(label);
        btn.setStyle("-fx-background-color: " + bgColorHex + "; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 22; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}