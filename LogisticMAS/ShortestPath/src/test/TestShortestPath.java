package test;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import pathFinder.path.ShortestPath;
import pathFinder.squareSpace.Coord;
import pathFinder.squareSpace.Space;

public class TestShortestPath {
	
	public static void main(String[] arg) {
		
		try{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream("..\\AutoMag\\space.dat"));
			Space spcIn = (Space) in.readObject();
			spcIn.mapSquares();
			System.out.println(spcIn.toVisual());
			in.close();
			
			ShortestPath path = new ShortestPath(spcIn);
			
			System.out.println("path:" + path.getShortestPath(new Coord(1,0), new Coord(1,2)).toString());			
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
			
	}

}
