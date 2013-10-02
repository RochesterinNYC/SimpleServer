
public class Account {
	private String userName;
	private String password;
	private boolean loggedIn;
	
	public Account(String userName, String password){
		this.userName = userName;
		this.password = password;
		this.loggedIn = false;
	}
	
	public boolean login(String userName, String password){
		if (this.userName.equals(userName) && this.password.equals(password)){
			this.loggedIn = true;
		}
		else {
			this.loggedIn = false;
		}
		return loggedIn;
	}
	
	public String toString(){
		return "Username: " + userName + " and Password: " + password;
	}

}
