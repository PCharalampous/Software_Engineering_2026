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

public class HomeScreenRightPanel {
	
	private Button profileButton, logoutBtn;
	private VBox rightBox;
	private Separator separator;
	private Label headerLbl;
	private Region spacer;
	private Button[] btnArr;
	
	HomeScreenRightPanel(){
		headerLbl = new Label("PROFILE SECTION");
		separator = new Separator();
		profileButton = new Button("Profile");
		
		profileButton.setPrefSize(100, 50);
		profileButton.setStyle("-fx-background-radius: 100;" +"-fx-font-size: 20px;");
		
		logoutBtn = new Button("Log Out");
		btnArr = new Button[] {profileButton,logoutBtn};
		
		spacer = new Region();
		
        rightBox = new VBox(10);
        rightBox.getChildren().addAll(headerLbl,separator,btnArr[0],spacer ,btnArr[1]);
                
        rightBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(spacer, Priority.ALWAYS);
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
		        "-fx-background-color: #0000FF;" +
		        "-fx-text-fill: white;"
		    );
		}
		
		
	}
	
	public VBox getRightPanel() {
		rightPanelStyling(rightBox, btnArr);
		return this.rightBox;
		
	}
}
