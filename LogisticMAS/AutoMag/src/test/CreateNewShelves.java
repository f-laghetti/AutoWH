/*
 * CREA I FILE .dat CONTENENTI LA PLANIMETRIA DEL MAGAZZINO (space.dat) E LA POSIZIONE DELLE SCAFFALATURE (shelves.dat)
 * */

package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.TreeMap;

import autoMag.shelves.ShelfUnit;
import autoMag.space.Coord;
import autoMag.space.CoordComparator;

public class CreateNewShelves {
	
	public static void main(String[] arg) {

		
		//creazione delle ShelfUnits
		
		TreeMap<Coord, ShelfUnit> shelves = new TreeMap<Coord, ShelfUnit>(new CoordComparator());
		
		Coord firstShelfCoord = new Coord("17");
		//Coord secondShelfCoord = new Coord("19");
		
		shelves.put(firstShelfCoord, new ShelfUnit(firstShelfCoord, 30));
		//shelves.put(secondShelfCoord, new ShelfUnit(secondShelfCoord, 3));
		
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("shelves.dat"));
			out.writeObject(shelves);
			out.close();
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("shelves.dat"));
			TreeMap<Coord, ShelfUnit> shelvesIn = (TreeMap<Coord, ShelfUnit>) in.readObject();			
			for (Coord key : shelvesIn.keySet()) {
				System.out.println(shelvesIn.get(key));
			}
			in.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		

		
		
	}

}
