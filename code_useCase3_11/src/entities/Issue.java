package entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Issue {
    private final StringProperty title;
    private final StringProperty description;
    private final StringProperty type;
    private final StringProperty reportedBy;
    private final StringProperty payers;
    private final StringProperty date;

    // --- Constructor: Ενώνει όλα τα πεδία και των δύο κλάσεων ---
    public Issue(String title, String description, String type, String reportedBy, String payers, String date) {
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.type = new SimpleStringProperty(type);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.payers = new SimpleStringProperty(payers);
        this.date = new SimpleStringProperty(date);
    }

    // --- JavaFX Property Methods (Απαραίτητες για TableView CellValueFactory) ---
    public StringProperty titleProperty() { return title; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty typeProperty() { return type; }
    public StringProperty reportedByProperty() { return reportedBy; }
    public StringProperty payersProperty() { return payers; }
    public StringProperty dateProperty() { return date; }

    // --- Standard Getters (Επιστρέφουν καθαρό String) ---
    public String getTitle() { return title.get(); }
    public String getDescription() { return description.get(); }
    public String getType() { return type.get(); }
    public String getReportedBy() { return reportedBy.get(); }
    public String getPayers() { return payers.get(); }
    public String getDate() { return date.get(); }

    // --- Standard Setters (Αν χρειαστεί να αλλάξεις τιμές programmatic) ---
    public void setTitle(String value) { this.title.set(value); }
    public void setDescription(String value) { this.description.set(value); }
    public void setType(String value) { this.type.set(value); }
    public void setReportedBy(String value) { this.reportedBy.set(value); }
    public void setPayers(String value) { this.payers.set(value); }
    public void setDate(String value) { this.date.set(value); }
}