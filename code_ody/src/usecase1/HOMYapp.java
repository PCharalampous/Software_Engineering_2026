package usecase1;

import java.sql.Connection;

import javafx.application.Application;
import javafx.stage.Stage;

public class HOMYapp extends Application{

    
	@SuppressWarnings("exports")
	@Override
    public void start(Stage primaryStage) {
		
		LogInScreen loginScr = new LogInScreen(primaryStage);
		//Authentication authScr = new Authentication();
//		SignUpScreen signupScr = new SignUpScreen();
//		HomeScreen homeScr = new HomeScreen();
		
		loginScr.createWindow();
//		signupScr.createWindow();
//		homeScr.createWindow();
		//authScr.createWindow();
		
    }
	
    public static void main(String[] args) {
    	
    	 System.out.println("Έναρξη δοκιμής σύνδεσης απευθείας από τον DatabaseManager...");
    	 DatabaseManager dataB = new DatabaseManager();
         
         Connection conn = dataB.getConnection();
         
         if (conn != null) {
             System.out.println("Η σύνδεση με το Clever Cloud πέτυχε.");
             dataB.showTables();
             launch(args); //call this only once in the entire program.
             
             dataB.closeConnection();
         } else {
             System.err.println("Αποτυχία σύνδεσης! Σιγουρέψου ότι το αρχείο config.properties βρίσκεται στον φάκελο src.");
         }
    }
    
   


}