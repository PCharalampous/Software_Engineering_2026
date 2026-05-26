package login;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import search.HomeScreen; // Εισαγωγή του HomeScreen σου
import ui.ErrorScreen;

import java.sql.Connection;
import java.sql.SQLException;

import entities.Authentication;
import entities.User;
import util.DatabaseManager;

public class LogInPanel extends GridPane {
    private GridPane credenInput;
    private Label useremailLabel;
    private Label psswdLabel;
    private TextField useremail, dispFirstnameTxt, dispSecondnameTxt, userNameTxt;
    private PasswordField password;
    private Button login, signup;
    private Stage stage;
    private String userEmailBuffer;
    private String userPassBuffer;
    private Connection conn;
    private Label dispFirstnameLabel;
    private Label dispSecondnameLabel;
    private Label usernameLabel;
    
    private String dispFirstnameTxtBuffer, dispSecondnameTxtBuffer, userNameTxtBuffer;
    private TextField userSignUpemail;
    private PasswordField passwordSignUp;

    @SuppressWarnings("exports")
    public LogInPanel(Stage stage) {
        this.stage = stage;
        credenInput = new GridPane();
        credenInput.setPadding(new Insets(15));
        credenInput.setVgap(10);
        credenInput.setHgap(10);
        useremailLabel = new Label("User Email");
        psswdLabel = new Label("Password");
        useremail = new TextField();
        password = new PasswordField();
        login = new Button("log in");
        signup = new Button("sign up");
        
        dispFirstnameLabel = new Label("First Name:");
        dispSecondnameLabel = new Label("Second Name:");
        usernameLabel = new Label("User Name:");
        dispFirstnameTxt = new TextField();
        dispSecondnameTxt = new TextField();
        userNameTxt = new TextField();
        userSignUpemail = new TextField();
        passwordSignUp = new PasswordField();
        
        credenInput.add(useremailLabel, 0, 0);
        credenInput.add(blockSpaces(useremail), 1, 0);
        credenInput.add(psswdLabel, 0, 1);
        credenInput.add(blockSpaces(password), 1, 1);
        credenInput.setAlignment(Pos.CENTER);
    }
    
    public void addToPanelComponentsForSignUp() {
        credenInput.getChildren().clear();

        credenInput.add(dispFirstnameLabel, 0, 0);
        credenInput.add(blockSpaces(dispFirstnameTxt), 1, 0);

        credenInput.add(dispSecondnameLabel, 0, 1);
        credenInput.add(blockSpaces(dispSecondnameTxt), 1, 1);

        credenInput.add(usernameLabel, 0, 2);
        credenInput.add(blockSpaces(userNameTxt), 1, 2);

        credenInput.add(useremailLabel, 0, 3);
        credenInput.add(blockSpaces(userSignUpemail), 1, 3);

        credenInput.add(psswdLabel, 0, 4);
        credenInput.add(blockSpaces(passwordSignUp), 1, 4);
        credenInput.setAlignment(Pos.CENTER);  
    }
    
    private TextField blockSpaces(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume();
            }
        });
        return textField;
    }
    
    public void setDataBaseConnection(Connection conn) {
        this.conn = conn;
    }
    
    private void panelFunctiability() {
        login.setOnAction(e -> {
                // Ανανέωση της static σύνδεσης τη στιγμή του κλικ
                this.conn = DatabaseManager.getConnection();
                Authentication auth = new Authentication(this.conn);
                
                System.out.println("from login gui " + useremail.getText() + ": " + password.getText());
                
                try {
                	// ... Μέσα στο event φιλτραρίσματος/κουμπιού του Log In:
                	if (auth.userLogIn(useremail.getText(), password.getText())) {
                	    User loggedInUser = Authentication.getCurrentUser();
                	    
                	    if (loggedInUser != null && loggedInUser.getRoomId() > 0) {
                	        // Αν ο χρήστης έχει ήδη δωμάτιο (είτε επειδή ήταν Owner είτε επειδή έγινε Accept)
                	        System.out.println("Ο χρήστης ανήκει σε δωμάτιο. Ανακατεύθυνση στο Central Hub...");
                	        main.HOMYApp.showCentralHub(); 
                	        
                	    } else {
                	        // Αν δεν έχει δωμάτιο, τον πάει στην αρχική οθόνη αναζήτησης/δημιουργίας
                	        HomeScreen homeScr = new HomeScreen(this.stage, this.conn);
                	        
                	        homeScr.createWindow();
                	    }
                	}
                	else {
                		 ErrorScreen errScr = new ErrorScreen("Log In Failed", 
                                 "Your credentials are not correct make sure you have an accound otherwise create account choosing sign up");
                         errScr.show();
                	}
                		
                } catch (SQLException e1) {
                	ErrorScreen errScr = new ErrorScreen("Log In Failed", 
                            "Database couldnt respond properly");
                    errScr.show();
                    e1.printStackTrace();
                }
            }
        );
        
        signup.setOnAction(e -> {
                this.conn = DatabaseManager.getConnection();
                SignUpScreen signUpScr = new SignUpScreen(this.stage, this.conn);
                signUpScr.createWindow();
            }
        );
    }
    
    public String getUserEmailBuffer() { return this.userEmailBuffer = userSignUpemail.getText(); }
    public String getUserPasswordBuffer() { return this.userPassBuffer = passwordSignUp.getText(); }
    public String getDispFirstnameBuffer() { return this.dispFirstnameTxtBuffer = dispFirstnameTxt.getText(); }
    public String getDispSecondnameBuffer() { return this.dispSecondnameTxtBuffer = dispSecondnameTxt.getText(); }
    public String getUserNameBuffer() { return this.userNameTxtBuffer = userNameTxt.getText(); }
    
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
