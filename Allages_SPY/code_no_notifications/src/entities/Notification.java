package entities;

import javafx.scene.control.Alert;

public class Notification {
    public int notificationId; // Προσθήκη ID από τη βάση
    public String category;
    public String text;
    public String detail;
    public String target;
    public String tagColor;
    public boolean read;

    // Νέος Constructor που παίρνει δεδομένα κατευθείαν από τη MySQL
    public Notification(int id, String cat, String txt, String det, String tgt, String tc, boolean isRead) {
        this.notificationId = id;
        this.category = cat;
        this.text = txt;
        this.detail = det;
        this.target = tgt;
        this.tagColor = tc;
        this.read = isRead; 
    }

    // Παλιός Constructor (τον κρατάμε για συμβατότητα σε περίπτωση που καλείται αλλού)
    public Notification(String cat, String txt, String det, String tgt, String tc) {
        this.category = cat;
        this.text = txt;
        this.detail = det;
        this.target = tgt;
        this.tagColor = tc;
        this.read = false; 
    }

    public void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification - " + this.category);
        alert.setHeaderText(this.text);
        alert.setContentText(this.detail);
        alert.show();
    }

    public static void makeNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); 
    }

    public static void makeNotification(String title, String message) {
        System.out.println("====== [NOTIFICATION TRIGGERED] ======");
        System.out.println("Title: " + title);
        System.out.println("Message: " + message);
        System.out.println("=======================================");
    }

    public int getNotificationId() { return notificationId; }
    public String getCategory() { return category; }
    public String getText() { return text; }
    public String getDetail() { return detail; }
    public String getTarget() { return target; }
    public String getTagColor() { return tagColor; }
    public boolean isRead() { return read; }
    
    public void setRead(boolean read) { this.read = read; }
}