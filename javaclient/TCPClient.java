//TCPClient.java

import java.io.*;
import java.net.*;

class TCPClient {
	private String FromServer, Host;
	private String ToServer;
	private BufferedReader inFromUser;
	private Socket clientSocket;
	private PrintWriter outToServer;
	private BufferedReader inFromServer;
	private int port = 5100;

	public TCPClient(String Host) throws IOException {
		this.Host = Host;
		this.init();
	}

	public TCPClient(String host, int port) throws IOException {
		this.Host = host;
		this.port = port;
		this.init();
	}

	private void init() throws IOException{

		this.clientSocket = new Socket(Host, port);
		this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
		this.outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
		this.inFromServer = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));

	}

	public void terminate() {
		System.out.println("Terminating TCP connection!");
		System.out.println("Arrivederci!");
		try {
			clientSocket.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public void sendMessage(String msg) throws IOException {

		ToServer = msg;

		if (ToServer.equals("Quit") || ToServer.equals("quit")) {
			outToServer.println(ToServer);
			System.out.println("Arrivederci!");
			clientSocket.close();
		}

		else {
			outToServer.println(ToServer);
			System.out.println("Message send: " + ToServer);
		}

		FromServer = inFromServer.readLine();
		System.out.println("RECIEVED:" + FromServer);
	}

	public void run() {
		try {
			while (true) {

				ToServer = inFromUser.readLine();

				if (ToServer.equals("Quit") || ToServer.equals("quit")) {
					outToServer.println(ToServer);
					System.out.println("Arrivederci!");
					clientSocket.close();
					break;
				}

				else {
					outToServer.println(ToServer);
					System.out.println("Message send: " + ToServer);
				}

				FromServer = inFromServer.readLine();
				System.out.println("RECIEVED:" + FromServer);

			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
}
