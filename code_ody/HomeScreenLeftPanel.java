package search;

import entities.Authentication;
import entities.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.HOMYApp;
import ui.ConfirmationScreen;
import ui.ErrorScreen;

public class HomeScreenLeftPanel {
	
	private Label leftBoxLabel, headerLbl;
	private VBox leftBox, joinRoom;
	private Separator separator;
	private Button joinBtn;
	private TextField roomIdSect;
	
	//tin ekana public
	
	public HomeScreenLeftPanel(Stage homeScreenStage){
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
		roomIdSect = new TextField("Enter room id...");
		joinBtn = new Button("JOIN");
		joinRoom.getChildren().addAll(joinRoomLbl,roomIdSect,joinBtn);
		joinRoom.setAlignment(Pos.CENTER);
		
		//action listener for buttons
		buttonsFunctiability();

		    
		return joinRoom;
	}
	
	private void buttonsFunctiability() {
		
		
		
		joinBtn.setOnAction(e -> {
			
			ConfirmationScreen confirScr = new ConfirmationScreen (
					"Confirm your actions", 
					"You will be transfered to the room",
					"confirm",
					"-fx-background-color: #22C55E; -fx-text-fill: white;"+
					"-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;",
					//---------------------------------------------------------------------
					()->transferToTheRoom()
					//---------------------------------------------------------------------
					
				);
				
				confirScr.show();
			
			
			
			
		});
	}
	
	//---------------------------------------------------------------------
	private void transferToTheRoom() {
		
		User sessionUser = Authentication.getCurrentUser();
	    String input = roomIdSect.getText().trim();
	    
	    if (input.isEmpty()) {
	    	roomIdSect.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: red; -fx-padding: 8;");
	        System.err.println("Room ID field is empty!");
	        return;
	    }
	    
	    try {
	        int targetRoomId = Integer.parseInt(input);
	        
	        
			// Invoke the instant join method using your ManageProfileClass manager instance
	        boolean joinedSuccessfully = sessionUser.joinRoomInstantly(targetRoomId);
	        
	        if (!joinedSuccessfully) {
	            // Visual feedback: turn the text field border red if full or invalid
	        	joinRoom.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #EF4444; -fx-padding: 8;");
	            
	            // Optional: You can instantiate a custom dialog window here to notify: "Room is full!"
	            ErrorScreen errScr = new ErrorScreen("Error", "Could not join room. It might be full or invalid");
		    	errScr.show();
	        }
	        
	    } catch (NumberFormatException ex) {
	        // Handle case where user types non-numeric characters
	    	roomIdSect.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: red; -fx-padding: 8;");
	    	ErrorScreen errScr = new ErrorScreen("Error", "Not room found with that room id");
	    	errScr.show();
	        
	    }
	
	
	}
	//---------------------------------------------------------------------
	
	
	private void leftPanelStyling() {
		leftBox.setStyle(
			"-fx-background-color: #D9D9FF;"+
	        "-fx-border-color: #0000FF;" +
	        "-fx-border-width: 2;"
	    );
		
		joinRoom.setStyle(
    			"-fx-background-color: white;" +
    			"-fx-border-color: #0000FF;" +
    			"-fx-border-width: 1;" +
    			"-fx-border-radius: 12;" +
    			"-fx-background-radius: 12;" +
    			"-fx-padding: 20;"
    		);
		
		joinBtn.setStyle(
			"-fx-background-radius: 20;" +
		    "-fx-background-color: #0000FF;" +
		    "-fx-text-fill: white;"
		);
	}
	
	public VBox getLeftPanel() {
		leftPanelStyling();
		return this.leftBox;
	} 
}
