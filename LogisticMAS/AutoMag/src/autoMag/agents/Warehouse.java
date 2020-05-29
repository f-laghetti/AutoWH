/*
 * agente che si occupa della gestione di tutte le azioni del magazzino
 */

package autoMag.agents;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import FIPA.stringsHelper;
import autoMag.httpManager.Request;
import autoMag.httpManager.RequestListener;
import autoMag.shelves.Shelf;
import autoMag.shelves.ShelfCoord;
import autoMag.shelves.ShelfStatus;
import autoMag.shelves.ShelfUnit;
import autoMag.shelves.Ware;
import autoMag.space.Coord;
import autoMag.space.CoordComparator;
import autoMag.transaction.Transaction;
import autoMag.transaction.TransactionStatus;
import autoMag.transaction.TransactionType;
import autoMag.utils.ChargerInfo;
import autoMag.utils.ChargerStatus;
import autoMag.utils.Decoders;
import autoMag.utils.TransactorInfo;
import autoMag.utils.TransporterInfo;
import autoMag.utils.TransporterStatus;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.List;


public class Warehouse extends Agent {	
	//interfaccia del warehouse
	WarehouseGui myGui;
	WarehouseAddTransporterGui addGui;
	//lista dei transactor
	private HashMap<AID, TransactorInfo> transactors;
	//planimetria delle scaffalature
	private TreeMap<Coord, ShelfUnit> shelves;
	//numero delle transazioni fino a quel momento, sar� il numero identificativo della prossima
	private int transactionCount = 0;
	//transaction list
	private HashMap<Integer, Transaction> transactionList;
	//
	static String TRANSPORTER_MANAGER_URL = "http://localhost:8000/";
	//
	static String CONNECT = "CONNECT";
	
	
	public void setup() {
		
		//richiama l'interfaccia
		myGui = new WarehouseGui(this);
		myGui.showGui();
		addGui = new WarehouseAddTransporterGui(this);
		addGui.showGui();
		
		System.out.println("NEW WAREHOUSE CREATED\nWarehouse name:\n"+getAID().getName());
		Object[] arg = getArguments();
		if (arg != null && arg.length == 2) {
			transactors = new HashMap<AID, TransactorInfo>();
			shelves = new TreeMap<Coord, ShelfUnit>(new CoordComparator());
			transactionList  = new HashMap<Integer, Transaction>();
			
			try{
				String urlShelves = (String) arg[1]; //prende la repository dove si trova il file contenente le posizioni delle scaffalature
				ObjectInputStream shelvesReader = new ObjectInputStream(new FileInputStream(urlShelves));
				shelves = (TreeMap<Coord, ShelfUnit>) shelvesReader.readObject();
				shelvesReader.close();
				//registra il magazzino nelle yellow pages
				DFAgentDescription dfd = new DFAgentDescription();
				dfd.setName(getAID());
				ServiceDescription sd = new ServiceDescription();
				sd.setType("warehousing");
				sd.setName("AutoMag-warehousing");
				dfd.addServices(sd);
				try {
					DFService.register(this, dfd);
				}
				catch (FIPAException e) {
					e.printStackTrace();
				}
				//crea il ricevitore di tutte le richieste che possono arrivare
				addBehaviour(new RequestReceiver());
				//crea il ricevitore di tutte le comunicazione con i transporter
				TransporterComunicationReceiver();
				
			}
			catch(FileNotFoundException e) {
				System.out.println("Warehouse space file not found.");
				doDelete();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
		}
		else {
			//termina l'agente se non � stata trovata una planimetria valida
			System.out.println("No warehouse space specified.");
			doDelete();
		}
		
	}
	
	public void takeDown() {
		System.out.println("The Warehouse "+getAID().getName()+" is shutting down.");
	}
	
	//METODI
	
	//richiesta di aggiunta di un nuovo trasportatore al magazzino
	public void AddNewTransporter(String mac, String sector, String cap, Boolean dir) { 
		
		String macAddress = mac;
		String sectorCoord = sector;
		String capacity = cap;
		Boolean antiClockWise = dir;
		
		ArrayList<String> infoList = new ArrayList<String>();
		infoList.add("CONNECT");
		infoList.add(macAddress);
		infoList.add(sectorCoord);
		if(antiClockWise) {
			infoList.add("F");
		}
		else {
			infoList.add("T");
		}
		infoList.add(capacity);
		String delimiter = "&";
		String addRequest = String.join(delimiter, infoList);
		//send add transporter request to the TransporterManager
		try {
			Request.sendPOST(TRANSPORTER_MANAGER_URL, addRequest);
		} catch (IOException e) {
			System.out.println("Add Transporter request failed!");
		}

		
	}
	
	public void printAID() { //stampa l'AID del magazzino
		System.out.print("This Warehouse AID: " + this.getAID().getName());		
	}
	
	/*public String getTransportersList() { //mostra la lista dei transporter registrati nel magazzino con le relative informazioni
		String list = "";
		for (AID key : transporters.keySet()) {
		    list += transporters.get(key).toString()+"\n";
		}
		return list;
	}*/
	
	public String getTransactorsList() { //mostra la lista dei transactor registrati nel magazzino con le relative informazioni
		String list = "";
		for (AID key : transactors.keySet()) {
		    list += transactors.get(key).toString()+"\n";
		}
		return list;
	}
	
	public String getShelvesList() { //mostra la lista degli scaffali
		String list = "";
		for (Coord key : shelves.keySet()) {
			list += shelves.get(key).toString()+"\n";
		}
		return list;
	}
	
	String getTransactionList() { //mostra la lista delle transazioni DELIVER aperte
		String list = "";
		for (Integer key : transactionList.keySet()) {
			list += transactionList.get(key).toString()+"\n";
		}
		return list;
	}
	
	/*public String getOpenTransactionList() { //mostra la lista delle transazioni DELIVER aperte
		String list = "";
		for (Integer key : deliveryOpen.keySet()) {
			list += deliveryOpen.get(key).toString()+"\n";
		}
		return list;
	}
	
	public String getClosedTransactionList() { //mostra la lista delle transazioni DELIVER chiuse
		String list = "";
		for (Integer key : deliveryClosed.keySet()) {
			list += deliveryClosed.get(key).toString()+"\n";
		}
		return list;
	}
	

	public String getOpenCollectList() { //mostra la lista delle transazioni COLLECT aperte
		String list = "";
		for (Integer key : collectOpen.keySet()) {
			list += collectOpen.get(key).toString()+"\n";
		}
		return list;
	}
	
	public String getClosedCollectList() { //mostra la lista delle transazioni COLLECT chiuse
		String list = "";
		for (Integer key : collectClosed.keySet()) {
			list += collectClosed.get(key).toString()+"\n";
		}
		return list;
	}*/
	
	
	
	private int countFreeShelves() { //conta il numero di scaffali liberi
		int freeCount = 0;
		Shelf[] tmpShelfUnit;
		
		for (Coord key : shelves.keySet()) {
		    tmpShelfUnit = shelves.get(key).getShelves();
		    for(Shelf shelf : tmpShelfUnit) {
		    	if(shelf.getStatus() == ShelfStatus.EMPTY) {
		    		freeCount++;
		    	}
		    }
		}
		
		return freeCount;
	}


	private int countWares(String wareCode, String wareName, AID wareOwner) { //conta il numero di merci di un determinato tipo
		int wareCount = 0;
		Shelf[] tmpShelfUnit;
		Ware goalWare, shelfWare;
		
		//crea il ware cercato
		goalWare = new Ware(wareCode, wareName, wareOwner);
		for (Coord key : shelves.keySet()) {
		    tmpShelfUnit = shelves.get(key).getShelves();
		    for(Shelf shelf : tmpShelfUnit) {
		    	//controlla che la shelf sia piena
		    	if(shelf.getStatus()==ShelfStatus.FULL) {
			    	//salva temporaneamente il Ware presente sulla shelf
		    		shelfWare = shelf.getWare();
			    	//controlla che esso corrisponda al Ware cercato
			    	if(goalWare.equals(shelfWare)) {
			    		wareCount++;
			    	}
		    	}
		    }
		}		
		return wareCount;
	}
	
	private ArrayList<ShelfCoord> reserveShelvesForCollect(String wareCode, String wareName, AID wareOwner, int quantity) { //prenota un determinato numero di shelf contenenti merci di un determinato tipo che devono essere ritirate
		ArrayList<ShelfCoord> reserved = new ArrayList<ShelfCoord>();
		Shelf[] tmpShelfUnit;
		int count=0; //conta il numero di shelves prenotate
		Ware goalWare, shelfWare;
		
		//crea il ware cercato
		goalWare = new Ware(wareCode, wareName, wareOwner);
		if(count<quantity) {
			for (Coord key : shelves.keySet()) {
				if(count<quantity) {	
				    tmpShelfUnit = shelves.get(key).getShelves();
				    for(int i=0; i<tmpShelfUnit.length; i++) {
						if(count<quantity) {
					    	if(tmpShelfUnit[i].getStatus() == ShelfStatus.FULL) {
						    	//salva temporaneamente il Ware presente sulla shelf
					    		shelfWare = tmpShelfUnit[i].getWare();
					    		if(goalWare.equals(shelfWare)) {
					    			reserved.add(new ShelfCoord(key, i));//aggiunge alla lista la shelf vuota trovata
						    		tmpShelfUnit[i].reserveForCollect();//
						    		count++;
					    		}
					    	} 
				    	}
				    }
				}
			}
		}
		
		return reserved;
	}
	
	private ArrayList<ShelfCoord> reserveShelves(int quantity) { //prenota un numero stabilito di scaffali
		ArrayList<ShelfCoord> reserved = new ArrayList<ShelfCoord>();
		Shelf[] tmpShelfUnit;
		int count=0; //conta il numero di shelves libere trovate
		
		if(count<quantity) {
			for (Coord key : shelves.keySet()) {
				if(count<quantity) {			
				    tmpShelfUnit = shelves.get(key).getShelves();
				    for(int i=0; i<tmpShelfUnit.length; i++) {
						if(count<quantity) {	
					    	if(tmpShelfUnit[i].getStatus() == ShelfStatus.EMPTY) {
					    		reserved.add(new ShelfCoord(key, i));//aggiunge alla lista la shelf vuota trovata
					    		tmpShelfUnit[i].reserve();//
					    		count++;
					    	} 
				    	}
				    }
				}
			}
		}
		
		return reserved;
	}

	public HashMap<Coord, ArrayList<Integer>> getShelvesToLoad(Transaction transaction, int numberNeeded) {
		ArrayList<ShelfCoord> shelvesReserved = transaction.getShelvesReserved();
		HashMap<Coord, ArrayList<Integer>> shelvesResult = new HashMap<Coord, ArrayList<Integer>>();
		ShelfCoord tmp;
		for(int i=0; i<numberNeeded; i++) {
			//controlla che ci siano ancora shelf riservate
			if(!shelvesReserved.isEmpty()) {
				//prende sempre la prima shelf prenotata (ad ogni ciclo la prima shelf viene eliminata dalla lista)
				tmp = shelvesReserved.get(0);
				//se una shelf con quelle coordinate � gi� stata inserita, si aggiunge il livello da riempire a quella coordinata
				if(shelvesResult.containsKey(tmp.getCoord())) {
					shelvesResult.get(tmp.getCoord()).add(tmp.getLevel());
				}
				//altrimenti si crea una nuova chiave
				else {
					//crea una nuova chiave e la relativa lista di integer in cui verr� inserito il livello della shelf da riempire
					shelvesResult.put(tmp.getCoord(), new ArrayList<Integer>(Arrays.asList(tmp.getLevel())));
				}
				//imposta la shelf come INPROGRESS
				shelves.get(tmp.getCoord()).inProgressShelf(tmp.getLevel());
				//rimuove la shelf dalla lista
				shelvesReserved.remove(0);
			}
			else {
				return shelvesResult;
			}
		}
		return shelvesResult;		
	}
	
	public HashMap<Coord, ArrayList<Integer>> getShelvesToCollect(Transaction transaction, int numberNeeded) {
		ArrayList<ShelfCoord> shelvesReserved = transaction.getShelvesReserved();
		HashMap<Coord, ArrayList<Integer>> shelvesResult = new HashMap<Coord, ArrayList<Integer>>();
		ShelfCoord tmp;
		for(int i=0; i<numberNeeded; i++) {
			//controlla che ci siano ancora shelf riservate
			if(!shelvesReserved.isEmpty()) {
				//prende sempre la prima shelf prenotata (ad ogni ciclo la prima shelf viene eliminata dalla lista)
				tmp = shelvesReserved.get(0);
				//se una shelf con quelle coordinate � gi� stata inserita, si aggiunge il livello da riempire a quella coordinata
				if(shelvesResult.containsKey(tmp.getCoord())) {
					shelvesResult.get(tmp.getCoord()).add(tmp.getLevel());
				}
				//altrimenti si crea una nuova chiave
				else {
					//crea una nuova chiave e la relativa lista di integer in cui verr� inserito il livello della shelf da riempire
					shelvesResult.put(tmp.getCoord(), new ArrayList<Integer>(Arrays.asList(tmp.getLevel())));
				}
				//imposta la shelf come COLLECTING
				shelves.get(tmp.getCoord()).isCollectingShelf(tmp.getLevel());
				//rimuove la shelf dalla lista
				shelvesReserved.remove(0);
			}
			else {
				return shelvesResult;
			}
		}
		return shelvesResult;		
	}
	
	//si occupa di inviare una richiesta per prelevare merci dall'esterno e di sistemarle all'interno del magazzino
		public void StoreWare(AID newTransactorAID, Transaction newTransaction) {
			AID transactorAID = newTransactorAID;
			Transaction transaction = newTransaction;
			
			String content = "DELIVER_SHELF&"+String.valueOf(transaction.getCode())+"&"; //aggiunge il tipo di comando e il codice dell'ordine
			content += transactors.get(transactorAID).getCoord().getSector()+"&"; //aggiunge la coordinata del transactor
			content += String.valueOf(transaction.getQuantity())+"&"; //aggiunge la quantità di merce nel transactor
			TreeMap<String, ArrayList<String>> tmp_shelves = new TreeMap<String, ArrayList<String>>();
			
			//si crea una lista di tutte le shelf prenotate con i relativi livelli 
			for(ShelfCoord tmp_shelf : transaction.getShelvesReserved()) {
				if (tmp_shelves.containsKey(tmp_shelf.getCoord().getSector())){ //se la shelf a quella coordinata è già stata trovata			
				}
				else {
					tmp_shelves.put(tmp_shelf.getCoord().getSector(), new ArrayList<String>()); //se la si incontra quella shelf la prima volta la si aggiunge
				}
				tmp_shelves.get(tmp_shelf.getCoord().getSector()).add(String.valueOf(tmp_shelf.getLevel())); //aggiunge il livello a quella coordinata	
			}
			
			//aggiunge le shelves così trovate al content
			for (String key : tmp_shelves.keySet()) {
				content += key+"|"; //aggiunge la coordinata
				for (String tmp_level : tmp_shelves.get(key)) {
					content += tmp_level+"/";
				}
				content = content.substring(0, content.length() - 1); //rimuove l'ultimo "/" che è superfluo
				content += "$"; 
			}
			
			content = content.substring(0, content.length() - 1); //rimuove l'ultimo "$" che è superfluo
			
			System.out.println("CONTENT = "+content);
			
			try {
				String responseRequest = Request.sendPOST(TRANSPORTER_MANAGER_URL, content);
				transaction.setInProgress(); //aggiorna lo stato della transazione in progress
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	
	public void setTransactorFree(AID transactorAID) {
		addBehaviour(new TransactorFreeBehaviour(transactorAID));
	}
		
	private void TransporterComunicationReceiver() {
		
		RequestListener server = new RequestListener(this, transactors, shelves, transactionList);
		try {
			server.run("127.0.0.1",8080);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//BEHAVIOUR
	
	private class TransactorFreeBehaviour extends Behaviour{
		
		AID transactorAID;
		ACLMessage confirmFreeTransactor;
		MessageTemplate mtFreeTransactorConfirm;
		boolean transactorFreeSent = false;
		boolean transactorIsFree = false;
		
		public TransactorFreeBehaviour(AID transactor) {
			transactorAID = transactor;
		}
		
		public void action() {
			
			if(transactorFreeSent == false) {
				ACLMessage freeMsg = new ACLMessage(ACLMessage.CFP);
				freeMsg.addReceiver(transactorAID);
				freeMsg.setContent("The transactor is free");
				freeMsg.setConversationId("transactor-free");
				freeMsg.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(freeMsg);
				//prepara il template per ricevere la risposta
				mtFreeTransactorConfirm = MessageTemplate.and(MessageTemplate.MatchConversationId("transactor-free"), MessageTemplate.MatchInReplyTo(freeMsg.getReplyWith()));
				System.out.println("Transactor set free message sent");
				transactorFreeSent = true;
			}
			
			else {
				confirmFreeTransactor = myAgent.receive(mtFreeTransactorConfirm);
				transactors.get(transactorAID).setFree();
				transactorIsFree = true;
				System.out.println("Liberato transactor in "+transactors.get(transactorAID).getCoord().getSector());
			}
			
			block();
		}
		
		public boolean done() {
			return transactorIsFree;
		}
		
	}
	
	private class RequestReceiver extends CyclicBehaviour{
		//TEMPLATE DI TUTTE LE RICHIESTE CHE POSSONO ARRIVARE ALL'AGENTE
		//Richiesta di aggiungere un transporter al magazzino
		//MessageTemplate mtAddTransporterRequest = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId("add-transporter"));
		//Richiesta di aggiungere un transactor al magazzino
		MessageTemplate mtAddTransactorRequest = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId("add-transactor"));
		//Richiesta di depositare merce nel magazzino
		MessageTemplate mtStoreRequest = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId("store-request"));
		//Richiesta di prelevare merce dal magazzino
		MessageTemplate mtCollectRequest = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId("collect-request"));
		//Richiesta di controllare se una richiesta di deposito � valida
		MessageTemplate mtCheckDelivery = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST), MessageTemplate.MatchConversationId("check-delivery"));		

		//CONTENITORI DELLE RICHIESTE
		ACLMessage rqstTransporter;
		ACLMessage rqstTransactor;
		ACLMessage rqstCharger;
		ACLMessage rqstStore;
		ACLMessage rqstCollect;
		ACLMessage rqstDeliveryCheck;
		
		public void action() {
			//Il behaviour controlla se il messaggio ricevuto � una delle richieste
			//rqstTransporter = myAgent.receive(mtAddTransporterRequest);
			rqstTransactor = myAgent.receive(mtAddTransactorRequest);
			rqstStore = myAgent.receive(mtStoreRequest);
			rqstCollect = myAgent.receive(mtCollectRequest);
			rqstDeliveryCheck = myAgent.receive(mtCheckDelivery);
			
			/*if(rqstTransporter != null) {
				addBehaviour(new AddNewTransporter((ACLMessage) rqstTransporter.clone()));//crea un nuovo opportuno behaviour che si occuper� di gestire il messaggio
				rqstTransporter = null;
			}*/
			if(rqstTransactor != null) {
				addBehaviour(new AddNewTransactor((ACLMessage) rqstTransactor.clone()));
				rqstTransactor = null;
			}
			if(rqstStore != null) {
				addBehaviour(new StoreRequest((ACLMessage) rqstStore.clone()));
				rqstStore = null;
			}
			if(rqstDeliveryCheck != null) {
				addBehaviour(new DeliveryCheck((ACLMessage) rqstDeliveryCheck.clone()));
				rqstDeliveryCheck = null;
			}
			if(rqstCollect != null) {
				addBehaviour(new CollectRequest((ACLMessage) rqstCollect.clone()));
				rqstCollect = null;
			}
			block();
		}
		
	}	
	
