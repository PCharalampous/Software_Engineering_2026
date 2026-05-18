package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeScreenLeftPanel {
	
	private Label leftBoxLabel, headerLbl;
	private VBox leftBox, joinRoom;
	private Separator separator;
	
	HomeScreenLeftPanel(){
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
		Button joinBtn = new Button("JOIN");
//		Separator joinRoomSeparator = new Separator();
		joinRoom.getChildren().addAll(joinRoomLbl,roomIdSect,joinBtn);
		joinRoom.setAlignment(Pos.CENTER);

		return joinRoom;
	}
	
	private void leftPanelStyling(VBox obj, VBox joinObj) {
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
	}
	
	public VBox getLeftPanel() {
		leftPanelStyling(this.leftBox, this.joinRoom);
		return this.leftBox;
	} 
}
