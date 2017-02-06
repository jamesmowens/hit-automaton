package com.hp.hpl.CHAOS.HIT;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerLauncher {
    static ServerSocket serverSocket = null;
	static Socket clientSocket = null;
	static PrintWriter out;
	static BufferedReader in;

    static private Object flagLock = new Object();
	static boolean flag = false;
    final static String endingString = "-----END-----";
	
	public static void main(String[] args) throws IOException {
		 
        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 8888.");
            System.exit(1);
        }

        InetAddress ip;
        try {

        	ip = InetAddress.getLocalHost();
        	System.out.println("Current IP address : " + ip.getHostAddress());

        } catch (UnknownHostException e) {

        	e.printStackTrace();

        }

        while(true) {
        	synchronized (flagLock) {
        		while(flag) {
        			try {
						flagLock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		flag = true;
        	}
        	getConnection();
        }
    }

	public static void getConnection() {
		System.out.println("wait for connection");
    	try {
    		clientSocket = serverSocket.accept();
    	} catch (IOException e) {
    		System.err.println("Accept failed.");
    		System.exit(1);
    	}

    	try {
    		out = new PrintWriter(clientSocket.getOutputStream(), true);
    		in = new BufferedReader(
    				new InputStreamReader(
    						clientSocket.getInputStream()));
    	} catch (IOException e) {
			e.printStackTrace();
    	}

    	String data = "";
    	
    	String inputLine;
    	
		try {
			while ((inputLine = in.readLine()) != null) {
				if(inputLine.equals(endingString)) {
					break;
				}
				data+=inputLine +"\n";
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	
    	try {
			PrintWriter out2 = new PrintWriter("input.xml");
			out2.print(data);
			out2.flush();
			out2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	CHAOSConnection.processData();
	}
	
	public static void finishConnection() {
		System.out.println("Closing connection");
		
		String outputFile = "output.xml";
		String feedback = getStringFromFile(outputFile);
		
		out.println(feedback);
		out.println(endingString);
		out.flush();
		
		try {
			in.close();
			out.close();
			clientSocket.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		synchronized (flagLock) {
			flag = false;
			flagLock.notifyAll();
		}
	}
	

	public static String getStringFromFile(String filePath) {
		BufferedReader br = null;
		String data = "";

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(filePath));

			while ((sCurrentLine = br.readLine()) != null) {
				data+=sCurrentLine + "\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return data;
	}
}
