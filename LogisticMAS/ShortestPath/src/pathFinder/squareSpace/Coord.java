/*
 * classe che descrive delle coordinate
 * */


package pathFinder.squareSpace;

import java.io.Serializable;

public class Coord implements Serializable {
	
	private int hor;
	private int ver;
	
	public Coord(int newVer, int newHor) {
		hor = newHor;
		ver = newVer;
	}
	
	public int getHor() {
		return hor;
	}
	
	public int getVer() {
		return ver;
	}
	
	public String toString() {
		return ver+":"+hor;
	}
	
	public Coord goNorth() {
		return new Coord(ver-1,hor);
	}
	
	public Coord goSouth() {
		return new Coord(ver+1,hor);
	}
	
	public Coord goWest() {
		return new Coord(ver,hor-1);
	}
	
	public Coord goEast() {
		return new Coord(ver,hor+1);
	}

	public boolean isEquals(Coord other) {
		if(ver == other.getVer() && hor == other.getHor()) {
			return true;
		}
		else {
			return false;
		}
	}

}
