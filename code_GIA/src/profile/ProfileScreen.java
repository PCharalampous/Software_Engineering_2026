package profile;

import entities.Application;
import entities.User;
import entities.Authentication;
import entities.Request;
import entities.UserProfile;
import myapplications.MyApplicationScreen;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class ProfileScreen {
    // Modern χρωματική παλέτα
    private static final String BG     = "#F8F7F4";
    private static final String CARD   = "#FFFFFF";
    private static final String ACCENT = "#1A1A1A";
    private static final String MUTED  = "#888888";
    private static final String BORDER = "#DDDDDD";

    // Static πεδία για τη διαχείριση των παραθύρων και της κοινής βάσης δεδομένων
    private static Stage primaryStage;
   
    private BorderPane container;
    private Runnable backAction; 
    
    // Πεδία για τα δυναμικά δεδομένα του προφίλ
    private ManageProfileClass manager;
    private List<Request> incomingRequests;
    
    public ProfileScreen() {
        this.container = new BorderPane();
        this.backAction = () -> {
            if (primaryStage != null) primaryStage.close();
        };

        // 1. Παίρνουμε τον τρέχοντα χρήστη από το "session" της Authentication
        User sessionUser = Authentication.getCurrentUser();
        
        // Βάζουμε ένα fallback ID (π.χ. 1) σε περίπτωση που τρέχεις την οθόνη 
        // απευθείας από το VS Code (χωρίς να κάνεις login) για να μη "σκάσει" με NullPointerException.
        int currentUserId = (sessionUser != null) ? sessionUser.getId() : 1; 

        // 2. Φορτώνουμε το profile δυναμικά χρησιμοποιώντας το ID του συνδεδεμένου χρήστη
        UserProfile currentUserProfile = ManageProfileClass.loadFromDatabase(currentUserId);
        
        if (currentUserProfile == null) {
            System.err.println("Αδυναμία φόρτωσης από βάση, χρήση default τιμών.");
            currentUserProfile = new UserProfile(
                currentUserId, "Unknown User", "@unknown",
                "Δεν βρέθηκε προφίλ.", "N/A", 0, "No Flat", 0, 0
            );
        }

        // 3. Φορτώνουμε τα pending requests δυναμικά με βάση το δωμάτιο του χρήστη
        this.incomingRequests = ManageProfileClass.loadRequestsFromDatabase(currentUserProfile.room_id);

        this.manager = new ManageProfileClass(currentUserProfile, incomingRequests);
    }

    // --- FXBootstrap: Η εσωτερική κλάση εκκίνησης για τη JavaFX ---
    public static class FXBootstrap extends javafx.application.Application {
        @Override
        public void start(Stage stage) {
            ProfileScreen.primaryStage = stage;
            ProfileScreen.primaryStage.setTitle("HOMY - Profile & Apps");
            ProfileScreen.primaryStage.setResizable(false);
            
            ProfileScreen profileScreen = new ProfileScreen();
            
            Scene scene = new Scene((javafx.scene.Parent) profileScreen.getView(), 420, 750);
            ProfileScreen.primaryStage.setScene(scene);
            ProfileScreen.primaryStage.show();
        }
    }

    public Node getView() {
        showProfileMain();
        return container;
    }

    public void showProfileMain() {
        UserProfile p = manager.queryProfile();
        
        VBox content = new VBox(15);
        content.setStyle("-fx-background-color:" + BG + "; -fx-padding: 0 0 20 0;");

        // --- Header Section ---
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0; -fx-padding:20;");
        
        Label lblName = new Label(p.name); 
        lblName.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        Label lblUser = new Label(p.username); 
        lblUser.setStyle("-fx-font-size:12px; -fx-text-fill:" + MUTED + ";");
        
        HBox stats = new HBox(12); 
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
            statCard("POINTS", String.valueOf(p.points)), 
            statCard("HOME-Y", p.flatName), 
            statCard("MEMBERS", String.valueOf(p.members))
        );
        header.getChildren().addAll(makeAvatar("MK", 80), lblName, lblUser, stats);

        // --- Bio Section ---
        VBox bioSec = new VBox(4);
        bioSec.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-border-width:1 0 1 0; -fx-padding:12 20;");
        Label bioText = new Label(p.bio); 
        bioText.setWrapText(true);
        bioSec.getChildren().addAll(fieldLbl("BIO"), bioText);

        // --- Buttons Section ---
        VBox btnSec = new VBox(10); 
        btnSec.setStyle("-fx-padding:0 20;");
        
        Button btnEdit = outlineBtn("EDIT PROFILE"); 
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        
        Button btnReq  = outlineBtn("REQUESTS (" + incomingRequests.size() + ")"); 
        btnReq.setMaxWidth(Double.MAX_VALUE);
        
        List<Application> myApps = Application.loadUserApplications(p.user_id);
        Button btnApps = outlineBtn("MY APPLICATIONS (" + myApps.size() + ")"); 
        btnApps.setMaxWidth(Double.MAX_VALUE);
        
        // ΔΙΟΡΘΩΣΗ EDIT PROFILE: Ανοίγει την πραγματική EditProfileScreen στο κέντρο του container
        btnEdit.setOnAction(e -> {
            EditProfileScreen editScreen = new EditProfileScreen(primaryStage, manager, this);
            container.setTop(editScreen.getTitleBar());
            container.setCenter(editScreen.getView());
        });
        
        // ΔΙΟΡΘΩΣΗ REQUESTS: Ανοίγει την πραγματική PendingRequestsScreen στο κέντρο του container
        btnReq.setOnAction(e -> {
            PendingRequestsScreen reqScreen = new PendingRequestsScreen(primaryStage, manager, incomingRequests, this);
            container.setTop(reqScreen.getTitleBar());
            container.setCenter(reqScreen.getView());
        });
        
        // ΔΙΟΡΘΩΘΗΚΕ: Περνάμε το reference 'this' στην MyApplicationScreen για αυτόματο refresh του counter
        btnApps.setOnAction(e -> {
            MyApplicationScreen myApplicationsScreen = new MyApplicationScreen(this);
            myApplicationsScreen.display();
        });
        
        btnSec.getChildren().addAll(btnEdit, btnReq, btnApps);
        content.getChildren().addAll(header, bioSec, btnSec);
        
        container.setTop(titleBar("PROFILE", backAction));
        container.setCenter(styledScroll(content));
        container.layout();
        System.out.println("UI ανανεώθηκε με όνομα: " + p.name);
    }

    // --- UI Helpers ---
    public HBox titleBar(String title, Runnable backAction) {
        HBox bar = new HBox(10); 
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:" + ACCENT + "; -fx-padding:14 20;");
        if (backAction != null) {
            Button b = new Button("←"); 
            b.setStyle("-fx-background-color:transparent; -fx-text-fill:white; -fx-font-size:18px; -fx-cursor:hand; -fx-padding: 0 10 0 0;");
            b.setOnAction(e -> backAction.run()); 
            bar.getChildren().add(b);
        }
        Label lbl = new Label(title); 
        lbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        bar.getChildren().add(lbl); 
        return bar;
    }

    public ScrollPane styledScroll(Node content) {
        ScrollPane sp = new ScrollPane(content); 
        sp.setFitToWidth(true); 
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";"); 
        return sp;
    }

    private StackPane makeAvatar(String initials, int size) {
        Circle c = new Circle(size / 2.0, Color.web("#E0E0E0")); 
        c.setStroke(Color.web(ACCENT)); 
        c.setStrokeWidth(1.5);
        Label l = new Label(initials); 
        l.setStyle("-fx-font-size:" + (size / 3) + "px; -fx-font-weight:bold;");
        StackPane sp = new StackPane(c, l); 
        sp.setMinSize(size, size); 
        sp.setMaxSize(size, size); 
        return sp;
    }

    private Button outlineBtn(String txt) {
        Button b = new Button(txt); 
        b.setStyle("-fx-background-color:" + CARD + "; -fx-text-fill:" + ACCENT + "; -fx-border-color:" + ACCENT + "; -fx-border-width:1; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;"); 
        return b;
    }

    private Label fieldLbl(String text) {
        Label l = new Label(text); 
        l.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";"); 
        return l;
    }

    private VBox statCard(String lbl, String val) {
        VBox c = new VBox(2); 
        c.setAlignment(Pos.CENTER);
        c.setStyle("-fx-background-color:" + CARD + "; -fx-border-color: " + BORDER + "; -fx-padding:6; -fx-min-width:85;");
        Label l1 = new Label(lbl); 
        l1.setStyle("-fx-font-size:9px; -fx-text-fill:" + MUTED + "; -fx-font-weight:bold;");
        Label l2 = new Label(val); 
        l2.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");
        c.getChildren().addAll(l1, l2); 
        return c;
    }

    public static Stage getStage() { return primaryStage; }
}