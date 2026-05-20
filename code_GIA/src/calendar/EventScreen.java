package calendar;

import entities.Calendar;
import entities.Event;
import ui.ErrorScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventScreen {
    private Calendar calendar;
    private CalendarScreen calendarScreen;
    private Stage stage;

    private TextField titleField;
    private DatePicker dayField; 
    private Spinner<Integer> hourSpinner;
    private Spinner<Integer> minuteSpinner;
    private TextArea descField;
    private LocalDate prefilledDate;
    
    private Event eventToView = null;

    // Κατασκευαστής για Προσθήκη Συμβάντος
    public EventScreen(Calendar calendar, CalendarScreen calendarScreen, LocalDate prefilledDate) {
        this.calendar = calendar;
        this.calendarScreen = calendarScreen;
        this.prefilledDate = prefilledDate;
        createUI();
    }

    // Κατασκευαστής για Προβολή Συμβάντος (Read-Only Mode)
    public EventScreen(Event eventToView, CalendarScreen calendarScreen) {
        this.eventToView = eventToView;
        this.calendarScreen = calendarScreen;
        createUI();
    }

    private void createUI() {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        
        boolean isViewMode = (eventToView != null);
        stage.setTitle(isViewMode ? "View Event Details" : "Add Event");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setHgap(10);
        grid.setVgap(10);

        ColumnConstraints colLabels = new ColumnConstraints();
        colLabels.setMinWidth(160);
        colLabels.setPrefWidth(160);
        grid.getColumnConstraints().add(colLabels);

        int row = 0;

        // 1. Τίτλος Συμβάντος
        grid.add(new Label(isViewMode ? "Title:" : "Title (Name):"), 0, row);
        if (isViewMode) {
            Label titleLabel = new Label(eventToView.getName());
            titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            grid.add(titleLabel, 1, row);
        } else {
            titleField = new TextField();
            grid.add(titleField, 1, row);
        }
        row++;
        
        // 2. Ημερομηνία Συμβάντος
        grid.add(new Label(isViewMode ? "Day:" : "Day (DD/MM/YYYY):"), 0, row);
        if (isViewMode) {
            // ΔΙΟΡΘΩΘΗΚΕ: Ανάκτηση και εμφάνιση του σωστού Μήνα και Έτους του αντικειμένου
            String formattedDate = String.format("%02d/%02d/%04d", eventToView.getDate(), eventToView.getMonth(), eventToView.getYear());
            Label dayLabel = new Label(formattedDate);
            dayLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            grid.add(dayLabel, 1, row);
        } else {
            dayField = new DatePicker();
            dayField.setPromptText("DD/MM/YYYY");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dayField.setConverter(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    if (date != null) return formatter.format(date);
                    return "";
                }
                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.trim().isEmpty()) {
                        try { return LocalDate.parse(string, formatter); } catch (Exception e) { return null; }
                    }
                    return null;
                }
            });

            if (prefilledDate != null) {
                dayField.setValue(prefilledDate);
            } else {
                dayField.setValue(null);
            }

            dayField.getEditor().setTextFormatter(new TextFormatter<>(change -> {
                if (!change.isContentChange()) return change;
                if (!change.getText().matches("[0-9/]*")) return null;
                
                String oldText = change.getControlText();
                if (change.isDeleted() && (change.getRangeEnd() - change.getRangeStart() == 1)) {
                    int start = change.getRangeStart();
                    if (start < oldText.length() && oldText.charAt(start) == '/') {
                        if (start > 0) change.setRange(start - 1, start + 1); 
                        else return null;
                    }
                }
                
                String proposedText = change.getControlNewText();
                String digits = proposedText.replaceAll("[^0-9]", "");
                if (digits.length() > 8) return null;
                
                try {
                    if (digits.length() >= 2) {
                        int dayVal = Integer.parseInt(digits.substring(0, 2));
                        if (dayVal < 1 || dayVal > 31) return null;
                    }
                    if (digits.length() >= 4) {
                        int monthVal = Integer.parseInt(digits.substring(2, 4));
                        if (monthVal < 1 || monthVal > 12) return null;
                    }
                } catch (NumberFormatException e) { return null; }
                
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    sb.append(digits.charAt(i));
                    if (sb.length() == 2 || sb.length() == 5) sb.append("/");
                }
                
                String formatted = sb.toString();
                change.setText(formatted);
                change.setRange(0, oldText.length());
                change.setCaretPosition(formatted.length());
                change.setAnchor(formatted.length());
                return change;
            }));
            grid.add(dayField, 1, row);
        }
        row++;

        // 3. Ώρα Συμβάντος
        grid.add(new Label(isViewMode ? "Time:" : "Time (HH:mm):"), 0, row);
        if (isViewMode) {
            Label timeLabel = new Label(eventToView.getTimeFormatted());
            timeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            grid.add(timeLabel, 1, row);
        } else {
            hourSpinner = new Spinner<>();
            SpinnerValueFactory.IntegerSpinnerValueFactory hourFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
            hourFactory.setWrapAround(true); 
            hourFactory.setConverter(new StringConverter<Integer>() {
                @Override public String toString(Integer value) { return String.format("%02d", value); }
                @Override public Integer fromString(String string) { return Integer.parseInt(string); }
            });
            hourSpinner.setValueFactory(hourFactory);
            hourSpinner.setPrefWidth(70);

            minuteSpinner = new Spinner<>();
            SpinnerValueFactory.IntegerSpinnerValueFactory minuteFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
            minuteFactory.setWrapAround(true); 
            minuteFactory.setConverter(new StringConverter<Integer>() {
                @Override public String toString(Integer value) { return String.format("%02d", value); }
                @Override public Integer fromString(String string) { return Integer.parseInt(string); }
            });
            minuteSpinner.setValueFactory(minuteFactory);
            minuteSpinner.setPrefWidth(70);

            HBox timeBox = new HBox(5);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            timeBox.getChildren().addAll(hourSpinner, new Label(":"), minuteSpinner);
            grid.add(timeBox, 1, row);
        }
        row++;

        // 4. Τύπος Συμβάντος
        if (isViewMode) {
            grid.add(new Label("Event Type:"), 0, row);
            Label typeLabel = new Label(eventToView.getType());
            typeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
            grid.add(typeLabel, 1, row);
            row++;
        }

        // 5. Περιγραφή / Σχόλια
        if (isViewMode) {
            String description = eventToView.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                grid.add(new Label("Description:"), 0, row);
                Label descLabel = new Label(description);
                descLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
                descLabel.setWrapText(true);
                descLabel.setMaxWidth(260); 
                grid.add(descLabel, 1, row);
                row++;
            }
        } else {
            grid.add(new Label("Description (Optional):"), 0, row);
            descField = new TextArea();
            descField.setPrefRowCount(3);
            descField.setWrapText(true); 
            grid.add(descField, 1, row);
            row++;
        }

        // 6. Κουμπί Επιβεβαίωσης
        if (!isViewMode) {
            Button confirmBtn = new Button("Confirm");
            confirmBtn.setOnAction(e -> insertEventStatus()); 

            HBox btnBox = new HBox(confirmBtn);
            btnBox.setAlignment(Pos.CENTER_RIGHT);
            grid.add(btnBox, 1, row);
        }

        if (isViewMode) {
            grid.setPrefWidth(460);
            Scene scene = new Scene(grid);
            stage.setScene(scene);
            stage.sizeToScene(); 
        } else {
            Scene scene = new Scene(grid, 460, 280);
            stage.setScene(scene);
        }
    }

    public void insertEventStatus() {
        try {
            String name = titleField.getText();
            
            LocalDate selectedDate = null;
            String dateText = dayField.getEditor().getText();
            if (dateText != null && !dateText.trim().isEmpty()) {
                selectedDate = dayField.getConverter().fromString(dateText);
            }
            
            int hours = hourSpinner.getValue();
            int minutes = minuteSpinner.getValue();
            int time = (hours * 100) + minutes;

            if (name.isEmpty() || selectedDate == null || time < 0 || time > 2359) {
                throw new Exception("Validation Error");
            }

            // ΔΙΟΡΘΩΘΗΚΕ: Εξαγωγή Ημέρας, Μήνας και Έτους από το DatePicker για αποθήκευση
            int date = selectedDate.getDayOfMonth();
            int month = selectedDate.getMonthValue();
            int year = selectedDate.getYear();
            String optionalDesc = descField.getText();

            // Δημιουργία με αποθήκευση και των 3 στοιχείων ημερομηνίας
            Event newEvent = new Event(date, month, year, time, name, "GENERAL", 0, optionalDesc);
            calendar.addEvent(newEvent);
            
            calendar.update();
            newEvent.update();

            calendarScreen.returnCalendar();
            goBack(); 

        } catch (Exception ex) {
            // ΔΙΟΡΘΩΣΗ: Περνάμε το goBack() ως Runnable λάμδα για να εκτελεστεί όταν πατηθεί το OK
            ErrorScreen errorScreen = new ErrorScreen("Λανθασμένα στοιχεία εισαγωγής συμβάντος!", () -> goBack());
            errorScreen.show();
        }
    }

    public void goBack() {
        stage.close();
    }

    public void show() { stage.showAndWait(); }
}