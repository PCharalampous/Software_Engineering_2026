import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class PointScreen extends VBox {
    private final Point pointModel;
    private final String currentUser = "Manos";
    private VBox leaderboardList;
    private VBox redeemedList;

    public PointScreen(Point pointModel) {
        this.pointModel = pointModel;
        this.setSpacing(20);
        this.setPadding(new Insets(20));
        this.setPrefWidth(300);
        this.setStyle("-fx-background-color: #F1F5F9;");
        
        buildSections();
    }

    private void buildSections() {
        // Leaderboard Box
        VBox lbBox = new VBox(10);
        lbBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
        Label lbHeader = new Label("🏆 LEADERBOARD");
        lbHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        leaderboardList = new VBox(8);
        
        // Redeemed Box
        VBox rbBox = new VBox(10);
        rbBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 15;");
        Label rbHeader = new Label("🎁 REDEEMED");
        rbHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        redeemedList = new VBox(8);

        updateUI();
        lbBox.getChildren().addAll(lbHeader, leaderboardList);
        rbBox.getChildren().addAll(rbHeader, redeemedList);
        this.getChildren().addAll(lbBox, rbBox);
    }

    public void updateUI() {
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

    // --- Application Entry Point ---
    public static class Launcher extends Application {
        @Override
        public void start(Stage primaryStage) {
            Point pointModel = new Point(null, 0, null); 
            PointScreen sidebar = new PointScreen(pointModel);
            RewardScreen shop = new RewardScreen(sidebar);
            
            HBox dashboard = new HBox(sidebar, shop);
            dashboard.setStyle("-fx-background-color: #F8FAF9;");

            Scene scene = new Scene(dashboard, 950, 600);
            primaryStage.setTitle("HOMY - Points Dashboard");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public static void main(String[] args) {
        Application.launch(Launcher.class, args);
    }
}