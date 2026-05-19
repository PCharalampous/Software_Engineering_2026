import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class ProfileScreen {
    private static final String BG     = "#F8F7F4";
    private static final String CARD   = "#FFFFFF";
    private static final String ACCENT = "#1A1A1A";
    private static final String MUTED  = "#888888";
    private static final String BORDER = "#DDDDDD";

    private BorderPane container;
    private ManageProfileClass manager;
    private List<Request> incomingRequests;
    private List<Application> myApplications;
    private Stage primaryStage;
    private Runnable backAction; 

    public ProfileScreen(Stage stage, ManageProfileClass manager, List<Request> requests, List<Application> apps, Runnable backAction) {
        this.primaryStage = stage;
        this.manager = manager;
        this.incomingRequests = requests;
        this.myApplications = apps;
        this.backAction = backAction;
        this.container = new BorderPane();
    }

    public Node getView() {
        showProfileMain();
        return container;
    }

    public void showProfileMain() {
        UserProfile p = manager.queryProfile();
        
        VBox content = new VBox(15);
        content.setStyle("-fx-background-color:" + BG + "; -fx-padding: 0 0 20 0;");

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0; -fx-padding:20;");
        
        Label lblName = new Label(p.name); lblName.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        Label lblUser = new Label(p.username); lblUser.setStyle("-fx-font-size:12px; -fx-text-fill:" + MUTED + ";");
        
        HBox stats = new HBox(12); stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(statCard("ΠΟΝΤΟΙ", String.valueOf(p.points)), statCard("ΟΙΚΙΑ", p.flatName), statCard("ΜΕΛΗ", String.valueOf(p.members)));
        header.getChildren().addAll(makeAvatar("MK", 80), lblName, lblUser, stats);

        VBox bioSec = new VBox(4);
        bioSec.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-border-width:1 0 1 0; -fx-padding:12 20;");
        Label bioText = new Label(p.bio); bioText.setWrapText(true);
        bioSec.getChildren().addAll(fieldLbl("BIO"), bioText);

        VBox btnSec = new VBox(10); 
        btnSec.setStyle("-fx-padding:0 20;");
        
        Button btnEdit = outlineBtn("EDIT PROFILE"); btnEdit.setMaxWidth(Double.MAX_VALUE);
        Button btnReq  = outlineBtn("REQUESTS (" + incomingRequests.size() + ")"); btnReq.setMaxWidth(Double.MAX_VALUE);
        Button btnApps = outlineBtn("MY APPLICATIONS (" + myApplications.size() + ")"); btnApps.setMaxWidth(Double.MAX_VALUE);
        
        // Σύνδεση με τις νέες εξωτερικές κλάσεις οθονών
        btnEdit.setOnAction(e -> {
            EditProfileScreen editScreen = new EditProfileScreen(primaryStage, manager, this);
            container.setTop(editScreen.getTitleBar());
            container.setCenter(editScreen.getView());
        });
        
        btnReq.setOnAction(e -> {
            PendingRequestsScreen reqScreen = new PendingRequestsScreen(primaryStage, manager, incomingRequests, this);
            container.setTop(reqScreen.getTitleBar());
            container.setCenter(reqScreen.getView());
        });
        
        btnApps.setOnAction(e -> showApplicationsScreen());
        
        btnSec.getChildren().addAll(btnEdit, btnReq, btnApps);
        content.getChildren().addAll(header, bioSec, btnSec);
        
        container.setTop(titleBar("PROFILE", backAction));
        container.setCenter(styledScroll(content));
    }

    private void showApplicationsScreen() {
        VBox appsList = new VBox(10);
        appsList.setStyle("-fx-padding:15; -fx-background-color:" + BG + ";");

        if (myApplications.isEmpty()) {
            appsList.getChildren().add(new Label("NO APPLICATIONS YET"));
        } else {
            for (Application app : new ArrayList<>(myApplications)) {
                HBox card = new HBox(10); card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-padding:12;");
                VBox info = new VBox(2); HBox.setHgrow(info, Priority.ALWAYS);
                Label t = new Label(app.title); t.setStyle("-fx-font-weight:bold;");
                info.getChildren().addAll(t, new Label(app.subtitle), new Label(app.status));
                
                Button btnCancel = smallBtn("CANCEL APP.");
                
                // Κλήση της εξωτερικής κλάσης ConfirmationScreen
                btnCancel.setOnAction(e -> { 
                    ConfirmationScreen conf = new ConfirmationScreen(primaryStage, app, myApplications, () -> {
                        showApplicationsScreen(); // Refresh τη λίστα μόλις γίνει confirm η διαγραφή
                    });
                    conf.show();
                });
                
                card.getChildren().addAll(info, btnCancel);
                appsList.getChildren().add(card);
            }
        }
        container.setTop(titleBar("MY APPLICATIONS", this::showProfileMain));
        container.setCenter(styledScroll(appsList));
    }

    public HBox titleBar(String title, Runnable backAction) {
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:" + ACCENT + "; -fx-padding:14 20;");
        if (backAction != null) {
            Button b = new Button("←"); b.setStyle("-fx-background-color:transparent; -fx-text-fill:white; -fx-font-size:18px; -fx-cursor:hand; -fx-padding: 0 10 0 0;");
            b.setOnAction(e -> backAction.run()); bar.getChildren().add(b);
        }
        Label lbl = new Label(title); lbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        bar.getChildren().add(lbl); return bar;
    }

    public ScrollPane styledScroll(Node content) {
        ScrollPane sp = new ScrollPane(content); sp.setFitToWidth(true); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";"); return sp;
    }

    private StackPane makeAvatar(String initials, int size) {
        Circle c = new Circle(size / 2.0, Color.web("#E0E0E0")); c.setStroke(Color.web(ACCENT)); c.setStrokeWidth(1.5);
        Label l = new Label(initials); l.setStyle("-fx-font-size:" + (size / 3) + "px; -fx-font-weight:bold;");
        StackPane sp = new StackPane(c, l); sp.setMinSize(size, size); sp.setMaxSize(size, size); return sp;
    }

    private Button outlineBtn(String txt) {
        Button b = new Button(txt); b.setStyle("-fx-background-color:" + CARD + "; -fx-text-fill:" + ACCENT + "; -fx-border-color:" + ACCENT + "; -fx-border-width:1; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;"); return b;
    }

    private Button smallBtn(String txt) {
        Button b = new Button(txt); b.setStyle("-fx-background-color:#EEEEEE; -fx-text-fill:" + ACCENT + "; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:4 8; -fx-cursor:hand;"); return b;
    }

    private Label fieldLbl(String text) {
        Label l = new Label(text); l.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";"); return l;
    }

    private VBox statCard(String lbl, String val) {
        VBox c = new VBox(2); c.setAlignment(Pos.CENTER);
        c.setStyle("-fx-background-color:" + CARD + "; -fx-border-color: " + BORDER + "; -fx-padding:6; -fx-min-width:85;");
        Label l1 = new Label(lbl); l1.setStyle("-fx-font-size:9px; -fx-text-fill:" + MUTED + "; -fx-font-weight:bold;");
        Label l2 = new Label(val); l2.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");
        c.getChildren().addAll(l1, l2); return c;
    }
}