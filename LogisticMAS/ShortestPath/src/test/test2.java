package test;

import java.util.Arrays;

import pathFinder.path.ShortestPath;
import pathFinder.squareSpace.Coord;
import pathFinder.squareSpace.Space;
import pathFinder.squareSpace.SqrType;

public class test2 {
	
	public static void main(String[] arg) {
		
		SqrType[][] spc = new SqrType[10][8];
		for(SqrType[] key : spc) {
			Arrays.fill(key, SqrType.EMPTY);
		}
		Space space = new Space(spc);
		System.out.println(space.toVisual());
		
		ShortestPath path = new ShortestPath(space);

		System.out.println(path.getShortestPath(new Coord(0,1), new Coord(5,6)));
		
	}

}
