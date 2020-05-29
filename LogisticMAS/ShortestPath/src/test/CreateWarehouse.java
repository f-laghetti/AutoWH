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

public class CreateWarehouse {
	
	public static void main(String[] arg) {
		
		SqrType[][] arr = new SqrType[16][19];
		Space spc;
		
		//creazione di uno space di un magazzino
		Arrays.fill(arr[0], SqrType.OBSTACLE);
		Arrays.fill(arr[arr.length-1], SqrType.OBSTACLE);
		for(int i=1; i<arr.length-1; i++) {
			Arrays.fill(arr[i], SqrType.EMPTY);
			arr[i][arr[i].length-1] = SqrType.OBSTACLE;
		}
		for(int i=3; i<7; i++) {
			arr[i][2] = SqrType.OBSTACLE;
			arr[i][3] = SqrType.OBSTACLE;
			arr[i][6] = SqrType.OBSTACLE;
			arr[i][7] = SqrType.OBSTACLE;
			arr[i][10] = SqrType.OBSTACLE;
			arr[i][11] = SqrType.OBSTACLE;
			arr[i][14] = SqrType.OBSTACLE;
			arr[i][15] = SqrType.OBSTACLE;
		}
		for(int i=9; i<13; i++) {
			arr[i][2] = SqrType.OBSTACLE;
			arr[i][3] = SqrType.OBSTACLE;
			arr[i][6] = SqrType.OBSTACLE;
			arr[i][7] = SqrType.OBSTACLE;
			arr[i][10] = SqrType.OBSTACLE;
			arr[i][11] = SqrType.OBSTACLE;
			arr[i][14] = SqrType.OBSTACLE;
			arr[i][15] = SqrType.OBSTACLE;
		}
		
		spc = new Space(arr);
		spc.mapSquares();
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
