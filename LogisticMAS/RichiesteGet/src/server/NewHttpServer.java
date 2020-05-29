package server;

import java.awt.desktop.PrintFilesEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class NewHttpServer {

	public void run(String address, int port) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(address, port), 0);
		server.createContext("/", new  MyHttpHandler());
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);
		server.start();
		System.out.println("Server started on port "+server.getAddress().toString());
	}
	
	private class MyHttpHandler implements HttpHandler {    
		  @Override    
		  public void handle(HttpExchange httpExchange) throws IOException {
			  if("POST".equals(httpExchange.getRequestMethod())) {
				  System.out.println("NUOVA RICHIESTA POST");
				  String requestParamValue = handlePostRequest(httpExchange); 
				  handleResponse(httpExchange,requestParamValue); 
			  }    
		  }
		  
		  private String handlePostRequest(HttpExchange httpExchange) {
			  try {
				  StringBuilder sb = new StringBuilder();
				  InputStream ios = httpExchange.getRequestBody();
				  int i;
				  while ((i = ios.read()) != -1) {
					  sb.append((char) i);
					  }
				  System.out.println("REQUEST: " + sb.toString());
				  return sb.toString();
			  } catch (IOException e) {
				  System.out.println("Richiesta non valida");
				  return "ERROR";
			  }
		  	}
		
		  private void handleResponse(HttpExchange httpExchange, String requestParamValue)  throws  IOException {

			  OutputStream outputStream = httpExchange.getResponseBody();
			  
			  String htmlResponse = "";
			  
			  if(requestParamValue == "ERROR"){
				  htmlResponse = "ERROR - "+requestParamValue;
			  }
			  else {
				  htmlResponse = "200 - "+requestParamValue;
			  }
			  
			  httpExchange.sendResponseHeaders(200, htmlResponse.length());
			  outputStream.write(htmlResponse.getBytes());
			  outputStream.flush();
			  outputStream.close();


		  }
	}
}
