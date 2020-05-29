/*
 * AGENTE IN FASE EMBRIONALE; PRATICAMENTE INUTILE AL MOMENTO E TOTALMENTE DA RIVEDERE
 * */


package autoMag.agents;

import autoMag.utils.TransporterStatus;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Client extends Agent {
	
	private ClientGui myGui;
	
	public void setup() {

		myGui = new ClientGui(this);
		myGui.showGui();
		
		System.out.println("\nNEW CLIENT CREATED\nClient name: "+getAID().getName());

		
	}
	
	public void requestTransaction(String warehouseName, String wareCode, String wareName, String wareQuantity) {
		addBehaviour(new StoreToWarehouse(warehouseName, wareCode, wareName, wareQuantity));
	}
	
	public void requestCollect(String warehouseName, String wareCode, String wareName, String wareQuantity) {
		addBehaviour(new CollectFromWarehouse(warehouseName, wareCode, wareName, wareQuantity));
	}
	
	private class StoreToWarehouse extends Behaviour{
		
		String warehouseName, wareCode, wareName, wareQuantity;
		AID warehouseAID;
		MessageTemplate mt;
		boolean requestSent = false;
		boolean replyReceived = false;
		boolean requestRejected = false;
		boolean warehouseNotFound = false;
		
		public StoreToWarehouse(String newWarehouseName, String newWareCode, String newWareName, String newWareQuantity) {
			warehouseName = newWarehouseName;
			wareCode = newWareCode;
			wareName = newWareName;
			wareQuantity = newWareQuantity;
		}
		
		public void action() {
			//la richiesta non è stata ancora mandata
			if(requestSent == false) {
				//cerca un warehouse nelle yellow pages corrispondente al warehouseName
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("warehousing");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					for (int i = 0; i < result.length; ++i) {
						if(result[i].getName().getName().equals(warehouseName)) {
							warehouseAID = result[i].getName();
						}
					}
					if(warehouseAID != null) {
						ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
						request.addReceiver(warehouseAID);
						request.setContent(wareCode+"|"+wareName+"|"+wareQuantity);
						request.setConversationId("store-request");
						request.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
						myAgent.send(request);
						//prepara il template per ricevere la risposta
						mt = MessageTemplate.and(MessageTemplate.MatchConversationId("store-request"), MessageTemplate.MatchInReplyTo(request.getReplyWith()));
						System.out.println(myAgent.getAID().getName()+": store request sent to warehouse: "+wareQuantity+"x "+wareName+" (code: "+wareCode+")");
						requestSent = true;
					}
					else {
						System.out.println("Warehouse not found.");
						warehouseNotFound = true;
					}
				}
				catch (FIPAException e) {
					e.printStackTrace();
				}
			}
			else if(replyReceived == false) {//la risposta non è ancora stata ricevuta
				ACLMessage reply = myAgent.receive(mt);				
				if (reply != null) {//risposta ricevuta
					replyReceived = true;
					if (reply.getPerformative() == ACLMessage.AGREE) {//la richiesta è stata accetta
						System.out.println("Store request accepted - Transaction code: "+reply.getContent());
						//aggiornare la lista delle transazioni attive
						//inserire risposta
						ACLMessage confirm = reply.createReply();
						confirm.setPerformative(ACLMessage.CONFIRM);
						myAgent.send(confirm);
					}
					else if(reply.getPerformative() == ACLMessage.REFUSE) {//la richiesta è stata respinta 
						System.out.println(reply.getContent()+": Store request rejected");
						requestRejected = true;
					}
				}
			}
			block();
		}
		
		public boolean done() {
			return (requestSent && replyReceived) || requestRejected || warehouseNotFound;
		}
		
		
	}
	
	private class CollectFromWarehouse extends Behaviour{
		
		String warehouseName, wareCode, wareName, wareQuantity;
		AID warehouseAID;
		MessageTemplate mt;
		boolean requestSent = false;
		boolean replyReceived = false;
		boolean requestRejected = false;
		boolean warehouseNotFound = false;
		
		public CollectFromWarehouse(String newWarehouseName, String newWareCode, String newWareName, String newWareQuantity) {
			warehouseName = newWarehouseName;
			wareCode = newWareCode;
			wareName = newWareName;
			wareQuantity = newWareQuantity;
		}
		
		public void action() {
			if(requestSent == false) {//la richiesta non è stata ancora mandata
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("warehousing");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template);
					for (int i = 0; i < result.length; ++i) {
						if(result[i].getName().getName().equals(warehouseName)) {
							warehouseAID = result[i].getName();
						}
					}
					if(warehouseAID != null) {
						ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
						request.addReceiver(warehouseAID);
						request.setContent(wareCode+":"+wareName+":"+wareQuantity);
						request.setConversationId("collect-request");
						request.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
						myAgent.send(request);
						//prepara il template per ricevere la risposta
						mt = MessageTemplate.and(MessageTemplate.MatchConversationId("store-request"), MessageTemplate.MatchInReplyTo(request.getReplyWith()));
						System.out.println(myAgent.getAID().getName()+": store request sent to warehouse: "+wareQuantity+"x "+wareName+" (code: "+wareCode+")");
						requestSent = true;
						block();
					}
					else {
						System.out.println("Warehouse not found.");
						warehouseNotFound = true;
					}
				}
				catch (FIPAException e) {
					e.printStackTrace();
				}
			}
			else if(replyReceived == false) {//la risposta non è ancora stata ricevuta
				ACLMessage reply = myAgent.receive(mt);				
				if (reply != null) {//risposta ricevuta
					replyReceived = true;
					if (reply.getPerformative() == ACLMessage.AGREE) {//la richiesta è stata accetta
						System.out.println("Collect request accepted - Transaction code: "+reply.getContent());
						//aggiornare la lista delle transazioni attive
						//inserire risposta
						ACLMessage confirm = reply.createReply();
						confirm.setPerformative(ACLMessage.CONFIRM);
						myAgent.send(confirm);
					}
					else if(reply.getPerformative() == ACLMessage.REFUSE) {//la richiesta è stata respinta 
						System.out.println(reply.getContent()+": Collect request rejected");
						requestRejected = true;
					}
				}
				else { 
					block();
				}
			}
		}
		
		public boolean done() {
			return (requestSent && replyReceived) || requestRejected || warehouseNotFound;
		}
		
		
	}

}
