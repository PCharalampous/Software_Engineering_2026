package entities;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Bill {
    private final SimpleStringProperty type;
    private final SimpleDoubleProperty amount;
    private final SimpleStringProperty date;
    private final SimpleStringProperty payers;
    private final SimpleStringProperty status; // "Pending" or "Paid"
    private final SimpleStringProperty approvalStatus;
    
    // 🌟 Added Option A fields to track vote counts persistently from DB
    private final SimpleIntegerProperty approveVotes;
    private final SimpleIntegerProperty rejectVotes;
    
    // Purely internal Java tracker — no database column needed!
    private String creatorUsername = ""; 

    public Bill(String type, double amount, String date, String payers, String status, String approvalStatus) {
        this.type = new SimpleStringProperty(type);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleStringProperty(date);
        this.payers = new SimpleStringProperty(payers);
        this.status = new SimpleStringProperty(status);
        this.approvalStatus = new SimpleStringProperty(approvalStatus);
        this.approveVotes = new SimpleIntegerProperty(0);
        this.rejectVotes = new SimpleIntegerProperty(0);
    }

    public String getType() { return type.get(); }
    public SimpleStringProperty typeProperty() { return type; }

    public double getAmount() { return amount.get(); }
    public SimpleDoubleProperty amountProperty() { return amount; }

    public String getDate() { return date.get(); }
    public SimpleStringProperty dateProperty() { return date; }

    public String getPayers() { return payers.get(); }
    public SimpleStringProperty payersProperty() { return payers; }

    public String getStatus() { return status.get(); }
    public SimpleStringProperty statusProperty() { return status; }
    
    public String getApprovalStatus() { return approvalStatus.get(); }
    public SimpleStringProperty approvalStatusProperty() { return approvalStatus; }

    // 🌟 Option A Getters/Setters/Properties for votes tracking
    public int getApproveVotes() { return approveVotes.get(); }
    public SimpleIntegerProperty approveVotesProperty() { return approveVotes; }
    public void setApproveVotes(int votes) { this.approveVotes.set(votes); }

    public int getRejectVotes() { return rejectVotes.get(); }
    public SimpleIntegerProperty rejectVotesProperty() { return rejectVotes; }
    public void setRejectVotes(int votes) { this.rejectVotes.set(votes); }
    
    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public void setStatus(String status) { this.status.set(status); }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus.set(approvalStatus); }
    public void setDate(String date) { this.date.set(date); }
}
