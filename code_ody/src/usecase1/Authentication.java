package usecase1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Authentication  {
	
	private String email;
	private String password;
	private Boolean isCorrect;
	private Integer id;
	private DatabaseManager dataBaseMng;
	private Connection conn;
//	private User currentUser;

	
	public Authentication(Connection conn) {
		dataBaseMng = new DatabaseManager();
		this.conn = conn;
	}
	
	public Boolean userLogIn() {
		isCorrect = true;
		
		ResultSet rs = dataBaseMng.getTable("users");
//		while (rs.next()) {
//        	for (int i = 1; i <= columnCount; i++) {
//
//                System.out.print(rs.getString(i) + " | ");
//            }
//
//            System.out.println();
//        }
		
		return isCorrect;
	}
	
	public void createAcc(String usrEmail, String usrPass) throws Exception {
		
		
			String sql = """
					INSERT INTO users
					(username, display_name, email, password_hash, bio, preferences, room_id)
					VALUES

					(
					    'makis99',
					    'Makis Kosta',
					    'makis99@example.com',
					    'hashed_password_1',
					    'Computer Science student',
					    'Quiet roommates preferred',
					    NULL
					),

					(
					    'anna_dev',
					    'Anna Papadopoulou',
					    'anna.dev@example.com',
					    'hashed_password_2',
					    'Loves Java and databases',
					    'Non-smokers only',
					    NULL
					),

					(
					    'george21',
					    'George Nikolaou',
					    'george21@example.com',
					    'hashed_password_3',
					    'Enjoys gaming and music',
					    'Pet friendly apartment',
					    NULL
					);
					""";

//					try 
//					{
//						this.conn = dataBaseMng.getConnection();
//					    PreparedStatement stmt = this.conn.prepareStatement(sql);
//					
//
//					    int rows = stmt.executeUpdate();
//
//					    System.out.println("Inserted rows: " + rows);
//					    dataBaseMng.getTable("users");
//					}
//					 catch (SQLException e) {
//					    e.printStackTrace();
//					}
			System.out.println("Inserted rows: " + 0+"apla einai se sxolia olo to method gia auto!");
				
	}
	
	private void authenticateUser() {
		
	}
	
	public void userLogOut() {
	
		
	}
	
	public void setUserEmail(String email) {
		this.email = email; 
	}
	
	public void setUserPassword(String password) {
		this.password = password; 
	}
	
	
	
}
