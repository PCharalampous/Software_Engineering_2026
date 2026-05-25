package chores;

import java.util.ArrayList;
import java.util.List;
import entities.Point;
import entities.Chore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import ui.ConfirmationScreen;

public class ChoreScreen extends VBox {
    
    private final ObservableList<Chore> choresList = FXCollections.observableArrayList();
    private final ObservableList<Point> historyList = FXCollections.observableArrayList();
    private final List<String> members = new ArrayList<>(List.of("Makis", "Manos", "Giannis"));

    private VBox rootLayout; 
    private VBox listContainer;
    private ListView<String> historyListView;



    public void start(Stage primaryStage) {
        choresList.add(new Chore("Σκούπισμα σαλονιού", 500, "Makis", members.size()));
        choresList.add(new Chore("Σφουγγάρισμα", 500, "Manos", members.size()));
        
        Chore completedChore = new Chore("Πλύσιμο πιάτων", 600, "Giannis", members.size());
        completedChore.setStatus("Completed");
        choresList.add(completedChore);

        rootLayout = new VBox();
        rootLayout.setSpacing(0);
        rootLayout.setStyle("-fx-background-color: #F3F4F6;");
        
        display(); 

        Scene scene = new Scene(rootLayout, 850, 650);
        primaryStage.setTitle("HOMY - Chores Module");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void display() {
        rootLayout.getChildren().clear();

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
        Label headerTitle = new Label("Chore Management");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        headerTitle.setTextFill(Color.web("#1F2937"));
        header.getChildren().add(headerTitle);
        rootLayout.getChildren().add(header);

        listContainer = new VBox(15);
        listContainer.setPadding(new Insets(25));
        refreshChoresUI();

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #F3F4F6; -fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        rootLayout.getChildren().add(scrollPane);

        HBox bottomArea = new HBox(20);
        bottomArea.setPadding(new Insets(20, 25, 20, 25));
        bottomArea.setAlignment(Pos.CENTER_LEFT);
        bottomArea.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, -2);");

        VBox historyBox = new VBox(8);
        historyBox.setPrefWidth(350);
        Label historyTitle = new Label("History (Points)");
        historyTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        historyTitle.setTextFill(Color.web("#4B5563"));
        
        historyListView = new ListView<>();
        historyListView.setPrefHeight(100);
        historyListView.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #E5E7EB; -fx-font-family: 'Segoe UI';");
        refreshHistoryUI();
        historyBox.getChildren().addAll(historyTitle, historyListView);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+");
        addBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 65px; -fx-min-height: 65px; -fx-font-size: 28px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(79,70,229,0.4), 10, 0, 0, 4);");
        addBtn.setOnAction(e -> selectAddChore());

