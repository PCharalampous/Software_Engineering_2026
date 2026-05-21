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
        this.prefilledDateForForm = currentLocalDate; // Αρχικοποίηση με τη σημερινή ημερομηνία
        this.backAction = backAction;
        createUI();
        loadEventsFromDatabase(); 
    }

    public void display() {
        this.primaryStage = new Stage();
        Scene scene = new Scene(root, 420, 550); 
        primaryStage.setTitle("HOMY - Calendar");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
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

        String query = "SELECT event_id, event_name, event_description, event_date, event_time, event_type, is_accepted " +
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
                    
                    if (sqlDate != null) {
                        LocalDate ld = sqlDate.toLocalDate();
                        Event ev = new Event(id, ld.getDayOfMonth(), ld.getMonthValue(), ld.getYear(), time, name, type, isAccepted, desc);
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
            stmt.executeUpdate();
            calendar.removeEvent(ev);
        } catch (SQLException e) {
            System.err.println("Database delete error: " + e.getMessage());
        }
    }

    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: white;");
        root.setPadding(new Insets(10));

        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        
        Button backBtn = new Button("← Back to Hub");
        backBtn.setStyle("-fx-background-color: transparent; -fx-font-weight: bold; -fx-text-fill: #1E3A5F; -fx-cursor: hand;");
        backBtn.setOnAction(e -> {
            primaryStage.close();
            if (backAction != null) {
                backAction.run();
            }
        });
        
        Label mainTitle = new Label("Calendar");
        mainTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        
        HBox navigationBox = new HBox(5);
        navigationBox.setAlignment(Pos.CENTER);
        
        Button prevMonthBtn = new Button("<");
        Button nextMonthBtn = new Button(">");
        
        prevMonthBtn.setMinWidth(30);
        prevMonthBtn.setMaxWidth(30);
        nextMonthBtn.setMinWidth(30);
        nextMonthBtn.setMaxWidth(30);
        
        monthTitle = new Label();
        monthTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        monthTitle.setPrefWidth(160);
        monthTitle.setMinWidth(160);
        monthTitle.setMaxWidth(160);
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
        calendarGrid.setStyle("-fx-border-color: black; -fx-border-width: 1 0 1 0;");

        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        
        Label eventsHeader = new Label("Events");
        eventsHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        eventsHeader.setStyle("-fx-border-color: black; -fx-border-width: 0 0 1 0;");
        eventsHeader.setMaxWidth(Double.MAX_VALUE);

        eventsContainer = new VBox(5);
        
        ScrollPane eventScrollPane = new ScrollPane(eventsContainer);
        eventScrollPane.setFitToWidth(true);
        eventScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        eventScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        eventScrollPane.setMinHeight(82);   
        eventScrollPane.setPrefHeight(82);
        eventScrollPane.setMaxHeight(82); 
        eventScrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        HBox btnContainer = new HBox();
        btnContainer.setAlignment(Pos.BOTTOM_RIGHT);
        Button addEventBtn = new Button("Add Event");
        
        addEventBtn.setOnAction(e -> {
            // Αν πατηθεί το γενικό κουμπί, παίρνει ως προεπιλογή την ήδη επιλεγμένη μέρα του ημερολογίου
            this.prefilledDateForForm = LocalDate.of(currentLocalDate.getYear(), currentLocalDate.getMonthValue(), selectedDay);
            addEvent();
        });
        btnContainer.getChildren().add(addEventBtn);

        bottomBox.getChildren().addAll(eventsHeader, eventScrollPane, btnContainer);
        
        VBox centerContainer = new VBox(10);
        centerContainer.getChildren().addAll(calendarGrid, bottomBox);
        root.setCenter(centerContainer);
    }

    public void selectDay(int day) {
        this.selectedDay = day;
        this.prefilledDateForForm = LocalDate.of(currentLocalDate.getYear(), currentLocalDate.getMonthValue(), day);
        returnCalendar(); 
        returnEvent();
    }

    public void returnCalendar() {
        String monthName = currentLocalDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
        monthTitle.setText(monthName + " " + currentLocalDate.getYear());

        calendarGrid.getChildren().clear();
        
        String[] daysOfWeek = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            dayLabel.setPrefSize(50, 25);
            dayLabel.setMinSize(50, 25);
            dayLabel.setMaxSize(50, 25);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setStyle("-fx-border-color: black; -fx-border-width: 1;");
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
                    emptyLabel.setPrefSize(50, 40);
                    emptyLabel.setMinSize(50, 40);
                    emptyLabel.setMaxSize(50, 40);
                    emptyLabel.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: #f5f5f5;");
                    calendarGrid.add(emptyLabel, col, row);
                } else if (currentDay <= daysInMonth) {
                    final int day = currentDay;
                    VBox dayCell = new VBox(2);
                    dayCell.setPadding(new Insets(2));
                    dayCell.setPrefSize(50, 40);
                    dayCell.setMinSize(50, 40);
                    dayCell.setMaxSize(50, 40);
                    
                    if (day == selectedDay) {
                        dayCell.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: #e0f7fa;");
                    } else {
                        dayCell.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: white;");
                    }
                    
                    Label dayNum = new Label(String.valueOf(day));
                    dayNum.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
                    HBox dotsBox = new HBox(2);
                    dotsBox.setAlignment(Pos.CENTER_LEFT);
                    
                    List<Event> dayEvents = calendar.getEvents().stream()
                            .filter(e -> e.getDate() == day 
                                      && e.getMonth() == currentLocalDate.getMonthValue() 
                                      && e.getYear() == currentLocalDate.getYear())
                            .collect(Collectors.toList());
                    for (Event e : dayEvents) {
                        Circle dot = new Circle(3);
                        if (e.getType().equals("BILL")) dot.setFill(Color.DARKRED);
                        else if (e.getType().equals("ISSUE")) dot.setFill(Color.BLUE);
                        else if (e.getIsAccepted() == 1) dot.setFill(Color.GREEN);
                        else dot.setFill(Color.LIGHTGREEN);
                        dotsBox.getChildren().add(dot);
                    }

                    dayCell.getChildren().addAll(dayNum, dotsBox);
                    
                    // ΔΙΟΡΘΩΘΗΚΕ: Σωστή διαχείριση μονού και διπλού κλικ
                    dayCell.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            // Με διπλό κλικ, κλειδώνει η ημερομηνία και ανοίγει αμέσως η φόρμα
                            this.prefilledDateForForm = LocalDate.of(currentLocalDate.getYear(), currentLocalDate.getMonthValue(), day);
                            this.selectedDay = day;
                            addEvent();
                        } else {
                            // Με μονό κλικ, απλώς επιλέγεται η ημέρα
                            selectDay(day);
                        }
                    });

                    calendarGrid.add(dayCell, col, row);
                    currentDay++;
                } else {
                    Label emptyLabel = new Label("");
                    emptyLabel.setPrefSize(50, 40);
                    emptyLabel.setMinSize(50, 40);
                    emptyLabel.setMaxSize(50, 40);
                    emptyLabel.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: #f5f5f5;");
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

        for (Event ev : dayEvents) {
            HBox eventRow = new HBox(10);
            eventRow.setAlignment(Pos.CENTER_LEFT);
            eventRow.setPadding(new Insets(5));
            eventRow.setStyle("-fx-border-color: #ccc; -fx-border-width: 1;");

            Label timeAndTitle = new Label(ev.getTimeFormatted() + " | " + ev.getName());
            timeAndTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            timeAndTitle.setStyle("-fx-cursor: hand;"); 
            
            if (ev.getType().equals("BILL")) {
                timeAndTitle.setTextFill(Color.DARKRED);
            } else if (ev.getType().equals("ISSUE")) {
                timeAndTitle.setTextFill(Color.BLUE); 
            } else if (ev.getIsAccepted() == 0) {
                timeAndTitle.setTextFill(Color.web("#b2ff59")); 
            } else if (ev.getIsAccepted() == 1) {
                timeAndTitle.setTextFill(Color.GREEN); 
            }

            timeAndTitle.setOnMouseClicked(e -> viewEvent(ev));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            eventRow.getChildren().addAll(timeAndTitle, spacer);

            if (ev.getIsAccepted() == 0 && !ev.getType().equals("BILL") && !ev.getType().equals("ISSUE")) {
                Button voteYesBtn = new Button("✓");
                Button voteNoBtn = new Button("✕");
                
                voteYesBtn.setOnAction(e -> {
                    updateEventStatusInDatabase(ev, 1);
                    returnCalendar();
                });
                
                voteNoBtn.setOnAction(e -> {
                    deleteEventFromDatabase(ev);
                    returnCalendar();
                });
                
                Button deleteBtn = new Button("Delete");
                deleteBtn.setOnAction(e -> deleteEvent(ev));

                eventRow.getChildren().addAll(voteYesBtn, voteNoBtn, deleteBtn);
            } else {
                Button deleteBtn = new Button("Delete");
                deleteBtn.setOnAction(e -> deleteEvent(ev));
                eventRow.getChildren().add(deleteBtn);
            }

            eventsContainer.getChildren().add(eventRow);
        }
    }

    public void returnEvent() {
        System.out.println("returnEvent() executed: UI Elements updated for Day " + selectedDay);
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

    public void goBack() {
        System.out.println("goBack() executed: Context returned smoothly.");
    }

    public BorderPane getRoot() { return root; }
    public int getSelectedDay() { return selectedDay; }
    public LocalDate getCurrentLocalDate() { return currentLocalDate; }
}