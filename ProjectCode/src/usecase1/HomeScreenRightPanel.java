package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class HomeScreenRightPanel {
	
	private Button profileButton, logoutBtn;
	private VBox rightBox;
	private Separator separator;
	private Label headerLbl;
	
	HomeScreenRightPanel(){
		headerLbl = new Label("PROFILE SECTION");
		separator = new Separator();
		profileButton = new Button("Profile");
		logoutBtn = new Button("Log Out");

        rightBox = new VBox(10);
        rightBox.getChildren().addAll(headerLbl,separator,profileButton,logoutBtn);

        rightBox.setAlignment(Pos.TOP_CENTER);
        
        rightBox.setPadding(new Insets(10));

        rightBox.setPrefWidth(200);
        
        
        
	}
	
	private void rightPanelStyling(VBox vbx, Button[] btn) {
		int i=0;
		vbx.setStyle(
	        	"-fx-background-color: #D9D9FF;"+
	        	"-fx-border-color: #0000FF;" +
	        	"-fx-border-width: 2;"
	        );
		for (i=0; i<btn.length; i++) {
			btn[i].setStyle(
		            "-fx-background-radius: 20;" +
		            "-fx-background-color: #6A6A80;" +
		            "-fx-text-fill: white;"
		        );
		}
		
		
	}
	
	public VBox getRightPanel() {
		rightPanelStyling(rightBox, new Button[] {profileButton,logoutBtn});
		return this.rightBox;
		
	}
}
