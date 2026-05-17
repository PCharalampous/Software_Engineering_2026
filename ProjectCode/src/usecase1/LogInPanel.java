package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogInPanel extends GridPane {
    private GridPane credenInput;
    private Label usernameLabel;
    private Label psswdLabel;
    private TextField username;
    private TextField password;
    private Button login, signup;

    @SuppressWarnings("exports")
	public LogInPanel(Stage stage) {
        //body
        credenInput = new GridPane();
        credenInput.setPadding(new Insets(15));
        credenInput.setVgap(10);
        credenInput.setHgap(10);
        usernameLabel = new Label("User Email");
        psswdLabel = new Label("Password");
        username = new TextField();
        password = new TextField();
        login = new Button("log in");
        signup = new Button("sign up");
        credenInput.add(usernameLabel, 0, 0);
        credenInput.add(username, 1, 0);
        credenInput.add(psswdLabel, 0, 1);
        credenInput.add(password, 1, 1);
        credenInput.setAlignment(Pos.CENTER);
    }
    
    
    
    private GridPane credenInputStyling(GridPane obj) {
    	obj.setStyle(
    		    "-fx-padding: 20;" +
    		    "-fx-background-color: #D9D9FF;" +
    		    "-fx-border-color: #0000FF;" +
    		    "-fx-border-width: 2;" +
    		    "-fx-background-radius: 4;" +
    		    "-fx-border-radius: 2;"
    		    
    		);
    	
    	return obj;
    }
    
    @SuppressWarnings("exports")
    public GridPane getCredenInput(){
    	
        return this.credenInputStyling(this.credenInput); 
    }
    
    
	private Button buttonStyling(Button btn) {
		btn.setStyle(
    		    "-fx-background-radius: 100; " +
    		    "-fx-min-width: 100px; " +
    		    "-fx-min-height: 25px; " +
    		    "-fx-max-width: 100px; " +
    		    "-fx-max-height: 50px; " +
    		    "-fx-font-size: 15px;"
    		);
		
		
    	return btn;
    }
	
    @SuppressWarnings("exports")
	public Button getLogInBtn(){
    	
        return this.buttonStyling(this.login);
    }
    
    public Button getSignUpBtn(){
    	
        return this.buttonStyling(this.signup);
    }
    

}
