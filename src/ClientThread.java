public class ClientThread extends Thread{
	
	private Client client;
	
	public ClientThread(Client client){
		this.client = client;
	}
	
	public void run(){
		if(client.checkIPBlocked()){
			System.out.println("This IP Address is currently blocked due to repeated failed login attempts.");
		}
		else if(client.login()){
	    	client.optionMenu();
		}
	}
	
	
	
}
