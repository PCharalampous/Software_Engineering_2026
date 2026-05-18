package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeScreenLeftPanel {
	
	private Label leftBoxLabel, headerLbl;
	private VBox leftBox, joinRoom;
	private Separator separator;
	private Button joinBtn;
	
	HomeScreenLeftPanel(Stage homeScreenStage){
		headerLbl = new Label("USERS SECTION");
		separator = new Separator();
        leftBoxLabel = new Label("Not in room yet");
        leftBox = new VBox(10);
        
        joinRoom = joinRoomComponent();
        leftBox.getChildren().addAll(headerLbl ,separator ,leftBoxLabel,joinRoom);
        
		leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.setPadding(new Insets(10));
        leftBox.setPrefWidth(200);
	}
	
	
	private VBox joinRoomComponent() {
		VBox joinRoom = new VBox(10);
		Label joinRoomLbl = new Label("JOIN ROOM");
		TextField roomIdSect = new TextField("Enter room id...");
		joinBtn = new Button("JOIN");
		joinRoom.getChildren().addAll(joinRoomLbl,roomIdSect,joinBtn);
		joinRoom.setAlignment(Pos.CENTER);
		
		//action listener for buttons
		joinBtn.setOnAction(e -> {
			
			ConfirmationScreen confirScr = new ConfirmationScreen (
				"Confirm your actions", 
				"You will be transfered to the room",
				"confirm",
				"-fx-background-color: #22C55E; -fx-text-fill: white;"+
				"-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;",
				()->System.out.println("button clicked")
				
			);
			
			confirScr.show();
		});

		    
		return joinRoom;
	}
	
	private void leftPanelStyling(VBox obj, VBox joinObj, Button btn) {
		obj.setStyle(
			"-fx-background-color: #D9D9FF;"+
	        "-fx-border-color: #0000FF;" +
	        "-fx-border-width: 2;"
	    );
		
		joinObj.setStyle(
			"-fx-background-color: white;" +
			"-fx-border-color: #0000FF;" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;" +
			"-fx-padding: 20;"
		);
		
		btn.setStyle(
			"-fx-background-radius: 20;" +
		    "-fx-background-color: #0000FF;" +
		    "-fx-text-fill: white;"
		);
	}
	
	public VBox getLeftPanel() {
		leftPanelStyling(this.leftBox, this.joinRoom, this.joinBtn);
		return this.leftBox;
	} 
}
