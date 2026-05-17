package usecase9;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ConfirmationScreen {
    private Runnable onConfirmAction;
    private CalendarScreen calendarScreen;

    public ConfirmationScreen(Runnable onConfirmAction, CalendarScreen calendarScreen) {
        this.onConfirmAction = onConfirmAction;
        this.calendarScreen = calendarScreen;
    }

    public void show() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this event?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirmAction.run(); 
        } else {
            goBack(); 
        }
    }

    public void goBack() {
        calendarScreen.goBack();
    }
}