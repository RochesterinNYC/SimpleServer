/**
* <b>Message Class</b>
* <p>
* Represents a message that has been sent from one account to another.
* As of version 1.0, only single line subject and content bodies are implemented.
* Each message has a unique ID and this is handled by the static idCount.
* @author James Wen - jrw2175
*/
public class Message {
	
	//Keeps track what unique message ID next message created will have
	private static int idCount = 0;
	
	private String subject;
	private String body;
	private Account sender;
	private Account recipient;
	private long timeSent;
	private int id;
	
	/**
	* <b>Message Constructor</b>
	* <p>
	* Creates a message that has a sender, receiver, subject, content body, time
	* of creation/sending, and unique id
	* @param sender - account message was sent from
	* @param recipient - account receiving message
	* @param subject - subject of message
	* @param body - content body of message
	*/
	public Message(Account sender, Account recipient, String subject, String body){
		this.subject = subject;
		this.sender = sender;
		this.recipient = recipient;
		this.body = body;
		this.timeSent = System.currentTimeMillis();
		this.id = ++idCount;
	}
	
	/**
	* <b>getID</b>
	* <p>
	* Returns the id of the message (each message has a unique ID).
	* @return id - message ID
	*/
	public int getID(){
		return this.id;
	}

	/**
	* <b>getRecipient</b>
	* <p>
	* Returns the account that message was sent to.
	* @return recipient - account that received message
	*/
	public Account getRecipient(){
		return recipient;
	}
	/**
	* <b>getSender</b>
	* <p>
	* Returns the account that message was sent from.
	* @return sender - account that sent message
	*/
	public Account getSender(){
		return sender;
	}
	
	/**
	* <b>getSubject</b>
	* <p>
	* Returns the subject of the message
	* @return subject - message subject
	*/
	public String getSubject(){
		return subject;
	}
	
	/**
	* <b>getBody</b>
	* <p>
	* Returns the content body of the message
	* @return body - message content body
	*/
	public String getBody(){
		return body;
	}
	
	/**
	* <b>getTimeSent</b>
	* <p>
	* Returns the time (system milliseconds) that the message was sent
	* @return timeSent - time of sending
	*/
	public long getTimeSent(){
		return timeSent;
	}

	/**
	* <b>getLogMessage</b>
	* <p>
	* Returns a logging message to be stored in the server log detailing
	* that a message was sent and who sent it to who
	* @return server log message
	*/
	public String getLogMessage(){
		return (sender.getUserName() + " sent a message to " + recipient.getUserName());
	}
	
}
