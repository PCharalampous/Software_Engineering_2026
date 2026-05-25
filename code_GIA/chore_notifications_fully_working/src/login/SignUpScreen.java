package login;

import java.sql.Connection;

import entities.Authentication;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
//import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.ErrorScreen;

public class SignUpScreen {
	
	
private Stage primaryStage;
	
	private Button createAcc;
	private LogInPanel innerPanel;
	private Connection conn;

	public SignUpScreen(Stage stage, Connection conn) {
		this.primaryStage = stage;
		this.conn = conn;
	}
	
	public void createWindow() {
		createAcc = new Button("Create Account");
        Label label = new Label("Create Account!");

        VBox root = new VBox(5);
        Scene loginScene = new Scene(root, 500, 400);
        HBox hbox = new HBox(5);
        
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("HOMY Sign Up Page");
        
        
        innerPanel = new LogInPanel(primaryStage);
        innerPanel.addToPanelComponentsForSignUp();
        
        root.setPadding(new Insets(5));
        root.getChildren().addAll(label, innerPanel.getCredenInput());        
        
        primaryStage.setResizable(false);

        hbox.getChildren().addAll(createAcc);
        hbox.setAlignment(Pos.CENTER);
        
        root.getChildren().add(hbox);
        root.setAlignment(Pos.CENTER);
        
        buttonStyling();
        buttonsFunctiability();
        // Show window
        primaryStage.show();
        
    }
	
	private void buttonsFunctiability() {
		
		createAcc.setOnAction(e -> {
				Authentication auth = new Authentication(this.conn);
				try {
					auth.createAcc(innerPanel.getUserEmailBuffer(), innerPanel.getUserPasswordBuffer(), 
							innerPanel.getUserNameBuffer(), innerPanel.getDispFirstnameBuffer(), innerPanel.getDispSecondnameBuffer());
				} catch (Exception e1) {
					
					e1.printStackTrace();
					ErrorScreen errScr = new ErrorScreen("Error", "Sign Up Failed");
    		    	errScr.show();
				}
				
				LogInScreen logINsc = new LogInScreen(this.primaryStage);
				logINsc.createWindow();
				
			}
		);
		
	}
	
	private void buttonStyling() {
		createAcc.setStyle(
    		    "-fx-background-radius: 100; " +
    		    "-fx-max-width: 1000px; " +
    		    "-fx-max-height: 1000px; " +
    		    "-fx-font-size: 15px;"
    		);
    }
	
	
	
}
