import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class ProfileScreen {
    private static Stage primaryStage;
    private static ObservableList<Application> database = FXCollections.observableArrayList();

    public static class FXBootstrap extends javafx.application.Application {
        public void start(Stage stage) {
            ProfileScreen.primaryStage = stage;
            ProfileScreen.primaryStage.setTitle("HOMY - MyApplications");
            
            new ProfileScreen().display();
            ProfileScreen.primaryStage.show();
        }
    }

    public void display() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #F8FAFC;");

        Label titleLabel = new Label("PROFILE");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        Circle avatarCircle = new Circle(45, Color.web("#6366F1"));
        Label avatarText = new Label("MK");
        avatarText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane avatarPane = new StackPane(avatarCircle, avatarText);

        Label nameLabel = new Label("Makis Kosta");
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Button myAppsBtn = new Button("MY APPLICATIONS");
        myAppsBtn.setMaxWidth(Double.MAX_VALUE);
        myAppsBtn.setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-weight: bold;");
        myAppsBtn.setOnAction(e -> selectMyApplications());

        root.getChildren().addAll(titleLabel, avatarPane, nameLabel, myAppsBtn);
        primaryStage.setScene(new Scene(root, 420, 750));
    }

    public void selectMyApplications() {
        MyApplicationScreen myApplicationsScreen = new MyApplicationScreen();
        myApplicationsScreen.display();
    }

    public static Stage getStage() { return primaryStage; }
    public static ObservableList<Application> getDatabase() { return database; }

    public static void main(String[] args) {
        database.add(new Application("Flat Κυψέλη - 2 δωμάτια", "Κυψέλη", "Ιθάκης 12", 350, 3, "Φοιτητής, λάτρης καφέ...", "PENDING"));
        database.add(new Application("Studio Εξάρχεια", "Εξάρχεια", "Σολωμού 45", 280, 2, "Ήσυχο περιβάλλον", "ACCEPTED"));
        
        database.add(new Application("Δωμάτιο Παγκράτι", "Παγκράτι", "Φιλολάου 89", 210, 1, "Κοντά σε μετρό", "DECLINED"));

        javafx.application.Application.launch(FXBootstrap.class, args);
    }
}