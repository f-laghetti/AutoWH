package autoMag.shelves;

import java.io.Serializable;

public class Shelf implements Serializable {
	
	private ShelfStatus status;
	private Ware ware;
	
	public Shelf() {
		status = ShelfStatus.EMPTY;
		ware = null;
	}
	
	public boolean removeWare() {
		if(status == ShelfStatus.FULL || status == ShelfStatus.COLLECTRESERVED) {
			ware = null;
			status = ShelfStatus.EMPTY;
			return true;
		}		
		else{
			return false;
		}
	}
	
	public boolean storeWare(Ware newWare) {
		if(status == ShelfStatus.FULL) {
			return false;
		}		
		else{
			ware = newWare;
			status = ShelfStatus.FULL;
			return true;
		}
	}
	
	public boolean isInProgress() {
		if(status == ShelfStatus.RESERVED) {
			status = ShelfStatus.INPROGRESS;
			return true;
		}		
		else{
			return false;
		}
	}
	
	public boolean isCollecting() {
		if(status == ShelfStatus.COLLECTRESERVED) {
			status = ShelfStatus.COLLECTING;
			return true;
		}		
		else{
			return false;
		}
	}
	
	public boolean reserve() {
		if(status == ShelfStatus.EMPTY) {
			status = ShelfStatus.RESERVED;
			return true;
		}		
		else{
			return false;
		}
	}
	
	public boolean reserveForCollect() {
		if(status == ShelfStatus.FULL) {
			status = ShelfStatus.COLLECTRESERVED;
			return true;
		}		
		else{
			return false;
		}
	}
	
	public ShelfStatus getStatus() {
		return status;
	}
	
	public Ware getWare() {
		Ware wareCopy = new Ware(ware.getID(), ware.getName(), ware.getOwner());//per la privacy leak
		return wareCopy;
	}
	
	public String toString() {
		String str = status.toString();
		if(status == ShelfStatus.FULL) {
			str += ": "+ware;
		}
		else if(status == ShelfStatus.COLLECTRESERVED) {
			str += ": "+ware;
		}
		return str;
	}

}
