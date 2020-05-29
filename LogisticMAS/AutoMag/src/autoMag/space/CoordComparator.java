package autoMag.space;

import java.io.Serializable;
import java.util.Comparator;

public class CoordComparator implements Comparator<Coord>, Serializable{
	
	public int compare(Coord a, Coord b) {

		return a.getSector().compareTo(b.getSector());
		
	}
	

}