import java.io.File;
import java.io.IOException;
import org.ini4j.*;


public class VirtReporter {

	static int[] delays = {8,8,5,8}; // Time delays for the scenario
									 // 1.Without BGT
									 //	2.With 5Mbits
									 //	3.With 12Mbits and Time to Hand-off to LTE
									 //	4.Time of Real client in LTE.
	
	static int WiFi = 25;
	static int LTE = 15;
	static double[] BGT = { 0.125, 4.925, 11.825 };

	static int realClientID = 46;
	static int virtClientID = 50;

	static String serverIP = "192.168.10.15";
	static int port = 5100;
	
	static boolean ipfw = false;
	
	static boolean dbg = false;
	
	static Scenario scenario = null;
	static TCPClient tcp = null;
	
	public static void main(String[] args) {
		
		 AddShutdownHook hook = new AddShutdownHook();
		 hook.attachShutDownHook();
		
		if (args.length > 0)
			try {
				readLogFile(args[0]);
			} catch (IOException e2) { e2.printStackTrace(); }
		
		if(dbg)
			dbg();
		
		
		try {
			tcp = new TCPClient(serverIP, port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // tcp client (MAP host, Server Reporter(IP, Port);

		scenario = new Scenario(realClientID, virtClientID, tcp, delays, WiFi, LTE, BGT, ipfw);

		// Scenario is executed in a loop;
		try {
			scenario.run();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	private static class AddShutdownHook {

		public void attachShutDownHook() {

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {

					System.out.println("Terminating VirtualReporter!");

					scenario.terminate();
					tcp.terminate();
				}
			});
		}
	}

	private static void readLogFile(String filename) throws IOException {
		
		Wini ini = new Wini(new File(filename));
		serverIP = ini.get("TCPServer", "serverIP" );
        port = ini.get("TCPServer", "port", int.class);
        System.out.println(serverIP +" "+ port);
        
        realClientID = ini.get("clients", "realClientID", int.class);
        virtClientID = ini.get("clients", "virtClientID", int.class);
        System.out.println(realClientID +" "+ virtClientID);
        
        Ini.Section scenario = ini.get("scenario");
        delays[0] = scenario.get("delay1", int.class);
        delays[1] = scenario.get("delay2", int.class);
        delays[2] = scenario.get("delay3", int.class);
        delays[3] = scenario.get("delay4", int.class);
        for(int i : delays){
        	System.out.print(i + " ");
        }
        System.out.println(" ");
        
        Ini.Section bandwidth = ini.get("bandwidth");
        WiFi = bandwidth.get("WiFi", int.class);
        LTE = bandwidth.get("LTE", int.class);
        BGT[0] = bandwidth.get("BGT1", double.class);
        BGT[1] = bandwidth.get("BGT2", double.class);
        BGT[2] = bandwidth.get("BGT3", double.class);
        System.out.println(WiFi +" "+ LTE);
        for(double d : BGT){
        	System.out.print(d + " ");
        }
        System.out.println(" ");
        
        Ini.Section dummynet = ini.get("dummynet");
        if(dummynet.get("functional").equals("true")){
        	ipfw = true;
        	System.out.println("Sending commands to Dummynet!");
        }
	}	
	
	private static void dbg(){
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
		
		tcp.terminate();
	}

}
