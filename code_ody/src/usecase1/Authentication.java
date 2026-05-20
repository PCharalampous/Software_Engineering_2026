package usecase1;


public class Authentication  {
	
	private String email;
	private String password;
	private Boolean isCorrect;
	private Integer id;
	
//	public Authentication() {
//		use default constructor
//	}
	
	public Boolean userLogIn() {
		isCorrect = true;
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
