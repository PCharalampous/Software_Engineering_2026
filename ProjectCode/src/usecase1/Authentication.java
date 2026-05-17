package usecase1;


import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Authentication  {
	
	private Stage primaryStage;
	
	public void createWindow() {
        Label label = new Label("Hello!");

        // Layout container
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Create scene (width, height)
        Scene scene = new Scene(root, 300, 200);
        
        primaryStage = new Stage();
        // window title
        
        primaryStage.setTitle("HOMY authentication");

        // Set scene
        primaryStage.setScene(scene);
        
        // Show window
        primaryStage.show();
        
    }
}
