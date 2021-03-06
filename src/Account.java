import java.util.ArrayList;

/**
* <b>Account Class</b>
* <p>
* Represents an account on the server with a username, password, and time at
* which the account was last logged into.
* Multiple clients can login to the server using the same account at once.
* @author James Wen - jrw2175
*/
public class Account {
	private String userName;
	private String password;
	private boolean loggedIn;
	private long lastLoginTime;
	private ArrayList<Message> messages;
	
	/**
	* <b>Account constructor</b>
	* <p>
	* Constructs a server account.
	* @param userName - account username
	* @param password - account password (encryption and digest/hash required 
	* for actual server implementation)
	*/
	public Account(String userName, String password){
		this.userName = userName;
		this.password = password;
		this.loggedIn = false;
		this.messages = new ArrayList<Message>();
	}
	
	//Login related methods
	/**
	* <b>login</b>
	* <p>
	* Action for an attempted login, eturns whether the attempt was successful.
	* Successful login will mark this account as currently active.
	* @param userName - username used in attempted login
	* @param password - password used in attempted login
	* @return correctLogin - whether the login succeeded or not
	*/
	public boolean login(String userName, String password){
		boolean correctLogin = false;
		if (this.userName.equals(userName) && this.password.equals(password)){
			this.lastLoginTime = System.currentTimeMillis();
			correctLogin = true;
		}
		return correctLogin;
	}
	
	/**
	* <b>markLoginStatus</b>
	* <p>
	* Changes this account to active or inactive.
	* Related to the multiple clients under one account functionality.
	* @param isLoggedIn - whether this account is active
	*/
	public void markLoginStatus(boolean isLoggedIn){
		this.loggedIn = isLoggedIn;
	}
	
	/**
	* <b>isLoggedIn</b>
	* <p>
	* Returns whether this account is active (whether a client is currently 
	* logged on the server using it).
	* @return loggedIn - whether this account is active
	*/
	public boolean isLoggedIn(){
		return loggedIn;
	}
	
	//Message Functionality methods
	/**
	* <b>getMessages</b>
	* <p>
	* Get the messages that this account has received.
	* @return messages - account messages
	*/
	public ArrayList<Message> getMessages(){
		return messages;
	}
	
	/**
	* <b>newMessage</b>
	* <p>
	* Adds a message to this account's received messages
	* @param message - new message that this account has received
	*/
	public void newMessage(Message message){
		messages.add(message);
	}
	
	/**
	* <b>getUserName</b>
	* <p>
	* Returns whether the username of this account.
	* @return userName - this account's username
	*/
	public String getUserName(){
		return userName;
	}

	/**
	* <b>getLastLoginTime</b>
	* <p>
	* Returns when a client last logged into the server with this account.
	* @return lastLoginTime - this account's time of last login
	*/
	public long getLastLoginTime(){
		return lastLoginTime;
	}

}
