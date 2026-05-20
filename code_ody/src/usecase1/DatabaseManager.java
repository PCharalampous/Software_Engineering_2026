package usecase1;

import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
	
    private Connection connection = null;

    /**
     * Επιστρέφει την ενεργή σύνδεση με τη βάση δεδομένων.
     * Αν δεν υπάρχει, τη δημιουργεί διαβάζοντας το αρχείο config.properties.
     */
    public Connection getConnection() {
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
    public void closeConnection() {
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
    
    public void showTables() {

        String sql = "SHOW TABLES";
        
        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
        ) {

            System.out.println("===== DATABASE TABLES =====");

            while (rs.next()) {
                System.out.println(rs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet getTable(String tableName) {
    	String sql = "SELECT * FROM "+tableName+"";
    	ResultSet rs = null;
    	int columnCount =0;
    	
    	
    	try { 
                Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                columnCount = metaData.getColumnCount();
                    
                
    	}
            catch (SQLException e) {
                e.printStackTrace();
            }
    	return rs;
    }
    
//    public void showTable(String tableName) {
//    	String sql = "SELECT * FROM "+tableName+"";
//    	ResultSet rs = null;
//    	int columnCount =0;
//
//        try {
//
//            ResultSetMetaData metaData = rs.getMetaData();
//            columnCount = metaData.getColumnCount();
//
//            while (rs.next()) {
//
//                for (int i = 1; i <= columnCount; i++) {
//                    System.out.print(rs.getString(i) + " ");
//                }
//
//                System.out.println();
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    
}