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
    private Runnable backAction; // Για τη διαχείριση της επιστροφής στο Hub
    private Stage primaryStage;

    // Constructor που δέχεται το backAction
    public CalendarScreen(Runnable backAction) {
        this.calendar = new Calendar(); 
        this.currentLocalDate = LocalDate.now(); 
        this.selectedDay = currentLocalDate.getDayOfMonth(); 
        this.backAction = backAction;
        createUI();
    }

    // Η μέθοδος αναλαμβάνει πλέον εξολοκλήρου το χτίσιμο και την εμφάνιση του Stage
    public void display() {
        this.primaryStage = new Stage();
        Scene scene = new Scene(root, 420, 580); // Ελαφρώς αυξημένο ύψος για να χωράει και το back button
        primaryStage.setTitle("HOMY - Calendar");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        returnCalendar();
    }

    private void createUI() {
        root = new BorderPane();
        root.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: white;");
        root.setPadding(new Insets(10));

        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        
        // Προσθήκη κουμπιού επιστροφής στην κορυφή του Calendar
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
            this.prefilledDateForForm = null;
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
                    
                    dayCell.setOnMouseClicked(e -> {
                        selectDay(day);
                        if (e.getClickCount() == 2) {
                            this.prefilledDateForForm = LocalDate.of(currentLocalDate.getYear(), currentLocalDate.getMonthValue(), day);
                            addEvent();
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
                    ev.setIsAccepted(1);
                    ev.update();
                    calendar.update();
                    returnCalendar();
                });
                
                voteNoBtn.setOnAction(e -> {
                    ev.delete();
                    calendar.delete();
                    calendar.removeEvent(ev);
                    returnCalendar();
                });
                
                Button deleteBtn = new Button("Delete");
                deleteBtn.setOnAction(e -> deleteEvent(ev));

                eventRow.getChildren().addAll(voteYesBtn, voteNoBtn, deleteBtn);
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
                event.delete();
                calendar.delete();
                calendar.removeEvent(event);
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