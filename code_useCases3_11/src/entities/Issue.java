package entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Issue {
   
    private int id;
    private final StringProperty type;
    private final StringProperty reportedBy;
    private final StringProperty payers;
    private final StringProperty date;
    private final StringProperty status = new SimpleStringProperty("Pending");

    // --- Constructor: Ενώνει όλα τα πεδία και των δύο κλάσεων ---
    public Issue(int id, String type, String reportedBy, String payers, String date) {
        
        this.id = id;
        this.type = new SimpleStringProperty(type);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.payers = new SimpleStringProperty(payers);
        this.date = new SimpleStringProperty(date);
    }
    
    public Issue(String type, String reportedBy, String payers, String date) {
        this.id = 0; // Temporary ID 0
        this.type = new SimpleStringProperty(type);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.payers = new SimpleStringProperty(payers);
        this.date = new SimpleStringProperty(date);
    }

    // --- JavaFX Property Methods (Απαραίτητες για TableView CellValueFactory) ---
   
    public StringProperty typeProperty() { return type; }
    public StringProperty reportedByProperty() { return reportedBy; }
    public StringProperty payersProperty() { return payers; }
    public StringProperty dateProperty() { return date; }
    public StringProperty statusProperty() { return status; }
    // --- Standard Getters (Επιστρέφουν καθαρό String) ---
   
    public int getId() { return id; }
    public String getType() { return type.get(); }
    public String getReportedBy() { return reportedBy.get(); }
    public String getPayers() { return payers.get(); }
    public String getDate() { return date.get(); }
    public String getStatus() { return status.get(); }
    // --- Standard Setters (Αν χρειαστεί να αλλάξεις τιμές programmatic) ---
    
    public void setId(int id) { this.id = id; }
    public void setType(String value) { this.type.set(value); }
    public void setReportedBy(String value) { this.reportedBy.set(value); }
    public void setPayers(String value) { this.payers.set(value); }
    public void setDate(String value) { this.date.set(value); }
    public void setStatus(String status) { this.status.set(status); }
}