	//esamina la richiesta di aggiunta di un nuovo Transactor al magazzino
	private class AddNewTransactor extends Behaviour {
		
		ACLMessage request;
		boolean requestReplied = false;
		boolean requestRejected = false;
		boolean existID = false;
		
		public AddNewTransactor(ACLMessage newRequest) {
			request = newRequest;
		}
		
		public void action() {
			String newTransactorInfoStr = request.getContent();
			String[] newTransactorInfo = newTransactorInfoStr.split(":");
			String newTransactorID = newTransactorInfo[0];
			Coord newTransactorCoord = new Coord(newTransactorInfo[1]);//TODO aggiungere il primo argomento ai dati passati
			System.out.println("Transactor ID:"+newTransactorID+" NAME:"+request.getSender().getName()+" asked to deploy in this warehouse at coord "+newTransactorCoord.toString());
			ACLMessage reply = request.createReply();
			if(!transactors.containsKey(request.getSender())) {//controlla che non esista gi� un transactor con lo stesso AID
				for (AID key : transactors.keySet()) {//controlla se � gi� presente un transactor con lo stesso ID
					if(transactors.get(key).getID().equals(newTransactorID)){
				    	existID = true;
				    }
				}
				if(!existID){//controlla che non sia stato trovato un transactor con lo stesso ID
					reply.setPerformative(ACLMessage.AGREE);
					reply.setContent("ACCEPTED");
					myAgent.send(reply);
					transactors.put(request.getSender(), new TransactorInfo(newTransactorID, newTransactorCoord));
					System.out.println("TRANSACTOR ACCEPTED");
					requestReplied = true;
				}
				else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("ERROR 102");
					myAgent.send(reply);
					System.out.println("TRANSACTOR REJECTED: TRANSACTOR ID ALREADY EXIST");
					requestRejected = true;
				}
			}
			else {
				reply.setPerformative(ACLMessage.REFUSE);
				reply.setContent("ERROR 101");
				myAgent.send(reply);
				System.out.println("TRANSACTOR REJECTED: TRANSACTOR AID ALREADY EXIST");
				requestRejected = true;
			}		
			block();
		}
		
