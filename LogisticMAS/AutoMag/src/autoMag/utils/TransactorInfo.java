/*
 * CLASSE CREATA PER CONTENERE TUTTE LE INFORMAZIONI RELATIVE AD UN TRANSACTOR IN UN UNICO OGGETTO. 
 * UTILIZZATA DALL'AGENTE Warehouse PER TENERE TRACCIA DI TUTTI GLI AGENTI Transporter AD ESSO COLLEGATI.
 * */

package autoMag.utils;

import autoMag.space.Coord;

public class TransactorInfo {
	
	//id identificativo del transactor
	private String id;
	//flag che indica se il transactor è al momento utilizzato
	private boolean occupied;
	//flag che indica se il transactor è al momento occupato da un transporter
	private boolean full;
	//le coordinate in cui si trova la zona di carico e scarico del transactor
	private Coord coord;
	//codice della transazione di cui si sta occupando
	private int transactionCode;//il valore -1 significa nessuna transazione
	//numero di merci che ha al proprio interno
	private int wareQuantity;
	
	//costruttore che imposta il transactor su libero
	public TransactorInfo(String newId, Coord newCoord) {
		id = newId;
		coord = newCoord;
		occupied = false;
		full = false;
		transactionCode = -1;
		wareQuantity = 0;
	}
	//costruttore che imposta il transactor su occupato
	public TransactorInfo(String newID, Coord newCoord, int newTransactionCode, int newWareQuantity) {
		id = newID;
		coord = newCoord;
		occupied = true;
		full = false;
		transactionCode = newTransactionCode;
		wareQuantity = newWareQuantity;
	}
	
	public String getID() {
		return id;
	}	
	
	public Coord getCoord() {
		return coord;
	}	

	public boolean isOccupied() {
		return occupied;
	}
	
	public boolean isFull() {
		return full;
	}
	
	public int getTransactionCode() {
		return transactionCode;
	}	
	
	public int getWareQuantity() {
		return wareQuantity;
	}
	
	public void setID(String newID) {
		id = newID;
	}
	
	public void setCoord(Coord newCoord) {
		coord = newCoord;
	}	
	
	public void setOccupied(int newTransactionCode) {		
		occupied = true;
		transactionCode = newTransactionCode;
	}
	
	public void setFree() {		
		occupied = false;
		transactionCode = -1;
		wareQuantity = 0;
	}
	
	public void setFull() {		
		full = true;
	}
	
	public void setEmpty() {		
		full = false;
	}
	
	public void setTransaction(int code) {
		transactionCode = code;
	}
	
	public void setWareQuantity(int newQuantity) {
		wareQuantity = newQuantity;
	}
	
	public String toString() {
		String str = id+"  |  ";
		if(occupied) {
			str += "OCCUPIED  |  ";
		}
		else {
			str += "FREE | ";
		}		
		if(full) {
			str += "FULL  |  ";
		}
		else {
			str += "EMPTY | ";
		}
		if(transactionCode == -1) {
			str += "no active transaction | ";
		}
		else {
			str += transactionCode+" | ";
		}
		str += wareQuantity+" | "+coord.toString();
		return str;
	}

}