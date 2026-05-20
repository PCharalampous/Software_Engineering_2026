package myapplications;

import entities.Application;
import profile.ProfileScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.ConfirmationScreen;
import java.util.List;

public class MyApplicationScreen {

    public void display() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F8FAFC;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        
        backBtn.setOnAction(e -> {
            ProfileScreen pScreen = new ProfileScreen();
            Scene profileScene = new Scene((javafx.scene.Parent) pScreen.getView(), 420, 750);
            ProfileScreen.getStage().setScene(profileScene);
        });
        
        Label titleLabel = new Label("My Applications");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 0 10;");
        header.getChildren().addAll(backBtn, titleLabel);
        root.setTop(header);

        VBox listContainer = new VBox(12);
        
        // --- ΑΛΛΑΓΗ ΕΔΩ: Ανάγνωση αγγελιών από τη βάση για τον χρήστη 1 ---
        List<Application> dbApplications = Application.loadUserApplications(1);
        for (Application app : dbApplications) {
            HBox card = new HBox(10);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-width: 1;");
            
            VBox textData = new VBox(4);
            textData.setPrefWidth(220);
            Label tLbl = new Label(app.getTitle());
            tLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
            
            Label details = new Label(app.getRent() + "€/month • " + app.getRoommatesWanted() + " roomm.");
            details.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
            textData.getChildren().addAll(tLbl, details);

            Button actionBtn = new Button(app.getStatus().equals("PENDING") ? "CANCEL" : "VIEW");
            actionBtn.setPrefWidth(80);
            actionBtn.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6 12;");
            
            if (app.getStatus().equals("PENDING")) {
                actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626;");
            } else {
                actionBtn.setStyle(actionBtn.getStyle() + "-fx-background-color: #E2E8F0; -fx-text-fill: #475569;");
            }

            actionBtn.setOnAction(e -> selectApplication(app));

            card.getChildren().addAll(textData, actionBtn);
            listContainer.getChildren().add(card);
        }

        ScrollPane scrollPane = new ScrollPane(listContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scrollPane);

        Button addBtn = new Button("+ Add Application");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
        addBtn.setOnAction(e -> selectAddApplications());
        root.setBottom(addBtn);

        ProfileScreen.getStage().setScene(new Scene(root, 420, 750));
    }

    public void selectAddApplications() {
        NewApplicationScreen newScreen = new NewApplicationScreen();
        newScreen.fillForm();
    }

    public void selectApplication(Application app) {
        if (app.getStatus().equals("PENDING")) {
            String cancelBtnStyle = "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;";
            
            boolean[] insideCancelConfirm = {false};

            ConfirmationScreen cancelDialog = new ConfirmationScreen(
                "Cancel Application",
                "Are you sure you want to cancel your pending listing?",
                "Yes, Cancel",
                cancelBtnStyle,
                () -> {
                    insideCancelConfirm[0] = true; 
                    // --- ΑΛΛΑΓΗ ΕΔΩ: Ακύρωση απευθείας στη βάση ---
                    app.cancelApplication(); 
                    StatusScreen.show("CANCELED");
                    display();
                }
            );
            cancelDialog.show();

            if (!insideCancelConfirm[0]) {
                noCancelation();
            }
        } else {
            StatusScreen.show(app.getStatus());
        }
    }

    public void noCancelation() {
        System.out.println("Execution of alt3.2 [Decline]: noCancelation() triggered.");
    }
}