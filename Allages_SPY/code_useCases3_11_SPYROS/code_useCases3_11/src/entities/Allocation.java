package entities;

import java.io.File;
import java.time.LocalDate;
import java.util.Map;

public class Allocation {
    private int allocationId; // Το ID από τη βάση δεδομένων για το sync
    private LocalDate date;
    private boolean isDone;
    private Map<String, Double> memberAmounts;
    private File imageFile;       
    private double totalAmount;   
    private String receiver;      

    // Constructor για νέα allocations (πριν μπουν στη βάση)
    public Allocation(LocalDate date, Map<String, Double> memberAmounts, File imageFile, double totalAmount, String receiver) {
        this.date = date;
        this.memberAmounts = memberAmounts;
        this.imageFile = imageFile;
        this.totalAmount = totalAmount;
        this.receiver = receiver;
        this.isDone = false;
    }

    // Constructor για allocations που έρχονται έτοιμα από τη βάση
    public Allocation(int allocationId, LocalDate date, Map<String, Double> memberAmounts, File imageFile, double totalAmount, String receiver, boolean isDone) {
        this.allocationId = allocationId;
        this.date = date;
        this.memberAmounts = memberAmounts;
        this.imageFile = imageFile;
        this.totalAmount = totalAmount;
        this.receiver = receiver;
        this.isDone = isDone;
    }

    // Getter και Setter για το allocationId
    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }

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