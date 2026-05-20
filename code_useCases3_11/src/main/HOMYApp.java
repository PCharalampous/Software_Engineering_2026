package main;

import chores.ChoreScreen;
import entities.Application;
import entities.Notification;
import notifications.ManageNotificationsClass;
import notifications.NotificationsScreen;
import entities.UnreadCounter;
import calendar.CalendarScreen;
import entities.Bill;
import entities.Issue;
import finances.FinancesScreen;
import finances.NewBillScreen;
import issues.HomeIssueScreen;
import issues.NewIssueScreen;
import issues.NewScheduleScreen;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import profile.ProfileScreen;
import points.PointScreen;
import points.RewardScreen;
import shoppinglist.ShoppingListScreen;

import java.util.ArrayList;
import java.util.List;

public class HOMYApp {

    private static Stage financesStage;         
    private static Stage issuesStage;
    private static Runnable financesBackAction; 
    private static Stage mainStage; 

    private static List<Notification> notifications = new ArrayList<>();
    private static UnreadCounter unreadCounter;
    private static ManageNotificationsClass notificationManager;

    public static void main(String[] args) {
        // 1. Αρχικοποίηση όλων των static δεδομένων της εφαρμογής
        initData();

        // 2. Εκκίνηση της JavaFX μέσω Platform.startup
        Platform.startup(() -> {
            Stage primaryStage = new Stage();
            mainStage = primaryStage; // Κρατάμε το instance για τις programmatic επιστροφές
            
            primaryStage.setTitle("HOMY - Central Hub");
            primaryStage.setResizable(false);

            // --- Header Section ---
            VBox headerBox = new VBox(5);
            headerBox.setAlignment(Pos.CENTER);
            headerBox.setPadding(new Insets(25, 20, 10, 20));
            
            Label logoLabel = new Label("🏠 HOMY");
            logoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
            logoLabel.setTextFill(Color.web("#1E3A5F"));
            
            Label subtitleLabel = new Label("Roommate Management System");
            subtitleLabel.setFont(Font.font("Segoe UI", 14));
            subtitleLabel.setTextFill(Color.web("#64748B"));
            
            headerBox.getChildren().addAll(logoLabel, subtitleLabel);

            // --- Navigation Cards Section (Πλέγμα 4x2) ---
            VBox cardsGrid = new VBox(15);
            cardsGrid.setAlignment(Pos.CENTER);
            cardsGrid.setPadding(new Insets(15, 30, 30, 30));

            // ΣΕΙΡΑ 1 (4 Κάρτες)
            HBox row1 = new HBox(15);
            row1.setAlignment(Pos.CENTER);

            // Card 1: Chores
            VBox choresCard = createMenuCard("📋 Chores", "Track daily duties.", "#4F46E5", () -> openChores());

            // Card 2: Rewards & Shop
            VBox pointsCard = createMenuCard("🎁 Rewards & Shop", "Check leaderboards.", "#10B981", () -> openRewards());

            // Card 3: Profile & Apps
            VBox profileCard = createMenuCard("👤 Profile & Apps", "View flat status.", "#6366F1", () -> openProfile());

            // Card 4: Shopping List
            VBox shoppingCard = createMenuCard("🛒 Shopping List", "Manage products.", "#F59E0B", () -> openShopping());
            row1.getChildren().addAll(choresCard, pointsCard, profileCard, shoppingCard);

            // ΣΕΙΡΑ 2 (4 Κάρτες - Τέλειο ευθυγραμμισμένο πλέγμα)
            HBox row2 = new HBox(15);
            row2.setAlignment(Pos.CENTER);

            // Card 5: Notifications
            VBox notificationsCard = createMenuCard("🔔 Notifications", "View alerts.", "#EF4444", () -> openNotifications());

            // Card 6: Calendar
            VBox calendarCard = createMenuCard("📅 Calendar & Events", "Schedule house meetings.", "#06B6D4", () -> openCalendar());

            // Card 7: Finances & Bills
            VBox financesCard = createMenuCard("💶 Finances & Bills", "Track utilities.", "#14B8A6", () -> openFinances());
            
            // Card 8: House Issues Module
            VBox issuesCard = createMenuCard("⚠️ House Issues", "Report broken maintenance objects.", "#F97316", () -> openIssues());

            row2.getChildren().addAll(notificationsCard, calendarCard, financesCard, issuesCard);
            cardsGrid.getChildren().addAll(row1, row2);

            // --- Profile Sidebar (Right Side) ---
            VBox profileSidebar = new VBox(10);
            profileSidebar.setPadding(new Insets(20));
            profileSidebar.setPrefWidth(180);
            Label profileLabel = new Label("👤 Profile: Makis");
            profileLabel.setStyle("-fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: #1E3A5F;");
            profileLabel.setOnMouseClicked(e -> openProfile());
            profileSidebar.getChildren().add(profileLabel);

            // Assembly με BorderPane για σωστό και μοντέρνο Mockup Layout
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #F8FAF9;");
            root.setTop(headerBox);
            root.setCenter(cardsGrid);
            root.setRight(profileSidebar);

            Scene hubScene = new Scene(root, 1150, 600);
            primaryStage.setScene(hubScene);
            primaryStage.show();
        });
    }

