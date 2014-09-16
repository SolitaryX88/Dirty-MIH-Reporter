import java.io.IOException;
import java.util.Random;

public class Scenario {
	private TCPClient tcp = null;
	private Random r;
	public boolean running = true;
	private Config cfg = null;
	private long startTime = 0L;

	//	private String realClientNetwork = "";
	
	public Scenario(Config c, TCPClient tcp) {
		this.cfg = c;
		this.tcp = tcp;
		this.r = new Random();
		
		if (c.tc)
			initTC();
		
		try { tcp.init();
		} catch (IOException e) { e.printStackTrace(); }

	}
	
	public Scenario(Config c) throws IOException {
		this(c, new TCPClient(c));
	}
	
	private void limitBW(double bgt, String network){

		String end	 = "Mbit allot 1500 prio 5 bounded isolated";
		String start = "tc class cha dev eth4 parent 1: classid 1:1 cbq rate ";

		if(network.equals("WiFi"))
		 ExecShell.executeCommand(start + cfg.bw.getAvailWiFiBW(bgt) + end);	
		else
		 ExecShell.executeCommand(start + cfg.bw.getAvailLTEBW(bgt) + end);		
	}
	
	private double fluctuator(){
		
		return ((double) (Math.abs(r.nextInt() % 20) + 90) / 100);
	}
	
	private void updateVirtualBandWidthBGT(double bgt) throws IOException{
		
		bgt = bgt * fluctuator() ;
		tcp.sendMessage("updateBW:" + cfg.virtClientWiFi + ":"+cfg.format(bgt)+ ":");
	}
	
	private void updateVirtualClientRate(double rate, int clientID) throws IOException {
		
		rate = rate * fluctuator() ;
		tcp.sendMessage("updateBW:" + clientID + ":" + cfg.format(rate) + ":");
		
	}
	
	private void initTC () {
		// Set Network limiter
		ExecShell.executeCommand("tc del dev eth4 root");
		ExecShell.executeCommand("tc qdisc add dev eth4 root handle 1: cbq avpkt 1000 bandwidth 100mbit");
		ExecShell.executeCommand("tc class add dev eth4 parent 1: classid 1:1 cbq rate 100Mbit allot 1500 prio 5 bounded isolated");
		ExecShell.executeCommand("tc filter add dev eth4 parent 1: protocol ip prio 16 u32 match ip src 192.168.46.1 flowid 1:1");
		ExecShell.executeCommand("tc qdisc add dev eth4 parent 1:1 sfq perturb 10");
		
		// Set PLR Limiter
		ExecShell.executeCommand("tc qdisc add dev eth1 root netem loss 0.1%");
	}
	
	private void reset() throws IOException {	
				
		tcp.sendMessage("setNet:" + cfg.realClientID + ":WiFi:");
		tcp.sendMessage("setNet:" + cfg.virtClientWiFi+ ":WiFi:");
		updateVirtualBandWidthBGT( cfg.bw.noBGT);
		if(cfg.tc && running) 
			limitBW(cfg.bw.noBGT, "WiFi");
	}
	
	public void terminate() throws IOException{
		
		running = false;
		
		if (cfg.tc) {
			System.out.println("Reseting NetEm!");
			ExecShell.executeCommand("tc qdisc del root dev eth4");
			ExecShell.executeCommand("tc qdisc del root dev eth1");

		}

		System.out.println("---");
		reset();
		tcp.terminate();
		
	}
	
	public void execScen() throws InterruptedException, IOException{
		
		if(cfg.scen.num==1)			this.first();
		else if(cfg.scen.num==2)	this.second();
		else if(cfg.scen.num==3)	this.third();
		else if(cfg.scen.num==4)	this.withHandOvers();
		else 						this.withoutHandoOvers();
		
		
	}
	
	private void handOverTo(String net, double bgt) throws IOException, InterruptedException{
	
		tcp.sendMessage("setNet:" + cfg.realClientID + ":"+ net +":");
		if(cfg.tc) 	ExecShell.executeCommand("tc qdisc cha dev eth1 root netem loss 90%");
				sleep((int) 170 + Math.abs(r.nextInt() % 200) );			
		if(cfg.tc){
			ExecShell.executeCommand("tc qdisc cha dev eth1 root netem loss 0.1%");
			limitBW(bgt, net); 
		}
	}
	
