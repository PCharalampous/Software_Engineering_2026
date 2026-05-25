package search;

import java.sql.Connection;

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
	
	private Connection conn;
	@SuppressWarnings("exports")
	public HomeScreen(Stage stage, Connection conn) {
		this.homeScrStage = stage;
		this.conn = conn;
	}
	
	public void createWindow() {
		//border pane config
		BorderPane root = new BorderPane();
		
        root.setPadding(new Insets(10));
        
        // LEFT SECTION
        
        HomeScreenLeftPanel leftSection = new HomeScreenLeftPanel(this.homeScrStage, false, "not in room yet");
        
        root.setLeft(leftSection.getLeftPanel());
        
        
        // CENTER SECTION
        HomeScreenMiddlePanel middleSection = new HomeScreenMiddlePanel(this.homeScrStage);
        root.setCenter(middleSection.getMiddlePanel());
        
        // RIGHT SECTION
        
        HomeScreenRightPanel rightSection = new HomeScreenRightPanel(this.homeScrStage, this.conn);
        
        root.setRight(rightSection.getRightPanel());
        
        //scene config
        Scene homeScene = new Scene(root, 1500, 800);
        
        homeScrStage.setScene(homeScene);
        homeScrStage.setTitle("HOMY-Home Screen");
        
        homeScrStage.setResizable(false);
        homeScrStage.show();
        homeScrStage.centerOnScreen();
        
    }

	
}