    // --- ΣΤΑΘΕΡΕΣ ΜΕΘΟΔΟΙ ΠΛΟΗΓΗΣΗΣ (Πλήρως Programmatic, No-FXML) ---
    private static void openChores() { 
        try { Stage s = new Stage(); s.setOnHiding(e -> mainStage.show()); mainStage.hide(); new ChoreScreen(() -> mainStage.show()).start(s); } catch(Exception e){ e.printStackTrace(); } 
    }
    
    private static void openRewards() { 
        try { Stage s = new Stage(); s.setOnHiding(e -> mainStage.show()); mainStage.hide(); 
              PointScreen sb = new PointScreen(new entities.Point(null,0,null), () -> mainStage.show()); sb.setStage(s);
              s.setScene(new Scene(new HBox(sb, new RewardScreen(sb)), 950, 600)); s.show(); } catch(Exception e){ e.printStackTrace(); }
    }
    
    private static void openShopping() { 
        mainStage.hide(); new ShoppingListScreen(() -> mainStage.show()).display(); 
    }
    
    private static void openNotifications() { 
        mainStage.hide(); new NotificationsScreen(notificationManager, () -> mainStage.show()).display(); 
    }
    
    private static void openCalendar() { 
        mainStage.hide(); new CalendarScreen(() -> mainStage.show()).display(); 
    }
    
    private static void openProfile() { 
        try { Stage s = new Stage(); s.setOnHiding(e -> mainStage.show()); mainStage.hide(); new ProfileScreen.FXBootstrap().start(s); } catch(Exception e){ e.printStackTrace(); }
    }

    private static void openFinances() { 
        financesStage = new Stage(); 
        financesBackAction = () -> { financesStage.close(); mainStage.show(); };
        financesStage.setOnHiding(w -> mainStage.show()); 
        mainStage.hide();
        
        // ΔΙΟΡΘΩΣΗ: Περνάμε σωστά τα callbacks πλοήγησης χωρίς compilation errors
        financesStage.setScene(new Scene(new FinancesScreen(
            () -> handleFinancesBack(), 
            () -> openNewBillForm()
        ), 850, 650)); 
        financesStage.show(); 
    }

    public static void setFinancesRootProgrammatic(VBox layout) { 
        if (financesStage != null && financesStage.getScene() != null) {
            financesStage.getScene().setRoot(layout);
        } 
    }
    
    private static void openNewBillForm() { 
        setFinancesRootProgrammatic(new NewBillScreen(() -> 
            setFinancesRootProgrammatic(new FinancesScreen(() -> handleFinancesBack(), () -> openNewBillForm()))
        )); 
    }
    
    public static void handleFinancesBack() { 
        if (financesBackAction != null) financesBackAction.run(); 
    }

    private static void openIssues() { 
        issuesStage = new Stage(); 
        issuesStage.setOnHiding(w -> mainStage.show()); 
        mainStage.hide();
        issuesStage.setScene(new Scene(new HomeIssueScreen(
            () -> { issuesStage.close(); mainStage.show(); }, 
            () -> openNewIssueForm(), 
            () -> openNewScheduleForm()
        ), 1180, 720));
        issuesStage.show(); 
    }

