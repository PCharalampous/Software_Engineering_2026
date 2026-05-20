package usecase1;

import java.sql.ResultSet;

public class Authentication  {
	
	private String email;
	private String password;
	private Boolean isCorrect;
	private Integer id;
//	private User currentUser;

	
//	public Authentication() {
//		use default constructor
//	}
	
	public Boolean userLogIn() {
		isCorrect = true;
		DatabaseManager dataBase = new DatabaseManager();
		ResultSet rs = dataBase.getUserTable();
		
		return isCorrect;
	}
	
	public void createAcc(String usrEmail, String usrPass) throws Exception {
		try {
			
		}
		catch(Exception e) {
			
		};
		
		
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
