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
	
	public Boolean userLogIn(String usrEmail, String usrPass) throws SQLException {
		isCorrect = false;
		
		ResultSet rs = dataBaseMng.getTable("users");
		
		while (rs.next()) {
			//in collumn: 4 is stored each user email and in collumn 5 the password
			System.out.println("!!!!!!user ftrom database: "+rs.getString(4)+" and user from log in ui: "+usrEmail+"");
			System.out.println("!!!!!!password ftrom database: "+rs.getString(5)+" and password from log in ui: "+usrPass+"");
			if((rs.getString(4).compareTo(usrEmail) == 0 ) && (rs.getString(5).compareTo(usrPass) == 0)) {
				System.out.println("user found");
				isCorrect = true;
				break;
				
			}
			else {
				System.out.println("user NOT found");
				isCorrect = false;
				break;
			}
			
        }
		
		return isCorrect;
	}
	
	public void createAcc(String usrEmail, String usrPass) throws Exception {
		
//		String sql = "INSERT INTO users"
//				+ "(email, password_hash)\r\n"
//				+ "VALUES";
//		
//		try 
//		{
//			this.conn = dataBaseMng.getConnection();
//			PreparedStatement stmt = this.conn.prepareStatement(sql);
//			int rows = stmt.executeUpdate();
//			System.out.println("Inserted rows: " + rows);
//			dataBaseMng.getTable("users");
//		}
//		catch (SQLException e) {
//			e.printStackTrace();
//		}
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
