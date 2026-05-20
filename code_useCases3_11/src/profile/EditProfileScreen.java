package profile;

import entities.UserProfile;
import ui.ErrorScreen;
import myapplications.SuccessScreen;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class EditProfileScreen {
    private Stage primaryStage;
    private ManageProfileClass manager;
    private ProfileScreen parentScreen;

    public EditProfileScreen(Stage stage, ManageProfileClass manager, ProfileScreen parentScreen) {
        this.primaryStage = stage;
        this.manager = manager;
        this.parentScreen = parentScreen;
    }

    public HBox getTitleBar() {
        return parentScreen.titleBar("EDIT PROFILE", () -> parentScreen.showProfileMain());
    }

    public Node getView() {
        UserProfile p = manager.queryProfile();
        VBox form = new VBox(12); 
        form.setStyle("-fx-background-color: #FFFFFF; -fx-padding:20;");

        TextField tfName = new TextField(p.name);
        TextArea taBio = new TextArea(p.bio); taBio.setPrefRowCount(3);
        TextArea taPrefs = new TextArea(p.preferences); taPrefs.setPrefRowCount(2);

        Button btnSave = new Button("SAVE"); 
        btnSave.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        
        Button btnCancel = new Button("CANCEL"); 
        btnCancel.setStyle("-fx-background-color:white; -fx-text-fill:#1A1A1A; -fx-border-color:#1A1A1A; -fx-border-width:1; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        
        HBox btnRow = new HBox(10, btnSave, btnCancel); 
        HBox.setHgrow(btnSave, Priority.ALWAYS); 
        HBox.setHgrow(btnCancel, Priority.ALWAYS);

        // Υλοποίηση της ροής του Sequence Diagram
        btnSave.setOnAction(e -> {
            if (!manager.validateChanges(tfName.getText())) {
                // Κλήση της εξωτερικής κλάσης ErrorScreen σε περίπτωση σφάλματος
                ErrorScreen.show(primaryStage, "ErrorScreen", "INVALID INPUT\nDisplay name cannot be empty.");
            } else {
                manager.save(tfName.getText(), taBio.getText(), taPrefs.getText());
                
                // ΔΙΟΡΘΩΣΗ: Τώρα καλείται η SuccessScreen με το σωστό native look-and-feel της JavaFX!
                SuccessScreen.display("ProfileUpdatedScreen", "PROFILE UPDATED", "Changes saved successfully.");
                
                parentScreen.showProfileMain();
            }
        });
        
        btnCancel.setOnAction(e -> parentScreen.showProfileMain());

        Label lblName = new Label("DISPLAY NAME"); lblName.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:#888888;");
        Label lblBio = new Label("BIO"); lblBio.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:#888888;");
        Label lblPrefs = new Label("ΠΡΟΤΙΜΗΣΕΙΣ"); lblPrefs.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:#888888;");

        form.getChildren().addAll(lblName, tfName, lblBio, taBio, lblPrefs, taPrefs, btnRow);
        return parentScreen.styledScroll(form);
    }
}