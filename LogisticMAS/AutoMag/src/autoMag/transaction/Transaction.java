package autoMag.transaction;

import java.util.ArrayList;
import java.util.Date;

import autoMag.shelves.ShelfCoord;
import autoMag.shelves.Ware;
import jade.core.AID;

public class Transaction {
	
	private int code;
	private TransactionType type;
	private AID client;
	private AID warehouse;
	private Ware ware;
	private int quantity;
	private TransactionStatus status;
	private Date opened;
	private Date received;
	private Date closed;
	private ArrayList<ShelfCoord> shelvesReserved;
	
	public Transaction(int newCode, TransactionType newType, AID newClient, AID newWarehouse, Ware newWare, int newQuantity, Date newDate, ArrayList<ShelfCoord> newShelvesReserved) {
		code = newCode;
		type = newType;
		client = newClient;
		warehouse = newWarehouse;
		ware = newWare;
		quantity = newQuantity;
		opened = newDate;
		shelvesReserved = newShelvesReserved;
		status = TransactionStatus.OPEN;
	}
	
	public void setOpen() {
		status = TransactionStatus.OPEN;
	}
	
	public void setInProgress() {
		status = TransactionStatus.INPROGRESS;
	}
	
	public void setDelivered() {
		status = TransactionStatus.DELIVERED;
		received = new Date();
	}
	
	public void setClosed() {
		status = TransactionStatus.CLOSED;
		closed = new Date();
	}
	
	public TransactionType getType() {
		return type;
	}
	
	public int getCode() {
		return code;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public TransactionStatus getStatus() {
		return status;
	}
	
	public Ware getWare() {
		return ware;
	}
	
	public ArrayList<ShelfCoord> getShelvesReserved(){
		return shelvesReserved;//passo la vera lista e non una copia perchè mi serve modificarla dall'esterno
	}
	
	public String toString() {
		String str = code+"\t| "+type+"\t| "+opened.toString()+"\t| "+client.getName()+"\t| "+warehouse.getName()+"\t| "+quantity+"x "+ware.toString();
		if (status == TransactionStatus.OPEN) {
			str += " OPEN \t| ";
		}
		else if (status == TransactionStatus.INPROGRESS) {
			str += " IN PROGRESS \t| ";
		}
		else if (status == TransactionStatus.DELIVERED) {
			str += " DELIVERED \t| ";
		}
		else if (status == TransactionStatus.CLOSED) {
			str += " CLOSED \t| ";
		}
		if(received == null) {
			str += "not received yet\t| ";
		}
		else {
			str += received.toString()+"\t| ";
		}		
		if(closed == null) {
			str += "not closed yet\t| ";
		}
		else {
			str += closed.toString()+"\t| ";
		}
		return str;
	}
	

}
