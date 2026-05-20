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
	
	// current logged in user
    private static User currentUser;

	
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
				currentUser = new User(rs.getInt(1), rs.getString(2), rs.getString(4));
				break;
				
			}else
				System.out.println("user NOT found");
			
        }
		
		rs.close();
		dataBaseMng.closeConnection();
		//dataBaseMng.showTable("users");
		return isCorrect;
	}
	
	public void createAcc(String usrEmail, String usrPass, String userName, String dispFirstName, String dispSecondName) throws Exception {
		
		this.conn = dataBaseMng.getConnection();
		String sql = "INSERT INTO users " +
                 "(username, display_name, email, password_hash) " +
                 "VALUES (?, ?, ?, ?)";
		//-------------
		String checkSql =
		        "SELECT * FROM users WHERE email = ?";
		
        PreparedStatement checkStmt =
                this.conn.prepareStatement(checkSql);

        checkStmt.setString(1, usrEmail);

        ResultSet rs = checkStmt.executeQuery();

        // if user exists
        if (rs.next()) {

            ErrorScreen errScr = new ErrorScreen(
                    "Sign Up failed",
                    "User email already exists"
            );

            errScr.show();

            rs.close();
            checkStmt.close();

            return;
        }

        rs.close();
        checkStmt.close();
	        
		
		//-------------
	    try {
	        this.conn = dataBaseMng.getConnection();
	
	        PreparedStatement stmt = this.conn.prepareStatement(sql);
	
	        // combine first + second name
	        String displayName = dispFirstName + " " + dispSecondName;
	
	        // set values into SQL query
	        stmt.setString(1, userName);
	        stmt.setString(2, displayName);
	        stmt.setString(3, usrEmail);
	        stmt.setString(4, usrPass);
	
	        int rows = stmt.executeUpdate();
	
	        System.out.println("Inserted rows: " + rows);
	
	        dataBaseMng.getTable("users");
	
	        stmt.close();
	
	    }catch (SQLException e) {
	
	    	e.printStackTrace();
	
	    }
	    
//			System.out.println("Inserted rows: " + 0+"apla einai se sxolia olo to method gia auto!");
	    System.out.println("usrEmail: " +usrEmail+"\nusrPass: "+usrPass+"\nuserName: "+userName+"\ndispFirstName: "+dispFirstName+"\ndispSecondName: "+dispSecondName);
	    dataBaseMng.closeConnection();		
	}
	
//	private void authenticateUser() {
//		
//	}
	
	public void userLogOut() {
		currentUser = null;
		
        System.out.println("User logged out");
		
	}
	
	// GET CURRENT USER
    public static User getCurrentUser() {

        return currentUser;
    }
	
	public void setUserEmail(String email) {
		this.email = email; 
	}
	
	public void setUserPassword(String password) {
		this.password = password; 
	}
	
	
	
}
