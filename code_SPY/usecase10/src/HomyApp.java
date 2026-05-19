import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class HomyApp {

    public static class FXLauncher extends javafx.application.Application {
        @Override
        public void start(Stage primaryStage) {
            HomyApp app = new HomyApp();
            app.startApp(primaryStage);
        }
    }

    private VBox mainRoot;
    private Scene hubScene; // Το Scene του Central Hub
    
    // Global App State (Μόνο για το Προφίλ και τις Αιτήσεις)
    private UserProfile currentUser;
    private List<Request> incomingRequests = new ArrayList<>();
    private List<Application> myApplications = new ArrayList<>();
    private ManageProfileClass profileManager;

    public void startApp(Stage primaryStage) {
        initData();

        primaryStage.setTitle("HOMY - Central Hub");
        primaryStage.setResizable(false);

        // --- Header Section (Όπως ακριβώς στη φωτογραφία) ---
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

        // --- Navigation Cards Section (Οριζόντια διάταξη 3 καρτών) ---
        HBox cardsContainer = new HBox(20);
        cardsContainer.setAlignment(Pos.CENTER);
        cardsContainer.setPadding(new Insets(20, 30, 40, 30));

        // Κάρτα 1: Chores Management (Placeholder)
        VBox choresCard = createMenuCard(
            "📋 Chores Management", 
            "Track daily household duties, assign tasks, and log completed work.",
            "#4F46E5", 
            () -> showPlaceholderAlert("Chores Management")
        );

        // Κάρτα 2: Rewards & Shop (Placeholder)
        VBox pointsCard = createMenuCard(
            "🎁 Rewards & Shop", 
            "Check point leaderboards and exchange hard-earned points for house perks.",
            "#10B981", 
            () -> showPlaceholderAlert("Rewards & Shop")
        );

        // Κάρτα 3: Profile & Apps -> Συνδέεται με το ProfileScreen
        VBox profileCard = createMenuCard(
            "👤 Profile & Apps", 
            "View your profile and track your flat applications status.",
            "#6366F1", 
            () -> {
                // Δημιουργία της οθόνης προφίλ με δυνατότητα επιστροφής στο hubScene
                ProfileScreen profScreen = new ProfileScreen(primaryStage, profileManager, incomingRequests, myApplications, () -> {
                    primaryStage.setScene(hubScene);
                    primaryStage.setTitle("HOMY - Central Hub");
                });
                
                // Αλλαγή Scene στο ίδιο παράθυρο
                Scene profileScene = new Scene((javafx.scene.Parent) profScreen.getView(), 420, 800);
                primaryStage.setScene(profileScene);
                primaryStage.setTitle("HOMY - Profile & Apps");
            }
        );

        cardsContainer.getChildren().addAll(choresCard, pointsCard, profileCard);

        // --- Main Layout ---
        mainRoot = new VBox(15, headerBox, cardsContainer);
        mainRoot.setStyle("-fx-background-color: #F8FAF9;");
        mainRoot.setAlignment(Pos.TOP_CENTER);

        // Διαστάσεις 1000x450 για να απλωθούν σωστά οι κάρτες οριζόντια στην αρχική
        hubScene = new Scene(mainRoot, 1000, 450);
        primaryStage.setScene(hubScene);
        primaryStage.show();
    }

    private void initData() {
        currentUser = new UserProfile(
            "Makis Kosta", "@makisk",
            "Φοιτητής, λάτρης καφέ, ψάχνω ήσυχους συγκατοίκους",
            "Ήσυχος, καπνιστής: όχι",
            1000, "Flat 4B", 4
        );
        
        incomingRequests.add(new Request("Nikos Kam.", "NK", "2 μέρες πριν"));
        incomingRequests.add(new Request("Anna P.", "AP", "4 μέρες πριν"));
        
        myApplications.add(new Application("Flat Κυψέλη – 2 δωμάτια", "350€/μήνα • 3 συγκατοίκοι", "PENDING"));
        myApplications.add(new Application("Studio Εξάρχεια", "280€/μήνα • 2 συγκατοίκοι", "PENDING"));

        profileManager = new ManageProfileClass(currentUser, incomingRequests);
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

    private void showPlaceholderAlert(String moduleName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(moduleName);
        alert.setHeaderText(null);
        alert.setContentText("Η ενότητα " + moduleName + " θα φορτωθεί από το αντίστοιχο sub-project.");
        alert.showAndWait();
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(FXLauncher.class, args);
    }
}