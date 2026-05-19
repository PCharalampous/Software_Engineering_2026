package util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static Connection connection = null;

    /**
     * Επιστρέφει την ενεργή σύνδεση με τη βάση δεδομένων.
     * Αν δεν υπάρχει, τη δημιουργεί διαβάζοντας το αρχείο config.properties.
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                Properties props = new Properties();
                
                // Διαβάζει το αρχείο config.properties από τον φάκελο src
                try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties")) {
                    if (input == null) {
                        System.err.println("Σφάλμα: Δεν βρέθηκε το αρχείο config.properties στο src!");
                        return null;
                    }
                    props.load(input);
                }

                // Λήψη των στοιχείων σύνδεσης από το αρχείο properties
                String url = props.getProperty("db.url");
                String user = props.getProperty("db.username");
                String password = props.getProperty("db.password");

                // Φορτώνει τον MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Δημιουργεί τη σύνδεση με το Clever Cloud
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Επιτυχής σύνδεση στην κοινή βάση (Clever Cloud) μέσω του DatabaseManager!");
                
            } catch (Exception e) {
                System.err.println("Αποτυχία σύνδεσης μέσω του DatabaseManager!");
                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * Κλείνει την τρέχουσα σύνδεση με τη βάση δεδομένων αν είναι ανοιχτή.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Η σύνδεση με τη βάση έκλεισε επιτυχώς.");
            } catch (SQLException e) {
                System.err.println("Σφάλμα κατά το κλείσιμο της σύνδεσης:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Μέθοδος main αποκλειστικά για τη δοκιμή της σύνδεσης.
     * Μπορείς να τη διαγράψεις ή να την αφήσεις όταν τελειώσεις τις δοκιμές.
     */
    public static void main(String[] args) {
        System.out.println("Έναρξη δοκιμής σύνδεσης απευθείας από τον DatabaseManager...");
        
        // Προσπάθεια σύνδεσης
        Connection conn = DatabaseManager.getConnection();
        
        if (conn != null) {
            System.out.println("Όλα λειτουργούν ρολόι! Η σύνδεση με το Clever Cloud πέτυχε.");
            
            // Κλείσιμο σύνδεσης μετά τη δοκιμή
            DatabaseManager.closeConnection();
        } else {
            System.err.println("Αποτυχία σύνδεσης! Σιγουρέψου ότι το αρχείο config.properties βρίσκεται στον φάκελο src.");
        }
    }
}