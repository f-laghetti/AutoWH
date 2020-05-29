/*
 * comparator pe oggetti Coord
 * */


package pathFinder.squareSpace;

import java.io.Serializable;
import java.util.Comparator;

public class CoordComparator implements Comparator<Coord>, Serializable{
	
	public int compare(Coord a, Coord b) {
		if(a.getHor()==b.getHor()) {
			if(a.getVer()==b.getVer()) {
				return 0;
			}
			else if(a.getVer()>b.getVer()){
				return 1;
			}
			else {
				return -1;
			}
		}
		else if(a.getHor()>b.getHor()) {
			return 1;
		}
		else {
			return -1;
		}
		
	}
	

}
