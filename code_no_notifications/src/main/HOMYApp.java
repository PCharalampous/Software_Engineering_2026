package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import login.LogInScreen;
import chores.ChoreScreen;
import notifications.ManageNotificationsClass;
import notifications.NotificationsScreen;
import calendar.CalendarScreen;
import finances.FinancesScreen;
import finances.NewBillScreen;
import issues.HomeIssueScreen;
import ui.ConfirmationScreen;
import issues.NewIssueScreen;
import issues.NewScheduleScreen;
import profile.ProfileScreen;
import points.PointScreen;
import points.RewardScreen;
import shoppinglist.ShoppingListScreen;
import util.DatabaseManager;
import search.HomeScreen;

import entities.Notification;
import entities.UnreadCounter;
import entities.Bill;
import entities.Issue;
import entities.Authentication;

public class HOMYApp extends Application {
    
    private static Connection conn;
    private static DatabaseManager dataB;
    
    private static Stage mainStage; 
    private static Stage financesStage;         
    private static Stage issuesStage;
    private static Runnable financesBackAction; 

    private static List<Notification> notifications = new ArrayList<>();
    private static UnreadCounter unreadCounter;
    private static ManageNotificationsClass notificationManager;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        initMockData();
        
        LogInScreen loginScr = new LogInScreen(primaryStage);
        loginScr.createWindow();
        
