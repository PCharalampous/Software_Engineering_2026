import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import java.util.*;
import java.util.stream.*;

/**
 * FlatmateApp – JavaFX GUI Mock-up
 * Use Case 5  : Notifications
 * Use Case 10 : Profile Management
 *
 * Classes derived from Sequence Diagrams:
 *   Entities    : UserProfile, Notification, FlatRequest, UnreadCounter
 *   Controllers : ManageProfileClass, ManageNotificationsClass
 *   Views       : ProfileScreen, EditProfileScreen, RequestsScreen,
 *                 NotificationsScreen, NotificationDetailsScreen,
 *                 ConfirmationScreen, ScreenWithOptionalJustification
 */
public class FlatmateApp extends Application {

    // ═══════════════════════════════════════════════════════════════
    //  STYLE CONSTANTS
    // ═══════════════════════════════════════════════════════════════
    static final String BG     = "#F8F7F4";
    static final String CARD   = "#FFFFFF";
    static final String ACCENT = "#1A1A1A";
    static final String MUTED  = "#888888";
    static final String BORDER = "#DDDDDD";
    static final String UNREAD = "#EEF2FF";
    static final String GREEN  = "#27AE60";
    static final String RED    = "#C0392B";

    // ═══════════════════════════════════════════════════════════════
    //  ENTITY: UserProfile
    // ═══════════════════════════════════════════════════════════════
    static class UserProfile {
        String name, username, bio, preferences;
        int    points, members;
        String flatName;

