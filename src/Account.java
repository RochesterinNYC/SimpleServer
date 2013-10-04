
public class Account {
	private String userName;
	private String password;
	private boolean loggedIn;
	private long lastLoginTime;
	
	public Account(String userName, String password){
		this.userName = userName;
		this.password = password;
		this.loggedIn = false;
	}
	
	public boolean login(String userName, String password){
		boolean correctLogin = false;
		if (this.userName.equals(userName) && this.password.equals(password)){
			this.lastLoginTime = System.currentTimeMillis();
			correctLogin = true;
		}
		return correctLogin;
	}
	
	public void markLoginStatus(boolean isLoggedIn){
		this.loggedIn = isLoggedIn;
	}
	public boolean isLoggedIn(){
		return loggedIn;
	}
	public String getUserName(){
		return userName;
	}
	
	public long getLastLoginTime(){
		return lastLoginTime;
	}
	
	public String toString(){
		return "Username: " + userName + " and Password: " + password;
	}

}
