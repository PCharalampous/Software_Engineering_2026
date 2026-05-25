package entities;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class Issue {
   
    private int id;
    private final StringProperty type;
    private final StringProperty reportedBy;
    private final StringProperty payers;
    private final StringProperty date;
    private final StringProperty status = new SimpleStringProperty("Pending");
    private final StringProperty approvalStatus;

    // 🌟 Added Option A fields to track vote counts persistently from DB
    private final IntegerProperty approveVotes;
    private final IntegerProperty rejectVotes;

    // --- Constructor: Με ID (Χρησιμοποιείται κατά το διάβασμα από τη Βάση) ---
    public Issue(int id, String type, String reportedBy, String payers, String date, String approvalStatus) {
        this.id = id;
        this.type = new SimpleStringProperty(type);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.payers = new SimpleStringProperty(payers);
        this.date = new SimpleStringProperty(date);
        this.approvalStatus = new SimpleStringProperty(approvalStatus);
        this.approveVotes = new SimpleIntegerProperty(0);
        this.rejectVotes = new SimpleIntegerProperty(0);
    }
    
    // --- Constructor: Χωρίς ID (Χρησιμοποιείται κατά τη δημιουργία) ---
    public Issue(String type, String reportedBy, String payers, String date, String approvalStatus) {
        this.id = 0; 
        this.type = new SimpleStringProperty(type);
        this.reportedBy = new SimpleStringProperty(reportedBy);
        this.payers = new SimpleStringProperty(payers);
        this.date = new SimpleStringProperty(date);
        this.approvalStatus = new SimpleStringProperty(approvalStatus);
        this.approveVotes = new SimpleIntegerProperty(0);
        this.rejectVotes = new SimpleIntegerProperty(0);
    }

    // --- JavaFX Property Methods ---
    public StringProperty typeProperty() { return type; }
    public StringProperty reportedByProperty() { return reportedBy; }
    public StringProperty payersProperty() { return payers; }
    public StringProperty dateProperty() { return date; }
    public StringProperty statusProperty() { return status; }
    public StringProperty approvalStatusProperty() { return approvalStatus; }
    
    // 🌟 Option A Properties
    public IntegerProperty approveVotesProperty() { return approveVotes; }
    public IntegerProperty rejectVotesProperty() { return rejectVotes; }

    // --- Standard Getters ---
    public int getId() { return id; }
    public String getType() { return type.get(); }
    public String getReportedBy() { return reportedBy.get(); }
    public String getPayers() { return payers.get(); }
    public String getDate() { return date.get(); }
    public String getStatus() { return status.get(); }
    public String getApprovalStatus() { return approvalStatus.get(); }
    
    // 🌟 Option A Getters
    public int getApproveVotes() { return approveVotes.get(); }
    public int getRejectVotes() { return rejectVotes.get(); }
    
    // --- Standard Setters ---
    public void setId(int id) { this.id = id; }
    public void setType(String value) { this.type.set(value); }
    public void setReportedBy(String value) { this.reportedBy.set(value); }
    public void setPayers(String value) { this.payers.set(value); }
    public void setDate(String value) { this.date.set(value); }
    public void setStatus(String status) { this.status.set(status); }
    public void setApprovalStatus(String value) { this.approvalStatus.set(value); }
    
    // 🌟 Option A Setters
    public void setApproveVotes(int votes) { this.approveVotes.set(votes); }
    public void setRejectVotes(int votes) { this.rejectVotes.set(votes); }
}
