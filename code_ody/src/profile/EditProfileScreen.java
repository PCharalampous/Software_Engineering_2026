package profile;

import entities.UserProfile;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EditProfileScreen {
    private static final String BG     = "#F8F7F4";
    private static final String CARD   = "#FFFFFF";
    private static final String ACCENT = "#1A1A1A";
    private static final String MUTED  = "#888888";
    private static final String BORDER = "#DDDDDD";

    private Stage stage;
    private ManageProfileClass manager;
    private ProfileScreen parentScreen;

    private TextField txtName;
    private TextArea txtBio;
    private TextField txtPreferences;

    public EditProfileScreen(Stage stage, ManageProfileClass manager, ProfileScreen parentScreen) {
        this.stage = stage;
        this.manager = manager;
        this.parentScreen = parentScreen;
    }

    public Node getTitleBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:" + ACCENT + "; -fx-padding:14 20;");

        Button btnBack = new Button("←");
        btnBack.setStyle("-fx-background-color:transparent; -fx-text-fill:white; -fx-font-size:18px; -fx-cursor:hand; -fx-padding: 0 10 0 0;");
        btnBack.setOnAction(e -> parentScreen.showProfileMain());

        Label lbl = new Label("EDIT PROFILE");
        lbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        
        bar.getChildren().addAll(btnBack, lbl);
        return bar;
    }

    public Node getView() {
        UserProfile profile = manager.queryProfile();

        VBox content = new VBox(15);
        content.setStyle("-fx-background-color:" + BG + "; -fx-padding: 20;");

        // Πεδίο: Όνομα
        VBox groupName = new VBox(5);
        Label lblName = new Label("DISPLAY NAME");
        lblName.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";");
        txtName = new TextField(profile.name);
        txtName.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-padding: 8;");
        groupName.getChildren().addAll(lblName, txtName);

        // Πεδίο: Bio
        VBox groupBio = new VBox(5);
        Label lblBio = new Label("BIO");
        lblBio.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";");
        txtBio = new TextArea(profile.bio);
        txtBio.setPrefRowCount(4);
        txtBio.setWrapText(true);
        txtBio.setStyle("-fx-text-box-border: " + BORDER + "; -fx-focus-color: " + ACCENT + ";");
        groupBio.getChildren().addAll(lblBio, txtBio);

        // Πεδίο: Preferences
        VBox groupPrefs = new VBox(5);
        Label lblPrefs = new Label("PREFERENCES");
        lblPrefs.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";");
        txtPreferences = new TextField(profile.preferences);
        txtPreferences.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-padding: 8;");
        groupPrefs.getChildren().addAll(lblPrefs, txtPreferences);

        // Κουμπί Αποθήκευσης
        Button btnSave = new Button("SAVE CHANGES");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-background-color:" + ACCENT + "; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:12; -fx-cursor:hand;");
        
        btnSave.setOnAction(e -> {
            String updatedName = txtName.getText();
            String updatedBio = txtBio.getText();
            String updatedPrefs = txtPreferences.getText();

            // Αντικατάσταση των null με άδεια strings
            if (updatedName == null) updatedName = "";
            if (updatedBio == null) updatedBio = "";
            if (updatedPrefs == null) updatedPrefs = "";

            if (manager.validateChanges(updatedName)) {
                // Αποθήκευση στη βάση δεδομένων
                manager.save(updatedName, updatedBio, updatedPrefs);
                
                // Επιστροφή και ανανέωση του UI
                parentScreen.showProfileMain();
            } else {
                txtName.setStyle("-fx-background-color:" + CARD + "; -fx-border-color: red; -fx-padding: 8;");
                System.err.println("Το όνομα δεν μπορεί να είναι κενό!");
            }
        });

        content.getChildren().addAll(groupName, groupBio, groupPrefs, btnSave);
        return content;
    }
}