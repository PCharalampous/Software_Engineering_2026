package usecase1;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HomeScreen {
	
private Stage homeScrStage;
	
	public void createWindow() {
        
		//border pane config
		BorderPane root = new BorderPane();
		
        root.setPadding(new Insets(10));
        
        // LEFT SECTION
        
        HomeScreenLeftPanel leftSection = new HomeScreenLeftPanel();
        
        root.setLeft(leftSection.getLeftPanel());
        
        
        // CENTER SECTION
        HomeScreenMiddlePanel middleSection = new HomeScreenMiddlePanel();
        root.setCenter(middleSection.getMiddlePanel());
        
        // RIGHT SECTION
        
        HomeScreenRightPanel rightSection = new HomeScreenRightPanel();
        
        root.setRight(rightSection.getRightPanel());
        
        //scene config
        Scene homeScene = new Scene(root, 1500, 800);
        homeScrStage = new Stage();
        homeScrStage.setScene(homeScene);
        homeScrStage.setTitle("HOMY-Home Screen");
        
        homeScrStage.setResizable(false);
        homeScrStage.show();
        
    }

	
}
