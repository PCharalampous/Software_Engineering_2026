package myapplications;

import entities.Application;
import entities.Authentication;
import entities.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import profile.ProfileScreen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import util.DatabaseManager;

public class MyApplicationScreen {
    private VBox applicationsContainer;
    private int currentUserId;
    private Stage currentStage;
    private ProfileScreen profileScreen;

    public MyApplicationScreen(ProfileScreen profileScreen) {
        this.profileScreen = profileScreen;
        User sessionUser = Authentication.getCurrentUser();
        this.currentUserId = (sessionUser != null) ? sessionUser.getId() : 1;
    }

    public MyApplicationScreen() {
        User sessionUser = Authentication.getCurrentUser();
        this.currentUserId = (sessionUser != null) ? sessionUser.getId() : 1;
    }

    public void display() {
        this.currentStage = new Stage();
        currentStage.setTitle("My Applications");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8F7F4;");

        // --- Header ---
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #1A1A1A; -fx-padding: 15;");
        header.setAlignment(Pos.CENTER_LEFT);

        Button backButton = new Button("←");
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        backButton.setOnAction(e -> currentStage.close());

        Label headerTitle = new Label("MY APPLICATIONS");
        headerTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 0 10;");

        header.getChildren().addAll(backButton, headerTitle);
        root.setTop(header);

        // --- Content Container ---
        applicationsContainer = new VBox(10);
        applicationsContainer.setPadding(new Insets(15, 15, 80, 15)); 
        applicationsContainer.setStyle("-fx-background-color: #F8F7F4;");

        refreshApplicationsList();

        ScrollPane scrollPane = new ScrollPane(applicationsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // --- Floating Button (Κάτω Δεξιά) ---
        Button addButton = new Button("+ ADD");
        
        // ΕΛΕΓΧΟΣ: Αν ο χρήστης είναι ήδη σε δωμάτιο, απενεργοποιούμε το κουμπί
        boolean hasRoom = Application.checkIfUserHasRoom(currentUserId);
        
        if (hasRoom) {
            addButton.setDisable(true);
            addButton.setText("Locked (Already in a Room)");
            addButton.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: #E5E7EB; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 20; -fx-padding: 10 20;");
        } else {
            addButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 20; -fx-cursor: hand; -fx-padding: 10 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);");
            
            addButton.setOnAction(e -> {
                currentStage.close(); 
                new NewApplicationScreen(profileScreen).fillForm(); 
            });
        }

        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(scrollPane, addButton);
        StackPane.setAlignment(addButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(addButton, new Insets(0, 20, 20, 0)); 

        root.setCenter(centerStack);

        Scene scene = new Scene(root, 420, 600);
        currentStage.setScene(scene);
        currentStage.show();
    }

    private void refreshApplicationsList() {
        applicationsContainer.getChildren().clear();

        List<Application> userApps = Application.loadUserApplications(currentUserId);

        if (userApps.isEmpty()) {
            Label noAppsLabel = new Label("No applications found.");
            noAppsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px; -fx-font-style: italic; -fx-padding: 20 0 0 0;");
            applicationsContainer.getChildren().add(noAppsLabel);
            return;
        }

        for (Application app : userApps) {
            VBox appCard = new VBox(8);
            appCard.setStyle("-fx-background-color: white; -fx-border-color: #DDDDDD; -fx-border-width: 1; -fx-padding: 12;");

            HBox topRow = new HBox();
            topRow.setAlignment(Pos.CENTER_LEFT);

            Label titleLabel = new Label(app.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;");

            Label statusLabel = new Label(app.getStatus().toUpperCase());
            if (app.getStatus().equalsIgnoreCase("pending")) {
                statusLabel.setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 0 0 0 10;");
            } else if (app.getStatus().equalsIgnoreCase("accepted")) {
                statusLabel.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 0 0 0 10;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 0 0 0 10;");
            }

            topRow.getChildren().addAll(titleLabel, statusLabel);

            Label descLabel = new Label(app.getDescription());
            descLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 13px;");
            descLabel.setWrapText(true);

            HBox actionsRow = new HBox();
            actionsRow.setAlignment(Pos.CENTER_RIGHT);

            Button cancelIdButton = new Button("Cancel");
            cancelIdButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-border-color: #DC2626; -fx-border-width: 1; -fx-cursor: hand; -fx-padding: 4 10;");
            
            cancelIdButton.setOnAction(e -> {
                boolean success = false;

                // ΕΞΥΠΝΟ CANCEL: Αν η κάρτα προέρχεται από αίτημα Connect (PENDING/ACCEPTED/DECLINED), 
                // σβήνουμε μόνο το request από τον πίνακα room_requests
                String checkOwnerSql = "SELECT user_id FROM applications WHERE application_id = ?";
                int ownerId = 0;
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(checkOwnerSql)) {
                    stmt.setInt(1, app.getId());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) ownerId = rs.getInt("user_id");
                    }
                } catch (SQLException ex) { ex.printStackTrace(); }

                if (currentUserId != ownerId) {
                    // Είναι αίτημα Connect άλλου χρήστη, σβήνουμε το αίτημα από τον πίνακα room_requests
                    String deleteRequestSql = "DELETE FROM room_requests WHERE sender_id = ? AND room_id = " +
                                              "(SELECT room_id FROM users WHERE user_id = ?)";
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(deleteRequestSql)) {
                        stmt.setInt(1, currentUserId);
                        stmt.setInt(2, ownerId);
                        int rows = stmt.executeUpdate();
                        success = rows > 0;
                    } catch (SQLException ex) { ex.printStackTrace(); }
                } else {
                    // Είναι δική του αγγελία, διαγράφεται πλήρως ολόκληρο το application
                    success = Application.deleteApplication(app.getId());
                }

                if (success) {
                    StatusScreen.show("CANCELED");
                    
                    // 1. Ανανεώνουμε το ProfileScreen στο υπόβαθρο για να αλλάξει ο counter π.χ. (MY APPLICATIONS (2))
                    if (profileScreen != null) {
                        profileScreen.showProfileMain();
                    }
                    
                    // 2. Ανανεώνουμε τη λίστα με τις κάρτες στην τρέχουσα οθόνη
                    refreshApplicationsList(); 
                    
                    // 3. ΣΗΜΑΝΤΙΚΟ: Φέρνουμε το παράθυρο των Applications ξανά μπροστά στο προσκήνιο
                    if (currentStage != null) {
                        currentStage.toFront();
                        currentStage.requestFocus();
                    }
                } else {
                    System.err.println("Αποτυχία διαγραφής από τη βάση δεδομένων.");
                }
            });

            actionsRow.getChildren().add(cancelIdButton);
            appCard.getChildren().addAll(topRow, descLabel, actionsRow);
            
            applicationsContainer.getChildren().add(appCard);
        }
    }
}