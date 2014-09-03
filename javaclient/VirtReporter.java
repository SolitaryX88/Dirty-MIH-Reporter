
import java.io.IOException;

public class VirtReporter {

	static boolean dbg = false;
	static TCPClient tcp = null;
	static Scenario scenario = null;
	
	public static void main(String[] args) {
		
		AddShutdownHook hook = new AddShutdownHook();
		hook.attachShutDownHook();
		Config config = new Config();
		
		try {
			if (args.length == 1)
				config.parse(args[0]);
		
			if (dbg)
				dbg();

			try {
				scenario = new Scenario(config, new TCPClient(config));
				scenario.execScen();
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private static class AddShutdownHook {
		
		public void attachShutDownHook() {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					if (scenario.running) {
						System.out.println("Terminating VirtualReporter!");
						try {
							scenario.terminate();
						} catch (IOException e) {
						 e.printStackTrace();
						}
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
