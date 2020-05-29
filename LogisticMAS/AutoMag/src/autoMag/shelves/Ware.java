/*
 * classe che rappresenta una merce
 * */

package autoMag.shelves;

import java.io.Serializable;

import jade.core.AID;

public class Ware implements Serializable {

	private String id; //codice identificativo della merce
	private String name; //nome della merce
	private AID owner; //codice identificativo del proprietario
	
	public Ware(String newID, String newName, AID newOwner) {
		id = newID;
		name = newName;
		owner = newOwner;
	}
	
	public String getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public AID getOwner(){
		return (AID) owner.clone();
	}
	
	public String toString() {
		return "| "+id+" | "+name+" | "+owner.getName()+" |";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ware other = (Ware) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

	
}