		public boolean done() {
			return requestReplied || requestRejected;
		}
		
	}

	
	//esamina la richiesta di deposito merci all'interno del magazzino
	private class StoreRequest extends Behaviour {
	
		MessageTemplate mtConfirmTransaction;
		ACLMessage request;
		ACLMessage confirm;
		boolean requestReplied = false;
		boolean requestConfirmed = false;
		boolean requestRejected = false;
		
		public StoreRequest(ACLMessage newRequest) {
			request = newRequest;
		}
		
		public void action() {
			if(!requestReplied) {
				String requestContent = request.getContent();
				String[] wareInfo = requestContent.split("\\|");
				String wareCode = wareInfo[0];
				String wareName = wareInfo[1];
				int wareQuantity = (int) Integer.parseInt(wareInfo[2]);
				System.out.println("CLIENT:"+request.getSender().getName()+" requested to store "+wareQuantity+"x "+wareName+" (code: "+wareCode+")");
				ACLMessage reply = request.createReply();
				if(countFreeShelves()>=wareQuantity) {//controlla che ci siano abbastanza scaffali liberi
					ArrayList<ShelfCoord> reservedShelves = reserveShelves(wareQuantity);//prenota tutte le shelf necessarie
					//crea una nuova transazione
					Transaction newTransaction = new Transaction(transactionCount, TransactionType.DELIVER, request.getSender(), myAgent.getAID(), new Ware(wareCode, wareName, request.getSender()), wareQuantity, new Date(), reservedShelves);
					//aggiunge la transazione alla lista
					transactionList.put(transactionCount, newTransaction);
					reply.setPerformative(ACLMessage.AGREE);
					//creare la nuova transazione da inserire nella lista delle transazioni attive e generare un codice identificativo
					//prenotare le shelves che dovranno ospitare le ware
					reply.setContent(String.valueOf(transactionCount));//passare il codice identificativo ed eventualmente assegnarlo come modalit� di risposta
					reply.setReplyWith(String.valueOf(transactionCount));
					transactionCount++;
					myAgent.send(reply);
					mtConfirmTransaction = MessageTemplate.and(MessageTemplate.MatchConversationId("store-request"),MessageTemplate.MatchInReplyTo(reply.getReplyWith()));
					System.out.println("Store request accepted");
					System.out.println("TRANSACTION: "+newTransaction);
					System.out.println("SHELF RESERVED: "+reservedShelves);
					requestReplied = true;
				}
				else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("Not enought free space");
					myAgent.send(reply);
					System.out.println("Store request rejected");
					requestRejected = true;
				}
			}
			else if(!requestConfirmed) {
				confirm = myAgent.receive(mtConfirmTransaction);
				if(confirm != null) {
					System.out.println("TRANSACTION CONFIRMED");
					requestConfirmed = true;
				}
			}
			block();
		}
		
		public boolean done() {
			return (requestReplied && requestConfirmed) || requestRejected;
		}
		
	}
	
	//esamina la richiesta di prelievo merci dall'interno del magazzino
	private class CollectRequest extends Behaviour {
		
		MessageTemplate mtConfirmCollect;
		ACLMessage request;
		ACLMessage confirm;
		boolean requestReplied = false;
		boolean requestConfirmed = false;
		boolean requestRejected = false;
		
		public CollectRequest(ACLMessage newRequest) {
			request = newRequest;
		}
		
		public void action() {
			if(!requestReplied) {
				String requestContent = request.getContent();
				String[] wareInfo = requestContent.split(":");
				String wareCode = wareInfo[0];
				String wareName = wareInfo[1];
				int wareQuantity = (int) Integer.parseInt(wareInfo[2]);
				System.out.println("CLIENT:"+request.getSender().getName()+" requested to collect "+wareQuantity+"x "+wareName+" (code: "+wareCode+")");
				ACLMessage reply = request.createReply();
				//FROM HERE
				if(countWares(wareCode, wareName, request.getSender())>=wareQuantity) {//controlla che ci siano abbastanza merci
					ArrayList<ShelfCoord> reservedShelves = reserveShelvesForCollect(wareCode, wareName, request.getSender(), wareQuantity);//prenota tutte le shelf necessarie
					//crea una nuova transazione
					Transaction newCollect = new Transaction(transactionCount, TransactionType.COLLECT, request.getSender(), myAgent.getAID(), new Ware(wareCode, wareName, request.getSender()), wareQuantity, new Date(), reservedShelves);
					//aggiunge la transazione alla lista
					transactionList.put(transactionCount, newCollect);
					reply.setPerformative(ACLMessage.AGREE);
					//creare la nuova transazione da inserire nella lista delle transazioni attive e generare un codice identificativo
					reply.setContent(String.valueOf(transactionCount));//passare il codice identificativo ed eventualmente assegnarlo come modalit� di risposta
					reply.setReplyWith(String.valueOf(transactionCount));
					transactionCount++;
					myAgent.send(reply);
					mtConfirmCollect = MessageTemplate.and(MessageTemplate.MatchConversationId("collect-request"),MessageTemplate.MatchInReplyTo(reply.getReplyWith()));
					System.out.println("Collect request accepted");
					System.out.println("COLLECT REQUEST: "+newCollect);
					System.out.println("SHELF RESERVED: "+reservedShelves);
					requestReplied = true;
				}
				else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("Not enought wares");
					myAgent.send(reply);
					System.out.println("Collect request rejected");
					requestRejected = true;
				}
			}
			else if(!requestConfirmed) {
				confirm = myAgent.receive(mtConfirmCollect);
				if(confirm != null) {
					System.out.println("COLLECT CONFIRMED");
					requestConfirmed = true;
				}
			}
			block();
		}
		
		public boolean done() {
			return (requestReplied && requestConfirmed) || requestRejected;
		}
		
	}
	
	//controlla la bont� della richiesta di ritiro o scarico merci 
	private class DeliveryCheck extends Behaviour{
		
		ACLMessage request;
		ACLMessage confirm;
		MessageTemplate mt;
		TransactionType type;
		boolean responseSent = false;
		boolean confirmReceived = false;
		boolean requestRejected = false;
		int code;
		
		public DeliveryCheck (ACLMessage newRequest) {
			request = newRequest;
			code = Integer.parseInt(request.getContent());
		}
		
		public void action() {
			//controlla se il responso non � gi� stato mandato
			if(!responseSent) {
				//mettere un controllo sul mittente per capire se � uno dei transactor del magazzino
				ACLMessage response = request.createReply();
				//controlla che tra le transazioni ci sia una corrspondente a quel codice
				if(transactionList.containsKey(code)) {
					//controlla che la transazione trovata sia aperta (e non in progress o closed)
					if(transactionList.get(code).getStatus() == TransactionStatus.OPEN){
						//setta le impostazioni del responso
						response.setPerformative(ACLMessage.CONFIRM);
						response.setReplyWith("cfp"+System.currentTimeMillis());
						//controlla se la transazione è DELIVER
						if(transactionList.get(code).getType() == TransactionType.DELIVER) {
							//il contenuto della risposta affermativa � la quantit� di merce che deve essere consegnata
							response.setContent("DELIVERY|"+String.valueOf(transactionList.get(code).getQuantity()));
						}
						else if(transactionList.get(code).getType() == TransactionType.COLLECT) {
							//il contenuto della risposta affermativa � la quantit� di merce che deve essere consegnata
							response.setContent("COLLECT|"+String.valueOf(transactionList.get(code).getQuantity()));
						}
						myAgent.send(response);
						//prepara il template per ricevere la risposta
						mt = MessageTemplate.and(MessageTemplate.MatchConversationId("check-delivery"), MessageTemplate.MatchInReplyTo(response.getReplyWith()));
						//cambia lo stato del transactor in "occupied"
						transactors.get(request.getSender()).setOccupied(code);
						//cambia lo stato della transaction in "in-progress"
						transactionList.get(code).setInProgress();
						//salva il tipo di transaction che sta eseguendo
						type = transactionList.get(code).getType();
						System.out.println("Transaction "+code+" is in progress");
						responseSent = true;						
					}
					else {
						response.setPerformative(ACLMessage.DISCONFIRM);
						myAgent.send(response);				
						System.out.println("Transaction "+code+" is already in progress or is already closed");
						requestRejected = true;
					}
				}
				
				else {
					response.setPerformative(ACLMessage.DISCONFIRM);
					myAgent.send(response);				
					System.out.println("Transaction "+code+" doesn't exist");
					requestRejected = true;
				}
			}
			//se ha ricevuto la risposta del transactor
			if(!confirmReceived) {
				confirm = myAgent.receive(mt);
				if(confirm != null) {
					if(type==TransactionType.DELIVER) {
						if(confirm.getPerformative() == ACLMessage.CONFIRM) {
							System.out.println("TRANSACTION DELIVERED");
							//mandare un messaggio al cliente che aveva richiesto la transazione in cui lo si informa dell'avvenuta consegna
							//cambia la quantit� di merce presente nel transactor
							transactors.get(confirm.getSender()).setWareQuantity(Integer.parseInt(confirm.getContent()));
							//cambia lo stato della transaction in "delivered"
							transactionList.get(code).setDelivered();
							confirmReceived = true;
							//TODO passa la transazione al behaviour che si occuper� di distribuire le merci relative
							addBehaviour(new StoreWare(confirm.getSender(),transactionList.get(code)));
						}
						else if(confirm.getPerformative() == ACLMessage.CANCEL) {
							System.out.println("TRANSACTION REJECTED: WRONG WARE QUANTITY DELIVERED");
							//mandare un messaggio al cliente che aveva richiesto la transazione in cui lo si informa dell'errore
							//imposta il transactor su free
							transactors.get(confirm.getSender()).setFree();
							//cambia lo stato della transaction in "open"
							transactionList.get(code).setOpen();
							confirmReceived = true;
						}
					}
					else if(type==TransactionType.COLLECT) {
						if(confirm.getPerformative() == ACLMessage.CONFIRM) {
							System.out.println("COLLECTION CONFIRMED");
							//mandare un messaggio al cliente che aveva richiesto la transazione in cui lo si informa dell'avvenuta consegna
							//cambia lo stato della transaction in "delivered"
							transactionList.get(code).setDelivered();
							confirmReceived = true;
							//passa la transazione al behaviour che si occuper� di distribuire le merci relative
							addBehaviour(new DeliverWare(confirm.getSender(),transactionList.get(code)));
						}
						else if(confirm.getPerformative() == ACLMessage.CANCEL) {
							System.out.println("COLLECTION REJECTED: WRONG WARE QUANTITY REQUESTED");
							//mandare un messaggio al cliente che aveva richiesto la transazione in cui lo si informa dell'errore
							//imposta il transactor su free
							transactors.get(confirm.getSender()).setFree();
							//cambia lo stato della transaction in "open"
							transactionList.get(code).setOpen();
							confirmReceived = true;
						}
					}
				}
			}
			block();
			
		}
		
		public boolean done() {
			return (responseSent && confirmReceived) || requestRejected;
		}
		
		
		
	}
	
	
	//behaviour che si occupa di prelevare merci dall'esterno e di sistemarle all'interno del magazzino
	private class DeliverWare extends OneShotBehaviour{
		
		AID transactorAID;
		Transaction transaction;
		
		/*MessageTemplate mtTransactorUpdate;
		MessageTemplate mtTransporterUpdate;
		MessageTemplate mtTransporterToTransactor;
		ACLMessage transactorUpdateMsg;
		ACLMessage transporterUpdateMsg;
		ACLMessage transporterToTransactorMsg;
		ACLMessage stopChargeMsg;*/
		
		public DeliverWare(AID newTransactorAID, Transaction newTransaction) {
			transactorAID = newTransactorAID;
			transaction = newTransaction;
			
			/*mtTransactorUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("transactor-update")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-shelves")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterToTransactor = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-transactor")), MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));*/
		
		}
		
		public void action() {
			String content = "RETRIEVE_SHELF&"+String.valueOf(transaction.getCode())+"&"; //aggiunge il tipo di comando e il codice dell'ordine
			System.out.println(transactors.get(transactorAID).getCoord()+"&"); //TESTING
			content += transactors.get(transactorAID).getCoord().getSector()+"&"; //aggiunge la coordinata del transactor 
			content += String.valueOf(transaction.getQuantity())+"&"; //aggiunge la quantità di merce nel transactor
			TreeMap<String, ArrayList<String>> tmp_shelves = new TreeMap<String, ArrayList<String>>(); //TODO
			
			//si crea una lista di tutte le shelf prenotate con i relativi livelli 
			for(ShelfCoord tmp_shelf : transaction.getShelvesReserved()) {
				if (tmp_shelves.containsKey(tmp_shelf.getCoord().getSector())){ //se la shelf a quella coordinata è già stata trovata			
				}
				else {
					tmp_shelves.put(tmp_shelf.getCoord().getSector(), new ArrayList<String>()); //se la si incontra quella shelf la prima volta la si aggiunge
				}
				tmp_shelves.get(tmp_shelf.getCoord().getSector()).add(String.valueOf(tmp_shelf.getLevel())); //aggiunge il livello a quella coordinata	
			}
			
			//aggiunge le shelves così trovate al content
			for (String key : tmp_shelves.keySet()) {
				content += key+"|"; //aggiunge la coordinata
				for (String tmp_level : tmp_shelves.get(key)) {
					content += tmp_level+"/";
				}
				content = content.substring(0, content.length() - 1); //rimuove l'ultimo "/" che è superfluo
				content += "$"; 
			}
			
			content = content.substring(0, content.length() - 1); //rimuove l'ultimo "$" che è superfluo
			
			try {
				String responseRequest = Request.sendPOST(TRANSPORTER_MANAGER_URL, content);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
	}
	
	/*private class StoreWare extends Behaviour{
		
		AID transactorAID;
		AID transporterAID;
		//l'AID del transporter attualmente impegnato nell'andare a ritirare le merci dal transactor
		AID transporterToTransactor;
		//l'AID del transporter che ha completato la sua mansione
		AID transporterComplete;
		//la lista dei transporter ancora impegnati nella transaction
		ArrayList<AID> activeTransporters;
		Transaction transaction;
		boolean transactionComplete;
		boolean transporterFound;
		ShortestPath pathFinder;
		LinkedList<Coord> path;
		MessageTemplate mtTransactorUpdate;
		MessageTemplate mtTransporterUpdate;
		MessageTemplate mtTransporterToTransactor;
		ACLMessage transactorUpdateMsg;
		ACLMessage transporterUpdateMsg;
		ACLMessage transporterToTransactorMsg;
		ACLMessage stopChargeMsg;
		
		public StoreWare(AID newTransactorAID, Transaction newTransaction) {
			transactorAID = newTransactorAID;
			transaction = newTransaction;
			activeTransporters = new ArrayList<AID>();
			transactionComplete = false;
			
			pathFinder = new ShortestPath(planimetry);
			mtTransactorUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("transactor-update")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-shelves")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterToTransactor = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-transactor")), MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
		}
		
		public void action() {
			
			//se la transazione non � ancora completa e non c'� nessun transporter attivo e nella zona di carico del transactor non � presente nessun transporter
			if(!transactionComplete && transporterToTransactor == null && !transactors.get(transactorAID).isFull()) {
				//cerca il primo transporter libero
				transporterAID = searchFreeTransporter();

				if(transporterAID != null) {
					//controlla se il transporter � in ricarica
					if(transporters.get(transporterAID).getStatus()==TransporterStatus.CHARGING) {
						//trova il charger che lo sta ricaricando
						AID chargerAID = findOccupiedCharger(transporterAID);
						//invia il messaggio di stop al charger
						stopChargeMsg = new ACLMessage(ACLMessage.CANCEL);
						stopChargeMsg.addReceiver(chargerAID);
						stopChargeMsg.setConversationId("stop-charge");
						myAgent.send(stopChargeMsg);
						//libera il charger
						chargers.get(chargerAID).setFree();
					}
					//imposta il transporter trovato come transporterToTransactor
					transporterToTransactor = transporterAID;
					//lo aggiunge alla lista dei transporter attivi
					activeTransporters.add(transporterAID);
					//lo imposta su moving
					transporters.get(transporterAID).setStatus(TransporterStatus.MOVING);
					//calcola il percorso dal transporter al transactor
					path = pathFinder.getShortestPath(transporters.get(transporterAID).getCoord(), transactors.get(transactorAID).getCoord());
					System.out.println(transporterToTransactor.getLocalName()+" - PATH: "+path);
					//comunica il percorso al transporter
					ACLMessage sendPathToTransactor = new ACLMessage(ACLMessage.CFP);
					sendPathToTransactor.addReceiver(transporterToTransactor);
					sendPathToTransactor.setContent(Decoders.removeBracket(path.toString()));
					sendPathToTransactor.setConversationId("travel-to-transactor");
					//il codice della risposta � il numero della transazione
					sendPathToTransactor.setReplyWith(String.valueOf(transaction.getCode()));
					myAgent.send(sendPathToTransactor);
				}
			}
			
			//cerca di riceve il messaggio che comunica che il transactor ha consegnato la merce al transporter ed eventualmente che la transazione � stata completata
			transactorUpdateMsg = myAgent.receive(mtTransactorUpdate);
			//riceve eventuali aggiornamenti dai transporter sull'andamento della loro mansione
			transporterUpdateMsg = myAgent.receive(mtTransporterUpdate);
			//riceve l'eventuale messaggio del transporter che arriva al transactor
			transporterToTransactorMsg = myAgent.receive(mtTransporterToTransactor);
			
			//messaggio che comunica che il transactor ha consegnato la merce al transporter ed eventualmente che la transazione � stata completata
			if(transactorUpdateMsg != null) {
				String decodedTransactorMsg[] = Decoders.decodeTransactorUpdate(transactorUpdateMsg.getContent());
				//imposta la nuova quantit� di merci presenti nel transactor
				transactors.get(transactorAID).setWareQuantity(Integer.parseInt(decodedTransactorMsg[2]));
				//se il contenuto del messaggio � "COMPLETE" allora vuol dire che sono state consegnate tutte le merci 
				if(decodedTransactorMsg[0].equals("COMPLETED")) {
					//imposta il transactor su libero
					transactors.get(transactorAID).setFree();
					//dichiare la transazione completata
					System.out.println("TRANSACTION COMPLETE");
					transactionComplete = true;
					transaction.setClosed();
					//sposta la transazione dall lista di quelle aperte alla lista di quelle chiuse
					deliveryClosed.put(transaction.getCode(), transaction);
					deliveryOpen.remove(transaction.getCode());
					//avvisa il cliente che � stata chiusa la transazione
				}
				//in ogni caso vuol dire che il transactor ha finito di caricare le merci sul transporter richiesto
				//prende le ShelfUnit in cui deve mettere la merce
				HashMap<Coord, ArrayList<Integer>> shelvesToLoad = getShelvesToLoad(transaction, Integer.parseInt(decodedTransactorMsg[1]));
				//trasforma tutto in string
				ArrayList<String> contentArray = new ArrayList<String>();
				Coord newStart = transactors.get(transactorAID).getCoord(); 
				for(Coord key : shelvesToLoad.keySet()) {
					//calcola l'accessPoint della shelvesToLoad
					Coord shelvesAccessPoint;
					if(shelves.get(key).getAccessPoint()==ShelfAccessPoint.NORTH) {
						shelvesAccessPoint = key.goNorth();
					}
					else if(shelves.get(key).getAccessPoint()==ShelfAccessPoint.SUD) {
						shelvesAccessPoint = key.goSouth();
					}
					else if(shelves.get(key).getAccessPoint()==ShelfAccessPoint.WEST) {
						shelvesAccessPoint = key.goWest();
					}
					else {
						shelvesAccessPoint = key.goEast();
					}
					//calcola per ogni coord delle shelvesToLoad il percorso da fare dal transactor all'access point di quella shelf					
					contentArray.add(key+"-"+Decoders.removeBracket(shelvesToLoad.get(key).toString())+"-"+Decoders.removeBracket(pathFinder.getShortestPath(newStart, shelvesAccessPoint).toString()));					
					//salva il nuovo punto di inizio
					newStart = shelvesAccessPoint;
				} 
				//crea il content del messaggio da inviare al transporter nel formato coord-livelli-path|coord-livelli-path|...
				String content = "";
				for(String str : contentArray) {
					content += str+"|";
				}
				content=content.substring(0,content.length()-1);
				//lo imposta su moving
				transporters.get(transporterAID).setStatus(TransporterStatus.MOVING);
				//comunica al transporter appena caricato che pu� iniziare a muoversi per andare a sistemare le merci nelle scaffalature
				ACLMessage sendPathToShelves = new ACLMessage(ACLMessage.CFP);
				sendPathToShelves.addReceiver(transporterToTransactor);
				sendPathToShelves.setContent(content);
				sendPathToShelves.setConversationId("travel-to-shelves");
				sendPathToShelves.setReplyWith(String.valueOf(transaction.getCode()));
				myAgent.send(sendPathToShelves);
				//libera l'activeTransporter
				transporterToTransactor = null;
				//imposta il transactor su EMPTY
				transactors.get(transactorAID).setEmpty();
				//svuota il contenitore del messaggio
				transactorUpdateMsg = null;
			}
			//riceve eventuali aggiornamenti dai transporter sull'andamento della loro mansione
			if(transporterUpdateMsg != null) {
				//decodifica il messaggio
				String[] decodedMsg = transporterUpdateMsg.getContent().split("\\|");
				//se tutte le consegne del transporter sono state completate, il warehouse lo imposta su libero e lo elimina dalla lista dei tansporter attivi
				if(decodedMsg[0].equals("COMPLETED")) {
					transporterComplete = transporterUpdateMsg.getSender();
					activeTransporters.remove(transporterComplete);
					//se il livello di carica � sotto il 20% manda il transporter a ricaricarsi
					if(transporters.get(transporterComplete).getCharge()<=20) {
						//lo imposta su MOVING
						transporters.get(transporterComplete).setStatus(TransporterStatus.MOVING);
						//trova la prima zona di ricarica libera
						AID chargeZone = searchEmptyChargeStation();
						//prenota il charger
						chargers.get(chargeZone).setReserved(transporterComplete);
						//calcola il percorso dal transporter alla zona di ricarica
						path = pathFinder.getShortestPath(transporters.get(transporterComplete).getCoord(), chargers.get(chargeZone).getCoord());
						//comunica il percorso al transporter
						ACLMessage sendPathToCharge = new ACLMessage(ACLMessage.CFP);
						sendPathToCharge.addReceiver(transporterComplete);
						sendPathToCharge.setContent(Decoders.removeBracket(path.toString()));
						sendPathToCharge.setConversationId("travel-to-charger");
						//il codice della risposta � unique value
						sendPathToCharge.setReplyWith("cfp"+System.currentTimeMillis());
						myAgent.send(sendPathToCharge);
					}
					//altrimenti lo manda nella sua zona di rest
					else {
						//lo imposta su BACKSTART
						transporters.get(transporterComplete).setStatus(TransporterStatus.BACKSTART);
						//calcola il percorso dal transporter al punto di rest
						path = pathFinder.getShortestPath(transporters.get(transporterComplete).getCoord(), transporters.get(transporterComplete).getRestCoord());
						//comunica il percorso al transporter
						ACLMessage sendPathToRest = new ACLMessage(ACLMessage.CFP);
						sendPathToRest.addReceiver(transporterComplete);
						sendPathToRest.setContent(Decoders.removeBracket(path.toString()));
						sendPathToRest.setConversationId("travel-to-rest");
						//il codice della risposta � unique value
						sendPathToRest.setReplyWith("cfp"+System.currentTimeMillis());
						myAgent.send(sendPathToRest);
					}
				}
				//aggiorna la ShelfUnit interessata
				//decodifica la coordinata della ShelfUnit
				Coord shelfUnitCoordDecoded = new Coord(Integer.parseInt(decodedMsg[1].split(":")[0]),Integer.parseInt(decodedMsg[1].split(":")[1]));
				//decodifica tutti i livelli della ShelfUnit interessata
				String[] levelsDecoded = decodedMsg[2].split(", ");
				//ad ogni livello della ShelfUnit aggiunge il Ware della transaction
				for(String level : levelsDecoded) {
					shelves.get(shelfUnitCoordDecoded).addWare(transaction.getWare(), Integer.parseInt(level));
				}
				//svuota il contenitore del messaggio
				transporterUpdateMsg = null;
			}
			//riceve l'eventuale messaggio del transporter che arriva al transactor
			if(transporterToTransactorMsg != null) {
				//imposta il transactor su FULL
				transactors.get(transactorAID).setFull();
				//imposta lo stato del transporter su working
				transporters.get(transporterToTransactorMsg.getSender()).setStatus(TransporterStatus.WORKING);
				//PROVA STAMPA
				for(int i=0; i<2; i++) {System.out.println("\n");}
			    printPlanimetry();
			    System.out.println(getTransportersList());
			    System.out.println(getTransactorsList());
			    System.out.println(getChargersList());
				//il warehouse comunica al transactor che pu� passare la merce al transporter
				ACLMessage loadTransactor = new ACLMessage(ACLMessage.CFP);
				loadTransactor.addReceiver(transactorAID);
				//passa al transactor la capacit� massima del transporter
				loadTransactor.setContent(String.valueOf(transporters.get(transporterToTransactor).getCapacity()));
				loadTransactor.setConversationId("transactor-update");
				loadTransactor.setReplyWith(String.valueOf(transaction.getCode()));
				myAgent.send(loadTransactor);
				//svuota il contenitore del messaggio
				transporterToTransactorMsg = null;
			}
		}
		//NON CI PU� ESSERE UN block() QUI PERCH� ANCHE SE NON ARRIVANO MESSAGGI IL BEHAVIOUR DEVE RIANDARE IN LOOP PER CERCARE UN NUOVO TRANSPORTER LIBERO
		
		public boolean done() {
			return transactionComplete && activeTransporters.isEmpty();
		}
		
	}*/
	
	//behaviour che si occupa di prelevare merci dall'esterno e di sistemarle all'interno del magazzino
	//TODO 28/05/2020 da fare
	private class StoreWare extends OneShotBehaviour{
		
		AID transactorAID;
		Transaction transaction;
		
		/*MessageTemplate mtTransactorUpdate;
		MessageTemplate mtTransporterUpdate;
		MessageTemplate mtTransporterToTransactor;
		ACLMessage transactorUpdateMsg;
		ACLMessage transporterUpdateMsg;
		ACLMessage transporterToTransactorMsg;
		ACLMessage stopChargeMsg;*/
		
		public StoreWare(AID newTransactorAID, Transaction newTransaction) {
			transactorAID = newTransactorAID;
			transaction = newTransaction;
			
			/*mtTransactorUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("transactor-update")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-shelves")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterToTransactor = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-transactor")), MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));*/
		
		}
		
		public void action() {
			String content = "DELIVER_SHELF&"+String.valueOf(transaction.getCode())+"&"; //aggiunge il tipo di comando e il codice dell'ordine
			System.out.println(transactors.get(transactorAID).getCoord()+"&"); //TESTING
			content += transactors.get(transactorAID).getCoord().getSector()+"&"; //aggiunge la coordinata del transactor 
			content += String.valueOf(transaction.getQuantity())+"&"; //aggiunge la quantità di merce nel transactor
			TreeMap<String, ArrayList<String>> tmp_shelves = new TreeMap<String, ArrayList<String>>(); //TODO
			
			//si crea una lista di tutte le shelf prenotate con i relativi livelli 
			for(ShelfCoord tmp_shelf : transaction.getShelvesReserved()) {
				if (tmp_shelves.containsKey(tmp_shelf.getCoord().getSector())){ //se la shelf a quella coordinata è già stata trovata			
				}
				else {
					tmp_shelves.put(tmp_shelf.getCoord().getSector(), new ArrayList<String>()); //se la si incontra quella shelf la prima volta la si aggiunge
				}
				tmp_shelves.get(tmp_shelf.getCoord().getSector()).add(String.valueOf(tmp_shelf.getLevel())); //aggiunge il livello a quella coordinata	
			}
			
			//aggiunge le shelves così trovate al content
			for (String key : tmp_shelves.keySet()) {
				content += key+"|"; //aggiunge la coordinata
				for (String tmp_level : tmp_shelves.get(key)) {
					content += tmp_level+"/";
				}
				content = content.substring(0, content.length() - 1); //rimuove l'ultimo "/" che è superfluo
				content += "$"; 
			}
			
			content = content.substring(0, content.length() - 1); //rimuove l'ultimo "$" che è superfluo
			
			try {
				String responseRequest = Request.sendPOST(TRANSPORTER_MANAGER_URL, content);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}	
		
	}
	
	/*private class DeliverWare extends Behaviour{
		
		AID transactorAID;
		ArrayList<AID> transporterFoundList;
		//l'AID del transporter attualmente impegnato nell'andare a ritirare le merci dal transactor
		AID transporterToTransactor;
		//l'AID del transporter che ha completato la sua mansione
		AID transporterComplete;
		//la lista dei transporter ancora impegnati nella transaction
		ArrayList<AID> activeTransporters;
		Transaction transaction;
		int quantityRemaning;
		boolean transactionComplete;
		boolean transporterFound;
		ShortestPath pathFinder;
		LinkedList<Coord> path;
		MessageTemplate mtTransactorUpdate;
		MessageTemplate mtTransporterUpdate;
		MessageTemplate mtTransporterToTransactor;
		ACLMessage transactorUpdateMsg;
		ACLMessage transporterUpdateMsg;
		ACLMessage transporterToTransactorMsg;
		ACLMessage stopChargeMsg;
		
		public DeliverWare(AID newTransactorAID, Transaction newTransaction) {
			transactorAID = newTransactorAID;
			transaction = newTransaction;
			quantityRemaning = transaction.getQuantity();
			transporterFoundList = new ArrayList<AID>();
			activeTransporters = new ArrayList<AID>();
			transactionComplete = false;
			
			pathFinder = new ShortestPath(planimetry);
			mtTransactorUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("transactor-collect")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterUpdate = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-shelves-collect")),MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
			mtTransporterToTransactor = MessageTemplate.and(MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchConversationId("travel-to-transactor-deliver")), MessageTemplate.MatchInReplyTo(String.valueOf(transaction.getCode())));
		}
		
		public void action() {			
			//cerca i transporter liberi
			transporterFoundList = searchAllFreeTransporter(quantityRemaning);			
			//se sono stati trovati
			if(transporterFoundList != null) {
				//per ogni transporter trovato
				for(int index = 0; index<transporterFoundList.size(); index++) {
					//salva in una variabile temporanea l'AID del transporter che si sta usando
					AID tmpTransporterAid = transporterFoundList.get(index);
					//controlla se il transporter � in ricarica
					if(transporters.get(tmpTransporterAid).getStatus()==TransporterStatus.CHARGING) {
						//trova il charger che lo sta ricaricando
						AID chargerAID = findOccupiedCharger(tmpTransporterAid);
						//invia il messaggio di stop al charger
						stopChargeMsg = new ACLMessage(ACLMessage.CANCEL);
						stopChargeMsg.addReceiver(chargerAID);
						stopChargeMsg.setConversationId("stop-charge");
						myAgent.send(stopChargeMsg);
						//libera il charger
						chargers.get(chargerAID).setFree();
					}
					//prende le ShelfUnit da cui deve prendere la merce
					HashMap<Coord, ArrayList<Integer>> shelvesToCollect = getShelvesToCollect(transaction, transporters.get(tmpTransporterAid).getCapacity());
					//trasforma tutto in string
					ArrayList<String> contentArray = new ArrayList<String>();
					//salva come inizio del percorso la posizione attuale del transporter
					Coord newStart = transporters.get(tmpTransporterAid).getCoord(); 
					for(Coord key : shelvesToCollect.keySet()) {
						//calcola l'accessPoint della shelvesToLoad
						Coord shelvesAccessPoint;
						if(shelves.get(key).getAccessPoint()==ShelfAccessPoint.NORTH) {
							shelvesAccessPoint = key.goNorth();
						}
						else if(shelves.get(key).getAccessPoint()==ShelfAccessPoint.SUD) {
							shelvesAccessPoint = key.goSouth();
						}
						else if(shelves.get(key).getAccessPoint()==ShelfAccessPoint.WEST) {
							shelvesAccessPoint = key.goWest();
						}
						else {
							shelvesAccessPoint = key.goEast();
						}
						//calcola per ogni coord delle shelvesToLoad il percorso da fare dal transactor all'access point di quella shelf					
						contentArray.add(key+"-"+Decoders.removeBracket(shelvesToCollect.get(key).toString())+"-"+Decoders.removeBracket(pathFinder.getShortestPath(newStart, shelvesAccessPoint).toString()));					
						//salva il nuovo punto di inizio
						newStart = shelvesAccessPoint;
					}
					//crea il content del messaggio da inviare al transporter nel formato coord-livelli-path|coord-livelli-path|...
					String content = "";
					for(String str : contentArray) {
						content += str+"|";
					}
					content=content.substring(0,content.length()-1);
					//lo imposta su moving
					transporters.get(tmpTransporterAid).setStatus(TransporterStatus.MOVING);
					//comunica al transporter appena caricato che pu� iniziare a muoversi per andare a sistemare le merci nelle scaffalature
					ACLMessage sendPathToShelves = new ACLMessage(ACLMessage.CFP);
					sendPathToShelves.addReceiver(tmpTransporterAid);
					sendPathToShelves.setContent(content);
					sendPathToShelves.setConversationId("travel-to-shelves-collect");
					sendPathToShelves.setReplyWith(String.valueOf(transaction.getCode()));
					myAgent.send(sendPathToShelves);
					//rimuove dalla quantit� di merce ancora da distribuire la quantit� che � stata assegnata al transporter
					for(Coord keyCoord : shelvesToCollect.keySet()) {
						for(Integer shelfLevel : shelvesToCollect.get(keyCoord)) {
							quantityRemaning--;
						}
					}
				}
				//svuota la lista dei transporter trovati
				transporterFoundList = null;
			}
			
			//FROM HERE
			//cerca di riceve il messaggio che comunica che il transactor ha consegnato la merce al transporter ed eventualmente che la transazione � stata completata
			transactorUpdateMsg = myAgent.receive(mtTransactorUpdate);
			//riceve eventuali aggiornamenti dai transporter sull'andamento della loro mansione
			transporterUpdateMsg = myAgent.receive(mtTransporterUpdate);
			//riceve l'eventuale messaggio del transporter che arriva al transactor
			transporterToTransactorMsg = myAgent.receive(mtTransporterToTransactor);
			
			//messaggio che comunica che il transactor ha preso la merce dal transporter ed eventualmente che la transazione � stata completata
			if(transactorUpdateMsg != null) {
				String decodedTransactorMsg[] = Decoders.decodeTransactorUpdate(transactorUpdateMsg.getContent());
				//se il contenuto del messaggio � "COMPLETE" allora vuol dire che sono state consegnate tutte le merci 
				if(decodedTransactorMsg[0].equals("COMPLETED")) {
					//imposta il transactor su libero
					transactors.get(transactorAID).setFree();
					//dichiare la transazione completata
					System.out.println("TRANSACTION COMPLETE");
					transactionComplete = true;
					transaction.setClosed();
					//sposta la transazione dall lista di quelle aperte alla lista di quelle chiuse
					collectClosed.put(transaction.getCode(), transaction);
					collectOpen.remove(transaction.getCode());
					//avvisa il cliente che � stata chiusa la transazione
				}
				else {
					//cambia la quantit� di merce presente nel transactor
					transactors.get(transactorAID).setWareQuantity(Integer.parseInt(decodedTransactorMsg[1]));
				}
				//svuota il contenitore del messaggio
				transactorUpdateMsg = null;
			}
			//riceve eventuali aggiornamenti dai transporter sull'andamento della loro mansione
			if(transporterUpdateMsg != null) {
				//salva temporaneamente l?AID del transporter con cui sta parlando
				transporterComplete = transporterUpdateMsg.getSender();
				//decodifica il messaggio
				String[] decodedMsg = transporterUpdateMsg.getContent().split("\\|");
				//aggiorna la ShelfUnit interessata
				//decodifica la coordinata della ShelfUnit
				Coord shelfUnitCoordDecoded = new Coord(Integer.parseInt(decodedMsg[1].split(":")[0]),Integer.parseInt(decodedMsg[1].split(":")[1]));
				//decodifica tutti i livelli della ShelfUnit interessata
				String[] levelsDecoded = decodedMsg[2].split(", ");
				//svuota ogni livello della ShelfUnit e aggiunge 1 alla quantit� di merce trasportata dal transporter
				for(String level : levelsDecoded) {
					shelves.get(shelfUnitCoordDecoded).clearShelf(Integer.parseInt(level));
					transporters.get(transporterComplete).setQuantity(transporters.get(transporterComplete).getQuantity()+1);
				}
				//se tutte i ritiri del transporter sono stati completati, il warehouse lo manda dal transactor
				if(decodedMsg[0].equals("COMPLETED")) {
					//lo imposta su moving
					transporters.get(transporterComplete).setStatus(TransporterStatus.MOVING);
					//calcola il percorso dal transporter al transactor
					path = pathFinder.getShortestPath(transporters.get(transporterComplete).getCoord(), transactors.get(transactorAID).getCoord());
					System.out.println(transporterComplete.getLocalName()+" - PATH: "+path);
					//comunica il percorso al transporter
					ACLMessage sendPathToTransactor = new ACLMessage(ACLMessage.CFP);
					sendPathToTransactor.addReceiver(transporterComplete);
					sendPathToTransactor.setContent(Decoders.removeBracket(path.toString()));
					sendPathToTransactor.setConversationId("travel-to-transactor-deliver");
					//il codice della risposta � il numero della transazione
					sendPathToTransactor.setReplyWith(String.valueOf(transaction.getCode()));
					myAgent.send(sendPathToTransactor);
				}
				//svuota il contenitore del messaggio
				transporterUpdateMsg = null;
			}
			//riceve l'eventuale messaggio del transporter che arriva al transactor e che ha scaricato la merce
			if(transporterToTransactorMsg != null) {
				//salva temporaneamente l'AID del transporter con cui sta parlando
				transporterToTransactor = transporterToTransactorMsg.getSender();
				//imposta il transactor su FULL
				transactors.get(transactorAID).setFull();
				//imposta lo stato del transporter su working
				transporters.get(transporterToTransactor).setStatus(TransporterStatus.WORKING);
				//PROVA STAMPA
				for(int i=0; i<2; i++) {System.out.println("\n");}
			    printPlanimetry();
			    System.out.println(getTransportersList());
			    System.out.println(getTransactorsList());
			    System.out.println(getChargersList());
				//il warehouse comunica al transactor che ha ricevuto la merce dal transporter
				ACLMessage loadTransactor = new ACLMessage(ACLMessage.INFORM);
				loadTransactor.addReceiver(transactorAID);
				//passa al transactor la capacit� massima del transporter
				loadTransactor.setContent(String.valueOf(transporters.get(transporterToTransactor).getQuantity()));
				loadTransactor.setConversationId("transactor-collect");
				loadTransactor.setReplyWith(String.valueOf(transaction.getCode()));
				myAgent.send(loadTransactor);
				//segna che il transporter si � svuotato
				transporters.get(transporterToTransactor).setQuantity(0);
				//se il livello di carica � sotto il 20% manda il transporter a ricaricarsi
				if(transporters.get(transporterToTransactor).getCharge()<=20) {
					//lo imposta su MOVING
					transporters.get(transporterToTransactor).setStatus(TransporterStatus.MOVING);
					//trova la prima zona di ricarica libera
					AID chargeZone = searchEmptyChargeStation();
					//prenota il charger
					chargers.get(chargeZone).setReserved(transporterToTransactor);
					//calcola il percorso dal transporter alla zona di ricarica
					path = pathFinder.getShortestPath(transporters.get(transporterToTransactor).getCoord(), chargers.get(chargeZone).getCoord());
					//comunica il percorso al transporter
					ACLMessage sendPathToCharge = new ACLMessage(ACLMessage.CFP);
					sendPathToCharge.addReceiver(transporterToTransactor);
					sendPathToCharge.setContent(Decoders.removeBracket(path.toString()));
					sendPathToCharge.setConversationId("travel-to-charger");
					//il codice della risposta � unique value
					sendPathToCharge.setReplyWith("cfp"+System.currentTimeMillis());
					myAgent.send(sendPathToCharge);
				}
				//altrimenti lo manda nella sua zona di rest
				else {
					//lo imposta su BACKSTART
					transporters.get(transporterToTransactor).setStatus(TransporterStatus.BACKSTART);
					//calcola il percorso dal transporter al punto di rest
					path = pathFinder.getShortestPath(transporters.get(transporterToTransactor).getCoord(), transporters.get(transporterToTransactor).getRestCoord());
					//comunica il percorso al transporter
					ACLMessage sendPathToRest = new ACLMessage(ACLMessage.CFP);
					sendPathToRest.addReceiver(transporterToTransactor);
					sendPathToRest.setContent(Decoders.removeBracket(path.toString()));
					sendPathToRest.setConversationId("travel-to-rest");
					//il codice della risposta � unique value
					sendPathToRest.setReplyWith("cfp"+System.currentTimeMillis());
					myAgent.send(sendPathToRest);
				}
				//imposta il transactor su EMPTY
				transactors.get(transactorAID).setEmpty();
				//svuota il contenitore del messaggio
				transporterToTransactorMsg = null;
			}
		}
		//NON CI PU� ESSERE UN block() QUI PERCH� ANCHE SE NON ARRIVANO MESSAGGI IL BEHAVIOUR DEVE RIANDARE IN LOOP PER CERCARE UN NUOVO TRANSPORTER LIBERO
		
		public boolean done() {
			return transactionComplete && activeTransporters.isEmpty();
		}
		
	}*/
	
}


