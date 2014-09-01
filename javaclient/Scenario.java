import java.io.IOException;
import java.util.Random;

public class Scenario {
	private TCPClient tcp;
	private int realClient = 0, virtClient = 0;
	private int[] delay;
	public int WiFi = 25, LTE = 15;
	private boolean ipfw = false;
	public double[] BGT = {0.125, 4.925, 11.825}; //Background traffic over time;
	private boolean repeat = true;
	private Random r;
	
	
	public Scenario(int real, int virtual, TCPClient tcp ,int[] delays, int WiFi, int LTE, double[] BGT, boolean ipfw, boolean repeat) {
		this.tcp = tcp;
		this.realClient = real;
		this.virtClient = virtual;
		this.delay = delays;
		r = new Random();
		
		this.WiFi = WiFi;
		this.LTE = LTE;
		this.BGT = BGT;
		this.repeat = repeat;
		this.ipfw=ipfw;
		
		if (ipfw)
			initIPFW();
		
		try { tcp.init();
		} catch (IOException e) { e.printStackTrace(); }

	}
	
	public Scenario(Config c, TCPClient tcp) {
		this(c.realClientID, c.virtClientID, tcp, c.delays,
				c.WiFi, c.LTE, c.BGT, c.ipfw, c.repeat);
	}

	public Scenario(Config c) throws IOException {
		this(c, new TCPClient(c));
	}

	private void initIPFW () {
		ExecShell.executeCommand(".\\ipfw -q flush");
		ExecShell.executeCommand(".\\ipfw -q pipe flush");
		ExecShell.executeCommand(".\\ipfw add 1030 pipe 1 ip from any to any");
		ExecShell.executeCommand(".\\ipfw pipe 1 config bw 100Mbit/s");
	}
	
		
	public void terminate(){

		if (ipfw) {
			System.out.println("Reseting Dummynet!");
			ExecShell.executeCommand(".\\ipfw -q flush");
			ExecShell.executeCommand(".\\ipfw -q pipe flush");
		}

		System.out.println("---");
		try {
			tcp.sendMessage("setNet:" + realClient + ":WiFi:");
			tcp.sendMessage("updateBW:" + virtClient + ":" + BGT[0] + ":");
		} catch (IOException e) {
			e.printStackTrace();
		}

		tcp.terminate();
		
	}
	
	public void run() throws InterruptedException, IOException {

		do {
			// Initialize
			Thread.sleep(1000);
			
			System.out.println("---");
			tcp.sendMessage("setNet:" + realClient + ":WiFi:");
			tcp.sendMessage("updateBW:" + virtClient + ":"+BGT[0]+ ":");
		
			if(ipfw){
				ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ (WiFi-BGT[0]) +"Mbit/s");
				System.out.println("ResetIPFW to "+ (WiFi-BGT[0]));
			}
			
			Thread.sleep(delay[0] * 1000);
			
			// 5Mbits of BGT from virtual client added
			System.out.println("---");
			tcp.sendMessage("updateBW:" + virtClient + ":"+BGT[1]+ ":");
			
			if(ipfw){
				ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ (WiFi-BGT[1]) +"Mbit/s");
				System.out.println("ResetIPFW to "+ (WiFi-BGT[1]));
			}
					
			Thread.sleep(delay[1] * 1000);
			
			// 12Mbits of BGT from virtual client added
			System.out.println("---");
			tcp.sendMessage("updateBW:" + virtClient + ":"+BGT[2]+ ":");
			if(ipfw){
				ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ (WiFi-BGT[2]) +"Mbit/s");
				System.out.println("ResetIPFW to "+ (WiFi-BGT[2]));
			}
			
			Thread.sleep(delay[2] * 1000);
			
			System.out.println("---");
			
			if(ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config plr 0.9");
			System.out.println("Handover to LTE");
			Thread.sleep((long) 100 + (r.nextInt() % 100) );
			
			tcp.sendMessage("setNet:" + realClient + ":LTE:");
			if(ipfw){
				ExecShell.executeCommand(".\\ipfw pipe 1 config plr 0.0" );
				ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ (LTE) +"Mbit/s" );
			}
			
			System.out.println("Increase to "+ (LTE));
			
			Thread.sleep(delay[3] * 1000);		
			
		}while(repeat);
		
		this.terminate();
	}
}