	private void sleep(int t) throws InterruptedException{
		
		System.out.println("----\nSleep for: " + t + "ms\n---");
		Thread.sleep(t);
	}
	
	
	public void first() throws InterruptedException, IOException {
		// Real Adaptation
		// Virtual BGT min
		// Virtual BGT med
		// Real Handover LTE
		// Virtual BGT min
		// Real Handover WiFi
		// Virtual Handover LTE
		// Real Handover LTE
		
		// Calculate total time
		
		System.out.println("Executing first scenario!");
		
		do {
			//No BGT all in WiFi for a small time
			startTime = System.currentTimeMillis();
			
			sleep(400);
			reset();
			sleep(400);
			
			// Minimum of BGT from virtual client added for small time
			updateVirtualBandWidthBGT(cfg.bw.medBGT);
			limitBW(cfg.bw.medBGT, "WiFi");
					
			sleep(cfg.scen.medTime * 1000);
			
			// Medium of BGT from virtual client added 
			updateVirtualBandWidthBGT(cfg.bw.maxBGT);
			limitBW(cfg.bw.maxBGT, "WiFi");

			sleep(cfg.scen.medTime * 1000);
			
			// Max of BGT from virtual client added 
			updateVirtualBandWidthBGT(cfg.bw.maxBGT);
			limitBW(cfg.bw.maxBGT, "WiFi");

			sleep(cfg.scen.medTime * 1000);

			//LTE Handover
			handOverTo("LTE", cfg.bw.noBGT);
		
			sleep(cfg.scen.medTime * 1000);
			
			// Decrease BGT to min
			updateVirtualBandWidthBGT(cfg.bw.medBGT);
			
			sleep(cfg.scen.smallTime * 1000);
			
			// Handover again to WiFi
			handOverTo("WiFi", cfg.bw.medBGT);
			
			sleep(cfg.scen.medTime * 1000);
			
			// Max of BGT from virtual client added 
			System.out.println("---");
			updateVirtualBandWidthBGT(cfg.bw.maxBGT);
			limitBW(cfg.bw.maxBGT, "WiFi");

			sleep(cfg.scen.smallTime * 1000);
			
			//LTE Handover
			handOverTo("LTE", cfg.bw.noBGT);
			
			sleep(cfg.scen.medTime * 1000);
			
			System.out.println("Total duration: " + (double)(System.currentTimeMillis() - startTime)/1000);
			
		}while(cfg.repeat);

		
		this.terminate();
	}
	
	public void second() throws InterruptedException, IOException {
		
		//Increasing steps of BGT a peak and some decreasing steps.
		
		System.out.println("Executing second scenario!");
		do {
			startTime = System.currentTimeMillis();
			// No BGT
			sleep(400);
			reset();
			sleep(400);

			// Minimum of BGT from virtual client added for med time
			updateVirtualBandWidthBGT(cfg.bw.medBGT);
			limitBW(cfg.bw.medBGT, "WiFi");

			sleep(cfg.scen.medTime * 1000);

			// Med of BGT from virtual client added for med time
			updateVirtualBandWidthBGT(cfg.bw.maxBGT);
			limitBW(cfg.bw.maxBGT, "WiFi");

			sleep(cfg.scen.largeTime * 1000);
			
			// Max of BGT from virtual client added for med time
			updateVirtualBandWidthBGT(cfg.bw.maxBGT);
			limitBW(cfg.bw.maxBGT, "WiFi");

			sleep(cfg.scen.largeTime * 1000);
			
			// Med of BGT from virtual client added for med time
			updateVirtualBandWidthBGT(cfg.bw.maxBGT);
			limitBW(cfg.bw.maxBGT, "WiFi");

			sleep(cfg.scen.largeTime * 1000);
			
			// Minimum of BGT from virtual client added for med time
			updateVirtualBandWidthBGT(cfg.bw.medBGT);
			limitBW(cfg.bw.medBGT, "WiFi");

			sleep(cfg.scen.medTime * 1000);
			
			// No BGT
			reset();
			
			sleep(cfg.scen.smallTime * 1000);
			
			System.out.println("Total duration: " + (double)(System.currentTimeMillis() - startTime)/1000);
		} while (cfg.repeat);

		this.terminate();
		
	}
	