        bottomArea.getChildren().addAll(historyBox, spacer, addBtn);
        rootLayout.getChildren().add(bottomArea);
    }

    private void refreshChoresUI() {
        listContainer.getChildren().clear();
        for (Chore chore : choresList) {
            listContainer.getChildren().add(createChoreCard(chore));
        }
    }

    private void refreshHistoryUI() {
        historyListView.getItems().clear();
        for (Point p : historyList) {
            historyListView.getItems().add("💎 " + p.toString());
        }
    }

    private HBox createChoreCard(Chore chore) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E5E7EB; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 5, 0, 0, 2);");

        VBox infoBox = new VBox(5);
        Label nameLbl = new Label(chore.getName());
        nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameLbl.setTextFill(Color.web("#111827"));
        Label pointsLbl = new Label(chore.getPoints() + " Points");
        pointsLbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        pointsLbl.setTextFill(Color.web("#6B7280"));
        infoBox.getChildren().addAll(nameLbl, pointsLbl);
        infoBox.setPrefWidth(160);

        Label arrow = new Label("➡");
        arrow.setTextFill(Color.web("#9CA3AF"));

        VBox assigneeBox = new VBox(2);
        Label assignTitle = new Label("Assignee");
        assignTitle.setFont(Font.font("Segoe UI", 10));
        assignTitle.setTextFill(Color.web("#9CA3AF"));
        Label assignLbl = new Label(chore.getAssignee());
        assignLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        assignLbl.setTextFill(Color.web("#374151"));
        assigneeBox.getChildren().addAll(assignTitle, assignLbl);
        assigneeBox.setPrefWidth(100);

        Label statusBadge = new Label(chore.getStatus());
        statusBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        statusBadge.setPadding(new Insets(4, 10, 4, 10));
        
        String badgeStyle = switch (chore.getStatus()) {
            case "Pending" -> "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;";
            case "Completed" -> "-fx-background-color: #DBEAFE; -fx-text-fill: #2563EB;";
            case "Suspended" -> "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;";
            default -> "-fx-background-color: #F3F4F6; -fx-text-fill: #4B5563;";
        };
        statusBadge.setStyle(badgeStyle + " -fx-background-radius: 20;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button reportBtn = createStyledButton("Report", "#F3F4F6", "#4B5563");
        reportBtn.setOnAction(e -> selectReportChore(chore));

        if (chore.getStatus().equals("Pending")) {
            Button completeBtn = createStyledButton("Complete", "#10B981", "white");
            completeBtn.setOnAction(e -> completeChore(chore));
            
            Button delBtn = createStyledButton("Delete", "#FEE2E2", "#DC2626");
            delBtn.setOnAction(e -> selectDeleteChore(chore));
            
            btnBox.getChildren().addAll(reportBtn, delBtn, completeBtn);
        } 
        else if (chore.getStatus().equals("Completed")) {
            Button approveBtn = createCircleButton("✓", "#10B981");
            approveBtn.setOnAction(e -> vote(chore, true));
            
            Button rejectBtn = createCircleButton("✕", "#EF4444");
            rejectBtn.setOnAction(e -> vote(chore, false));
            
            btnBox.getChildren().addAll(reportBtn, rejectBtn, approveBtn);
        }
        else if (chore.getStatus().equals("Suspended")) {
            Button restoreBtn = createStyledButton("Restore", "#D1FAE5", "#059669");
            restoreBtn.setOnAction(e -> {
                chore.setStatus("Pending"); 
                refreshChoresUI();      
            });
            
            Button delBtn = createStyledButton("Delete", "#FEE2E2", "#DC2626");
            delBtn.setOnAction(e -> selectDeleteChore(chore));
            
            btnBox.getChildren().addAll(reportBtn, restoreBtn, delBtn);
        }

        card.getChildren().addAll(infoBox, arrow, assigneeBox, statusBadge, spacer, btnBox);
        return card;
    }

    private void completeChore(Chore chore) {
        chore.setStatus("Completed");
        refreshChoresUI();
    }

    private void vote(Chore chore, boolean isApprove) {
        boolean isApproved = chore.vote(isApprove);
        
        if (isApproved) {
            updateHistory(chore);
            choresList.remove(chore);
        }
        refreshChoresUI();
    }

    private void updateHistory(Chore chore) {
        Point p = new Point(chore.getAssignee(), chore.getPoints(), chore.getName());
        historyList.add(p);
        refreshHistoryUI();
    }

    private void selectAddChore() {
        NewChoreScreen form = new NewChoreScreen(members, (name, pts, duty) -> {
            Chore newChore = new Chore(name, pts, duty, members.size());
            choresList.add(newChore);
            refreshChoresUI();
        });
        form.show();
    }

    private void selectReportChore(Chore chore) {
        ReportScreen report = new ReportScreen(chore, (description) -> {
            chore.setStatus("Suspended"); 
            refreshChoresUI();            
            System.out.println("Report Logged: " + description);
        });
        report.show();
    }

    private void selectDeleteChore(Chore chore) {
        ConfirmationScreen confirm = new ConfirmationScreen(
            "DELETE CHORE", 
            "Are you sure you want to delete: " + chore.getName() + "?", 
            "Yes, Delete", 
            "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;", 
            () -> {
                choresList.remove(chore);
                refreshChoresUI();
            }
        );
        confirm.show();
    }

    private Button createStyledButton(String text, String bg, String textFill) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + textFill + "; -fx-background-radius: 6; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;");
        return btn;
    }

    private Button createCircleButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 50; -fx-min-width: 35px; -fx-min-height: 35px; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        return btn;
    }
}