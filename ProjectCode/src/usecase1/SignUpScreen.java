package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
//import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignUpScreen {
	
	
private Stage primaryStage;
	
	public void createWindow() {
        Label label = new Label("Create Account!");

        VBox root = new VBox(5);
        Scene loginScene = new Scene(root, 500, 400);
        HBox hbox = new HBox(5);
        
        primaryStage = new Stage();
        
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("HOMY Sign Up Page");
        
        
        LogInPanel innerPanel = new LogInPanel(primaryStage);

        
        root.setPadding(new Insets(5));
        root.getChildren().addAll(
        		
        		label,
                innerPanel.getCredenInput(),
                innerPanel.getSignUpBtn()
                
        );        
        
        primaryStage.setResizable(false);

        hbox.getChildren().addAll(innerPanel.getSignUpBtn());
        hbox.setAlignment(Pos.CENTER);
        
        root.getChildren().add(hbox);
        root.setAlignment(Pos.CENTER);
        // Show window
        primaryStage.show();
        
    }
}