	public void third() throws InterruptedException, IOException {
		// Adaptation in WiFi w/ BGT of virtual
		// Handover or realClient to LTE
		// Handover of virtual to LTE
		// Adaptation in LTE.
		
		System.out.println("Executing third scenario!");
		
		do {
			startTime = System.currentTimeMillis();
			
			sleep(400);
			reset();
			sleep(cfg.scen.medTime * 1000);
			
			// Minimum of BGT from virtual client added
			updateVirtualBandWidthBGT(cfg.bw.medBGT);
			limitBW(cfg.bw.medBGT, "WiFi");
					
			sleep(cfg.scen.largeTime * 1000);
			
			// med of BGT from virtual client added
			updateVirtualBandWidthBGT(cfg.bw.medBGT);
			limitBW(cfg.bw.medBGT, "WiFi");
					
			sleep(cfg.scen.largeTime * 1000);
			
			// Max of BGT from virtual client added
			updateVirtualBandWidthBGT(cfg.bw.maxBGT);
			limitBW(cfg.bw.maxBGT, "WiFi");

			sleep(cfg.scen.smallTime * 1000);

			//LTE Handover
			handOverTo("LTE", cfg.bw.noBGT);
			
			sleep(cfg.scen.largeTime * 1000);		
			
			// Medium of BGT from virtual client added in LTE
			tcp.sendMessage("setNet:" + cfg.virtClientWiFi + ":LTE:");
			updateVirtualBandWidthBGT(cfg.bw.minBGT);
			limitBW(cfg.bw.minBGT, "LTE");
			
			sleep(cfg.scen.largeTime * 1000);	
			
			System.out.println("Total duration: " + (double)(System.currentTimeMillis() - startTime)/1000);
		}while(cfg.repeat);
		
		this.terminate();
	}
	
	public void framework() throws InterruptedException, IOException {
		
		//Increasing steps of BGT a peak and some decreasing steps.
		double bgt = 0.0;
		
		System.out.println("Executing XXX scenario!");
		do {
			startTime = System.currentTimeMillis();
			// No BGT
			sleep(400);
			reset();
			sleep(400);
			
			// 
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			
			reset();
			sleep(cfg.scen.smallTime * 1000);
			
			System.out.println("Total duration: " + (double)(System.currentTimeMillis() - startTime)/1000);
		} while (cfg.repeat);

		this.terminate();
		
	}
	
	public void withoutHandoOvers() throws InterruptedException, IOException {
		
		//Increasing steps of BGT a peak and some decreasing steps.
		double bgt = 0.0;
		
		System.out.println("Executing Without Handover scenario!");
		do {
			startTime = System.currentTimeMillis();
			// No BGT
			sleep(400);
			reset();
			sleep(400);
			
			// 
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			
			reset();
			sleep(cfg.scen.smallTime * 1000);
			
			System.out.println("Total duration: " + (double)(System.currentTimeMillis() - startTime)/1000);
		} while (cfg.repeat);

		this.terminate();
		
	}
	
public void withHandOvers() throws InterruptedException, IOException {
		
		//Increasing steps of BGT a peak and some decreasing steps.
		double bgt = 0.0;
		
		System.out.println("Executing Handover scenario!");
		do {
			startTime = System.currentTimeMillis();
			// No BGT
			sleep(400);
			reset();
			sleep(400);
			
			// 
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);
			
			// Handover to LTE
			handOverTo("LTE", cfg.bw.noBGT);
			sleep(cfg.scen.medTime * 1000);	
			
			// 
			bgt = cfg.bw.maxBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			//limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			// 
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			//limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			

			//  Handover to WiFi
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			handOverTo("WiFi", bgt);
			sleep(cfg.scen.medTime * 1000);	

			//
			bgt = cfg.bw.medBGT * fluctuator();
			updateVirtualBandWidthBGT(bgt);
			limitBW(bgt, "WiFi");
			sleep(cfg.scen.medTime * 1000);			
			
			reset();
			sleep(cfg.scen.smallTime * 1000);
			
			System.out.println("Total duration: " + (double)(System.currentTimeMillis() - startTime)/1000);
		} while (cfg.repeat);

		this.terminate();
		
	}
}
