package entities;

public class Item {
    private int itemId; // Το ID από τη βάση δεδομένων για το sync
    private String name;
    private int quantity; 
    private boolean isChecked; 

    // Constructor για νέα αντικείμενα (πριν μπουν στη βάση)
    public Item(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        this.isChecked = false;
    }

    // Constructor για αντικείμενα που έρχονται έτοιμα από τη βάση
    public Item(int itemId, String name, int quantity, boolean isChecked) {
        this.itemId = itemId;
        this.name = name;
        this.quantity = quantity;
        this.isChecked = isChecked;
    }

    // Getter και Setter για το itemId
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isChecked() { return isChecked; }

    public void swap() {
        this.isChecked = !this.isChecked;
    }

    public void delete() {
        // Λογική εκκαθάρισης
    }
}