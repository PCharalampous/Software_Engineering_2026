package myapplications;

import javafx.scene.control.Alert;
import profile.ProfileScreen;

public class StatusScreen {

    public static void show(String status) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(ProfileScreen.getStage());
        
        if (status.equalsIgnoreCase("ACCEPTED")) {
            alert.setTitle("Listing Status");
            alert.setHeaderText("Approval Information");
            alert.setContentText("Your ad has been approved by the platform!");
        } 
        else if (status.equalsIgnoreCase("DECLINED")) {
            alert.setTitle("Listing Status");
            alert.setHeaderText("Rejection Information");
            alert.setContentText("Your ad has been rejected by the platform.");
        } 
        else if (status.equalsIgnoreCase("CANCELED")) {
            alert.setTitle("Ad Cancellation");
            alert.setHeaderText("Cancellation Message");
            alert.setContentText("Your ad was successfully cancelled from the platform!");
        }
        
        alert.showAndWait();
    }
}