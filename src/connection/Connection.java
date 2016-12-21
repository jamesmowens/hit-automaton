package connection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.usfca.vas.window.WindowMachineAbstract;

public class Connection {
    static Socket serverSocket = null;
    static PrintWriter out = null;
    static BufferedReader in = null;
    final static String endingString = "-----END-----";

	public static boolean sendData(String dataString) {   
		 
        try {
        	serverSocket = new Socket("localhost", 8888);
            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: localhost.");
            return false;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: localhost.");
            return false;
        }
        
        out.println(dataString);
        out.println(endingString);
		out.flush();
        
        String fromServer;
        String feedback = "";
        try {
			while ((fromServer = in.readLine()) != null) {
				if(fromServer.equals(endingString)) {
					break;
				}
				feedback+=fromServer +"\n";
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        
        System.out.println(feedback);

    	try {
			PrintWriter out2 = new PrintWriter("feedback.xml");
			out2.print(feedback);
			out2.flush();
			out2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

        try {
        	in.close();
        	out.close();
        	serverSocket.close();
        } catch (IOException e) {
        	// TODO Auto-generated catch block
        	e.printStackTrace();
		}
        
        return true;
	}
}
