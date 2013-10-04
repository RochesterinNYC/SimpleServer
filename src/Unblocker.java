
class Unblocker implements Runnable {
	private Server server;
	public Unblocker(Server server){
		this.server = server;
	}
	public void run(){
		for(BlockedIP ip : server.getBlockedIPs()){
			if(ip.toUnblock()){
				server.removeBlockedIP(ip);
				System.out.println("IP Address " + ip.getIP().toString() + " is now unblocked");
			}
		}
	}
}
