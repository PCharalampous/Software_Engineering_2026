package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class HomeScreenRightPanel {
	
	private Button profileButton;
	private VBox rightBox;
	
	HomeScreenRightPanel(){
		profileButton = new Button("Profile");

        rightBox = new VBox(profileButton);

        rightBox.setAlignment(Pos.TOP_RIGHT);

        rightBox.setPadding(new Insets(10));

        rightBox.setPrefWidth(200);
        
        
	}
	
	private void rightPanelStyling(VBox vbx, Button btn) {
		vbx.setStyle(
	        	"-fx-background-color: #D9D9FF;"+
	        	"-fx-border-color: #0000FF;" +
	        	"-fx-border-width: 2;"
	        );
		
		btn.setStyle(
	            "-fx-background-radius: 20;" +
	            "-fx-background-color: #6A6A80;" +
	            "-fx-text-fill: white;"
	        );
		
	}
	
	public VBox getRightPanel() {
		rightPanelStyling(rightBox, profileButton);
		return this.rightBox;
		
	}
}
