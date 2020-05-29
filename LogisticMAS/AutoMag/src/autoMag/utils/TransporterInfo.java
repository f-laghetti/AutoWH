/*
 * CLASSE CREATA PER CONTENERE TUTTE LE INFORMAZIONI RELATIVE AD UN TRANSPORTER IN UN UNICO OGGETTO. 
 * UTILIZZATA DALL'AGENTE Warehouse PER TENERE TRACCIA DI TUTTI GLI AGENTI Transporter AD ESSO COLLEGATI.
 * */

package autoMag.utils;

import autoMag.space.Coord;

public class TransporterInfo {
	
	//id identificativo del transporter
	String id;
	//lo stato in cui si trova
	TransporterStatus status;
	//le coordinate del punto di rest del transporter
	Coord rest;
	//le coordinate in cui si trova il transporter
	Coord coord;
	//numero massimo di merci che può portare
	int capacity;
	//numero di merci che sta trasportando
	int quantity;
	//il livello di carica calcolato in percentuale
	Float charge;
	
	public TransporterInfo(String newID, Coord newCoord, TransporterStatus newStatus, int newCapacity, Float newCharge) {
		id = newID;
		rest = newCoord;
		coord = newCoord;
		status = newStatus;
		capacity = newCapacity;
		quantity = 0;
		charge = newCharge;
	}

	
	public String getID() {
		return id;
	}	
	
	public Coord getRestCoord() {
		return rest;
	}	
	
	public Coord getCoord() {
		return coord;
	}	
	
	public TransporterStatus getStatus() {
		return status;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public Float getCharge() {
		return charge;
	}
	
	public void setID(String newID) {
		id = newID;
	}
	
	public void setStatus(TransporterStatus newStatus) {
		status = newStatus;
	}	
	
	public void setQuantity(int newQuantity) {
		quantity = newQuantity;
	}
	
	public void setCoord(Coord newCoord) {
		coord = newCoord;
	}
	
	public void setCharge(Float newCharge) {
		charge = newCharge;
	}
	
	public String toString() {
		return id+"  |  "+status+"  |  "+rest+"  |  "+coord+"  |  "+quantity+"  |  "+capacity+"  |  "+charge+"%";
	}

}
