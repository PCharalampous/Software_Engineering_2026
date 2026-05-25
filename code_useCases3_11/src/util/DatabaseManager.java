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
     * ΔΙΟΡΘΩΣΗ: Αν η σύνδεση έχει κλείσει, τη δημιουργεί ξανά από την αρχή!
     */
    public static Connection getConnection() {
        try {
            // Έλεγχος αν η σύνδεση είναι null Ή αν έχει κλείσει από προηγούμενο try-with-resources
            if (connection == null || connection.isClosed()) {
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
            }
        } catch (Exception e) {
            System.err.println("Αποτυχία σύνδεσης μέσω του DatabaseManager!");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Κλείνει την τρέχουσα σύνδεση με τη βάση δεδομένων αν είναι ανοιχτή.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
                connection = null;
                System.out.println("Η σύνδεση με τη βάση έκλεισε επιτυχώς.");
            } catch (SQLException e) {
                System.err.println("Σφάλμα κατά το κλείσιμο της σύνδεσης:");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Έναρξη δοκιμής σύνδεσης απευθείας από τον DatabaseManager...");
        Connection conn = DatabaseManager.getConnection();
        if (conn != null) {
            System.out.println("Όλα λειτουργούν ρολόι! Η σύνδεση με το Clever Cloud πέτυχε.");
            DatabaseManager.closeConnection();
        } else {
            System.err.println("Αποτυχία σύνδεσης! Σιγουρέψου ότι το αρχείο config.properties βρίσκεται στον φάκελο src.");
        }
    }
}