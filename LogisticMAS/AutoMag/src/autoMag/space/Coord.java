/*
 * classe che descrive delle coordinate
 * */


package autoMag.space;

import java.io.Serializable;

public class Coord implements Serializable {
	
	private String sector;
	
	public Coord(String newSector) {
		sector = newSector;
	}
	
	public String getSector() {
		return sector;
	}

	
	public String toString() {
		return "Sector: "+sector;
	}


}
