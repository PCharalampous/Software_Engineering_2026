package usecase1;

import javafx.application.Application;
import javafx.stage.Stage;

public class HOMYapp extends Application{

    
	@SuppressWarnings("exports")
	@Override
    public void start(Stage primaryStage) {
		
		LogInScreen loginScr = new LogInScreen(primaryStage);
		//Authentication authScr = new Authentication();
		SignUpScreen signupScr = new SignUpScreen();
		HomeScreen homeScr = new HomeScreen();
		
		//loginScr.createWindow();
		//signupScr.createWindow();
		homeScr.createWindow();
		//authScr.createWindow();
    }
	
    public static void main(String[] args) {
    	launch(args); //call this only once in the entire program.
    	
    }
    
   


}