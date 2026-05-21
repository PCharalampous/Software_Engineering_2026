package entities;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Bill {
    private final SimpleStringProperty type;
    private final SimpleDoubleProperty amount;
    private final SimpleStringProperty date;
    private final SimpleStringProperty payers;
    private final SimpleStringProperty status; // "Pending" or "Paid"

    public Bill(String type, double amount, String date, String payers, String status) {
        this.type = new SimpleStringProperty(type);
        this.amount = new SimpleDoubleProperty(amount);
        this.date = new SimpleStringProperty(date);
        this.payers = new SimpleStringProperty(payers);
        this.status = new SimpleStringProperty(status);
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
    
    public void setStatus(String status) { this.status.set(status); }
    
    // FIXED: Uses the property wrapper's .set() method safely 
    public void setDate(String date) { 
        this.date.set(date); 
    }
}