/*
 * Classe utilizzata per indicare la posizione di una Shelf, creando una congiunzione tra la posizione della ShelfUnit a cui
 * appartiene e il livello a cui si trova
 */

package autoMag.shelves;

import autoMag.space.Coord;

public class ShelfCoord {
	
	private Coord coord; //indica la posizione della ShelfUnit
	private int level; //indica a quale livello si trova la Shelf
	
	public ShelfCoord(Coord newCoord, int newLevel) {
		coord = newCoord;
		level = newLevel;
	}
	
	public Coord getCoord() {
		return coord; //privacy leak
	}
	
	public int getLevel() {
		return level;
	}
	
	public String toString() {
		return coord.getSector()+" - "+level;
	}

}