    public static void setIssuesRootProgrammatic(VBox layout) { 
        if (issuesStage != null && issuesStage.getScene() != null) {
            issuesStage.getScene().setRoot(layout);
        } 
    }
    
    private static void openNewIssueForm() { 
        setIssuesRootProgrammatic(new NewIssueScreen(() -> 
            setIssuesRootProgrammatic(new HomeIssueScreen(() -> { issuesStage.close(); mainStage.show(); }, () -> openNewIssueForm(), () -> openNewScheduleForm()))
        )); 
    }
    
    private static void openNewScheduleForm() { 
        setIssuesRootProgrammatic(new NewScheduleScreen(() -> 
            setIssuesRootProgrammatic(new HomeIssueScreen(() -> { issuesStage.close(); mainStage.show(); }, () -> openNewIssueForm(), () -> openNewScheduleForm()))
        )); 
    }

    private static void initData() {
        ProfileScreen.getDatabase().add(new Application("Flat Κυψέλη - 2 δωμάτια", "Κυψέλη", "Ιθάκης 12", 350, 3, "Φοιτητής", "PENDING"));
        ProfileScreen.getDatabase().add(new Application("Studio Εξάρχεια", "Εξάρχεια", "Σολωμού 45", 280, 2, "Ήσυχο", "ACCEPTED"));
        ProfileScreen.getDatabase().add(new Application("Studio Εξάρχεια", "Εξάρχεια", "Σολωμού 45", 280, 2, "Ήσυχο", "DECLINED"));
        
        notifications.add(new Notification("CHORES", "Έχεις εκκρεμή εργασία: Σκούπισμα", "Σκούπισμα", "CHORES", "#D4EDDA"));
        notifications.add(new Notification("BILLS", "Ο λογαριασμός ΔΕΗ λήγει", "ΔΕΗ 45€", "BILLS", "#FFF3CD"));

        unreadCounter = new UnreadCounter((int) notifications.stream().filter(n -> !n.read).count());
        notificationManager = new ManageNotificationsClass(notifications, unreadCounter);

        FinancesScreen.allBills.clear(); 
        FinancesScreen.allBills.add(new Bill("Electricity", 120.50, "2026-05-10", "Giorgos, Alex", "Pending"));
        FinancesScreen.allBills.add(new Bill("Internet", 35.00, "2026-05-01", "All Roommates", "Paid"));

        issues.HomeIssueScreen.allIssues.clear(); 
        issues.HomeIssueScreen.allIssues.add(new Issue("Plumbing - Kitchen Leak", "Τρέχει νερό κάτω από τον νιπτήρα", "Plumbing", "Alex", "All Roommates", "2026-05-14"));
        issues.HomeIssueScreen.allIssues.add(new Issue("Electrical - HVAC Failure", "Δεν βγάζει κρύο αέρα", "Electrical", "John", "John", "2026-05-16"));
        issues.HomeIssueScreen.allIssues.add(new Issue("Structural - Door Lock", "Μαγκώνει η κλειδαριά της εξώπορτας", "Structural", "Sarah", "All Roommates", "2026-05-18"));
        issues.HomeIssueScreen.allIssues.add(new Issue("Appliance - Refrigerator Fix", "[RESOLVED] Επισκευάστηκε το μοτέρ", "Appliance", "Emma", "John, Alex", "2026-04-29"));
    }

    private static VBox createMenuCard(String title, String description, String accentColor, Runnable action) {
        VBox card = new VBox(12); card.setPrefSize(260, 180); card.setPadding(new Insets(20)); card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-cursor: hand;");
        Label t = new Label(title); t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15)); t.setTextFill(Color.web("#1E293B"));
        Label d = new Label(description); d.setFont(Font.font("Segoe UI", 12.5)); d.setTextFill(Color.web("#64748B")); d.setWrapText(true);
        card.getChildren().addAll(t, d);
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: " + accentColor + "; -fx-border-width: 2; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-cursor: hand;"));
        card.setOnMouseClicked(e -> action.run()); return card;
    }
}