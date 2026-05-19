package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeScreenRightPanel {
	
	private Button profileButton, logoutBtn;
	private VBox rightBox;
	private Separator separator;
	private Label headerLbl;
	private Region spacer;
//	private Button[] btnArr;
	private TextField userNameField;
	private String userName;
	private Stage homeScrStage;
	
	HomeScreenRightPanel(Stage homeScreenStage){
		this.homeScrStage = homeScreenStage;
		headerLbl = new Label("PROFILE SECTION");
		separator = new Separator();
		profileButton = new Button("Profile");
		
		//for testing purposes: (delete after)
		userName = "Makis";
		userNameField = new TextField(userName);
		
		logoutBtn = new Button("Log Out");
//		btnArr = new Button[] {profileButton,logoutBtn};
		
		spacer = new Region();
		
        rightBox = new VBox(10);
        rightBox.getChildren().addAll(headerLbl ,separator ,profileButton ,userNameField ,spacer ,logoutBtn);
                
        rightBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        rightBox.setPadding(new Insets(10));

        rightBox.setPrefWidth(200);
        
        
	}
	
	private void rightPanelStyling() {
		int i=0;
		rightBox.setStyle(
	       "-fx-background-color: #D9D9FF;"+
	       "-fx-border-color: #0000FF;" +
	       "-fx-border-width: 2;"
	    );
		
		logoutBtn.setStyle(
			"-fx-background-radius: 20;" +
		    "-fx-background-color: #0000FF;" +
		    "-fx-text-fill: white;"
		);
		
		
		profileButton.setStyle(
			"-fx-background-color: #f8fbff;" +
			"-fx-background-radius: 12;" +
			"-fx-border-radius: 12;" +
			"-fx-border-color: #93c5fd;" +
			"-fx-border-width: 2;" +
			"-fx-font-size: 15px;" +
			"-fx-font-weight: bold;" +
			"-fx-text-fill: #2563eb;" +
		   	"-fx-alignment: center;" +
			"-fx-padding: 8;"+
			"-fx-pref-width: 300px;" +
			"-fx-pref-height: 40px;"
		);
		
		
		userNameField.setStyle(
			"-fx-background-color: #D9D9FF;" +
		    "-fx-border-color: transparent;" +
		    "-fx-font-size: 15px;" +
		    "-fx-font-weight: bold;" +
		    "-fx-text-fill: #2563eb;" +
	   	    "-fx-alignment: center;" +
		    "-fx-pref-width: 300px;" +
			"-fx-pref-height: 40px;"
		);
		
		userNameField.setEditable(false);
		
	}
	
	private void buttonsFunctiability() {
		
		
		this.logoutBtn.setOnAction(e -> {
				LogInScreen logInScr = new LogInScreen(homeScrStage);
				logInScr.createWindow();
				
			}
		);
	}
	
	public VBox getRightPanel() {
		rightPanelStyling();
		buttonsFunctiability();
		return this.rightBox;
		
	}
}
