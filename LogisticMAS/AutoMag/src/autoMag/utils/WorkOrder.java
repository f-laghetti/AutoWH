package autoMag.utils;

import java.util.ArrayList;

import autoMag.space.Coord;

public class WorkOrder {
	private WorkOrderType type;
	private Coord shelf;
	private ArrayList<Integer> levels;
	private ArrayList<Coord> path;

	public WorkOrder(WorkOrderType newType, Coord newShelf, ArrayList<Integer> newLevels, ArrayList<Coord> newPaths){
		type = newType;
		shelf = newShelf;
		levels = newLevels;
		path = newPaths;
	}
	
	public WorkOrder(WorkOrderType newType, ArrayList<Coord> newPaths){
		type = newType;
		shelf = null;
		levels = null;
		path = newPaths;
	}

	public WorkOrderType getType() {
		return type;
	}

	public void setType(WorkOrderType type) {
		this.type = type;
	}

	public Coord getShelf() {
		return shelf;
	}

	public void setShelf(Coord shelf) {
		this.shelf = shelf;
	}

	public ArrayList<Integer> getLevels() {
		return levels;
	}

	public void setLevels(ArrayList<Integer> levels) {
		this.levels = levels;
	}

	public ArrayList<Coord> getPath() {
		return path;
	}

	public void setPath(ArrayList<Coord> path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "WorkOrderToShelf [type=" + type + ", shelf=" + shelf + ", levels=" + levels + ", path=" + path + "]";
	}

}
