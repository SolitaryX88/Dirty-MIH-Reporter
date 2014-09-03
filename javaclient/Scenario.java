import java.io.IOException;
import java.util.Random;

public class Scenario {
	private TCPClient tcp = null;
	private Random r;
	public boolean running = true;
	private Config cfg = null;
	private long startTime = 0L;
	
	public Scenario(Config c, TCPClient tcp) {
		this.cfg = c;
		this.tcp = tcp;
		this.r = new Random();
		
		if (c.ipfw)
			initIPFW();
		
		try { tcp.init();
		} catch (IOException e) { e.printStackTrace(); }

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
	
	private void reset() throws IOException {	
				
		tcp.sendMessage("setNet:" + cfg.realClientID + ":WiFi:");
		tcp.sendMessage("setNet:" + cfg.virtClientID+ ":WiFi:");
		tcp.sendMessage("updateBW:" + cfg.virtClientID + ":"+ cfg.bw.none+ ":");
		if(cfg.ipfw && running) 
			ExecShell.executeCommand(".\\ipfw pipe 1 config bw " + cfg.bw.getAvailWiFiBW(cfg.bw.none) +"Mbit/s");
	}
	
	public void terminate() throws IOException{
		
		running = false;
		
		if (cfg.ipfw) {
			System.out.println("Reseting Dummynet!");
			ExecShell.executeCommand(".\\ipfw -q flush");
			ExecShell.executeCommand(".\\ipfw -q pipe flush");
		}

		System.out.println("---");
		reset();
		tcp.terminate();
		
	}
	
	public void execScen() throws InterruptedException, IOException{
		
		if(cfg.scen.num==1)			this.first();
		else if(cfg.scen.num==2)	this.second();
		else 						this.third();
		
	}
	
	private void handOverTo(String net, double bgt, int client) throws IOException, InterruptedException{
	
		tcp.sendMessage("setNet:" + client + ":"+ net +":");
		if(cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config plr 0.9");
				sleep((int) 100 + (r.nextInt() % 100) );			
		if(cfg.ipfw){
			ExecShell.executeCommand(".\\ipfw pipe 1 config plr 0.0" );
			if (net.equals("WiFi"))
				ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailWiFiBW(bgt) +"Mbit/s" );
			else
				ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailLTEBW(bgt) +"Mbit/s" );
		}
	}
	
	private void sleep(int t) throws InterruptedException{
		Thread.sleep(t);
		System.out.println("---");
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
			sleep(cfg.scen.smallTime * 1000);
			
			// Minimum of BGT from virtual client added for small time
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":"+cfg.bw.min+ ":");
			if(cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailWiFiBW(cfg.bw.min) +"Mbit/s");
					
			sleep(cfg.scen.smallTime * 1000);
			
			// Medium of BGT from virtual client added 
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":"+cfg.bw.med+ ":");
			if(cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailWiFiBW(cfg.bw.med) +"Mbit/s");

			sleep(cfg.scen.smallTime * 1000);
			
			// Max of BGT from virtual client added 
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":"+cfg.bw.max+ ":");
			if(cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailWiFiBW(cfg.bw.max) +"Mbit/s");

			sleep(cfg.scen.smallTime * 1000);

			//LTE Handover
			handOverTo("LTE", cfg.bw.none, cfg.realClientID);
		
			sleep(cfg.scen.medTime * 1000);
			
			// Decrease BGT to none
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":"+cfg.bw.none+ ":");
			
			sleep(cfg.scen.smallTime * 1000);
			
			// Handover again to WiFi
			handOverTo("WiFi", cfg.bw.none, cfg.realClientID);
			
			sleep(cfg.scen.medTime * 1000);
			
			// Max of BGT from virtual client added 
			System.out.println("---");
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":" + cfg.bw.max + ":");
			if (cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw " + cfg.bw.getAvailWiFiBW(cfg.bw.max) + "Mbit/s");

			sleep(cfg.scen.smallTime * 1000);
			
			//LTE Handover
			handOverTo("LTE", cfg.bw.none, cfg.realClientID);
			
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
			sleep(cfg.scen.medTime * 1000);

			// Minimum of BGT from virtual client added for med time
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":" + cfg.bw.min + ":");
			if (cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw " + cfg.bw.getAvailWiFiBW(cfg.bw.min) + "Mbit/s");

			sleep(cfg.scen.medTime * 1000);

			// Med of BGT from virtual client added for med time
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":" + cfg.bw.med + ":");
			if (cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw " + cfg.bw.getAvailWiFiBW(cfg.bw.med) + "Mbit/s");

			sleep(cfg.scen.medTime * 1000);
			
			// Max of BGT from virtual client added for med time
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":" + cfg.bw.max + ":");
			if (cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw " + cfg.bw.getAvailWiFiBW(cfg.bw.max) + "Mbit/s");

			sleep(cfg.scen.medTime * 1000);
			
			// Med of BGT from virtual client added for med time
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":" + cfg.bw.med + ":");
			if (cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw " + cfg.bw.getAvailWiFiBW(cfg.bw.med) + "Mbit/s");

			sleep(cfg.scen.medTime * 1000);
			
			// Minimum of BGT from virtual client added for med time
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":" + cfg.bw.min + ":");
			if (cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw " + cfg.bw.getAvailWiFiBW(cfg.bw.min) + "Mbit/s");

			sleep(cfg.scen.medTime * 1000);
			
			// No BGT
			reset();
			
			sleep(cfg.scen.medTime * 1000);
			
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
			sleep(cfg.scen.largeTime * 1000);
			
			// Minimum of BGT from virtual client added
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":"+cfg.bw.med+ ":");
			if(cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailWiFiBW(cfg.bw.med) +"Mbit/s");
					
			sleep(cfg.scen.largeTime * 1000);
			
			// Max of BGT from virtual client added
			tcp.sendMessage("updateBW:" + cfg.virtClientID + ":"+cfg.bw.max+ ":");
			if(cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailWiFiBW(cfg.bw.max) +"Mbit/s");

			sleep(cfg.scen.smallTime * 1000);

			//LTE Handover
			handOverTo("LTE", cfg.bw.none, cfg.realClientID);
			
			sleep(cfg.scen.largeTime * 1000);		
			
			// Medium of BGT from virtual client added in LTE
			tcp.sendMessage("setNet:" + cfg.virtClientID + ":LTE:");
			System.out.println("Handover of virtual client to LTE");
			
			if(cfg.ipfw) ExecShell.executeCommand(".\\ipfw pipe 1 config bw "+ cfg.bw.getAvailLTEBW(cfg.bw.med) +"Mbit/s");
			
			sleep(cfg.scen.largeTime * 1000);	
			
			System.out.println("Total duration: " + (double)(System.currentTimeMillis() - startTime)/1000);
		}while(cfg.repeat);
		
		this.terminate();
	}
}
