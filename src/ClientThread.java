
public class ClientThread extends Thread{
	
	private Client client;
	
	public ClientThread(Client client){
		this.client = client;
	}
	
	public void run(){
		if(client.login()){
	    	client.optionMenu();
		}
	}
	
	
	
}
