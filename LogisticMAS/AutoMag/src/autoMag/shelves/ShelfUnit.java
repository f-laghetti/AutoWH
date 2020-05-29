/*
 * classe che rappresenta una scaffalatura
 * */

package autoMag.shelves;

import java.io.Serializable;

import autoMag.space.Coord;

public class ShelfUnit implements Serializable {

	private Coord coord; //posizione
	private Shelf[] shelves; /*insieme dei livelli della scaffalatura; 
	la grandezza dell'array rappresenta il numero di livelli che ha la scaffalatura;
	*/
	
	public ShelfUnit(Coord newCoord, int shelvesLevels) {
		coord = newCoord;
		shelves = new Shelf[shelvesLevels];
		for (int i=0; i<shelvesLevels; i++) {
			shelves[i]=new Shelf();
		}
	}
	
	public int getLevelNumber() {
		return shelves.length;
	}
	
	public Coord getCoord() {
		return coord;
	}
	
	public Shelf[] getShelves()	{
		return shelves.clone();
	}
	
	public Ware getWareOnShelf(int level) {//return una copia e mettere un controllo se si cerca un livello non presente
		return shelves[level].getWare();
	}
	
	public boolean addWare(Ware newWare, int level) {//aggiunge una merce ad un livello della scaffalatura
		System.out.println("LIVELLO PASSATO A addWare = "+level+" e shelves.length = "+shelves.length+" = "+(level>=0 && level<shelves.length));
		if(level>=0 && level<shelves.length) {
			return shelves[level].storeWare(newWare);
		}
		else {
			return false;
		}
	}
	
	public boolean clearShelf(int level) {//svuota un livello della scaffaltura
		System.out.println("LIVELLO PASSATO A clearShelf = "+level+" e shelves.length = "+shelves.length+" = "+(level>=0 && level<shelves.length));
		if(level>=0 && level<shelves.length) {
			return shelves[level].removeWare();
		}
		else {
			return false;
		}
	}
	
	public boolean reserveShelf(int level) {//prenota un livello della scaffaltura
		if(level>=0 && level<shelves.length) {
			return shelves[level].reserve();
		}
		else {
			return false;
		}
	}
	
	public boolean inProgressShelf(int level) {//imposta su INPROGRESS un livello della scaffaltura
		if(level>=0 && level<shelves.length) {
			return shelves[level].isInProgress();
		}
		else {
			return false;
		}
	}
	
	public boolean isCollectingShelf(int level) {//imposta su INPROGRESS un livello della scaffaltura
		if(level>=0 && level<shelves.length) {
			return shelves[level].isCollecting();
		}
		else {
			return false;
		}
	}
	
	public String toString() {
		String str = "Shelf unit "+coord.getSector()+"\n";
		int level = 0;
		for(Shelf shelf : shelves) {
			if(shelf == null) {
				str += "Level "+level+": EMPTY\n";
			}
			else {
				str += "Level "+level+": "+shelf.toString()+"\n";
			}
			level++;
		}
		return str;		
	}
	
}
