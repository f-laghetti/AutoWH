package server;

import java.io.IOException;

public class TestServer {
	
	public static void main(String arg[]) {
		
		NewHttpServer server = new NewHttpServer();
		
		try {
			server.run("localhost",8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

}
