package calendar;

import entities.Calendar;
import entities.Event;
import ui.ConfirmationScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;

import util.DatabaseManager; 
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CalendarScreen {
    private BorderPane root;
    private Calendar calendar;
    private GridPane calendarGrid;
    private VBox eventsContainer;
    
    private int selectedDay;
    private LocalDate currentLocalDate;
    private Label monthTitle; 
    
    private LocalDate prefilledDateForForm = null;
    private Runnable backAction; 
    private Stage primaryStage;

    public CalendarScreen(Runnable backAction) {
        this.calendar = new Calendar(); 
        this.currentLocalDate = LocalDate.now(); 
        this.selectedDay = currentLocalDate.getDayOfMonth(); 
        this.prefilledDateForForm = currentLocalDate; 
        this.backAction = backAction;
        createUI();
        loadEventsFromDatabase(); 
    }

    public void display() {
        this.primaryStage = new Stage();
        Scene scene = new Scene(root, 440, 600); 
        primaryStage.setTitle("HOMY - Calendar");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        
        primaryStage.setOnCloseRequest(e -> {
            if (backAction != null) {
                backAction.run();
            }
        });
        
        primaryStage.show();
        returnCalendar();
    }

    public void loadEventsFromDatabase() {
        calendar.getEvents().clear();
        
        int currentRoomId = 0;
        if (entities.Authentication.getCurrentUser() != null) {
            currentRoomId = entities.Authentication.getCurrentUser().getRoomId();
        }
        
        if (currentRoomId <= 0) {
            System.out.println("User does not belong to a room. Calendar is empty.");
            return;
        }

        String query = "SELECT event_id, event_name, event_description, event_date, event_time, event_type, is_accepted, created_by " +
                       "FROM calendar_events WHERE room_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentRoomId); 
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("event_id");
                    String name = rs.getString("event_name");
                    String desc = rs.getString("event_description");
                    Date sqlDate = rs.getDate("event_date");
                    int time = rs.getInt("event_time");
                    String type = rs.getString("event_type");
                    int isAccepted = rs.getInt("is_accepted");
                    int createdBy = rs.getInt("created_by"); 
                    
                    if (sqlDate != null) {
                        LocalDate ld = sqlDate.toLocalDate();
                        Event ev = new Event(id, ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear(), time, name, type, isAccepted, desc, createdBy);
                        calendar.addEvent(ev);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database loading error: " + e.getMessage());
        }
    }

    public void updateEventStatusInDatabase(Event ev, int newStatus) {
        String query = "UPDATE calendar_events SET is_accepted = ? WHERE event_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, newStatus);
            stmt.setInt(2, ev.getEventId());
            stmt.executeUpdate();
            ev.setIsAccepted(newStatus);
        } catch (SQLException e) {
            System.err.println("Database update error: " + e.getMessage());
        }
    }

    public void deleteEventFromDatabase(Event ev) {
        String query = "DELETE FROM calendar_events WHERE event_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ev.getEventId());
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                String notifText = "Διαγραφή event στο ημερολόγιο";
                String notifDetail = "Το συμβάν '" + ev.getName() + "' διαγράφηκε.";
                entities.Notification.createNotificationToRoom(conn, "CALENDAR", notifText, notifDetail, "CALENDAR_SCREEN", "#06B6D4");
            }
            
            calendar.removeEvent(ev);
        } catch (SQLException e) {
            System.err.println("Database delete error: " + e.getMessage());
        }
    }

    private boolean hasUserVoted(int eventId, int userId) {
        String query = "SELECT COUNT(*) FROM notifications WHERE category = 'CALENDAR_VOTE' AND user_id = ? AND notification_text LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, "EventID:" + eventId + "|%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void registerUserVote(Event ev, int userId, int roomId, String voteType) {
        String logQuery = "INSERT INTO notifications (user_id, room_id, category, notification_text, detail, target_screen, tag_color, is_read) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(logQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, roomId);
                stmt.setString(3, "CALENDAR_VOTE");
                stmt.setString(4, "EventID:" + ev.getEventId() + "|Vote:" + voteType);
                stmt.setString(5, "Vote system tracking");
                stmt.setString(6, "None");
                stmt.setString(7, "#000000");
                stmt.setInt(8, 1);
                stmt.executeUpdate();
            }

            int totalRoommates = 1;
            String countRoommates = "SELECT COUNT(*) FROM users WHERE room_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(countRoommates)) {
                stmt.setInt(1, roomId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalRoommates = rs.getInt(1);
                    }
                }
            }

            int noVotes = 0;
            String countNo = "SELECT COUNT(*) FROM notifications WHERE category = 'CALENDAR_VOTE' AND room_id = ? AND notification_text = ?";
            try (PreparedStatement stmt = conn.prepareStatement(countNo)) {
                stmt.setInt(1, roomId);
                stmt.setString(2, "EventID:" + ev.getEventId() + "|Vote:NO");
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        noVotes = rs.getInt(1);
                    }
                }
            }

            if (noVotes >= (totalRoommates / 2.0)) {
                deleteEventFromDatabase(ev);
                return;
            }

            int yesVotes = 0;
            String countYes = "SELECT COUNT(*) FROM notifications WHERE category = 'CALENDAR_VOTE' AND room_id = ? AND notification_text = ?";
            try (PreparedStatement stmt = conn.prepareStatement(countYes)) {
                stmt.setInt(1, roomId);
                stmt.setString(2, "EventID:" + ev.getEventId() + "|Vote:YES");
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        yesVotes = rs.getInt(1);
                    }
                }
            }

            if (yesVotes > (totalRoommates / 2.0)) {
                String acceptQuery = "UPDATE calendar_events SET is_accepted = 1 WHERE event_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(acceptQuery)) {
                    stmt.setInt(1, ev.getEventId());
                    stmt.executeUpdate();
                    ev.setIsAccepted(1);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F8FAFC;");
        root.setPadding(new Insets(15));

        VBox headerBox = new VBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        Button backBtn = new Button("← Back to Hub");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #475569; -fx-cursor: hand; -fx-font-size: 13px;");
        backBtn.setOnMouseEntered(e -> backBtn.setStyle("-fx-background-color: transparent; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-cursor: hand; -fx-font-size: 13px;"));
        backBtn.setOnMouseExited(e -> backBtn.setStyle("-fx-background-color: transparent; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-text-fill: #475569; -fx-cursor: hand; -fx-font-size: 13px;"));
        backBtn.setOnAction(e -> {
            primaryStage.close();
            if (backAction != null) {
                backAction.run();
            }
        });
        
        Label mainTitle = new Label("Calendar");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        mainTitle.setTextFill(Color.web("#1E293B"));
        
        HBox navigationBox = new HBox(15);
        navigationBox.setAlignment(Pos.CENTER);
        
        Button prevMonthBtn = new Button("<");
        Button nextMonthBtn = new Button(">");
        
        String navBtnStyle = "-fx-background-color: #E2E8F0; -fx-background-radius: 6; -fx-font-weight: bold; -fx-text-fill: #475569; -fx-cursor: hand;";
        String navBtnHover = "-fx-background-color: #CBD5E1; -fx-background-radius: 6; -fx-font-weight: bold; -fx-text-fill: #1E293B; -fx-cursor: hand;";
        
        prevMonthBtn.setStyle(navBtnStyle);
        prevMonthBtn.setOnMouseEntered(e -> prevMonthBtn.setStyle(navBtnHover));
        prevMonthBtn.setOnMouseExited(e -> prevMonthBtn.setStyle(navBtnStyle));
        
        nextMonthBtn.setStyle(navBtnStyle);
        nextMonthBtn.setOnMouseEntered(e -> nextMonthBtn.setStyle(navBtnHover));
        nextMonthBtn.setOnMouseExited(e -> nextMonthBtn.setStyle(navBtnStyle));
        
        prevMonthBtn.setMinWidth(32); prevMonthBtn.setMaxWidth(32);
        nextMonthBtn.setMinWidth(32); nextMonthBtn.setMaxWidth(32);
        
        monthTitle = new Label();
        monthTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        monthTitle.setTextFill(Color.web("#334155"));
        monthTitle.setPrefWidth(180);
        monthTitle.setAlignment(Pos.CENTER);
        
        prevMonthBtn.setOnAction(e -> {
            currentLocalDate = currentLocalDate.minusMonths(1);
            int maxDays = YearMonth.of(currentLocalDate.getYear(), currentLocalDate.getMonth()).lengthOfMonth();
            if (selectedDay > maxDays) {
                selectedDay = maxDays;
            }
            returnCalendar();
        });
        
        nextMonthBtn.setOnAction(e -> {
            currentLocalDate = currentLocalDate.plusMonths(1);
            int maxDays = YearMonth.of(currentLocalDate.getYear(), currentLocalDate.getMonth()).lengthOfMonth();
            if (selectedDay > maxDays) {
                selectedDay = maxDays;
            }
            returnCalendar();
        });
        
        navigationBox.getChildren().addAll(prevMonthBtn, monthTitle, nextMonthBtn);
        headerBox.getChildren().addAll(backBtn, mainTitle, navigationBox);
        root.setTop(headerBox);

        calendarGrid = new GridPane();
        calendarGrid.setAlignment(Pos.CENTER);
        calendarGrid.setHgap(4);
        calendarGrid.setVgap(4);
        calendarGrid.setPadding(new Insets(10));
        calendarGrid.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-border-width: 1;");
        
        DropShadow gridShadow = new DropShadow(8, Color.web("#000000", 0.04));
        gridShadow.setOffsetY(2);
        calendarGrid.setEffect(gridShadow);

        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));
        
        Label eventsHeader = new Label("Events");
        eventsHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        eventsHeader.setTextFill(Color.web("#1E293B"));
        eventsHeader.setMaxWidth(Double.MAX_VALUE);

        eventsContainer = new VBox(8);
        eventsContainer.setPadding(new Insets(2));
        
        ScrollPane eventScrollPane = new ScrollPane(eventsContainer);
        eventScrollPane.setFitToWidth(true);
        eventScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        eventScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        eventScrollPane.setMinHeight(110);   
        eventScrollPane.setPrefHeight(110);
        eventScrollPane.setMaxHeight(110); 
        eventScrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0; -fx-viewport-background-color: transparent;");

        HBox btnContainer = new HBox();
        btnContainer.setAlignment(Pos.BOTTOM_RIGHT);
        Button addEventBtn = new Button("+ Add Event");
        addEventBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
        addEventBtn.setOnMouseEntered(e -> addEventBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));
        addEventBtn.setOnMouseExited(e -> addEventBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;"));
        
        addEventBtn.setOnAction(e -> {
            this.prefilledDateForForm = LocalDate.of(currentLocalDate.getYear(), currentLocalDate.getMonthValue(), selectedDay);
            addEvent();
        });
        btnContainer.getChildren().add(addEventBtn);

        bottomBox.getChildren().addAll(eventsHeader, eventScrollPane, btnContainer);
        
        VBox centerContainer = new VBox(15);
        centerContainer.getChildren().addAll(calendarGrid, bottomBox);
        root.setCenter(centerContainer);
    }

    public void selectDay(int day) {
        this.selectedDay = day;
        this.prefilledDateForForm = LocalDate.of(currentLocalDate.getYear(), currentLocalDate.getMonthValue(), day);
        returnCalendar(); 
    }

    public void returnCalendar() {
        String monthName = currentLocalDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
        monthTitle.setText(monthName + " " + currentLocalDate.getYear());

        calendarGrid.getChildren().clear();
        
        String[] daysOfWeek = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
            dayLabel.setTextFill(Color.web("#94A3B8"));
            dayLabel.setPrefSize(52, 25);
            dayLabel.setAlignment(Pos.CENTER);
            calendarGrid.add(dayLabel, i, 0);
        }

        YearMonth yearMonth = YearMonth.of(currentLocalDate.getYear(), currentLocalDate.getMonth());
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int startColumn = firstOfMonth.getDayOfWeek().getValue() - 1; 
        int currentDay = 1;

        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < startColumn) {
                    Label emptyLabel = new Label("");
                    emptyLabel.setPrefSize(52, 45);
                    emptyLabel.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 6;");
                    calendarGrid.add(emptyLabel, col, row);
                } else if (currentDay <= daysInMonth) {
                    final int day = currentDay;
                    VBox dayCell = new VBox(4);
                    dayCell.setPadding(new Insets(4));
                    dayCell.setPrefSize(52, 45);
                    dayCell.setAlignment(Pos.TOP_CENTER);
                    
                    if (day == selectedDay) {
                        dayCell.setStyle("-fx-background-color: #DBEAFE; -fx-background-radius: 8; -fx-border-color: #3B82F6; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-cursor: hand;");
                    } else {
                        dayCell.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-cursor: hand;");
                        dayCell.setOnMouseEntered(e -> dayCell.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 8; -fx-cursor: hand;"));
                        dayCell.setOnMouseExited(e -> dayCell.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-cursor: hand;"));
                    }
                    
                    Label dayNum = new Label(String.valueOf(day));
                    dayNum.setFont(Font.font("Segoe UI", day == selectedDay ? FontWeight.BOLD : FontWeight.NORMAL, 12));
                    dayNum.setTextFill(day == selectedDay ? Color.web("#1E40AF") : Color.web("#334155"));
                    
                    HBox dotsBox = new HBox(2);
                    dotsBox.setAlignment(Pos.CENTER);
                    
                    // Φιλτράρισμα και ταξινόμηση βάσει ώρας για να συμβαδίζουν απόλυτα με τη λίστα
                    List<Event> dayEvents = calendar.getEvents().stream()
                            .filter(e -> e.getDate() == day 
                                      && e.getMonth() == currentLocalDate.getMonthValue() 
                                      && e.getYear() == currentLocalDate.getYear())
                            .sorted((e1, e2) -> Integer.compare(e1.getTime(), e2.getTime()))
                            .collect(Collectors.toList());
                    
                    // ΔΙΟΡΘΩΣΗ: Εμφάνιση των 3 πρώτων χρωματιστών κύκλων και ένδειξη "+x" αν υπάρχουν παραπάνω
                    int dotCount = 0;
                    for (Event e : dayEvents) {
                        if (dotCount < 3) {
                            Circle dot = new Circle(3);
                            if (e.getType().equals("BILL")) dot.setFill(Color.web("#EF4444"));
                            else if (e.getType().equals("ISSUE")) dot.setFill(Color.web("#3B82F6"));
                            else if (e.getIsAccepted() == 1) dot.setFill(Color.web("#10B981"));
                            else dot.setFill(Color.web("#34D399"));
                            dotsBox.getChildren().add(dot);
                        }
                        dotCount++;
                    }
                    
                    if (dotCount > 3) {
                        Label plusLabel = new Label("+" + (dotCount - 3));
                        plusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
                        plusLabel.setTextFill(Color.web("#64748B")); // Διακριτικό γκρι-μπλε χρώμα
                        dotsBox.getChildren().add(plusLabel);
                    }

                    dayCell.getChildren().addAll(dayNum, dotsBox);
                    
                    dayCell.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            this.prefilledDateForForm = LocalDate.of(currentLocalDate.getYear(), currentLocalDate.getMonthValue(), day);
                            this.selectedDay = day;
                            addEvent();
                        } else {
                            selectDay(day);
                        }
                    });

                    calendarGrid.add(dayCell, col, row);
                    currentDay++;
                } else {
                    Label emptyLabel = new Label("");
                    emptyLabel.setPrefSize(52, 45);
                    emptyLabel.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 6;");
                    calendarGrid.add(emptyLabel, col, row);
                }
            }
        }

        eventsContainer.getChildren().clear();
        List<Event> dayEvents = calendar.getEvents().stream()
                .filter(e -> e.getDate() == selectedDay 
                          && e.getMonth() == currentLocalDate.getMonthValue() 
                          && e.getYear() == currentLocalDate.getYear())
                .sorted((e1, e2) -> Integer.compare(e1.getTime(), e2.getTime()))
                .collect(Collectors.toList());

        int currentUserId = 0;
        int currentRoomId = 0;
        if (entities.Authentication.getCurrentUser() != null) {
            currentUserId = entities.Authentication.getCurrentUser().getId();
            currentRoomId = entities.Authentication.getCurrentUser().getRoomId();
        }

        for (Event ev : dayEvents) {
            HBox eventRow = new HBox(12);
            eventRow.setAlignment(Pos.CENTER_LEFT);
            eventRow.setPadding(new Insets(8, 12, 8, 12));
            eventRow.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-border-width: 1;");

            Label timeAndTitle = new Label(ev.getTimeFormatted() + "  |  " + ev.getName());
            timeAndTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            timeAndTitle.setStyle("-fx-cursor: hand;"); 
            
            if (ev.getType().equals("BILL")) {
                timeAndTitle.setTextFill(Color.web("#B91C1C"));
            } else if (ev.getType().equals("ISSUE")) {
                timeAndTitle.setTextFill(Color.web("#1D4ED8")); 
            } else if (ev.getIsAccepted() == 0) {
                timeAndTitle.setTextFill(Color.web("#34D399")); 
            } else if (ev.getIsAccepted() == 1) {
                timeAndTitle.setTextFill(Color.web("#047857")); 
            }

            timeAndTitle.setOnMouseClicked(e -> viewEvent(ev));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            eventRow.getChildren().addAll(timeAndTitle, spacer);

            boolean isBillOrIssue = ev.getType().equals("BILL") || ev.getType().equals("ISSUE");
            boolean isCreator = (ev.getCreatedBy() == currentUserId);

            if (!isBillOrIssue) {
                if (isCreator) {
                    Button deleteBtn = new Button("Delete");
                    deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;");
                    deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;"));
                    deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 8; -fx-cursor: hand;"));
                    deleteBtn.setOnAction(e -> deleteEvent(ev));
                    eventRow.getChildren().add(deleteBtn);
                } else if (ev.getIsAccepted() == 0) {
                    if (!hasUserVoted(ev.getEventId(), currentUserId)) {
                        Button voteYesBtn = new Button("✓");
                        Button voteNoBtn = new Button("✕");
                        
                        String voteYesStyle = "-fx-background-color: #D1FAE5; -fx-text-fill: #10B981; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;";
                        String voteNoStyle = "-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;";
                        
                        voteYesBtn.setStyle(voteYesStyle);
                        voteYesBtn.setOnMouseEntered(e -> voteYesBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
                        voteYesBtn.setOnMouseExited(e -> voteYesBtn.setStyle(voteYesStyle));

                        voteNoBtn.setStyle(voteNoStyle);
                        voteNoBtn.setOnMouseEntered(e -> voteNoBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-cursor: hand;"));
                        voteNoBtn.setOnMouseExited(e -> voteNoBtn.setStyle(voteNoStyle));
                        
                        final int finalRoomId = currentRoomId;
                        final int finalUserId = currentUserId;
                        
                        voteYesBtn.setOnAction(e -> {
                            registerUserVote(ev, finalUserId, finalRoomId, "YES");
                            loadEventsFromDatabase();
                            returnCalendar();
                        });
                        
                        voteNoBtn.setOnAction(e -> {
                            registerUserVote(ev, finalUserId, finalRoomId, "NO");
                            loadEventsFromDatabase();
                            returnCalendar();
                        });

                        eventRow.getChildren().addAll(voteYesBtn, voteNoBtn);
                    } else {
                        Label votedLbl = new Label("Voted");
                        votedLbl.setFont(Font.font("Segoe UI", 12));
                        votedLbl.setTextFill(Color.GRAY);
                        eventRow.getChildren().add(votedLbl);
                    }
                }
            }

            eventsContainer.getChildren().add(eventRow);
        }
    }

    public void addEvent() {
        EventScreen eventScreen = new EventScreen(calendar, this, prefilledDateForForm);
        eventScreen.show();
    }

    public void viewEvent(Event event) {
        EventScreen eventScreen = new EventScreen(event, this);
        eventScreen.show();
    }

    public void deleteEvent(Event event) {
        ConfirmationScreen confirmationScreen = new ConfirmationScreen(
            () -> {
                deleteEventFromDatabase(event); 
                returnCalendar();
            },
            () -> {
                returnCalendar(); 
            }
        );
        confirmationScreen.show();
    }

    public BorderPane getRoot() { return root; }
    public int getSelectedDay() { return selectedDay; }
    public LocalDate getCurrentLocalDate() { return currentLocalDate; }
}