        UserProfile(String name, String username, String bio, String prefs,
                    int points, String flatName, int members) {
            this.name = name; this.username = username;
            this.bio = bio;   this.preferences = prefs;
            this.points = points; this.flatName = flatName; this.members = members;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ENTITY: Notification
    // ═══════════════════════════════════════════════════════════════
    static class Notification {
        String  category, text, detail, target, tagColor;
        boolean read;

        Notification(String cat, String txt, String det, String tgt, String tc) {
            category = cat; text = txt; detail = det; target = tgt; tagColor = tc;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ENTITY: FlatRequest
    // ═══════════════════════════════════════════════════════════════
    static class FlatRequest {
        String name, initials, time;
        FlatRequest(String n, String i, String t) { name = n; initials = i; time = t; }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ENTITY: UnreadCounter
    // ═══════════════════════════════════════════════════════════════
    static class UnreadCounter {
        private int count;

        UnreadCounter(int initial) { count = initial; }

        int  getCount()  { return count; }
        void decrement() { if (count > 0) count--; }
        void reset()     { count = 0; }
        void update(List<Notification> list) {
            count = (int) list.stream().filter(n -> !n.read).count();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CONTROLLER: ManageProfileClass
    // ═══════════════════════════════════════════════════════════════
    class ManageProfileClass {

        /** seq: queryProfile() → return profile */
        UserProfile queryProfile() { return currentUser; }

        /** seq: validateChanges() */
        boolean validateChanges(String name) {
            return name != null && !name.trim().isEmpty();
        }

        /** seq: save() */
        void save(String name, String bio, String prefs) {
            currentUser.name         = name.trim();
            currentUser.bio          = bio.trim();
            currentUser.preferences  = prefs.trim();
        }

        /** seq: choseACCEPT() – creates ScreenWithOptionalJustification */
        void choseACCEPT(FlatRequest req) {
            incomingRequests.remove(req);
            // Notification sent (simulated – seq: <<create>> :Notification)
        }

        /** seq: choseDECLINE() */
        void choseDECLINE(FlatRequest req) {
            incomingRequests.remove(req);
        }

        /** seq: refreshScreen() */
        void refreshScreen() { showScreen(buildProfileScreen()); }
    }

    // ═══════════════════════════════════════════════════════════════
    //  CONTROLLER: ManageNotificationsClass
    // ═══════════════════════════════════════════════════════════════
    class ManageNotificationsClass {

        /** seq: queryPendingEvents() → return pendingEvents */
        List<Notification> queryPendingEvents() { return notifications; }

        /** seq: getCount() → return count */
        int getCount() { return unreadCounter.getCount(); }

        /** seq: markAsRead() → update() + decrement() */
        void markAsRead(Notification n) {
            if (!n.read) {
                n.read = true;
                unreadCounter.decrement();   // seq: decrement() on UnreadCounter
            }
        }

        /** seq: mark AS read (alt2) → update counter */
        void markAllAsRead() {
            notifications.forEach(n -> n.read = true);
            unreadCounter.reset();
        }

        /** seq: confirm delete → removes notification */
        void deleteNotification(Notification n) {
            notifications.remove(n);
            unreadCounter.update(notifications);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ENTITY: MyApplication
    // ═══════════════════════════════════════════════════════════════
    static class MyApplication {
        String title, subtitle, status;
        MyApplication(String t, String s, String st) { title=t; subtitle=s; status=st; }
    }

    // ═══════════════════════════════════════════════════════════════
    //  APP STATE
    // ═══════════════════════════════════════════════════════════════
    UserProfile              currentUser;
    List<Notification>       notifications    = new ArrayList<>();
    List<FlatRequest>        incomingRequests = new ArrayList<>();
    List<MyApplication>      myApplications   = new ArrayList<>();
    UnreadCounter            unreadCounter;
    ManageProfileClass       profileManager;
    ManageNotificationsClass notifManager;

    BorderPane root;
    Stage      primaryStage;

    // Live UI references for refresh
    VBox    notifListVBox;
    VBox    requestListVBox;
    boolean showUnreadOnly = false;

    // ═══════════════════════════════════════════════════════════════
    //  APPLICATION START
    // ═══════════════════════════════════════════════════════════════
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        initData();

        root = new BorderPane();
        root.setStyle("-fx-background-color:" + BG + ";");
        root.setBottom(buildNavBar());
        showScreen(buildProfileScreen());

        Scene scene = new Scene(root, 420, 820);
        stage.setScene(scene);
        stage.setTitle("FlatmateApp");
        stage.setResizable(false);
        stage.show();
    }

    void initData() {
        currentUser = new UserProfile(
            "Makis Kosta", "@makisk",
            "Φοιτητής, λάτρης καφέ, ψάχνω ήσυχους συγκατοίκους",
            "Ήσυχος, καπνιστής: όχι",
            1000, "Flat 4B", 4
        );

        notifications.add(new Notification("CHORES",
            "Έχεις εκκρεμή εργασία: Σκούπισμα",
            "Εκκρεμής εργασία:\nΣκούπισμα – 500pts\nΠροθεσμία: Κυριακή 23/03",
            "CHORES", "#D4EDDA"));
        notifications.add(new Notification("BILLS",
            "Ο λογαριασμός ΔΕΗ λήγει σε 2 μέρες",
            "Λογαριασμός ΔΕΗ\nΠοσό: 45€\nΛήξη: 18/05/2026",
            "BILLS", "#FFF3CD"));
        notifications.add(new Notification("CALENDAR",
            "Νέο event: Καλεσμένοι Σαββατοκύριακο",
            "Event: Καλεσμένοι\nΤαριφή: Σαββατοκύριακο 24-25/05",
            "CALENDAR", "#D1ECF1"));
        notifications.add(new Notification("SHOPPING",
            "Ο Makis πρόσθεσε 3 προϊόντα στη λίστα",
            "Νέα προϊόντα:\n• Γάλα\n• Καφές\n• Ψωμί",
            "SHOPPING", "#F8D7DA"));
        notifications.add(new Notification("ISSUES",
            "Νέα βλάβη καταγράφηκε: Διαρροή νερού",
            "Βλάβη: Διαρροή νερού\nΚατάσταση: OPEN\nΑναφέρθηκε: σήμερα",
            "ISSUES", "#E2D9F3"));

        // Pre-mark last two as read (matches wireframe)
        notifications.get(3).read = true;
        notifications.get(4).read = true;

        unreadCounter = new UnreadCounter(
            (int) notifications.stream().filter(n -> !n.read).count()
        );

        incomingRequests.add(new FlatRequest("Nikos Kam.", "NK", "2 μέρες πριν"));
        incomingRequests.add(new FlatRequest("Anna P.",    "AP", "4 μέρες πριν"));

        myApplications.add(new MyApplication(
            "Flat Κυψέλη – 2 δωμάτια", "350€/μήνα  •  3 συγκατοικοί", "PENDING  •  2 μέρες πριν"));
        myApplications.add(new MyApplication(
            "Studio Εξάρχεια", "280€/μήνα  •  2 συγκατοικοί", "PENDING  •  5 μέρες πριν"));

        profileManager = new ManageProfileClass();
        notifManager   = new ManageNotificationsClass();
    }

    void showScreen(Node n) { root.setCenter(n); }

    // ═══════════════════════════════════════════════════════════════
    //  NAV BAR (bottom)
    // ═══════════════════════════════════════════════════════════════
    HBox buildNavBar() {
        HBox bar = new HBox();
        bar.setStyle("-fx-background-color:" + CARD + ";" +
                     "-fx-border-color:" + ACCENT + "; -fx-border-width:1 0 0 0;");
        bar.setPrefHeight(56);

        Button btnP = navBtn("👤  Profile");
        Button btnN = navBtn("🔔  Notifications");
        btnP.setOnAction(e -> showScreen(buildProfileScreen()));
        btnN.setOnAction(e -> { showUnreadOnly = false; showScreen(buildNotificationsScreen()); });

        for (Button b : new Button[]{btnP, btnN}) {
            HBox.setHgrow(b, Priority.ALWAYS);
            b.setMaxWidth(Double.MAX_VALUE);
        }
        bar.getChildren().addAll(btnP, btnN);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW: ProfileScreen
    // ═══════════════════════════════════════════════════════════════
    Node buildProfileScreen() {
        UserProfile p = profileManager.queryProfile();   // seq: queryProfile()

        VBox content = new VBox();
        content.setStyle("-fx-background-color:" + BG + ";");

        // ── Header card ──────────────────────────────────────────
        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0; -fx-padding:20 20 16 20;");

        Label lblName = new Label(p.name);
        lblName.setStyle("-fx-font-size:17px; -fx-font-weight:bold;");

        Label lblUser = new Label(p.username);
        lblUser.setStyle("-fx-font-size:11px; -fx-text-fill:" + MUTED + ";");

        HBox stats = new HBox(8);
        stats.setAlignment(Pos.CENTER);
        stats.getChildren().addAll(
            statCard("ΠΟΝΤΟΙ", String.valueOf(p.points)),
            statCard("ΟΙΚΙΑ",  p.flatName),
            statCard("ΜΕΛΗ",   String.valueOf(p.members))
        );
        header.getChildren().addAll(makeAvatar("MK", 64), lblName, lblUser, stats);

        // ── Bio ──────────────────────────────────────────────────
        VBox bioSec = new VBox(4);
        bioSec.setStyle("-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0; -fx-padding:12 20 12 20;");
        Label bioTitle = new Label("BIO");
        bioTitle.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";");
        Label bioText = new Label(p.bio);
        bioText.setStyle("-fx-font-size:13px;");
        bioText.setWrapText(true);
        bioSec.getChildren().addAll(bioTitle, bioText);

        // ── Buttons ──────────────────────────────────────────────
        VBox btnSec = new VBox(10);
        btnSec.setStyle("-fx-padding:16 20 0 20; -fx-background-color:" + BG + ";");
        Button btnEdit = outlineBtnFull("EDIT PROFILE");
        Button btnReq  = outlineBtnFull("REQUESTS  (" + incomingRequests.size() + ")");
        btnEdit.setOnAction(e -> showScreen(buildEditProfileScreen()));   // seq: go to "Edit Profile"
        btnReq.setOnAction(e  -> showScreen(buildRequestsScreen()));      // seq: chose "Request"
        btnSec.getChildren().addAll(btnEdit, btnReq);

        // ── My Applications ──────────────────────────────────────
        VBox appsSec = new VBox(8);
        appsSec.setStyle("-fx-padding:16 20 20 20; -fx-background-color:" + BG + ";");
        Label appsTitle = new Label("MY APPLICATIONS");
        appsTitle.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");
        appsSec.getChildren().add(appsTitle);
        if (myApplications.isEmpty()) {
            Label noApps = new Label("NO APPLICATIONS YET");
            noApps.setStyle("-fx-font-size:13px; -fx-text-fill:" + MUTED + ";");
            appsSec.getChildren().add(noApps);
        } else {
            for (MyApplication app : new ArrayList<>(myApplications)) {
                appsSec.getChildren().add(appCard(app, appsSec));
            }
        }

        content.getChildren().addAll(header, bioSec, btnSec, appsSec);

        ScrollPane scroll = styledScroll(content);

        BorderPane screen = new BorderPane();
        screen.setTop(titleBar("PROFILE", null));
        screen.setCenter(scroll);
        return screen;
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW: EditProfileScreen
    // ═══════════════════════════════════════════════════════════════
    Node buildEditProfileScreen() {
        UserProfile p = profileManager.queryProfile();

        VBox form = new VBox(12);
        form.setStyle("-fx-background-color:" + CARD + "; -fx-padding:20;");

        TextField tfName  = styledTF(p.name);
        TextArea  taBio   = styledTA(p.bio, 3);
        TextArea  taPrefs = styledTA(p.preferences, 2);

        Label lblHint = new Label("* Κενό display name → INVALID INPUT");
        lblHint.setStyle("-fx-font-size:10px; -fx-text-fill:" + MUTED + ";");

        Label lblStatus = new Label();
        lblStatus.setWrapText(true);
        lblStatus.setVisible(false);

        // SAVE / CANCEL buttons
        Button btnSave   = filledBtn("SAVE");
        Button btnCancel = outlineBtn("CANCEL");
        HBox btnRow = new HBox(10);
        HBox.setHgrow(btnSave,   Priority.ALWAYS); btnSave.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCancel, Priority.ALWAYS); btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnRow.getChildren().addAll(btnSave, btnCancel);

        // seq: validateChanges() → save()  OR  INVALID INPUT (alt1)
        btnSave.setOnAction(e -> {
            if (!profileManager.validateChanges(tfName.getText())) {
                lblStatus.setText("⚠  INVALID INPUT – Display name cannot be empty.");
                lblStatus.setStyle("-fx-font-size:13px; -fx-text-fill:" + RED + ";");
                lblStatus.setVisible(true);
            } else {
                profileManager.save(tfName.getText(), taBio.getText(), taPrefs.getText());
                lblStatus.setText("✔  PROFILE UPDATED");
                lblStatus.setStyle("-fx-font-size:13px; -fx-text-fill:" + GREEN + ";");
                lblStatus.setVisible(true);
            }
        });
        // seq: CANCEL → return to ProfileScreen
        btnCancel.setOnAction(e -> showScreen(buildProfileScreen()));

        form.getChildren().addAll(
            fieldLbl("DISPLAY NAME"), tfName,
            fieldLbl("BIO"),          taBio,
            fieldLbl("ΠΡΟΤΙΜΗΣΕΙΣ ΣΥΓΚΑΤΟΙΚΗΣΗΣ"), taPrefs,
            lblHint, btnRow, lblStatus
        );

        ScrollPane scroll = styledScroll(form);
        scroll.setStyle("-fx-background-color:" + CARD + "; -fx-background:" + CARD + ";");

        BorderPane screen = new BorderPane();
        screen.setTop(titleBar("EDIT PROFILE", () -> showScreen(buildProfileScreen())));
        screen.setCenter(scroll);
        return screen;
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW: RequestsScreen
    // ═══════════════════════════════════════════════════════════════
    Node buildRequestsScreen() {
        // INCOMING tab
        requestListVBox = new VBox(8);
        requestListVBox.setStyle("-fx-padding:12 16 12 16; -fx-background-color:" + BG + ";");
        refreshRequestList();

        ScrollPane scrollIn = styledScroll(requestListVBox);

        // OUTGOING tab
        VBox outVBox = new VBox(8);
        outVBox.setStyle("-fx-padding:12 16 12 16; -fx-background-color:" + BG + ";");

        HBox outCard = new HBox(10);
        outCard.setAlignment(Pos.CENTER_LEFT);
        outCard.setMaxWidth(Double.MAX_VALUE);
        outCard.setStyle("-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + BORDER + "; -fx-border-width:1; -fx-padding:10 12 10 12;");

        VBox outInfo = new VBox(2);
        HBox.setHgrow(outInfo, Priority.ALWAYS);
        Label outTitle  = new Label("Flat 5A Invite");
        outTitle.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
        Label outStatus = new Label("Κατάσταση: PENDING");
        outStatus.setStyle("-fx-font-size:10px; -fx-text-fill:" + MUTED + ";");
        outInfo.getChildren().addAll(outTitle, outStatus);

        Button btnCR = smallBtn("CANCEL REQUEST");
        outCard.getChildren().addAll(outInfo, btnCR);
        outVBox.getChildren().add(outCard);

        ScrollPane scrollOut = styledScroll(outVBox);

        TabPane tabPane = new TabPane();
        Tab tabIn  = new Tab("INCOMING (" + incomingRequests.size() + ")", scrollIn);
        Tab tabOut = new Tab("OUTGOING (1)", scrollOut);
        tabIn.setClosable(false); tabOut.setClosable(false);
        tabPane.getTabs().addAll(tabIn, tabOut);
        tabPane.setStyle("-fx-font-size:12px; -fx-font-weight:bold;");

        BorderPane screen = new BorderPane();
        screen.setTop(titleBar("REQUESTS", () -> showScreen(buildProfileScreen())));
        screen.setCenter(tabPane);
        return screen;
    }

    void refreshRequestList() {
        if (requestListVBox == null) return;
        requestListVBox.getChildren().clear();
        if (incomingRequests.isEmpty()) {
            Label empty = new Label("NO PENDING REQUESTS");
            empty.setStyle("-fx-font-size:13px; -fx-text-fill:" + MUTED + ";");
            requestListVBox.getChildren().add(empty);
        } else {
            incomingRequests.forEach(r -> requestListVBox.getChildren().add(requestCard(r)));
        }
    }

    HBox requestCard(FlatRequest req) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + BORDER + "; -fx-border-width:1; -fx-padding:10 12 10 12;");

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(req.name);
        name.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");
        Label st = new Label("PENDING  •  " + req.time);
        st.setStyle("-fx-font-size:10px; -fx-text-fill:" + MUTED + ";");
        info.getChildren().addAll(name, st);

        Button btnAcc = filledBtnSm("ACCEPT");
        Button btnDec = outlineBtnSm("DECLINE");
        // seq: choseACCEPT() / choseDECLINE() → ScreenWithOptionalJustification
        btnAcc.setOnAction(e -> showJustificationScreen(req, true));
        btnDec.setOnAction(e -> showJustificationScreen(req, false));

        HBox btns = new HBox(6);
        btns.setAlignment(Pos.CENTER);
        btns.getChildren().addAll(btnAcc, btnDec);

        card.getChildren().addAll(makeAvatar(req.initials, 40), info, btns);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW: ScreenWithOptionalJustification
    //  (used for both ACCEPT and DECLINE flows – alt 2 & 2.1)
    // ═══════════════════════════════════════════════════════════════
    void showJustificationScreen(FlatRequest req, boolean accept) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(primaryStage);
        dlg.setTitle(accept ? "ACCEPT REQUEST" : "DECLINE REQUEST");

        VBox body = new VBox(12);
        body.setStyle("-fx-padding:20; -fx-background-color:" + CARD + ";");

        Label title = new Label((accept ? "Αποδοχή" : "Απόρριψη") +
                                " αίτησης από " + req.name);
        title.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");
        title.setWrapText(true);

        TextArea taJust = styledTA("", 3);

        Button btnConfirm = filledBtn("CONFIRM");
        Button btnCancel  = outlineBtn("CANCEL");
        HBox btnRow = new HBox(10);
        HBox.setHgrow(btnConfirm, Priority.ALWAYS); btnConfirm.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnCancel,  Priority.ALWAYS); btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnRow.getChildren().addAll(btnConfirm, btnCancel);

        // seq: Ο χρήστης επιβεβαιώνει → choseACCEPT / choseDECLINE → refreshScreen()
        btnConfirm.setOnAction(e -> {
            if (accept) profileManager.choseACCEPT(req);
            else        profileManager.choseDECLINE(req);
            dlg.close();
            // Rebuild requests screen so INCOMING tab counter updates correctly
            showScreen(buildRequestsScreen());
            info((accept ? "✔  Αποδοχή" : "✖  Απόρριψη") +
                 " αίτησης!\nΕιδοποίηση εστάλη στον " + req.name + ".");
        });
        // seq: Ο χρήστης δεν επιβεβαιώνει → return
        btnCancel.setOnAction(e -> dlg.close());

        body.getChildren().addAll(
            title, fieldLbl("Αιτιολόγηση (προαιρετικό):"), taJust, btnRow
        );
        dlg.setScene(new Scene(body, 300, 250));
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW: NotificationsScreen
    // ═══════════════════════════════════════════════════════════════
    Node buildNotificationsScreen() {
        // seq: <<create>> ManageNotificationsClass → queryPendingEvents()
        notifListVBox = new VBox(6);
        notifListVBox.setStyle("-fx-padding:10 12 12 12; -fx-background-color:" + BG + ";");
        refreshNotifList();

        ScrollPane scroll = styledScroll(notifListVBox);

        // Top controls
        Button btnAll    = tabBtn("ALL");
        Button btnUnread = tabBtn("UNREAD (" + unreadCounter.getCount() + ")");
        Button btnMarkAll  = smallBtn("MARK ALL AS READ");
        Button btnSettings = smallBtn("SETTINGS");

        HBox tabRow = new HBox();
        HBox.setHgrow(btnAll,    Priority.ALWAYS); btnAll.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnUnread, Priority.ALWAYS); btnUnread.setMaxWidth(Double.MAX_VALUE);
        tabRow.getChildren().addAll(btnAll, btnUnread);

        HBox actionRow = new HBox(8);
        actionRow.setStyle("-fx-padding:6 12 6 12;");
        actionRow.getChildren().addAll(btnMarkAll, btnSettings);

        VBox topCtrl = new VBox();
        topCtrl.setStyle("-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + BORDER + "; -fx-border-width:0 0 1 0;");
        topCtrl.getChildren().addAll(tabRow, actionRow);

        // seq: ALL tab
        btnAll.setOnAction(e -> {
            showUnreadOnly = false;
            refreshNotifList();
        });
        // seq: UNREAD tab → getCount()
        btnUnread.setOnAction(e -> {
            showUnreadOnly = true;
            refreshNotifList();
            btnUnread.setText("UNREAD (" + unreadCounter.getCount() + ")");
        });
        // seq (alt2): mark AS read → go to notifications → update counter
        btnMarkAll.setOnAction(e -> {
            notifManager.markAllAsRead();
            showUnreadOnly = false;
            refreshNotifList();
            btnUnread.setText("UNREAD (0)");
        });

        BorderPane center = new BorderPane();
        center.setTop(topCtrl);
        center.setCenter(scroll);

        BorderPane screen = new BorderPane();
        screen.setTop(titleBar("NOTIFICATIONS", null));
        screen.setCenter(center);
        return screen;
    }

    void refreshNotifList() {
        if (notifListVBox == null) return;
        notifListVBox.getChildren().clear();

        List<Notification> shown = showUnreadOnly
            ? notifications.stream().filter(n -> !n.read).collect(Collectors.toList())
            : new ArrayList<>(notifications);

        if (shown.isEmpty()) {
            Label empty = new Label(showUnreadOnly
                ? "Καμία αδιάβαστη ειδοποίηση."
                : "Καμία ειδοποίηση.");
            empty.setStyle("-fx-font-size:13px; -fx-text-fill:" + MUTED + "; -fx-padding:20;");
            notifListVBox.getChildren().add(empty);
        } else {
            shown.forEach(n -> notifListVBox.getChildren().add(notifRow(n)));
        }
    }

    HBox notifRow(Notification n) {
        String base = (n.read
            ? "-fx-background-color:" + CARD
            : "-fx-background-color:" + UNREAD)
            + "; -fx-border-color:" + BORDER + "; -fx-border-width:1;"
            + " -fx-padding:10 12 10 12; -fx-cursor:hand;";

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setStyle(base);

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label tag = new Label(n.category);
        tag.setStyle("-fx-background-color:" + n.tagColor + ";" +
            "-fx-padding:2 6 2 6; -fx-font-size:10px; -fx-font-weight:bold;");
        Label txt = new Label(n.text);
        txt.setStyle("-fx-font-size:12px;");
        txt.setWrapText(true);
        info.getChildren().addAll(tag, txt);

        Label arrow = new Label("›");
        arrow.setStyle("-fx-font-size:22px; -fx-text-fill:" + MUTED + ";");

        row.getChildren().addAll(info, arrow);
        row.setOnMouseEntered(e -> row.setStyle(base.replace(n.read ? CARD : UNREAD, "#E8E8E8")));
        row.setOnMouseExited(e  -> row.setStyle(base));

        // seq: selectNotification() → markAsRead() → update() + decrement()
        //      → <<create>> NotificationDetailsScreen
        row.setOnMouseClicked(e -> {
            notifManager.markAsRead(n);
            refreshNotifList();
            showNotificationDetailsScreen(n);
        });
        return row;
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW: NotificationDetailsScreen
    // ═══════════════════════════════════════════════════════════════
    void showNotificationDetailsScreen(Notification n) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(primaryStage);
        dlg.setTitle("NOTIFICATION DETAIL");

        HBox hdrBar = new HBox();
        hdrBar.setStyle("-fx-background-color:" + n.tagColor + "; -fx-padding:12 16 12 16;");
        Label hdrLbl = new Label(n.category);
        hdrLbl.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");
        hdrBar.getChildren().add(hdrLbl);

        VBox body = new VBox(12);
        body.setStyle("-fx-padding:20; -fx-background-color:" + CARD + ";");

        Label detail = new Label(n.detail);
        detail.setStyle("-fx-font-size:13px;");
        detail.setWrapText(true);

        Button btnGoTo = filledBtn("GO TO " + n.target);
        Button btnDel  = outlineBtn("DELETE");
        btnGoTo.setMaxWidth(Double.MAX_VALUE);
        btnDel.setMaxWidth(Double.MAX_VALUE);

        // seq: selectGoTo()
        btnGoTo.setOnAction(e -> {
            dlg.close();
            info("Μεταφορά στην ενότητα:\n" + n.target);
        });
        // seq: choseDELETE() → <<create>> ConfirmationScreen
        btnDel.setOnAction(e -> showConfirmationScreen(n, dlg));

        body.getChildren().addAll(detail, btnGoTo, btnDel);

        BorderPane layout = new BorderPane();
        layout.setTop(hdrBar);
        layout.setCenter(body);

        dlg.setScene(new Scene(layout, 300, 290));
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    //  VIEW: ConfirmationScreen  (alt3: Confirm Delete / Decline Delete)
    // ═══════════════════════════════════════════════════════════════
    void showConfirmationScreen(Notification n, Stage parentDlg) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(primaryStage);
        dlg.setTitle("Επιβεβαίωση Διαγραφής");

        VBox body = new VBox(20);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding:24; -fx-background-color:" + CARD + ";");

        Label msg = new Label("Διαγραφή ειδοποίησης;");
        msg.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");

        Button btnConfirm = filledBtn("CONFIRM");
        Button btnCancel  = outlineBtn("CANCEL");
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.getChildren().addAll(btnConfirm, btnCancel);

        // seq (alt3 [Confirm Delete]): confirm delete → deleteNotification → refreshScreen
        btnConfirm.setOnAction(e -> {
            notifManager.deleteNotification(n);
            refreshNotifList();
            dlg.close();
            parentDlg.close();
        });
        // seq (alt3 [Decline Delete]): cancel → return()
        btnCancel.setOnAction(e -> dlg.close());

        body.getChildren().addAll(msg, btnRow);
        dlg.setScene(new Scene(body, 260, 160));
        dlg.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════
    //  UI HELPER COMPONENTS
    // ═══════════════════════════════════════════════════════════════

    HBox titleBar(String title, Runnable back) {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color:" + ACCENT + "; -fx-padding:14 20 14 20;");
        if (back != null) {
            Button b = new Button("←");
            b.setStyle("-fx-background-color:transparent; -fx-text-fill:white;" +
                       "-fx-font-size:18px; -fx-cursor:hand; -fx-padding:0 8 0 0;");
            b.setOnAction(e -> back.run());
            bar.getChildren().add(b);
        }
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size:17px; -fx-font-weight:bold; -fx-text-fill:white;");
        bar.getChildren().add(lbl);
        return bar;
    }

    StackPane makeAvatar(String initials, int size) {
        Circle c = new Circle(size / 2.0);
        c.setFill(Color.web("#E0E0E0"));
        c.setStroke(Color.web(ACCENT));
        c.setStrokeWidth(1.5);
        Label l = new Label(initials);
        l.setStyle("-fx-font-size:" + (size / 3) + "px; -fx-font-weight:bold; -fx-text-fill:" + ACCENT + ";");
        StackPane sp = new StackPane(c, l);
        sp.setMinSize(size, size);
        sp.setMaxSize(size, size);
        return sp;
    }

    VBox statCard(String lbl, String val) {
        VBox c = new VBox(2);
        c.setAlignment(Pos.CENTER);
        c.setStyle("-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + ACCENT + "; -fx-border-width:1;" +
            "-fx-padding:6 12 6 12; -fx-min-width:80;");
        Label l1 = new Label(lbl); l1.setStyle("-fx-font-size:9px; -fx-text-fill:" + MUTED + ";");
        Label l2 = new Label(val); l2.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");
        c.getChildren().addAll(l1, l2);
        return c;
    }

    HBox appCard(MyApplication app, VBox parentVBox) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle("-fx-background-color:" + CARD + ";" +
            "-fx-border-color:" + BORDER + "; -fx-border-width:1; -fx-padding:10 12 10 12;");
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label t  = new Label(app.title);    t.setStyle("-fx-font-weight:bold; -fx-font-size:12px;");
        Label s  = new Label(app.subtitle); s.setStyle("-fx-font-size:10px; -fx-text-fill:" + MUTED + ";");
        Label st = new Label(app.status);  st.setStyle("-fx-font-size:10px; -fx-text-fill:" + MUTED + ";");
        info.getChildren().addAll(t, s, st);

        Button btnCancel = smallBtn("CANCEL APP.");
        btnCancel.setOnAction(e -> showCancelAppDialog(app, card, parentVBox));
        card.getChildren().addAll(info, btnCancel);
        return card;
    }

    // ── Confirmation dialog for CANCEL APP ──────────────────────────
    void showCancelAppDialog(MyApplication app, HBox card, VBox parentVBox) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(primaryStage);
        dlg.setTitle("Cancel Application");

        VBox body = new VBox(16);
        body.setAlignment(Pos.CENTER);
        body.setStyle("-fx-padding:24; -fx-background-color:" + CARD + ";");

        Label msg = new Label("Ακύρωση αίτησης για:\n" + app.title + ";");
        msg.setStyle("-fx-font-size:13px; -fx-font-weight:bold;");
        msg.setWrapText(true);

        Button btnConfirm = filledBtn("CONFIRM");
        Button btnCancel  = outlineBtn("CANCEL");
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER);
        btnRow.getChildren().addAll(btnConfirm, btnCancel);

        btnConfirm.setOnAction(e -> {
            myApplications.remove(app);
            parentVBox.getChildren().remove(card);
            // If no applications left, show "NO APPLICATIONS YET"
            if (myApplications.isEmpty()) {
                Label noApps = new Label("NO APPLICATIONS YET");
                noApps.setStyle("-fx-font-size:13px; -fx-text-fill:" + MUTED + ";");
                parentVBox.getChildren().add(noApps);
            }
            dlg.close();
            info("Η αίτηση για " + app.title + " ακυρώθηκε.");
        });
        btnCancel.setOnAction(e -> dlg.close());

        body.getChildren().addAll(msg, btnRow);
        dlg.setScene(new Scene(body, 300, 180));
        dlg.showAndWait();
    }

    ScrollPane styledScroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background-color:" + BG + "; -fx-background:" + BG + ";");
        return sp;
    }

    Label fieldLbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:" + MUTED + ";");
        return l;
    }

    TextField styledTF(String text) {
        TextField tf = new TextField(text);
        tf.setStyle("-fx-font-size:13px; -fx-padding:8 10 8 10;" +
                    "-fx-border-color:" + BORDER + "; -fx-border-width:1;");
        return tf;
    }

    TextArea styledTA(String text, int rows) {
        TextArea ta = new TextArea(text);
        ta.setPrefRowCount(rows);
        ta.setWrapText(true);
        ta.setStyle("-fx-font-size:13px;");
        return ta;
    }

    Button filledBtn(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + ACCENT + "; -fx-text-fill:white;" +
                   "-fx-font-weight:bold; -fx-font-size:12px;" +
                   "-fx-padding:10 16 10 16; -fx-cursor:hand;");
        return b;
    }

    Button filledBtnSm(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + ACCENT + "; -fx-text-fill:white;" +
                   "-fx-font-weight:bold; -fx-font-size:10px;" +
                   "-fx-padding:5 10 5 10; -fx-cursor:hand;");
        return b;
    }

    Button outlineBtn(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + CARD + "; -fx-text-fill:" + ACCENT + ";" +
                   "-fx-border-color:" + ACCENT + "; -fx-border-width:1;" +
                   "-fx-font-weight:bold; -fx-font-size:12px;" +
                   "-fx-padding:10 16 10 16; -fx-cursor:hand;");
        return b;
    }

