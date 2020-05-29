package test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import pathFinder.path.ShortestPath;
import pathFinder.squareSpace.Coord;
import pathFinder.squareSpace.Space;
import pathFinder.squareSpace.SqrType;

public class CreateWarehouseSmall {
	
	public static void main(String[] arg) {
		
		SqrType[][] arr = new SqrType[8][8];
		Space spc;
		
		for(int i=0;i<arr.length;i++) {
			Arrays.fill(arr[i], SqrType.EMPTY);
		}
		arr[6][0] = SqrType.CHARGE;
		spc = new Space(arr);
		
		System.out.println(spc.toVisual());
		
		ShortestPath path = new ShortestPath(spc);
		
		System.out.println("path:" + path.getShortestPath(new Coord(1,1), new Coord(2,2)).toString());
		try{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("..\\AutoMag\\space.dat"));
			out.writeObject(spc);
			out.close();
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("..\\AutoMag\\space.dat"));
			Space spcIn = (Space) in.readObject();
			spcIn.mapSquares();
			System.out.println(spcIn.toVisual());
			in.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

}

