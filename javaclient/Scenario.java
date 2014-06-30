import java.io.IOException;
import java.util.Random;

public class Scenario {
	private TCPClient cl;
	private int realClient = 0, virtClient = 0;
	private int[] del;
	public int WiFi = 15, LTE = 10;
	public double[] BGT = {0.125, 4.925, 11.825}; //Background traffic over time;
	private Random r;
	public Scenario(int real, int virtual, TCPClient tcp ,int[] delays) {
		this.cl = tcp;
		this.realClient = real;
		this.virtClient = virtual;
		this.del = delays;
		r = new Random();

	}

	public void run() throws InterruptedException, IOException {

		while (true) {
			// Initialize
			Thread.sleep(1000);
			this.cl.sendMessage("setNet:" + realClient + ":WiFi:");
			cl.sendMessage("updateBW:" + virtClient + ":"+BGT[0]+ ":");
//			ExecShell.executeCommand("ResetIPFW to"+ (WiFi-BGT[0]));
			System.out.println("ResetIPFW to "+ (WiFi-BGT[0]));
			
			Thread.sleep(del[0] * 1000);
			
			// 5Mbits of BGT from virtual client added
			cl.sendMessage("updateBW:" + virtClient + ":"+BGT[1]+ ":");
//			ExecShell.executeCommand("Decrease to "+ (WiFi-BGT[1]));
			System.out.println("ResetIPFW to "+ (WiFi-BGT[1]));
					
			Thread.sleep(del[1] * 1000);
			
			// 12Mbits of BGT from virtual client added
			cl.sendMessage("updateBW:" + virtClient + ":"+BGT[2]+ ":");
//			ExecShell.executeCommand("Decrease to "+ (WiFi-BGT[2]));
			System.out.println("ResetIPFW to "+ (WiFi-BGT[2]));
						
			Thread.sleep(del[2] * 1000);

//			ExecShell.executeCommand("90%PLR");
			System.out.println("90%PLR");
			Thread.sleep((long) 100 + (r.nextInt() % 100) );
			cl.sendMessage("setNet:" + realClient + ":LTE:");
//			ExecShell.executeCommand("Increase to "+ (LTE)");
			System.out.println("Increase to "+ (LTE));
			
			Thread.sleep(del[3] * 1000);		
			
		}
	}
}