        conn = DatabaseManager.getConnection();
        loginScr.setDataBaseConnection(conn);
    }
    
    public static void showCentralHub() {
        mainStage.setTitle("HOMY - Central Hub");
        mainStage.setResizable(false);

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

        VBox cardsGrid = new VBox(15);
        cardsGrid.setAlignment(Pos.CENTER);
        cardsGrid.setPadding(new Insets(15, 30, 30, 30));

        HBox row1 = new HBox(15);
        row1.setAlignment(Pos.CENTER);
        VBox choresCard = createMenuCard("📋 Chores", "Track daily duties.", "#4F46E5", () -> openChores());
        VBox pointsCard = createMenuCard("🎁 Rewards & Shop", "Check leaderboards.", "#10B981", () -> openRewards());
        VBox shoppingCard = createMenuCard("🛒 Shopping List", "Manage products.", "#F59E0B", () -> openShopping());
        row1.getChildren().addAll(choresCard, pointsCard, shoppingCard);

        HBox row2 = new HBox(15);
        row2.setAlignment(Pos.CENTER);
        VBox notificationsCard = createMenuCard("🔔 Notifications", "View alerts.", "#EF4444", () -> openNotifications());
        VBox calendarCard = createMenuCard("📅 Calendar & Events", "Schedule house meetings.", "#06B6D4", () -> openCalendar());
        VBox financesCard = createMenuCard("💶 Finances & Bills", "Track utilities.", "#14B8A6", () -> openFinances());
        VBox issuesCard = createMenuCard("⚠️ House Issues", "Report broken maintenance objects.", "#F97316", () -> openIssues());
        row2.getChildren().addAll(notificationsCard, calendarCard, financesCard, issuesCard);
        cardsGrid.getChildren().addAll(row1, row2);

        // --- Sidebar (Δεξιά) ---
        VBox profileSidebar = new VBox(15);
        profileSidebar.setPadding(new Insets(20));
        profileSidebar.setPrefWidth(200);
        profileSidebar.setAlignment(Pos.TOP_CENTER);
        
        String username = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getUsername() : "Makis";
        Label profileLabel = new Label("👤 Profile: " + username);
        profileLabel.setStyle("-fx-font-weight: bold; -fx-cursor: hand; -fx-text-fill: #1E3A5F; -fx-font-size: 14px;");
        profileLabel.setOnMouseClicked(e -> openProfile());
        profileSidebar.getChildren().add(profileLabel);

        Region verticalSpacer = new Region();
        VBox.setVgrow(verticalSpacer, Priority.ALWAYS);
        profileSidebar.getChildren().add(verticalSpacer);

        // --- ΔΙΟΡΘΩΘΗΚΕ: ΚΟΥΜΠΙ LEAVE ROOM ΜΕ ΑΥΤΟΜΑΤΗ ΑΥΞΗΣΗ ΘΕΣΕΩΝ ΣΤΗΝ ΑΓΓΕΛΙΑ ---
        Button leaveRoomBtn = new Button("🚪 Leave Room");
        leaveRoomBtn.setMaxWidth(Double.MAX_VALUE);
        leaveRoomBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
        
        leaveRoomBtn.setOnAction(e -> {
            ConfirmationScreen confirmDialog = new ConfirmationScreen(
                "Leave Room Confirmation",
                "Are you sure you want to leave this room? Your room ID association will be removed.",
                "Leave Room",
                "-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;",
                () -> {
                    int userId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getId() : 1;
                    
                    try (Connection updateConn = DatabaseManager.getConnection()) {
                        updateConn.setAutoCommit(false); // Έναρξη Transaction

                        // 1. Βρίσκουμε το τρέχον room_id του χρήστη πριν το κάνουμε NULL
                        int userRoomId = 0;
                        String findUserRoom = "SELECT room_id FROM users WHERE user_id = ?";
                        try (PreparedStatement psFind = updateConn.prepareStatement(findUserRoom)) {
                            psFind.setInt(1, userId);
                            try (var rs = psFind.executeQuery()) {
                                if (rs.next()) {
                                    userRoomId = rs.getInt("room_id");
                                }
                            }
                        }

                        // 2. Θέτουμε το room_id του χρήστη σε NULL
                        String sqlLeave = "UPDATE users SET room_id = NULL WHERE user_id = ?";
                        try (PreparedStatement psLeave = updateConn.prepareStatement(sqlLeave)) {
                            psLeave.setInt(1, userId);
                            psLeave.executeUpdate();
                        }
                        
                        // 3. Αυξάνουμε το roommates_wanted κατά 1 στην αγγελία αυτού του δωματίου
                        if (userRoomId > 0) {
                            String sqlIncrease = 
                                "UPDATE applications a " +
                                "JOIN users u ON a.user_id = u.user_id " +
                                "SET a.roommates_wanted = a.roommates_wanted + 1 " +
                                "WHERE u.room_id = ?";
                            try (PreparedStatement psInc = updateConn.prepareStatement(sqlIncrease)) {
                                psInc.setInt(1, userRoomId);
                                psInc.executeUpdate();
                            }
                        }
                        
                        updateConn.commit(); // Commit όλης της συναλλαγής
                        System.out.println("Left room successfully! Roommates wanted listing updated.");
                        
                        HomeScreen searchScreen = new HomeScreen(mainStage, conn);
                        searchScreen.createWindow();
                        
                    } catch (Exception ex) {
                        System.err.println("Database error during leave room transaction:");
                        ex.printStackTrace();
                    }
                }
            );
            confirmDialog.show();
        });
        
        profileSidebar.getChildren().add(leaveRoomBtn);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAF9;");
        root.setTop(headerBox);
        root.setCenter(cardsGrid);
        root.setRight(profileSidebar);

        Scene hubScene = new Scene(root, 1150, 650);
        mainStage.setScene(hubScene);
        mainStage.show();
    }

    public static void main(String[] args) {
        System.out.println("Έναρξη δοκιμής σύνδεσης απευθείας από τον DatabaseManager...");
        dataB = new DatabaseManager();
        conn = DatabaseManager.getConnection();
         
        if (conn != null) {
            System.out.println("Η σύνδεση με το Clever Cloud πέτυχε.");
            dataB.showTables();
            launch(args); 
        } else {
            System.err.println("Αποτυχία σύνδεσης! Σιγουρέψου ότι το αρχείο config.properties βρίσκεται στον φάκελο src.");
        }
    }
    
    @Override
    public void stop() throws Exception {
        if (dataB != null) {
            dataB.closeConnection();
        }
        super.stop();
    }
    
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
    
    public static void openProfile() { 
        try { Stage s = new Stage(); s.setOnHiding(e -> mainStage.show()); mainStage.hide(); ProfileScreen.FXBootstrap bootstrap = new ProfileScreen.FXBootstrap(); bootstrap.start(s); } catch(Exception e){ e.printStackTrace(); }
    }

    private static void openFinances() { 
        financesStage = new Stage(); financesBackAction = () -> { financesStage.close(); mainStage.show(); }; financesStage.setOnHiding(w -> mainStage.show()); mainStage.hide();
        financesStage.setScene(new Scene(new FinancesScreen(() -> handleFinancesBack(), () -> openNewBillForm()), 850, 650)); financesStage.show(); 
    }

    public static void setFinancesRootProgrammatic(VBox layout) { if (financesStage != null && financesStage.getScene() != null) { financesStage.getScene().setRoot(layout); } }
    private static void openNewBillForm() { setFinancesRootProgrammatic(new NewBillScreen(() -> setFinancesRootProgrammatic(new FinancesScreen(() -> handleFinancesBack(), () -> openNewBillForm())))); }
    public static void handleFinancesBack() { if (financesBackAction != null) financesBackAction.run(); }

    private static void openIssues() { 
        issuesStage = new Stage(); issuesStage.setOnHiding(w -> mainStage.show()); mainStage.hide();
        issuesStage.setScene(new Scene(new HomeIssueScreen(() -> { issuesStage.close(); mainStage.show(); }, () -> openNewIssueForm(), () -> openNewScheduleForm()), 1180, 720)); issuesStage.show(); 
    }

    public static void setIssuesRootProgrammatic(VBox layout) { if (issuesStage != null && issuesStage.getScene() != null) { issuesStage.getScene().setRoot(layout); } }
    private static void openNewIssueForm() { setIssuesRootProgrammatic(new NewIssueScreen(() -> setIssuesRootProgrammatic(new HomeIssueScreen(() -> { issuesStage.close(); mainStage.show(); }, () -> openNewIssueForm(), () -> openNewScheduleForm())))); }
    private static void openNewScheduleForm() { setIssuesRootProgrammatic(new NewScheduleScreen(() -> setIssuesRootProgrammatic(new HomeIssueScreen(() -> { issuesStage.close(); mainStage.show(); }, () -> openNewIssueForm(), () -> openNewScheduleForm())))); }

    private static void initMockData() {
        notifications.add(new Notification("CHORES", "Έχεις εκκρεμή εργασία: Σκούπισμα", "Σκούπισμα", "CHORES", "#D4EDDA"));
        notifications.add(new Notification("BILLS", "Ο λογαριασμός ΔΕΗ λήγει", "ΔΕΗ 45€", "BILLS", "#FFF3CD"));
        unreadCounter = new UnreadCounter((int) notifications.stream().filter(n -> !n.read).count());
        notificationManager = new ManageNotificationsClass(notifications, unreadCounter);
        FinancesScreen.allBills.clear(); 
        FinancesScreen.allBills.add(new Bill("Electricity", 120.50, "2026-05-10", "Giorgos, Alex", "Pending"));
        FinancesScreen.allBills.add(new Bill("Internet", 35.00, "2026-05-01", "All Roommates", "Paid"));
        issues.HomeIssueScreen.allIssues.clear(); 
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