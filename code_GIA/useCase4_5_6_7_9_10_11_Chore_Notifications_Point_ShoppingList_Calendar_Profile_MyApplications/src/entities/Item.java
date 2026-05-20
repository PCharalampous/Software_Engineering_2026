package entities;

public class Item {
    // Attributes από το Domain Model
    private String name;
    private int quantity; // integer
    private boolean isChecked; // Εσωτερικό state για τον διαχωρισμό των λιστών

    public Item(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
        this.isChecked = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public boolean isChecked() { return isChecked; }

    // Methods από το Domain Model
    public void swap() {
        this.isChecked = !this.isChecked;
    }

    public void delete() {
        // Λογική εκκαθάρισης/αποδέσμευσης αντικειμένου
    }
}