package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeScreenLeftPanel {
	
	private Label leftBoxLabel;
	private VBox leftBox;
	
	HomeScreenLeftPanel(){
		
        leftBoxLabel = new Label("Not in room yet");
        leftBox = new VBox();
        leftBox.getChildren().add(leftBoxLabel);
		leftBox.setAlignment(Pos.TOP_LEFT);
        leftBox.setPadding(new Insets(10));
        leftBox.setPrefWidth(200);
	}
	
	
	private VBox leftPanelStyling(VBox obj) {
		obj.setStyle(
			"-fx-background-color: #D9D9FF;"+
	        "-fx-border-color: #0000FF;" +
	        "-fx-border-width: 2;"
	    );
		return obj;
	}
	
	public VBox getLeftPanel() {
		
		return leftPanelStyling(this.leftBox);
	}
}
