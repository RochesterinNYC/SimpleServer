
public class Message {
	
	private static int idCount = 0;
	
	private String subject;
	private String body;
	private Account sender;
	private Account recipient;
	private long timeSent;
	private int id;
	
	public Message(Account sender, Account recipient, String subject, String body){
		this.subject = subject;
		this.sender = sender;
		this.recipient = recipient;
		this.body = body;
		this.timeSent = System.currentTimeMillis();
		this.id = ++idCount;
	}
	
	public int getID(){
		return this.id;
	}
	
	public Account getRecipient(){
		return recipient;
	}
	
	public String getSubject(){
		return subject;
	}
	
	public String getBody(){
		return body;
	}
	
	public long getTimeSent(){
		return timeSent;
	}
	
	
}
