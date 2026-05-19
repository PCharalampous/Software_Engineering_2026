package main;

import chores.ChoreScreen;
import entities.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import myapplications.ProfileScreen;
import points.PointScreen;

public class HOMYApp extends javafx.application.Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("HOMY - Central Hub");
        primaryStage.setResizable(false);

        // --- Header Section ---
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(30, 20, 10, 20));
        
        Label logoLabel = new Label("🏠 HOMY");
        logoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        logoLabel.setTextFill(Color.web("#1E3A5F"));
        
        Label subtitleLabel = new Label("Roommate Management System");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(Color.web("#64748B"));
        
        headerBox.getChildren().addAll(logoLabel, subtitleLabel);

        // --- Navigation Cards Section ---
        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(Pos.CENTER);
        cardsContainer.setPadding(new Insets(20, 30, 40, 30));

        // Card 1: Chores Module
        VBox choresCard = createMenuCard(
            "📋 Chores Management", 
            "Track daily household duties, assign tasks, and log completed work.",
            "#4F46E5", 
            () -> {
                try {
                    new ChoreScreen().start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        );

        // Card 2: Points & Rewards Module
        VBox pointsCard = createMenuCard(
            "🎁 Rewards & Shop", 
            "Check point leaderboards and exchange hard-earned points for house perks.",
            "#10B981", 
            () -> {
                try {
                    new PointScreen.Launcher().start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        );

        // Card 3: Profile & Applications Module
        VBox profileCard = createMenuCard(
            "👤 Profile & Apps", 
            "View your profile and track your flat applications status.",
            "#6366F1", 
            () -> {
                try {
                    new ProfileScreen.FXBootstrap().start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        );

        cardsContainer.getChildren().addAll(choresCard, pointsCard, profileCard);

        // --- Main Layout ---
        VBox root = new VBox(15, headerBox, cardsContainer);
        root.setStyle("-fx-background-color: #F8FAF9;");
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root, 1000, 430);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createMenuCard(String title, String description, String accentColor, Runnable action) {
        VBox card = new VBox(12);
        card.setPrefSize(280, 180); 
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; "
                    + "-fx-background-radius: 12; "
                    + "-fx-border-radius: 12; "
                    + "-fx-border-color: #E2E8F0; "
                    + "-fx-border-width: 1; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#1E293B"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 13));
        descLabel.setTextFill(Color.web("#64748B"));
        descLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, descLabel);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; "
                                                + "-fx-background-radius: 12; "
                                                + "-fx-border-radius: 12; "
                                                + "-fx-border-color: " + accentColor + "; "
                                                + "-fx-border-width: 2; "
                                                + "-fx-cursor: hand; "
                                                + "-fx-effect: dropshadow(three-pass-box, " + accentColor + "33, 12, 0, 0, 6);"));
        
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; "
                                               + "-fx-background-radius: 12; "
                                               + "-fx-border-radius: 12; "
                                               + "-fx-border-color: #E2E8F0; "
                                               + "-fx-border-width: 1; "
                                               + "-fx-cursor: hand; "
                                               + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);"));

        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    public static void main(String[] args) {
        ProfileScreen.getDatabase().add(new Application("Flat Κυψέλη - 2 δωμάτια", "Κυψέλη", "Ιθάκης 12", 350, 3, "Φοιτητής, λάτρης καφέ...", "PENDING"));
        ProfileScreen.getDatabase().add(new Application("Studio Εξάρχεια", "Εξάρχεια", "Σολωμού 45", 280, 2, "Ήσυχο περιβάλλον", "ACCEPTED"));
        ProfileScreen.getDatabase().add(new Application("Δωμάτιο Παγκράτι", "Παγκράτι", "Φιλολάου 89", 210, 1, "Κοντά σε μετρό", "DECLINED"));

        launch(args);
    }
}