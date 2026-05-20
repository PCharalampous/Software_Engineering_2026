package points;

import entities.Reward;
import entities.Point;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ui.ConfirmationScreen;
import ui.ErrorScreen;

public class RewardScreen extends VBox {
    private final PointScreen pointSidebar;
    private boolean workExemptionRedeemed = false;

    public RewardScreen(PointScreen sidebar) {
        this.pointSidebar = sidebar;
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        HBox.setHgrow(this, Priority.ALWAYS);

        Label title = new Label("🛒 AVAILABLE REWARDS");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1E3A5F;");
        
        VBox container = new VBox(10);
        container.getChildren().addAll(
            createCard(new Reward("WORK EXEMPTION PASS", 700, true, "#FEF08A")),
            createCard(new Reward("MEAL SELECTION PASS", 250, true, "#A7F3D0")),
            createCard(new Reward("MOVIE NIGHT PASS", 150, true, "#BBF7D0"))
        );
        
        this.getChildren().addAll(title, container);
    }

    private HBox createCard(Reward r) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #E2E8F0;");
        
        StackPane icon = new StackPane();
        icon.setPrefSize(40, 40);
        icon.setStyle("-fx-background-color: " + r.getColor() + "; -fx-background-radius: 8;");
        
        VBox txt = new VBox(2);
        Label name = new Label(r.getName()); name.setStyle("-fx-font-weight: bold;");
        Label cost = new Label(r.getCost() + " points"); cost.setStyle("-fx-font-size: 11; -fx-text-fill: #64748B;");
        txt.getChildren().addAll(name, cost);
        
        Pane spacer = new Pane(); HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button buyBtn = new Button("BUY");
        buyBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        
        buyBtn.setOnAction(e -> handlePurchase(r));
        
        card.getChildren().addAll(icon, txt, spacer, buyBtn);
        return card;
    }

    private void handlePurchase(Reward r) {
        Point model = pointSidebar.getPointModel();
        String user = pointSidebar.getCurrentUser();
        
        // 1. Limit 1 Validation
        if (r.getName().equals("WORK EXEMPTION PASS") && workExemptionRedeemed) {
            new ErrorScreen("LIMIT REACHED", 
                "You have already redeemed the WORK EXEMPTION PASS. This reward is limited to 1 purchase per user.").show();
            return;
        }
        
        // 2. Insufficient Points Validation
        if (!model.checkPoints(user, r.getCost())) {
            new ErrorScreen("NOT ENOUGH POINTS", 
                "Your current point balance is not high enough to redeem this pass. Please check your points and try again.").show();
        } 
        // 3. Success Flow
        else {
            new ConfirmationScreen("CONFIRM PURCHASE", "Do you want to redeem " + r.getName() + "?", "Yes, Buy", "-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 8 20; -fx-cursor: hand;", () -> {
                model.pointDeduction(user, r.getCost());
                r.rewardUpdate();
                
                if (r.getName().equals("WORK EXEMPTION PASS")) {
                    workExemptionRedeemed = true;
                }
                
                pointSidebar.updateUI();
                pointSidebar.addRedeemed(r.getName());
            }).show();
        }
    }
}