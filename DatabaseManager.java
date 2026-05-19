package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
	private static final String URL = "jdbc:mysql://172.23.25.199:3306/homy_db?serverTimezone=UTC";
	private static final String USER = "dbadmin";
	private static final String PASS = "1234";
	
	private static Connection connection = null;
	
	public static Connection getConnection() {
		if(connection == null) {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(URL, USER, PASS);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
				System.err.println("Failed to connect to the database!");
			}
		}
		return connection;
	}

}