package autoMag.httpManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import autoMag.agents.Warehouse;
import autoMag.shelves.ShelfUnit;
import autoMag.shelves.Ware;
import autoMag.space.Coord;
import autoMag.transaction.Transaction;
import autoMag.utils.Decoders;
import autoMag.utils.TransactorInfo;
import jade.core.AID;

public class RequestListener {
	
	Warehouse agentWarehouse;
	HashMap<AID, TransactorInfo> transactorsList;
	TreeMap<Coord, ShelfUnit> shelvesList;
	HashMap<Integer, Transaction> transactionsList;
	
	public RequestListener(Warehouse agent, HashMap<AID, TransactorInfo> transactors, TreeMap<Coord, ShelfUnit> shelves, HashMap<Integer, Transaction> transactions) {
		agentWarehouse = agent;
		transactorsList = transactors;
		shelvesList = shelves;
		transactionsList = transactions;
	}

	public void run(String address, int port) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(address, port), 0);
		server.createContext("/", new  MyHttpHandler(transactorsList, shelvesList, transactionsList));
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);
		server.start();
		System.out.println("Server started on port "+server.getAddress().toString());
	}
	
	private class UpdateThread extends Thread {
		
	    String[] update;
        public UpdateThread(String[] updateMsg) {
            update = updateMsg;
        }

        public void run() {
        	executeTransporterManagerUpdate(update);
        }
        
        private void executeTransporterManagerUpdate(String[] update) {
        	
        	String[] updateMsg = update;
			  
			  System.out.println("EXECUTING UPDATE = "+updateMsg.toString());
			  
			if(updateMsg[0].equals("DELIVERED_TRANSACTOR")) {
				AID transactorAID = null;
				//System.out.println("STARTA IL FOR DI UPDATETHREAD");
				for (AID key : transactorsList.keySet()) {
					//System.out.println(transactorsList.get(key).getCoord().getSector()+" == "+updateMsg[1]);
					if(transactorsList.get(key).getCoord().getSector().equals(updateMsg[1])) {						
						transactorAID = key;
					}
				}
				if(transactorAID != null) {
					transactorsList.get(transactorAID).setWareQuantity(transactorsList.get(transactorAID).getWareQuantity()+Integer.valueOf(updateMsg[2]));
					System.out.println("messo nel transactor "+transactorsList.get(transactorAID).getCoord().getSector()+" un numero di merci pari a  "+Integer.valueOf(updateMsg[2])+" ora "+transactorsList.get(transactorAID).getWareQuantity());
					int transactionCode = transactorsList.get(transactorAID).getTransactionCode();
					//controlla se la quantità di merce nel transactor corrisponde a quella dell'ordine e nel caso libera il transactor
					if(transactionsList.get(transactionCode).getQuantity() == transactorsList.get(transactorAID).getWareQuantity()) {
						agentWarehouse.setTransactorFree(transactorAID);
					}
				}
				else {
					System.out.println("ERROR: transactor not found");
				}
			}
			
			else if(updateMsg[0].equals("RETRIEVED_TRANSACTOR")) {
				AID transactorAID = null;
				//System.out.println("STARTA IL FOR DI UPDATETHREAD");
				for (AID key : transactorsList.keySet()) {
					//System.out.println(transactorsList.get(key).getCoord().getSector()+" == "+updateMsg[1]);
					if(transactorsList.get(key).getCoord().getSector().equals(updateMsg[1])) {
						transactorAID = key;
					}
				}
				if(transactorAID != null) {
					transactorsList.get(transactorAID).setWareQuantity(transactorsList.get(transactorAID).getWareQuantity()-Integer.valueOf(updateMsg[2]));
					System.out.println("preso dal transactor "+transactorsList.get(transactorAID).getCoord().getSector()+" un numero di merci pari a  "+Integer.valueOf(updateMsg[2])+" ora "+transactorsList.get(transactorAID).getWareQuantity());
				}
				else {
					System.out.println("ERROR: transactor not found");
				}
			}
			
			else if(updateMsg[0].equals("DELIVERED_SHELF")) {
				
				Coord shelfUnitCoord = new Coord(updateMsg[2]);
				int[] levels = Decoders.decodeShelfLevels(updateMsg[3]);
				Ware wareInfo = transactionsList.get(Integer.valueOf(updateMsg[1])).getWare();
				for(int i=0; i<levels.length; i++) {
					shelvesList.get(shelfUnitCoord).addWare(wareInfo, levels[i]);
					System.out.println("riempita la shelf "+shelfUnitCoord+" nel livello "+levels[i]+" con "+wareInfo.toString());
				}
				
			}
			
			else if(updateMsg[0].equals("RETRIEVED_SHELF")) {
				
				Coord shelfUnitCoord = new Coord(updateMsg[2]);
				int[] levels = Decoders.decodeShelfLevels(updateMsg[3]);
				for(int i=0; i<levels.length; i++) {
					boolean shelfResult = shelvesList.get(shelfUnitCoord).clearShelf(levels[i]);
					System.out.println("svuotata la shelf "+shelfUnitCoord.getSector()+" nel livello "+levels[i]+" = "+shelfResult);
				}				
				
			}
			
			else if(updateMsg[0].equals("ORDER_COMPLETED")) {
				
				transactionsList.get(Integer.valueOf(updateMsg[1])).setClosed();				
				
			}
			
			else if(updateMsg[0].equals("FREE_TRANSACTOR")) {
				
				//TODO mandare un messaggio al Transactor agente per liberarlo
				
				AID transactorAID = null;
				//System.out.println("STARTA IL FOR DI UPDATETHREAD");
				for (AID key : transactorsList.keySet()) {
					//System.out.println(transactorsList.get(key).getCoord().getSector()+" == "+updateMsg[1]);
					if(transactorsList.get(key).getCoord().getSector().equals(updateMsg[1])) {
						transactorAID = key;
					}
				}
				if(transactorAID != null) {
					agentWarehouse.setTransactorFree(transactorAID);
				}
				else {
					System.out.println("ERROR: transactor not found");
				}
				
			}
			
			else {
				System.out.println("ERROR: wrong update type");
			}
		}
    }
	
	private class MyHttpHandler implements HttpHandler { 
		
		HashMap<AID, TransactorInfo> transactorsList;
		TreeMap<Coord, ShelfUnit> shelvesList;
		HashMap<Integer, Transaction> transactionsList;
		
		public MyHttpHandler(HashMap<AID, TransactorInfo> transactors, TreeMap<Coord, ShelfUnit> shelves, HashMap<Integer, Transaction> transactions) {
			transactorsList = transactors;
			shelvesList = shelves;
			transactionsList = transactions;
		}
		
		@Override    
		  public void handle(HttpExchange httpExchange) throws IOException {
			  if("POST".equals(httpExchange.getRequestMethod())) {
				System.out.println("NEW POST REQUEST");
				handlePostRequest(httpExchange);
		  		handleResponse(httpExchange);
			  }    
		  }
		  
		  private void handlePostRequest(HttpExchange httpExchange) {
			  try {
				  StringBuilder sb = new StringBuilder();
				  InputStream ios = httpExchange.getRequestBody();
				  int i;
				  while ((i = ios.read()) != -1) {
					  sb.append((char) i);
					  }
				  System.out.println("REQUEST: " + sb.toString());
				  //update infos
				  //System.out.println("BEFORE EXECUTING UPDATE = "+Decoders.parseTransporterManagerUpdate(sb.toString()));
				  UpdateThread p = new UpdateThread(Decoders.parseTransporterManagerUpdate(sb.toString()));
				  p.start();
				  //System.out.println("AFTER EXECUTING UPDATE");
			  } catch (IOException e) {
				  e.printStackTrace();
				  System.out.println("Richiesta non valida");		  
			  }
		  }
		
		  
		
		private void handleResponse(HttpExchange httpExchange)  throws  IOException {
		
			OutputStream outputStream = httpExchange.getResponseBody();
			  
			String htmlResponse = "200";
			  
			httpExchange.sendResponseHeaders(200, htmlResponse.length());
			outputStream.write(htmlResponse.getBytes());
			outputStream.flush();
			outputStream.close();
		
		
		}
	}
}
