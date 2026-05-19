package myapplications;

import javafx.scene.control.Alert;

public class SuccessScreen {
    public static void display(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(ProfileScreen.getStage());
        
        alert.setTitle("Listing Success");
        alert.setHeaderText("The ad has been saved!");
        alert.setContentText(message);
        
        alert.showAndWait();
    }
}