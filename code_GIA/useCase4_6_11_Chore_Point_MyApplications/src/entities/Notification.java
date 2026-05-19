package entities;

import javafx.scene.control.Alert;

public class Notification {
    public static void makeNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); 
    }
}