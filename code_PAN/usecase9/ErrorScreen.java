package usecase9;

import javafx.scene.control.Alert;

public class ErrorScreen {
    private String errorMessage;
    private EventScreen eventScreen;

    public ErrorScreen(String errorMessage, EventScreen eventScreen) {
        this.errorMessage = errorMessage;
        this.eventScreen = eventScreen;
    }

    public void show() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
        
        goBack(); 
    }

    public void goBack() {
        eventScreen.goBack();
    }
}