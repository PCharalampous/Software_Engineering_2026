package entities;

import javafx.scene.control.Alert;

public class Notification {
    // Πεδία δεδομένων (τα κρατάμε public για συμβατότητα, αλλά προσθέτουμε και getters)
    public String category;
    public String text;
    public String detail;
    public String target;
    public String tagColor;
    public boolean read;

    // --- Constructor: Δημιουργία αντικειμένου ειδοποίησης ---
    public Notification(String cat, String txt, String det, String tgt, String tc) {
        this.category = cat;
        this.text = txt;
        this.detail = det;
        this.target = tgt;
        this.tagColor = tc;
        this.read = false; // Αρχικά κάθε νέα ειδοποίηση είναι μη διαβασμένη
    }

    // --- Μέθοδος Στιγμιοτύπου (Instance Method): Εμφάνιση παραθύρου με τα δεδομένα του αντικειμένου ---
    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification - " + this.category);
        alert.setHeaderText(this.text);
        alert.setContentText(this.detail);
        alert.show();
    }

    // --- Static Μέθοδος: Από το 2ο Project για γρήγορη ειδοποίηση χωρίς δημιουργία αντικειμένου ---
    public static void makeNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); 
    }

    // --- Getters: Απαραίτητοι για τη μεταφορά δεδομένων σε screens άλλων πακέτων ---
    public String getCategory() { return category; }
    public String getText() { return text; }
    public String getDetail() { return detail; }
    public String getTarget() { return target; }
    public String getTagColor() { return tagColor; }
    public boolean isRead() { return read; }
    
    public void setRead(boolean read) { this.read = read; }
}