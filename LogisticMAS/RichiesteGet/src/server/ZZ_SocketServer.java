package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ZZ_SocketServer {

	public static void main(String args[] ) throws IOException {
	
	    ServerSocket server = new ServerSocket(8080);
	    System.out.println("Listening for connection on port "+server.getInetAddress().toString());
        while (true) {
            try (Socket clientSocket = server.accept()) {
            	System.out.println("NUOVA RICHIESTA");            	
    	        InputStreamReader isr =  new InputStreamReader(clientSocket.getInputStream());
    	        BufferedReader reader = new BufferedReader(isr);
    	        String line = reader.readLine();            
    	        while (!line.isEmpty()) {
    	            System.out.println(line);
    	            line = reader.readLine();
    	        }
                String httpResponse = "HTTP/1.1 200 OK\r\n\r\n";
                clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            }
        }
	}
}