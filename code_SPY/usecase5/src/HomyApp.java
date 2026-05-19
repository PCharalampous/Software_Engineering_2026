import javafx.application.Application;
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
import java.util.ArrayList;
import java.util.List;

public class HomyApp extends Application {

    private List<Notification> notifications = new ArrayList<>();
    private UnreadCounter unreadCounter;
    private ManageNotificationsClass notificationManager;
    
    private Scene hubScene; // Το Scene του κεντρικού Hub

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("HOMY - Central Hub");
        primaryStage.setResizable(false);

        // Αρχικοποίηση των δεδομένων
        initNotificationData();

        // --- Header Section (Όπως η δεύτερη φωτογραφία) ---
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(40, 20, 20, 20));
        
        Label logoLabel = new Label("🏠 HOMY");
        logoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        logoLabel.setTextFill(Color.web("#1E3A5F"));
        
        Label subtitleLabel = new Label("Roommate Management System");
        subtitleLabel.setFont(Font.font("Segoe UI", 14));
        subtitleLabel.setTextFill(Color.web("#64748B"));
        
        headerBox.getChildren().addAll(logoLabel, subtitleLabel);

        // --- Navigation Cards Section (Μόνο η κάρτα Notifications) ---
        HBox cardsContainer = new HBox();
        cardsContainer.setAlignment(Pos.CENTER);
        cardsContainer.setPadding(new Insets(20, 30, 40, 30));

        VBox notificationsCard = createMenuCard(
            "🔔 Notifications Center", 
            "View pending household events, system alerts, and track unread room updates.",
            "#EF4444", 
            () -> {
                // Όταν πατηθεί η κάρτα, δημιουργείται η οθόνη και της δίνουμε λύση επιστροφής (backAction)
                NotificationsScreen notifScreen = new NotificationsScreen(primaryStage, notificationManager, () -> {
                    primaryStage.setScene(hubScene); // Επιστροφή στο Hub
                    primaryStage.setTitle("HOMY - Central Hub");
                });
                
                Scene notifScene = new Scene((javafx.scene.Parent) notifScreen.getView(), 420, 800);
                primaryStage.setScene(notifScene);
                primaryStage.setTitle("HOMY - Notifications");
            }
        );

        cardsContainer.getChildren().add(notificationsCard);

        // --- Main Layout ---
        VBox root = new VBox(20, headerBox, cardsContainer);
        root.setStyle("-fx-background-color: #F8FAF9;");
        root.setAlignment(Pos.TOP_CENTER);

        // Δημιουργία και εμφάνιση του Hub
        hubScene = new Scene(root, 420, 800);
        primaryStage.setScene(hubScene);
        primaryStage.show();
    }

    private void initNotificationData() {
        notifications.add(new Notification("CHORES", "Έχεις εκκρεμή εργασία: Σκούπισμα", "Εκκρεμής εργασία:\nΣκούπισμα – 500pts\nΠροθεσμία: Κυριακή 23/03", "CHORES", "#D4EDDA"));
        notifications.add(new Notification("BILLS", "Ο λογαριασμός ΔΕΗ λήγει σε 2 μέρες", "Λογαριασμός ΔΕΗ\nΠοσό: 45€\nΛήξη: 18/05/2026", "BILLS", "#FFF3CD"));
        notifications.add(new Notification("SHOPPING", "Ο Makis πρόσθεσε 3 προϊόντα στη λίστα", "Νέα προϊόντα:\n• Γάλα\n• Καφές\n• Ψωμί", "SHOPPING", "#F8D7DA"));

        unreadCounter = new UnreadCounter((int) notifications.stream().filter(n -> !n.read).count());
        notificationManager = new ManageNotificationsClass(notifications, unreadCounter);
    }

    // Μέθοδος δημιουργίας της κάρτας (Αντιγραφή από το στυλ της δεύτερης φωτογραφίας)
    private VBox createMenuCard(String title, String description, String accentColor, Runnable action) {
        VBox card = new VBox(12);
        card.setPrefSize(320, 200); 
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; "
                    + "-fx-background-radius: 12; "
                    + "-fx-border-radius: 12; "
                    + "-fx-border-color: #E2E8F0; "
                    + "-fx-border-width: 1; "
                    + "-fx-cursor: hand; "
                    + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 10, 0, 0, 4);");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
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
        launch(args);
    }
}