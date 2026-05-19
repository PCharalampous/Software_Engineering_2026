import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsScreen {
    private static final String BG     = "#F8F7F4";
    private static final String CARD   = "#FFFFFF";
    private static final String ACCENT = "#1A1A1A";
    private static final String MUTED  = "#888888";
    private static final String BORDER = "#DDDDDD";
    private static final String UNREAD = "#EEF2FF";

    private BorderPane container;
    private ManageNotificationsClass manager;
    private Stage primaryStage;
    private VBox notifListVBox;
    private boolean showUnreadOnly = false;
    private Button btnUnread;
    private Runnable backAction; 

    public NotificationsScreen(Stage stage, ManageNotificationsClass manager, Runnable backAction) {
        this.primaryStage = stage;
        this.manager = manager;
        this.backAction = backAction;
        this.container = new BorderPane();
    }

    public Node getView() {
        notifListVBox = new VBox(6);
        notifListVBox.setStyle("-fx-padding:10; -fx-background-color:" + BG + ";");
        
        Button btnAll = new Button("ALL");
        btnUnread = new Button("UNREAD (" + manager.getCount() + ")");
        Button btnMarkAll = smallBtn("MARK ALL AS READ");

        btnAll.setOnAction(e -> { showUnreadOnly = false; refreshList(); });
        btnUnread.setOnAction(e -> { showUnreadOnly = true; refreshList(); });
        btnMarkAll.setOnAction(e -> { manager.markAllAsRead(); refreshList(); });

        HBox tabs = new HBox(btnAll, btnUnread); HBox.setHgrow(btnAll, Priority.ALWAYS); HBox.setHgrow(btnUnread, Priority.ALWAYS);
        btnAll.setMaxWidth(Double.MAX_VALUE); btnUnread.setMaxWidth(Double.MAX_VALUE);
        btnAll.setStyle("-fx-background-color: white; -fx-font-weight: bold; -fx-padding: 10;");
        btnUnread.setStyle("-fx-background-color: white; -fx-font-weight: bold; -fx-padding: 10;");
        
        // Αφαιρέθηκε το btnSettings από το actionRow
        HBox actionRow = new HBox(8, btnMarkAll); actionRow.setStyle("-fx-padding: 5 10;");
        VBox topCtrl = new VBox(tabs, actionRow);
        
        container.setTop(titleBar("NOTIFICATIONS", backAction));
        container.setCenter(new BorderPane(styledScroll(notifListVBox), topCtrl, null, null, null));
        
        refreshList();
        return container;
    }

    public void refreshList() {
        notifListVBox.getChildren().clear();
        btnUnread.setText("UNREAD (" + manager.getCount() + ")");
        
        List<Notification> shown = showUnreadOnly 
            ? manager.queryPendingEvents().stream().filter(n -> !n.read).collect(Collectors.toList())
            : manager.queryPendingEvents();

        if (shown.isEmpty()) {
            Label lblEmpty = new Label("Show message 'no pending notifications'");
            lblEmpty.setStyle("-fx-text-fill:" + MUTED + "; -fx-padding: 20; -fx-font-style: italic;");
            notifListVBox.getChildren().add(lblEmpty);
        } else {
            shown.forEach(n -> {
                HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-padding:12; -fx-border-color:" + BORDER + "; -fx-background-color:" + (n.read ? CARD : UNREAD) + "; -fx-cursor:hand;");
                
                VBox info = new VBox(4); HBox.setHgrow(info, Priority.ALWAYS);
                Label tag = new Label(n.category); tag.setStyle("-fx-background-color:" + n.tagColor + "; -fx-padding:2 6; -fx-font-size:10px; -fx-font-weight:bold;");
                Label txt = new Label(n.text); txt.setStyle("-fx-font-size:13px;");
                info.getChildren().addAll(tag, txt);
                
                row.getChildren().addAll(info, new Label("›"));
                
                row.setOnMouseClicked(e -> { 
                    manager.markAsRead(n); 
                    refreshList(); 
                    
                    NotificationDetailsScreen detailsScreen = new NotificationDetailsScreen(primaryStage, n, manager, this);
                    detailsScreen.show();
                });
                notifListVBox.getChildren().add(row);
            });
        }
    }

    private HBox titleBar(String title, Runnable backAction) {
        HBox bar = new HBox(10); bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:" + ACCENT + "; -fx-padding:14 20;");
        if (backAction != null) {
            Button b = new Button("←"); b.setStyle("-fx-background-color:transparent; -fx-text-fill:white; -fx-font-size:18px; -fx-cursor:hand; -fx-padding:0 10 0 0;");
            b.setOnAction(e -> backAction.run()); bar.getChildren().add(b);
        }
        Label lbl = new Label(title); lbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        bar.getChildren().add(lbl); return bar;
    }

    private ScrollPane styledScroll(Node content) {
        ScrollPane sp = new ScrollPane(content); sp.setFitToWidth(true); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";"); return sp;
    }

    private Button smallBtn(String txt) {
        Button b = new Button(txt); b.setStyle("-fx-background-color:#EEEEEE; -fx-text-fill:" + ACCENT + "; -fx-font-size:10px; -fx-font-weight:bold; -fx-padding:4 8; -fx-cursor:hand;"); return b;
    }
}