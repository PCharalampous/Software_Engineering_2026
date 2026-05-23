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
    
    

    /**
     * Εμφανίζει ένα JavaFX Alert χρησιμοποιώντας τα δεδομένα 
     * αυτού του συγκεκριμένου αντικειμένου Notification.
     */
    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification - " + this.category);
        alert.setHeaderText(this.text);
        alert.setContentText(this.detail);
        alert.show();
    }

    /**
     * Static μέθοδος (από το δεύτερο project) για γρήγορη εμφάνιση 
     * ενός απλού μηνύματος σε JavaFX Alert χωρίς τη δημιουργία αντικειμένου.
     */
    public static void makeNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); 
    }

    /**
     * ΝΕΑ Static μέθοδος (από το usecase7) για την προσομοίωση/καταγραφή
     * αποστολής μιας ειδοποίησης στο σύστημα μέσω της κονσόλας.
     */
    public static void makeNotification(String title, String message) {
        System.out.println("====== [NOTIFICATION TRIGGERED] ======");
        System.out.println("Title: " + title);
        System.out.println("Message: " + message);
        System.out.println("=======================================");
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