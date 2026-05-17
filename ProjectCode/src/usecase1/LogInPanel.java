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
    private GridPane body;
    private Label usernameLabel;
    private Label psswdLabel;
    private TextField username;
    private TextField password;
    private Button login, signup;

    @SuppressWarnings("exports")
	public LogInPanel(Stage stage) {
        //body
        body = new GridPane();
        body.setPadding(new Insets(15));
        body.setVgap(10);
        body.setHgap(10);
        usernameLabel = new Label("User Name");
        psswdLabel = new Label("Password");
        username = new TextField();
        password = new TextField();
        login = new Button("log in");
        signup = new Button("sign up");
        body.add(usernameLabel, 0, 0);
        body.add(username, 1, 0);
        body.add(psswdLabel, 0, 1);
        body.add(password, 1, 1);
        body.setAlignment(Pos.CENTER);
    }
    
    @SuppressWarnings("exports")
	public GridPane getBody(){
        return this.body;
    }
    
    @SuppressWarnings("exports")
	public Button getLogInBtn(){
        return this.login;
    }
    
    public Button getSignUpBtn(){
        return this.signup;
    }
    

}
