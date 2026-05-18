package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

public class HomeScreenLeftPanel {
	
	private Label leftBoxLabel, headerLbl;
	private VBox leftBox;
	private Separator separator;
	
	HomeScreenLeftPanel(){
		headerLbl = new Label("USERS SECTION");
		separator = new Separator();
        leftBoxLabel = new Label("Not in room yet");
        leftBox = new VBox(10);
        leftBox.getChildren().addAll(headerLbl ,separator ,leftBoxLabel);
		leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.setPadding(new Insets(10));
        leftBox.setPrefWidth(200);
	}
	
	
	private void leftPanelStyling(VBox obj) {
		obj.setStyle(
			"-fx-background-color: #D9D9FF;"+
	        "-fx-border-color: #0000FF;" +
	        "-fx-border-width: 2;"
	    );
	}
	
	public VBox getLeftPanel() {
		leftPanelStyling(this.leftBox);
		return this.leftBox;
	} 
}
