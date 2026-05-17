package usecase1;


import javafx.application.Application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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
		
		Label label = new Label("Log In!");
        VBox root = new VBox(5);
        Scene loginScene = new Scene(root, 500, 400);
        HBox hbox = new HBox(5);
        
        logINstage.setScene(loginScene);
        logINstage.setTitle("HOMY Login Page");
        
        
        LogInPanel innerPanel = new LogInPanel(logINstage);

        
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
