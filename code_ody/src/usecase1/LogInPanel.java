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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

public class LogInPanel extends GridPane {
    private GridPane credenInput;
    private Label useremailLabel;
    private Label psswdLabel;
    private TextField useremail,dispFirstnameTxt, dispSecondnameTxt, userNameTxt;
    private TextField password;
    private Button login, signup;
    private Stage stage;
    private String userEmailBuffer;
    private String userPassBuffer;
    private Connection conn;
    private Label dispFirstnameLabel;
    private Label dispSecondnameLabel;
    private Label usernameLabel;
//    private Boolean add;
    
    @SuppressWarnings("exports")
	public LogInPanel(Stage stage) {
        //body
//    	this.add= false;
    	this.stage = stage;
        credenInput = new GridPane();
        credenInput.setPadding(new Insets(15));
        credenInput.setVgap(10);
        credenInput.setHgap(10);
        useremailLabel = new Label("User Email");
        psswdLabel = new Label("Password");
        useremail = new TextField();
        password = new TextField();
        login = new Button("log in");
        signup = new Button("sign up");
        //
        
        dispFirstnameLabel = new Label("First Name:");
        dispSecondnameLabel = new Label("Second Name:");
        usernameLabel = new Label("User Name:");
        dispFirstnameTxt = new TextField();
        dispSecondnameTxt = new TextField();
        userNameTxt = new TextField();
        
        //
        
        credenInput.add(useremailLabel, 0, 0);
        credenInput.add(useremail, 1, 0);
        credenInput.add(psswdLabel, 0, 1);
        credenInput.add(password, 1, 1);
        credenInput.setAlignment(Pos.CENTER);
    }
    
    public void addToPanelComponentsForSignUp() {
    	credenInput.getChildren().clear();

        credenInput.add(dispFirstnameLabel, 0, 0);
        credenInput.add(dispFirstnameTxt, 1, 0);

        credenInput.add(dispSecondnameLabel, 0, 1);
        credenInput.add(dispSecondnameTxt, 1, 1);

        credenInput.add(usernameLabel, 0, 2);
        credenInput.add(userNameTxt, 1, 2);

        credenInput.add(useremailLabel, 0, 3);
        credenInput.add(useremail, 1, 3);

        credenInput.add(psswdLabel, 0, 4);
        credenInput.add(password, 1, 4);
        
      //functiability for sign up extra components
	     = .getText();
	     = .getText();
	     = .getText();
    }
    
    
    public void setDataBaseConnection(Connection conn) {
    	this.conn = conn;
    }
    
    private void panelFunctiability() {
    	Authentication auth = new Authentication(this.conn);
    	
    	login.setOnAction(e -> {
    			userEmailBuffer = useremail.getText();

    		    userPassBuffer = password.getText();
    		    System.out.println("from login gui "+userEmailBuffer+": " + userPassBuffer);
    		    
//    		    auth.setUserEmail(userEmailBuffer);
//    		    auth.setUserPassword(userPassBuffer);
    		    
    		    try {
					if(auth.userLogIn(userEmailBuffer,userPassBuffer) == true) {
						HomeScreen homeScr = new HomeScreen(this.stage);
						homeScr.createWindow();
					}
					
					else {
						ErrorScreen errScr = new ErrorScreen("Log In Failed" , 
								"Ensure that the credentials are correct, otherwise create account");
						errScr.show();
					}
				} catch (SQLException e1) {
					ErrorScreen errScr = new ErrorScreen("Log In Failed" , 
							"Database couldnt respond properly");
					errScr.show();
					e1.printStackTrace();
				}
    		    
				
			}
		);
		
		signup.setOnAction(e -> {
				
				SignUpScreen signUpScr = new SignUpScreen(this.stage, this.conn);
				signUpScr.createWindow();
			}
		);
	}
    
    public String getUserEmail() {
		return this.userEmailBuffer; 
	}
	
	public String getUserPassword() {
		return this.userPassBuffer ;
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
    	this.panelFunctiability();
        return this.buttonStyling(this.login);
    }
    
    public Button getSignUpBtn(){
    	this.panelFunctiability();
        return this.buttonStyling(this.signup);
    }
    

}
