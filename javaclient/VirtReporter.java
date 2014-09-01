
import java.io.IOException;

public class VirtReporter {

	static boolean dbg = false;
	static TCPClient tcp = null;
	static Scenario scenario = null;
	
	public static void main(String[] args) {
		
		AddShutdownHook hook = new AddShutdownHook();
		hook.attachShutDownHook();
		Config config = new Config();
		
		if (args.length > 0)
			try {config.parse(args[0]);} catch (IOException e) { e.printStackTrace(); }
		
		if(dbg)
			dbg();
		
		try { tcp = new TCPClient(config);
		} catch (IOException e1) { e1.printStackTrace();}

		scenario = new Scenario(config, tcp);

		// Scenario is executed in a loop or once;
		try { scenario.run();
		} catch (IOException e1 ) { e1.printStackTrace();
		} catch (InterruptedException e2) { e2.printStackTrace(); }		
	}
	
	private static class AddShutdownHook {
		
		public void attachShutDownHook() {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (scenario.running) {
						System.out.println("Terminating VirtualReporter!");
						scenario.terminate();
					}
				}
			});
		}
	}
	
	private static void dbg(){
		TCPClient tcp = null;
		try {
			tcp = new TCPClient("172.16.64.140", 5100);
		} catch (IOException e1) { e1.printStackTrace();
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
