package usecase7;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;

public class Allocation {
    private LocalDate date;
    private boolean isDone;
    private Map<String, Double> memberAmounts;
    private File imageFile;       
    private double totalAmount;   
    private String receiver;      // Αποθήκευση του συγκατοίκου που πλήρωσε

    public Allocation(LocalDate date, Map<String, Double> memberAmounts, File imageFile, double totalAmount, String receiver) {
        this.date = date;
        this.memberAmounts = memberAmounts;
        this.imageFile = imageFile;
        this.totalAmount = totalAmount;
        this.receiver = receiver;
        this.isDone = false;
    }

    public LocalDate getDate() { return date; }
    public File getImageFile() { return imageFile; }
    public double getTotalAmount() { return totalAmount; }
    public String getReceiver() { return receiver; }
    
    public void done() { this.isDone = true; }
    public boolean isDone() { return isDone; }
    public Map<String, Double> getMemberAmounts() { return memberAmounts; }
    
    public void delete() {
        // Λογική διαγραφής
    }
}