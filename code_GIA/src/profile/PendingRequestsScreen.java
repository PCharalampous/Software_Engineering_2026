package profile;

import entities.Request;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class PendingRequestsScreen {
    private Stage primaryStage;
    private ManageProfileClass manager;
    private List<Request> incomingRequests;
    private ProfileScreen parentScreen;
    private VBox listContainer;

    public PendingRequestsScreen(Stage stage, ManageProfileClass manager, List<Request> requests, ProfileScreen parentScreen) {
        this.primaryStage = stage;
        this.manager = manager;
        this.incomingRequests = requests;
        this.parentScreen = parentScreen;
        this.listContainer = new VBox(8);
        this.listContainer.setStyle("-fx-padding:12 16;");
    }

    public HBox getTitleBar() {
        return parentScreen.titleBar("REQUESTS", () -> parentScreen.showProfileMain());
    }

    public Node getView() {
        refreshList();
        return parentScreen.styledScroll(listContainer);
    }

    private void refreshList() {
        listContainer.getChildren().clear();
        if (incomingRequests.isEmpty()) {
            Label noReqs = new Label("NO PENDING REQUESTS");
            noReqs.setStyle("-fx-text-fill:#888888; -fx-font-weight:bold;");
            listContainer.getChildren().add(noReqs);
        } else {
            incomingRequests.forEach(r -> {
                HBox card = new HBox(10); card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color:white; -fx-border-color:#DDDDDD; -fx-padding:10;");
                
                VBox info = new VBox(2); HBox.setHgrow(info, Priority.ALWAYS);
                Label name = new Label(r.name); name.setStyle("-fx-font-weight:bold;");
                Label subtitle = new Label("PENDING • " + r.time); subtitle.setStyle("-fx-text-fill:#888888; -fx-font-size:11px;");
                info.getChildren().addAll(name, subtitle);

                Button btnAcc = new Button("ACCEPT"); btnAcc.setOnAction(e -> openJustification(r, true));
                Button btnDec = new Button("DECLINE"); btnDec.setOnAction(e -> openJustification(r, false));
                
                // Styling στα κουμπιά για να ταιριάζουν με το minimal UX
                btnAcc.setStyle("-fx-background-color:#10b981; -fx-text-fill:white; -fx-font-weight:bold; -fx-cursor:hand;");
                btnDec.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:bold; -fx-cursor:hand;");
                
                card.getChildren().addAll(makeAvatar(r.initials, 40), info, btnAcc, btnDec);
                listContainer.getChildren().add(card);
            });
        }
    }

    private void openJustification(Request req, boolean accept) {
        Stage dlg = new Stage(); dlg.initModality(Modality.APPLICATION_MODAL); dlg.initOwner(primaryStage);
        dlg.setTitle(accept ? "Accept Request" : "Decline Request");
        dlg.setResizable(false);
        
        VBox body = new VBox(12); body.setStyle("-fx-padding:20; -fx-background-color:white;");
        
        // ΜΕΤΑΦΡΑΣΗ: Αλλαγή τίτλου popup σε αγγλικά
        Label title = new Label((accept ? "Accept" : "Decline") + " request from " + req.name + "?");
        title.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        
        TextArea ta = new TextArea(); 
        ta.setPrefRowCount(2);
        ta.setPromptText("Type a reason (optional)...");
        
        Button btnConfirm = new Button("CONFIRM");
        btnConfirm.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        
        btnConfirm.setOnAction(e -> {
            if (accept) {
                manager.choseACCEPT(req);
                incomingRequests.remove(req); // Αφαιρείται από τα pending live
            } else {
                manager.choseDECLINE(req);
                incomingRequests.remove(req);
            }
            dlg.close(); 
            refreshList();
        });
        
        // ΜΕΤΑΦΡΑΣΗ: Αλλαγή του label της αιτιολόγησης
        Label lblJust = new Label("Justification (Optional):"); lblJust.setStyle("-fx-font-size:11px; -fx-text-fill:#888888; -fx-font-weight:bold;");
        body.getChildren().addAll(title, lblJust, ta, btnConfirm);
        
        dlg.setScene(new Scene(body, 340, 230)); dlg.showAndWait();
    }

    private StackPane makeAvatar(String initials, int size) {
        Circle c = new Circle(size / 2.0, Color.web("#E0E0E0")); c.setStroke(Color.web("#1A1A1A")); c.setStrokeWidth(1.5);
        Label l = new Label(initials); l.setStyle("-fx-font-size:" + (size / 3) + "px; -fx-font-weight:bold;");
        return new StackPane(c, l);
    }
}