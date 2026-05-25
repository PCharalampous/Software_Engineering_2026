package notifications;

import ui.ConfirmationScreen;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import entities.Notification;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NotificationDetailsScreen {
    private Stage ownerStage;
    private Notification notification;
    private ManageNotificationsClass manager;
    private NotificationsScreen parentScreen;

    public NotificationDetailsScreen(Stage ownerStage, Notification notification, ManageNotificationsClass manager, NotificationsScreen parentScreen) {
        this.ownerStage = ownerStage;
        this.notification = notification;
        this.manager = manager;
        this.parentScreen = parentScreen;
    }

    public void show() {
        Stage dlg = new Stage(); 
        dlg.initModality(Modality.APPLICATION_MODAL); 
        dlg.initOwner(ownerStage);
        dlg.setTitle("NOTIFICATION DETAIL");

        VBox body = new VBox(12); 
        body.setStyle("-fx-padding:20; -fx-background-color: white;");
        
        Label title = new Label(notification.category); 
        title.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        
        Label det = new Label(notification.detail); 
        det.setWrapText(true);
        
        body.getChildren().addAll(title, det);

        // SAFE CHECKS: Χρήση των σωστών μεταβλητών για την αποφυγή NullPointerException
        String categorySafe = (notification.category != null) ? notification.category.toUpperCase() : "";
        String textSafe = (notification.text != null) ? notification.text.toUpperCase() : ""; 

        boolean isApprovalRequest = categorySafe.contains("APPROVAL") || 
                                    categorySafe.contains("FINANCES") || 
                                    categorySafe.contains("ISSUES") || 
                                    textSafe.contains("APPROVAL");  

        boolean alreadyProcessed = textSafe.contains("CHANGED TO") || 
                                   textSafe.contains("APPROVED") || 
                                   textSafe.contains("DECLINED");

        if (isApprovalRequest && !alreadyProcessed) {
            // --- Διάταξη Κουμπιών Έγκρισης (Accept / Decline) ---
            HBox approvalButtons = new HBox(10);
            approvalButtons.setAlignment(Pos.CENTER);

            Button btnAccept = new Button("ACCEPT");
            btnAccept.setStyle("-fx-background-color:#10b981; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
            HBox.setHgrow(btnAccept, Priority.ALWAYS);
            btnAccept.setMaxWidth(Double.MAX_VALUE);

            Button btnDecline = new Button("DECLINE");
            btnDecline.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
            HBox.setHgrow(btnDecline, Priority.ALWAYS);
            btnDecline.setMaxWidth(Double.MAX_VALUE);

            btnAccept.setOnAction(e -> {
                handleApprovalAction(notification, "Accepted");
                dlg.close();
            });

            btnDecline.setOnAction(e -> {
                handleApprovalAction(notification, "Declined");
                dlg.close();
            });

            approvalButtons.getChildren().addAll(btnAccept, btnDecline);
            body.getChildren().add(approvalButtons);

        } else {
            // --- Κλασική Λειτουργία: Απλό Redirect ---
            Button btnGoTo = new Button("GO TO " + notification.target); 
            btnGoTo.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
            btnGoTo.setMaxWidth(Double.MAX_VALUE);
            
            btnGoTo.setOnAction(e -> {
                dlg.close();
                if (parentScreen != null) {
                    parentScreen.closeScreen(); 
                    parentScreen.handleRedirect(notification.target);
                }
            });
            body.getChildren().add(btnGoTo);
        }

        // Κουμπί Διαγραφής (Delete)
        Button btnDel = new Button("DELETE"); 
        btnDel.setStyle("-fx-background-color:white; -fx-text-fill:#1A1A1A; -fx-border-color:#1A1A1A; -fx-border-width:1; -fx-font-weight:bold; -fx-padding:10 16; -fx-cursor:hand;");
        btnDel.setMaxWidth(Double.MAX_VALUE);
        
        btnDel.setOnAction(e -> {
            Runnable deleteAction = () -> {
                String deleteSql = "DELETE FROM notifications WHERE notification_id = ?"; 
                try (Connection conn = util.DatabaseManager.getConnection();
                     PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, notification.id);
                    ps.executeUpdate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (manager != null) {
                    manager.queryPendingEvents().remove(notification);
                }
                dlg.close();
                if (parentScreen != null) {
                    parentScreen.refreshList();
                }
            };

            ConfirmationScreen confScreen = new ConfirmationScreen(dlg, ownerStage, notification, manager, deleteAction);
            confScreen.show();
        });
        
        body.getChildren().add(btnDel);
        dlg.setScene(new Scene(body, 340, 280)); 
        dlg.showAndWait();
    }

    private void showTargetSectionMockup(String target) {
        Stage dlg = new Stage(); dlg.initModality(Modality.APPLICATION_MODAL); dlg.initOwner(ownerStage);
        VBox body = new VBox(10); body.setAlignment(Pos.CENTER); body.setStyle("-fx-padding:30;");
        body.getChildren().addAll(new Label("TargetSectionScreen Created!"), new Label("Welcome to: " + target));
        Button b = new Button("Close"); b.setStyle("-fx-background-color:#1A1A1A; -fx-text-fill:white; -fx-font-weight:bold; -fx-padding:8 16;");
        b.setOnAction(e -> dlg.close()); body.getChildren().add(b);
        dlg.setScene(new Scene(body, 250, 150)); dlg.setTitle(target); dlg.showAndWait();
    }

    /**
     * Ενημερώνει τη βάση δεδομένων (πίνακες bills ή issues) με την επιλογή (Accepted ή Declined),
     * υλοποιεί την ομόφωνη έγκριση (Unanimous Support) με Veto Guard και στέλνει ειδοποίηση απόρριψης στον δημιουργό.
     */
    private void handleApprovalAction(Notification n, String newStatus) {
        int currentRoomId = (entities.Authentication.getCurrentUser() != null) ? entities.Authentication.getCurrentUser().getRoomId() : 0;
        
        // Default τιμές για Issues (HOME ISSUE REPORT)
        String targetTable = "issues";
        String matchColumn = "issue_type";
        String logLabel = "Issue Request";
        String calendarEventType = "ISSUE";
        String calendarTime = "1200"; 
        String calendarDescPrefix = "Technical Issue resolved/scheduled.";
        
        String rejectionCategory = "HOME ISSUE REPORT";
        String rejectionColor = "#a855f7"; // 🌟 Μοβ για Issues

        // Μετατροπή σε κεφαλαία για απόλυτα ασφαλή έλεγχο δρομολόγησης (Routing)
        String detailSafe = (n.detail != null) ? n.detail.toUpperCase() : "";
        String categorySafe = (n.category != null) ? n.category.toUpperCase() : "";
        String targetSafe = (n.target != null) ? n.target.toUpperCase() : "";

        // Έλεγχος αν η ειδοποίηση αφορά Λογαριασμούς (Bills / FINANCES)
        if (categorySafe.contains("BILL") || targetSafe.contains("BILL") || categorySafe.contains("FINANCES") || detailSafe.contains("NEW BILL")) {
            targetTable = "bills";
            matchColumn = "bill_type";
            logLabel = "Bill Request";
            calendarEventType = "BILL";
            calendarTime = "0900"; 
            calendarDescPrefix = "Approved shared bill entry.";
            
            rejectionCategory = "FINANCES";
            rejectionColor = "#25880d"; // 🌟 Πράσινο για Bills
        }

        // --- ΑΠΟΣΠΑΣΗ ΤΥΠΟΥ (Multi-line Parsing) ---
        String itemType = "";
        if (n.detail != null) {
            String[] lines = n.detail.split("\n");
            for (String line : lines) {
                String cleanLine = line.trim();
                if (cleanLine.toLowerCase().startsWith("type:")) {
                    itemType = cleanLine.substring(5).trim(); 
                    break;
                }
            }
        }
        
        if (itemType.isEmpty() && n.detail != null) {
            itemType = n.detail.trim();
        }

        String updateNotificationSql = "UPDATE notifications SET notification_text = ?, is_read = 1 WHERE notification_id = ?";
        String calendarSql = "INSERT INTO calendar_events (room_id, event_name, event_description, event_date, event_time, event_type) VALUES (?, ?, ?, ?, ?, ?)";
        String updatedNotificationText = logLabel + " [" + itemType + "] : " + newStatus.toUpperCase();

        try (Connection conn = util.DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); 

            // 1. Ενημερώνουμε την τρέχουσα ειδοποίηση ως διαβασμένη
            try (PreparedStatement psUpdateNotif = conn.prepareStatement(updateNotificationSql)) {
                psUpdateNotif.setString(1, updatedNotificationText);
                psUpdateNotif.setInt(2, n.id);
                psUpdateNotif.executeUpdate();
            }

            // 2. ΔΙΑΧΕΙΡΙΣΗ ΑΠΟΦΑΣΗΣ
            if ("Accepted".equals(newStatus)) {
                // ΕΛΕΓΧΟΣ ΟΜΟΦΩΝΙΑΣ
                String checkOthersSql = "SELECT COUNT(*) FROM notifications WHERE detail LIKE ? AND is_read = 0 AND room_id = ? AND notification_id != ?";
                boolean isLastApproval = true;

                try (PreparedStatement psCheck = conn.prepareStatement(checkOthersSql)) {
                    psCheck.setString(1, "%Type: " + itemType + "%");
                    psCheck.setInt(2, currentRoomId);
                    psCheck.setInt(3, n.id);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            isLastApproval = false; 
                        }
                    }
                }

                if (isLastApproval) {
                    // VETO GUARD: Έλεγχος αν κάποιος συγκατοικός είχε ήδη απορρίψει την εγγραφή νωρίτερα
                    String checkVetoSql = "SELECT approval_status FROM " + targetTable + " WHERE LOWER(" + matchColumn + ") LIKE LOWER(?) AND room_id = ?";
                    boolean alreadyDeclined = false;

                    try (PreparedStatement psVetoCheck = conn.prepareStatement(checkVetoSql)) {
                        psVetoCheck.setString(1, itemType);
                        psVetoCheck.setInt(2, currentRoomId);
                        try (ResultSet rs = psVetoCheck.executeQuery()) {
                            if (rs.next()) {
                                String currentStatus = rs.getString("approval_status");
                                if ("Declined".equalsIgnoreCase(currentStatus)) {
                                    alreadyDeclined = true; 
                                }
                            }
                        }
                    }

                    if (!alreadyDeclined) {
                        // Κανείς δεν έβαλε βέτο! ΟΛΟΙ ενέκριναν!
                        String updateMainTableQuery = "UPDATE " + targetTable + " SET approval_status = 'Accepted' WHERE LOWER(" + matchColumn + ") LIKE LOWER(?) AND room_id = ?";
                        try (PreparedStatement psUpdateMain = conn.prepareStatement(updateMainTableQuery)) {
                            psUpdateMain.setString(1, itemType);
                            psUpdateMain.setInt(2, currentRoomId);
                            psUpdateMain.executeUpdate();
                        }

                        // Εγγραφή στο κοινό ημερολόγιο
                        try (PreparedStatement psCal = conn.prepareStatement(calendarSql)) {
                            psCal.setInt(1, currentRoomId);
                            psCal.setString(2, logLabel + ": " + itemType);
                            psCal.setString(3, calendarDescPrefix);
                            psCal.setString(4, java.time.LocalDate.now().toString()); 
                            psCal.setString(5, calendarTime);
                            psCal.setString(6, calendarEventType);
                            psCal.executeUpdate();
                        }
                    }
                }

            } else if ("Declined".equals(newStatus)) {
                // ΑΜΕΣΗ ΑΚΥΡΩΣΗ (Instant Veto)
                String updateMainTableQuery = "UPDATE " + targetTable + " SET approval_status = 'Declined' WHERE LOWER(" + matchColumn + ") LIKE LOWER(?) AND room_id = ?";
                try (PreparedStatement psUpdateMain = conn.prepareStatement(updateMainTableQuery)) {
                    psUpdateMain.setString(1, itemType);
                    psUpdateMain.setInt(2, currentRoomId);
                    psUpdateMain.executeUpdate();
                }

                // ΑΠΟΣΤΟΛΗ ΕΙΔΟΠΟΙΗΣΗΣ ΑΠΟΡΡΙΨΗΣ ΣΤΟΝ ΔΗΜΙΟΥΡΓΟ
                String rejectorName = (entities.Authentication.getCurrentUser() != null) ? entities.Authentication.getCurrentUser().getUsername() : "A roommate";
                int creatorId = 0;

                if ("issues".equals(targetTable)) {
                    String findCreatorSql = "SELECT user_id FROM users WHERE username = (SELECT reported_by FROM issues WHERE issue_type = ? AND room_id = ?) AND room_id = ?";
                    try (PreparedStatement psFind = conn.prepareStatement(findCreatorSql)) {
                        psFind.setString(1, itemType);
                        psFind.setInt(2, currentRoomId);
                        psFind.setInt(3, currentRoomId);
                        try (ResultSet rs = psFind.executeQuery()) {
                            if (rs.next()) creatorId = rs.getInt("user_id");
                        }
                    }
                } else {
                    String findCreatorSql = "SELECT user_id FROM users WHERE room_id = ? AND username != ? LIMIT 1";
                    try (PreparedStatement psFind = conn.prepareStatement(findCreatorSql)) {
                        psFind.setInt(1, currentRoomId);
                        psFind.setString(2, rejectorName);
                        try (ResultSet rs = psFind.executeQuery()) {
                            if (rs.next()) creatorId = rs.getInt("user_id");
                        }
                    }
                }

                // Εισαγωγή στη βάση με τα ανανεωμένα hex-color tags
                if (creatorId > 0) {
                    String alertSql = "INSERT INTO notifications (user_id, room_id, category, notification_text, detail, target_screen, tag_color, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
                    try (PreparedStatement psAlert = conn.prepareStatement(alertSql)) {
                        psAlert.setInt(1, creatorId);
                        psAlert.setInt(2, currentRoomId);
                        psAlert.setString(3, rejectionCategory); 
                        psAlert.setString(4, logLabel + " Rejected");
                        psAlert.setString(5, "Roommate '" + rejectorName + "' declined your request for: " + itemType);
                        psAlert.setString(6, targetTable.toUpperCase());
                        psAlert.setString(7, rejectionColor); 
                        psAlert.executeUpdate();
                    }
                }
            }

            conn.commit(); 
            notification.text = updatedNotificationText; 
            notification.read = true; 

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (parentScreen != null) {
            parentScreen.refreshList();
        }
    }
}
