package login;


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
        Scene loginScene = new Scene(root, 500, 400);
        HBox hbox = new HBox(5);
        
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
