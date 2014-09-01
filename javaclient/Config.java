import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;
import org.ini4j.Wini;

public class Config {
	

	//All the Bandwidth values are in Mbpps!
	//All the time values are in seconds
	
	// Time delays for the scenario
	int[] delays =	{ 8,	// 1.Without BGT
					  8,	// 2.With 5Mbits
					  5,	// 3.With 12Mbits and Time to Hand-off to LTE
					  8 };// 4.Time of Real client in LTE.
					

	int WiFi = 25;
	int LTE = 15;
	double[] BGT = { 0.125, 4.925, 11.825 };

	int realClientID = 46;
	int virtClientID = 50;

	String serverIP = "192.168.10.15";
	int port = 5100;

	boolean ipfw = false;

	Scenario scenario = null;

	boolean repeat = true;

	public Config(String filepath) throws IOException {
		parse(filepath);
	}
	
	public Config(){
		
	}
	
	public void parse(String filepath) throws IOException {
		Wini ini = new Wini(new File(filepath));
		serverIP = ini.get("TCPServer", "serverIP" );
        port = ini.get("TCPServer", "port", int.class);
        System.out.println(serverIP +" "+ port);
        
        realClientID = ini.get("clients", "realClientID", int.class);
        virtClientID = ini.get("clients", "virtClientID", int.class);
        System.out.println(realClientID +" "+ virtClientID);
        
        Ini.Section scenario = ini.get("scenario");
        repeat = scenario.get("repeat", boolean.class);
        if(repeat)
        	System.out.println("Repeatitive scenario!");
        
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

}
