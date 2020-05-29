/*
 * CLASSE CREATA PER CONTENERE TUTTE LE INFORMAZIONI RELATIVE AD UN TRANSPORTER IN UN UNICO OGGETTO. 
 * UTILIZZATA DALL'AGENTE Warehouse PER TENERE TRACCIA DI TUTTI GLI AGENTI Transporter AD ESSO COLLEGATI.
 * */

package autoMag.utils;

import autoMag.space.Coord;
import jade.core.AID;

public class ChargerInfo {
	
	//id identificativo del transporter
	private String id;
	//le coordinate in cui si trova la charge station
	private Coord coord;
	//AID del transporter che sta occupando il charger
	private AID transporter;
	//flag che indica se la charge station è al momento utilizzato
	private ChargerStatus status;
	
	public ChargerInfo(String newID, Coord newCoord) {
		id = newID;
		coord = newCoord;
		transporter = null;
		status = ChargerStatus.FREE;
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public Coord getCoord() {
		return coord;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	public ChargerStatus getStatus() {
		return status;
	}

	public void setFree() {
		status = ChargerStatus.FREE;
		transporter = null;
	}
	
	public void setReserved(AID transp) {
		status = ChargerStatus.RESERVED;
		transporter = transp;
	}
	
	public void setOccupied() {
		status = ChargerStatus.OCCUPIED;
	}

	public AID getTransporter() {
		return transporter;
	}

	@Override
	public String toString() {
		String returnStr;
		returnStr = id + " | " + coord + " | " + status;
		if(transporter != null) {
			returnStr += " | " + transporter.getLocalName();
		}
		return returnStr;
	}




}

