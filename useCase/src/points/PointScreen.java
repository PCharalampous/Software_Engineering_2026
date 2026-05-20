package points;

import entities.Point;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class PointScreen extends VBox {
    private final Point pointModel;
    private final String currentUser = "Manos";
    private VBox leaderboardList;
    private VBox redeemedList;
    
    private Stage primaryStage;
    private Runnable backAction; // <--- ΠΡΟΣΘΕΣΗ ΓΙΑ ΤΗΝ ΕΠΙΣΤΡΟΦΗ ΣΤΟ HUB

    // --- Constructor 1: Ο νέος που συνδέεται με το HOMYApp Hub ---
    public PointScreen(Point pointModel, Runnable backAction) {
        this.pointModel = pointModel;
        this.backAction = backAction;
        
        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.setPrefWidth(320); // Ελαφρώς αυξημένο για να χωράει άνετα το back button
        this.setStyle("-fx-background-color: #F1F5F9;");
        
        buildSections();
    }

    // --- Constructor 2: Fallback για συμβατότητα ---
    public PointScreen(Point pointModel) {
        this(pointModel, null);
    }

    // Μέθοδος για να περάσουμε το Stage από τον εξωτερικό Launcher
    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void buildSections() {
        // --- Header με κουμπί επιστροφής Back ---
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Button backBtn = new Button("←");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E3A5F; -fx-cursor: hand; -fx-padding: 0 5 0 0;");
        backBtn.setOnAction(e -> {
            if (primaryStage != null) {
                primaryStage.close(); // Κλείνει το τρέχον παράθυρο των Points
            }
            if (backAction != null) {
                backAction.run(); // Ξαναεμφανίζει το Central Hub
            }
        });
        
        Label mainTitle = new Label("Points Center");
        mainTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        headerBox.getChildren().addAll(backBtn, mainTitle);
        this.getChildren().add(headerBox);

        // Leaderboard Box
        VBox lbBox = new VBox(10);
        lbBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
        Label lbHeader = new Label("🏆 LEADERBOARD");
        lbHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        leaderboardList = new VBox(8);
        lbBox.getChildren().addAll(lbHeader, leaderboardList);
        
        // Redeemed Box
        VBox rbBox = new VBox(10);
        rbBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
        Label rbHeader = new Label("🎁 REDEEMED");
        rbHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        redeemedList = new VBox(8);
        rbBox.getChildren().addAll(rbHeader, redeemedList);

        updateUI();
        this.getChildren().addAll(lbBox, rbBox);
    }

    public void updateUI() {
        if (leaderboardList == null) return;
        
        leaderboardList.getChildren().clear();
        leaderboardList.getChildren().addAll(
            createRow("1. Makis", pointModel.getBalance("Makis") + " pts"),
            createRow("2. Giannis", pointModel.getBalance("Giannis") + " pts"),
            createRow("3. " + currentUser, pointModel.getBalance(currentUser) + " pts")
        );
    }

    private HBox createRow(String name, String pts) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().addAll(new Circle(12, Color.web("#CBD5E1")), new Label(name), new Pane(), new Label(pts));
        HBox.setHgrow(row.getChildren().get(2), Priority.ALWAYS);
        return row;
    }

    public void addRedeemed(String name) {
        Label l = new Label("✔ " + name);
        l.setStyle("-fx-font-size: 11; -fx-text-fill: #10B981; -fx-font-weight: bold;");
        redeemedList.getChildren().add(0, l);
    }

    public Point getPointModel() { return pointModel; }
    public String getCurrentUser() { return currentUser; }

    // --- Ο ΝΕΟΣ ΚΑΘΑΡΟΣ LAUNCHER (Χωρίς extends Application, εναρμονισμένος με το Hub) ---
    public static class Launcher {
        public void start(Stage primaryStage) {
            Point pointModel = new Point(null, 0, null); 
            
            // Δημιουργούμε το PointScreen περνώντας το Runnable για το back action
            PointScreen sidebar = new PointScreen(pointModel, () -> primaryStage.close());
            sidebar.setStage(primaryStage); // Συνδέουμε το stage
            
            RewardScreen shop = new RewardScreen(sidebar);
            
            HBox dashboard = new HBox(sidebar, shop);
            dashboard.setStyle("-fx-background-color: #F8FAF9;");

            Scene scene = new Scene(dashboard, 950, 600);
            primaryStage.setTitle("HOMY - Points Dashboard");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        }
    }
}