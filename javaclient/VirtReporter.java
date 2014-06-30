import java.io.IOException;


public class VirtReporter {

	public static void main(String[] args) {
		
		int[] delays = {8,8,5,8}; // Time delays for the scenario 	1.Without BGT
																	//	2.With 5Mbits
																	//	3.With 12Mbits and Time to Hand-off to LTE
																	//	4.Time of Real client in LTE.
		int realClientID = 46;
		int virtClientID = 50;
		
		TCPClient tcp = null;
		try {
			tcp = new TCPClient("172.16.64.142");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // tcp client (MAP host, Server Reporter(IP, Port);
											
		Scenario scenario = new Scenario(realClientID, virtClientID, tcp, delays); 
		
		//Scenario is executed in a loop;
		try {
			scenario.run();
		} catch (IOException e) 
			{ e.printStackTrace();
		} catch (InterruptedException e)
			{ e.printStackTrace();
		}
		
	}
	
	private void dbg(){
		TCPClient tcp = null;
		try {
			tcp = new TCPClient("172.16.64.140", 5100);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // tcp client (MAP host, Server Reporter(IP, Port);
		
		
		try {
			tcp.sendMessage("Hallo!");
			tcp.sendMessage("Hallo!");
			tcp.sendMessage("Hallo!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		tcp.end();
	}

}
