package usecase1;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


public class LogInScreen {
	
	private Stage logINstage;
	
	LogInScreen(Stage stage){
		this.logINstage = stage;
	}
	
	public void createWindow(){
		
        VBox root = new VBox(5);
        Scene loginScene = new Scene(root, 500, 500);
        
        logINstage.setScene(loginScene);
        logINstage.setTitle("Login Page");
        
        
        LogInPanel innerPanel = new LogInPanel(logINstage);

        
        root.setPadding(new Insets(5));
        root.getChildren().addAll(
                innerPanel.getBody(),
                innerPanel.getLogInBtn()
        );        
        root.setAlignment(Pos.CENTER);
        // Show window
        logINstage.show();

        
        
       //-0-------------------------------------------
      
        
        

//        register.setOnMouseClicked(event -> {
//            RegPane regPane = new RegPane(stage,loginScene);
//            VBox reg = new VBox(5);
//            reg.setPadding(new Insets(5));
//            info.setText("Registration page");
//            reg.getChildren().addAll(
//                    info,
//                    regPane.getBody(),
//                    regPane.getRegister()
//                    );
//            reg.setAlignment(Pos.CENTER);
//            Scene registrationScene =  new Scene(reg,300,200);
//            primaryStage.setScene(registrationScene);
//            primaryStage.setTitle("Registration Page");
//
//        });

        
    }
	
}
