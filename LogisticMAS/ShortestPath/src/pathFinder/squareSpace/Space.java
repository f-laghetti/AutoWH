/*
 * classe utilizzata per definire uno spazio diviso in caselle 
 * */


package pathFinder.squareSpace;

import java.io.Serializable;
import java.util.HashMap;

public class Space implements Serializable {
	
	private SqrType[][] space; //lo spazio vero e proprio, ovvero un array bidimensionale di SqrType 

	public Space(SqrType[][] newSpace) {
		space = newSpace; 
	}
	
	public void mapSquares() {//trasforma tutte le caselle null in caselle OUT così da evitare NullPointerException
		for(int i=0; i<space.length; i++) {
			for(int j=0; j<space[i].length; j++) {
				if(space[i][j] == null) {
					space[i][j] = SqrType.OUT;
				}
			}
		}
	}
	
	public boolean squareExist(int y, int x) { //metodo che controlla se le coordinate passate corrispondono ad una casella esistente (se una casella è OUT esiste)
		try {
			SqrType result = space[y][x]; //forza una ArrayIndexOutOfBoundsException se non esiste una casella a quelle coordinate
			return true;
		}
		catch(ArrayIndexOutOfBoundsException e) { //se si verifica l'eccezione vuol dire che a quelle coordinate non esiste una casella 
			return false;
		}
		
	}
	
	public boolean squareExist(Coord coord) { //come sopra ma l'argomento è un oggetto Coord invece che 2 int		
		int y = coord.getVer();
		int x = coord.getHor();
		try {
			SqrType result = space[y][x];
			return true;
		}
		catch(ArrayIndexOutOfBoundsException e) {
			return false;
		}
		
	}
	
	public void setSqrType(int y, int x, SqrType newType) { //imposta il tipo di una casella 
		space[y][x] = newType;
	}
	
	public void setSqrType(Coord coord, SqrType newType) { //come sopra ma il primo argomento è un oggetto Coord invece che 2 int
		space[coord.getVer()][coord.getHor()] = newType;
	}
		
	public SqrType getSqrType(int y, int x) { //restituisce il tipo della casella corrispondente alle coordinate passate
		return space[y][x];
	}
	
	public SqrType getSqrType(Coord coord) { //come sopra ma l'argomento è un oggetto Coord invece che 2 int
		return space[coord.getVer()][coord.getHor()];
	}
	
	public String toVisual() { //restituisce una String corrispondente ad una descrizione visiva dello spazio
		String result = "";
		for(int i=0; i<space.length; i++) {
			for(int j=0; j<space[i].length; j++) {
				if(space[i][j] == SqrType.EMPTY || space[i][j] == SqrType.TMPOCCUPIED) {
					result += "[ ]";
				}
				else if(space[i][j] == SqrType.LOADINGDOCK || space[i][j] == SqrType.TMPOCCUPIEDLD) {
					result += "[D]";
				}
				else if(space[i][j] == SqrType.LOADINGDOCKOCCUPIED) {
					result += "[O]";
				}
				else if(space[i][j] == SqrType.REST || space[i][j] == SqrType.TMPOCCUPIEDR) {
					result += "[E]";
				}
				else if(space[i][j] == SqrType.RESTOCCUPIED) {
					result += "[R]";
				}
				else if(space[i][j] == SqrType.CHARGE || space[i][j] == SqrType.TMPOCCUPIEDC) {
					result += "[Z]";
				}
				else if(space[i][j] == SqrType.CHARGEOCCUPIED) {
					result += "[C]";
				}
				else if(space[i][j] == SqrType.OBSTACLE) {
					result += "[X]";
				}
				else if(space[i][j] == SqrType.SHELF) {
					result += "[S]";
				}
				else if(space[i][j] == SqrType.TRANSPORTER) {
					result += "[T]";
				}
				else {
					result += "   ";
				}
			}
			result += "\n";
		}
		return result;
	}
	
}
