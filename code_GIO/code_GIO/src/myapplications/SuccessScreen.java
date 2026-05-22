package myapplications;

import javafx.scene.control.Alert;
import profile.ProfileScreen;

public class SuccessScreen {
    
    // Η αρχική σου μέθοδος για τις αγγελίες (παραμένει ίδια για να μην σπάσει τίποτα)
    public static void display(String message) {
        display("Listing Success", "The ad has been saved!", message);
    }

    // Η ΝΕΑ παραμετροποιημένη μέθοδος για γενική χρήση (π.χ. για το προφίλ)
    public static void display(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(ProfileScreen.getStage());
        
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        alert.showAndWait();
    }
}