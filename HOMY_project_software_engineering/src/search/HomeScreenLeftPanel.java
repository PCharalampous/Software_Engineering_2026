package search;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main.HOMYApp;
import ui.ConfirmationScreen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import entities.Authentication;
import entities.Notification;
import entities.User;
import ui.ErrorScreen;
import util.DatabaseManager;

public class HomeScreenLeftPanel {
	
	private Label leftBoxLabel, headerLbl;
	private VBox leftBox, joinRoom;
	private Separator separator;
	private Button joinBtn;
	private TextField roomIdSect;
	private VBox roommatesBox;
	
	public HomeScreenLeftPanel(Stage stage, boolean inRoom, String labelMessage) {
        
        headerLbl = new Label("USERS SECTION");
		separator = new Separator();
        leftBoxLabel = new Label(labelMessage);
        leftBox = new VBox(10);
        
        
        // Conditional Check: Only build the Join Room component if the user is NOT already in a room
        // --- LAYER CONDITIONALS ---
        if (!inRoom) {
            // LAYER A: Show the instant join UI if the user is OUTSIDE a room
        	
        	joinRoom = joinRoomComponent();
        	leftBox.getChildren().addAll(headerLbl ,separator ,leftBoxLabel,joinRoom);
        	leftBox.setAlignment(Pos.TOP_CENTER);
            
        } else {
            // LAYER B: Show Room Info & Members if the user is INSIDE a room
            int activeRoomId = (Authentication.getCurrentUser() != null) ? Authentication.getCurrentUser().getRoomId() : 0;
            
            // 2. Button to reveal/get current Room ID
            Button btnGetRoomId = new Button("Show Room ID");
            btnGetRoomId.setMaxWidth(Double.MAX_VALUE);
            btnGetRoomId.setStyle("-fx-background-color: transparent ;"
            		+ " -fx-text-fill: #1E3A5F; -fx-font-weight: bold; -fx-cursor: hand;-fx-font-size: 14px;\n"
            		+ "-fx-font-weight: bold;");
            
            btnGetRoomId.setOnAction(e -> {
                // Show an alert with the ID so they can easily share it with friends
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Room Share Code");
                alert.setHeaderText("Share this ID with your friends:");
                alert.setContentText(String.valueOf(activeRoomId));
                alert.showAndWait();
            });
            
            this.leftBox.getChildren().add(btnGetRoomId);
            
            Button btnRefresh = new Button("Refresh Room's Roomates List");
            btnRefresh.setMaxWidth(Double.MAX_VALUE);
            btnRefresh.setStyle("-fx-background-color: #1E3A5F; -fx-text-fill: white; "
                    + "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 12px; "
                    + "-fx-background-radius: 6; -fx-padding: 6 12;");
            
            // Set action to refresh the list dynamically from the database
            btnRefresh.setOnAction(e -> updateRoommatesList(activeRoomId));
            this.leftBox.getChildren().add(btnRefresh);
            
            // Separator Line
            Separator sep = new Separator();
            this.leftBox.getChildren().add(sep);

            // 3. Room Occupants Panel Frame
            roommatesBox = new VBox(8);
            this.leftBox.getChildren().add(roommatesBox);
            
            // Initial dynamic load of roommates
            updateRoommatesList(activeRoomId);
        }
    }
	
	
	private VBox joinRoomComponent() {
		VBox joinRoom = new VBox(10);
		Label joinRoomLbl = new Label("JOIN ROOM");
		roomIdSect = new TextField();
		roomIdSect.setPromptText("Enter room id...");
		joinBtn = new Button("JOIN");
		joinRoom.getChildren().addAll(joinRoomLbl,roomIdSect,joinBtn);
		joinRoom.setAlignment(Pos.CENTER);
		
		//action listener for buttons
		buttonsFunctiability();

		    
		return joinRoom;
	}
	
	//----------
	private void updateRoommatesList(int activeRoomId) {
		// 1. Clear old data
		roommatesBox.getChildren().clear();
		
		// 2. Re-add header title
		Label lblTitle = new Label("ROOMMATES");
        lblTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748B;");
        roommatesBox.getChildren().add(lblTitle);
        
        // 3. Fetch names from DB
        if (activeRoomId > 0) {
            String sql = "SELECT username FROM users WHERE room_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, activeRoomId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Label lblUser = new Label("• " + rs.getString("username"));
                        lblUser.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");
                        roommatesBox.getChildren().add(lblUser);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error pulling room elements layout configuration profile list:");
                e.printStackTrace();
            }
        } else {
            roommatesBox.getChildren().add(new Label("No roommates detected."));
        }
	}
	//----------
	
	private void buttonsFunctiability() {
		
		
		
		joinBtn.setOnAction(e -> {
			
			ConfirmationScreen confirScr = new ConfirmationScreen (
				"Confirm your actions", 
				"You will be transfered to the room",
				"confirm",
				"-fx-background-color: #22C55E; -fx-text-fill: white;"+
				"-fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;",
				()->transferToTheRoom()
				
			);
			
			confirScr.show();
		});
	}
	
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
	        //inform all users of the room that a new roomates joined directly
	        Connection connect = DatabaseManager.getConnection();
	        Notification.createNotificationToRoom(connect, "room screen", "new roomate joined" , "", "ROOM_SCREEN", "#F59E0B");
	        try {
	        	connect.close();
	        }catch(Exception e) {
	        	System.out.print(e);
	        }
	        
	        
	        
	    } catch (NumberFormatException ex) {
	        // Handle case where user types non-numeric characters
	    	roomIdSect.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: red; -fx-padding: 8;");
	    	ErrorScreen errScr = new ErrorScreen("Error", "Not room found with that room id");
	    	errScr.show();
	        
	    }
	
	
	}
	
	private void leftPanelStyling() {
	    // Check if joinRoom is null to determine if the user is inside a room
	    if (joinRoom == null) {
	        // --- LAYER B: STYLING FOR INSIDE THE ROOM (Matches HOMYApp central hub) ---
	        leftBox.setStyle(
	        	"-fx-background-color: #F8FAF9;" +
	            "-fx-padding: 20 15 20 15;"
	        );
	        
	    } else {
	        
	        leftBox.setStyle(
	            "-fx-background-color: #D9D9FF;" +
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
	        
	        if (joinBtn != null) {
	            joinBtn.setStyle(
	                "-fx-background-radius: 20;" +
	                "-fx-background-color: #0000FF;" +
	                "-fx-text-fill: white;"
	            );
	        }
	    }
	}
	
	public VBox getLeftPanel() {
		leftPanelStyling();
		return this.leftBox;
	} 
}
