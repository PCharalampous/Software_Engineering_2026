package usecase7;

public class Notification {
    public static void makeNotification(String title, String message) {
        // Προσομοίωση αποστολής ειδοποίησης στο σύστημα
        System.out.println("====== [NOTIFICATION TRIGGERED] ======");
        System.out.println("Title: " + title);
        System.out.println("Message: " + message);
        System.out.println("=======================================");
    }
}