    Button outlineBtnFull(String txt) {
        Button b = outlineBtn(txt);
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    Button outlineBtnSm(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + CARD + "; -fx-text-fill:" + ACCENT + ";" +
                   "-fx-border-color:" + ACCENT + "; -fx-border-width:1;" +
                   "-fx-font-weight:bold; -fx-font-size:10px;" +
                   "-fx-padding:5 10 5 10; -fx-cursor:hand;");
        return b;
    }

    Button navBtn(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + CARD + "; -fx-text-fill:" + ACCENT + ";" +
                   "-fx-font-weight:bold; -fx-font-size:12px; -fx-cursor:hand; -fx-border-width:0;");
        return b;
    }

    Button tabBtn(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:" + CARD + "; -fx-text-fill:" + ACCENT + ";" +
                   "-fx-font-weight:bold; -fx-font-size:12px; -fx-cursor:hand;" +
                   "-fx-padding:10 8 10 8; -fx-border-width:0;");
        return b;
    }

    Button smallBtn(String txt) {
        Button b = new Button(txt);
        b.setStyle("-fx-background-color:#EEEEEE; -fx-text-fill:" + ACCENT + ";" +
                   "-fx-font-size:10px; -fx-font-weight:bold; -fx-padding:4 8 4 8; -fx-cursor:hand;");
        return b;
    }

    void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.initOwner(primaryStage);
        a.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
