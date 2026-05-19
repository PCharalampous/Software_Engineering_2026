package main;

import chores.ChoreScreen;
import entities.Application;
import entities.Notification;
import notifications.ManageNotificationsClass;
import notifications.NotificationsScreen;
import entities.UnreadCounter;
import javafx.application.Platform;
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
import profile.ProfileScreen;
import points.PointScreen;

import java.util.ArrayList;
import java.util.List;

public class HOMYApp {

    private static Scene hubScene; // Το Scene του Central Hub για επιστροφή
    
    // Global App State (Δεδομένα από τα Chores, Profile & Notifications)
    private static Object currentUser; 
    private static List<Object> incomingRequests = new ArrayList<>(); 
    private static Object profileManager; 

    private static List<Notification> notifications = new ArrayList<>();
    private static UnreadCounter unreadCounter;
    private static ManageNotificationsClass notificationManager;

    public static void main(String[] args) {
        // 1. Αρχικοποίηση όλων των δεδομένων της εφαρμογής
        initData();

        // 2. Εκκίνηση της JavaFX μέσω Platform.startup
        Platform.startup(() -> {
            Stage primaryStage = new Stage();
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

            // --- Navigation Cards Section (Οριζόντια διάταξη 4 καρτών πλέον!) ---
            HBox cardsContainer = new HBox(15); // Ελαφρώς μικρότερο gap για να χωρέσουν άνετα
            cardsContainer.setAlignment(Pos.CENTER);
            cardsContainer.setPadding(new Insets(20, 20, 40, 20));

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

            // Card 4: ΝΕΑ ΚΑΡΤΑ - Notifications Center (Πλήρως Λειτουργική!)
            VBox notificationsCard = createMenuCard(
                "🔔 Notifications Center", 
                "View pending household events, system alerts, and track unread room updates.",
                "#EF4444", 
                () -> {
                	try {
                        // Δημιουργούμε την οθόνη και περνάμε τι θέλουμε να γίνει όταν πατηθεί το πίσω βέλος (←)
                        NotificationsScreen notifScreen = new NotificationsScreen(notificationManager, () -> {
                            // Επανεμφάνιση ή εστίαση στο κεντρικό hub παράθυρο αν χρειάζεται
                            primaryStage.show(); 
                        });
                        
                        // Κρύβουμε προαιρετικά το κεντρικό hub και αφήνουμε τη screen να ανοίξει το δικό της Stage
                        notifScreen.display(); // <--- ΕΔΩ ΓΙΝΕΤΑΙ Η ΑΥΤΟΝΟΜΗ ΕΚΚΙΝΗΣΗ ΤΟΥ STAGE!
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            );

            // Προσθήκη όλων των καρτών στο container
            cardsContainer.getChildren().addAll(choresCard, pointsCard, profileCard, notificationsCard);

            // --- Main Layout ---
            VBox root = new VBox(15, headerBox, cardsContainer);
            root.setStyle("-fx-background-color: #F8FAF9;");
            root.setAlignment(Pos.TOP_CENTER);

            // Αυξάνουμε ελαφρώς το πλάτος σε 1250 για να απλωθούν όμορφα και οι 4 κάρτες οριζόντια
            hubScene = new Scene(root, 1250, 450);
            primaryStage.setScene(hubScene);
            primaryStage.show();
        });
    }

    // Μέθοδος προετοιμασίας και γεμίσματος όλων των Δεδομένων
    private static void initData() {
        // Α) Δεδομένα για το Profile & Applications
        ProfileScreen.getDatabase().add(new Application("Flat Κυψέλη - 2 δωμάτια", "Κυψέλη", "Ιθάκης 12", 350, 3, "Φοιτητής, λάτρης καφέ...", "PENDING"));
        ProfileScreen.getDatabase().add(new Application("Studio Εξάρχεια", "Εξάρχεια", "Σολωμού 45", 280, 2, "Ήσυχο περιβάλλον", "ACCEPTED"));
        ProfileScreen.getDatabase().add(new Application("Δωμάτιο Παγκράτι", "Παγκράτι", "Φιλολάου 89", 210, 1, "Κοντά σε μετρό", "DECLINED"));

        // Β) Δεδομένα για το Notifications Center
        notifications.add(new Notification("CHORES", "Έχεις εκκρεμή εργασία: Σκούπισμα", "Εκκρεμής εργασία:\nΣκούπισμα – 500pts\nΠροθεσμία: Κυριακή 23/03", "CHORES", "#D4EDDA"));
        notifications.add(new Notification("BILLS", "Ο λογαριασμός ΔΕΗ λήγει σε 2 μέρες", "Λογαριασμός ΔΕΗ\nΠοσό: 45€\nΛήξη: 18/05/2026", "BILLS", "#FFF3CD"));
        notifications.add(new Notification("SHOPPING", "Ο Makis πρόσθεσε 3 προϊόντα στη λίστα", "Νέα προϊόντα:\n• Γάλα\n• Καφές\n• Ψωμί", "SHOPPING", "#F8D7DA"));

        unreadCounter = new UnreadCounter((int) notifications.stream().filter(n -> !n.read).count());
        notificationManager = new ManageNotificationsClass(notifications, unreadCounter);
    }

    // Βοηθητική μέθοδος για τη δημιουργία Modern Menu Cards
    private static VBox createMenuCard(String title, String description, String accentColor, Runnable action) {
        VBox card = new VBox(12);
        card.setPrefSize(270, 180); // Ελαφρώς προσαρμοσμένο μέγεθος για τέλεια αναλογία 4 καρτών
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
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#1E293B"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 12.5));
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
}