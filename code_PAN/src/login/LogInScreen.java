package login;


import javafx.scene.image.Image;

import java.sql.Connection;

import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;


public class LogInScreen {
	
	private Stage logINstage;
	private Connection conn;
	
	public LogInScreen(Stage stage){
		this.logINstage = stage;
	}
	
	public void setDataBaseConnection(Connection conn) {
    	this.conn = conn;
    }
	
	public void createWindow(){
		
		Label label = new Label("Log In!");
        VBox root = new VBox(5);
        Scene loginScene = new Scene(root, 600, 500);
        HBox hbox = new HBox(5);
        try {
            // 1. Load the image from the classpath
            Image img = new Image(getClass().getResourceAsStream("logoHOMY.png"));
            
            // 2. Wrap it inside an ImageView node
            ImageView imageView = new ImageView(img);
            
            // 3. Optional: Resize the image while maintaining its aspect ratio
            imageView.setFitWidth(200);  // Set target width in pixels
            imageView.setPreserveRatio(true); // Prevent stretching
            imageView.setSmooth(true);        // Improves scaling quality
            
            // 4. Add it to your HBox alongside your other nodes
            root.getChildren().add(imageView);
            
        } catch (NullPointerException e) {
            System.err.println("Error: Could not find the image file! Check your file path.");
        }
        
        logINstage.setScene(loginScene);
        logINstage.setTitle("HOMY Login Page");
        
        
        LogInPanel innerPanel = new LogInPanel(logINstage);
        innerPanel.setDataBaseConnection(this.conn);
        
        root.setPadding(new Insets(5));
        root.getChildren().addAll(	
        	label,
            innerPanel.getCredenInput(),
            innerPanel.getLogInBtn(),
            innerPanel.getSignUpBtn()
        );        
        
        logINstage.setResizable(false);
        
        

        hbox.getChildren().addAll(innerPanel.getLogInBtn(), innerPanel.getSignUpBtn());
        hbox.setAlignment(Pos.CENTER);
        
        root.getChildren().add(hbox);
        root.setAlignment(Pos.CENTER);
        // Show window
        logINstage.show();
    }
	
	
	
}
