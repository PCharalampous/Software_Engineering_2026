package usecase7;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorScreen {
    public static void show(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Σφάλμα εισαγωγής");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}