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
	
private Stage primaryStage;
	
	public void createWindow() {
        
		//border pane config
		BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // LEFT SECTION
        HomeScreenLeftPanel leftPanel = new HomeScreenLeftPanel();
        
        root.setLeft(leftPanel.getLeftPanel());
        
        // CENTER SECTION
        TextField searchBar = new TextField();

        searchBar.setPromptText("Search...");

        //searchBar.setPrefWidth(300);

        VBox centerBox = new VBox(searchBar);

        centerBox.setAlignment(Pos.TOP_CENTER);
        
        root.setCenter(centerBox);
        
        // RIGHT SECTION
        
        HomeScreenRightPanel rightPanel = new HomeScreenRightPanel();
        
        root.setRight(rightPanel.getRightPanel());
        
        //scene config
        Scene loginScene = new Scene(root, 1280, 720);
        primaryStage = new Stage();
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("HOMY-Home Screen");
        
        primaryStage.setResizable(false);
        primaryStage.show();
        
    }

	
}
