import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.ini4j.Ini;
import org.ini4j.Wini;

public class Config {
	private DecimalFormat precision = new DecimalFormat("###.##");
	public scenario scen = new scenario();
	public bandwidth bw = new bandwidth();

	int realClientID = 46;
	int virtClientID = 50;

	String serverIP = "192.168.10.15";
	int port = 5100;

	boolean tc = false;
	boolean repeat = true;

	public Config(String filepath) throws IOException {
		parse(filepath);
	}

	public Config() {

	}
	
	public void parse(String filepath) throws IOException {
		
		Wini ini = new Wini(new File(filepath));
		
		serverIP = ini.get("TCPServer", "serverIP" );
        port = ini.get("TCPServer", "port", int.class);
        System.out.println("Server: "+serverIP +" "+ port);
        
        realClientID = ini.get("clients", "realClientID", int.class);
        virtClientID = ini.get("clients", "virtClientID", int.class);
        System.out.println("Clients: " +realClientID +" "+ virtClientID);
        
        Ini.Section scenario = ini.get("scenario");
         scen.parse(scenario);
        
        Ini.Section bandwidth = ini.get("bandwidth");
         bw.parse(bandwidth);
        
        Ini.Section netem = ini.get("netem");
         tc = netem.get("functional", boolean.class);
         if(tc)
        	System.out.println("Sending commands to Network Emulator!");
         
	}

	//TODO Throw exception in case the BGT is greater than LTE or WiFi
	public class bandwidth {
		public double WiFi = 13;
		public double LTE = 15;
		public double noBGT = 0.15;
		public double minBGT = 4.85;
		public double medBGT = 9.75;
		public double maxBGT = 14.5;

		public bandwidth() {
		}

		public bandwidth(Ini.Section bw) {
			this.parse(bw);
		}

		private void parse(Ini.Section bw) {
			WiFi = bw.get("WiFi", int.class);
			LTE = bw.get("LTE", int.class);

			noBGT = bw.get("none", double.class);
			minBGT = bw.get("min", double.class);
			medBGT = bw.get("med", double.class);
			maxBGT = bw.get("max", double.class);

			System.out.println("BW: " + WiFi + " " + LTE);
			System.out.println("BGT: " + noBGT + " " + minBGT + " " + medBGT + " " + maxBGT);
		}
		
		public String getAvailWiFiBW(double bgt){ return(precision.format(WiFi-bgt)); }
		
		public String getAvailLTEBW(double bgt){ return(precision.format(LTE-bgt)); }
	}

	public class scenario {
		public int smallTime = 5;
		public int medTime = 7;
		public int largeTime = 10;
		public boolean repeative = false;
		public int num = 2;
		
		public scenario() {
		}

		public scenario(Ini.Section scenario) {
			this.parse(scenario);
		}

		private void parse(Ini.Section scenario) {
			repeat = scenario.get("repeat", boolean.class);
			if (repeat)
				System.out.println("Repeatitive scenario!");
			
			num = scenario.get("scenario", int.class);
			
			smallTime = scenario.get("small", int.class);
			medTime = scenario.get("medium", int.class);
			largeTime = scenario.get("large", int.class);

			System.out.println("Time: " + smallTime + " " + medTime + " " + largeTime);

		}
	}
}
