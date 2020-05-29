/*
 * classe che calcola il percorso più breve tra due punti in uno spazio diviso in caselle
 * */

package pathFinder.path;

import java.util.LinkedHashSet;
import java.util.LinkedList;

import pathFinder.squareSpace.Coord;
import pathFinder.squareSpace.Space;
import pathFinder.squareSpace.SqrType;

public class ShortestPath {
	
	LinkedHashSet<Coord> steps; //tutti i passi analizati finora
	LinkedHashSet<LinkedList<Coord>> paths; //tutti i percorsi ancora papabili per essere il percorso più breve
	Space space;
	
	public ShortestPath(Space newSpace){
		steps = new LinkedHashSet<Coord>();
		paths = new LinkedHashSet<LinkedList<Coord>>();
		space = newSpace;
	}
	
	public LinkedList<Coord> getShortestPath(Coord startCoord, Coord goalCoord){
		
		space.mapSquares();

		if(space.squareExist(startCoord) && space.squareExist(goalCoord)) { //controlla che lo start e il goal esistano
			
		}
		else {
			return null;
		}
		
		steps.clear(); //svuota la raccolta dei passi percorsi
		paths.clear(); //svuota la raccolta dei percorsi ancora papabili
		
		if(space.getSqrType(goalCoord)==SqrType.EMPTY || space.getSqrType(goalCoord)==SqrType.LOADINGDOCK || space.getSqrType(goalCoord)==SqrType.CHARGE || space.getSqrType(goalCoord)==SqrType.REST) {	//controlla che lo start e il goal siano spazi liberi (DA CAMBIARE LO START CHE PUò ESSERE OCCUPATO DA UN TRANSPORTER)	
			
		}
		else {
			return null;	
		}
		
		LinkedList<Coord> goodPath = new LinkedList<Coord>(); //oggetto usato per salvare un percorso papabile per essere il percorso più breve
		LinkedList<Coord> shortestPath = new LinkedList<Coord>(); //il precorso più breve
		LinkedHashSet<LinkedList<Coord>> tmpPaths = new LinkedHashSet<LinkedList<Coord>>(); //collezione temporanea di percorsi
		Coord tmpSqr; //oggetto temporaneo per salvare un passo
		Coord nextStep; //oggetto che contiene il passo successivo che si vuole analizzare
		
		
		steps.add(startCoord); //aggiunge lo start ai passi percorsi 
		goodPath.add(startCoord); //crea il percorso contenente solo il passo start
		paths.add(goodPath); //aggiunge il percorso appenareato ai percorsi papabili

		boolean found = false; //boolean che indica se il percorso più breve è stato trovato o meno
		
		while(!found) {
			
			if(!paths.isEmpty()) {//controlla che ci siano ancora dei percorsi in path
				for(LinkedList<Coord> path : paths) { //per ogni percorso ancora papabile...
					tmpSqr = path.getLast(); //...recupera l'ultimo passo
					
					//analizza le caselle adiacenti al passo recuperato
					
					//north
					if(space.squareExist(tmpSqr.goNorth())) {//prende le coordinate dello square, le manda a north e controlla che esista uno square con quelle coordinate in space
						nextStep = tmpSqr.goNorth();//se esiste lo Square a quelle coordinate lo salva in nextStep
						if(nextStep.isEquals(goalCoord)) {//lo step successivo è il goal quindi si è trovato uno dei path più veloce
							shortestPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
							shortestPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
							return shortestPath;//si restituisce il path più veloce così trovato
						}
						else if(space.getSqrType(nextStep) == SqrType.EMPTY) {//controlla se lo square è vuoto
							if(isNewStep(nextStep)) {//controlla che lo step non sia già presente negli step presenti in steps
								steps.add(nextStep);//aggiunge lo square salvato agli steps
								goodPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
								goodPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
								tmpPaths.add((LinkedList<Coord>) goodPath.clone());//si aggiunge il path così ottenuto all'insieme dei path aggiornati
							}
						}						
					}
					//south
					if(space.squareExist(tmpSqr.goSouth())) {//prende le coordinate dello square, le manda a south e controlla che esista uno square con quelle coordinate in space
						nextStep = tmpSqr.goSouth();//se esiste lo Square a quelle coordinate lo salva in nextStep
						if(nextStep.isEquals(goalCoord)) {//lo step successivo è il goal quindi si è trovatoss uno dei path più veloce
							shortestPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
							shortestPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
							return shortestPath;//si restituisce il path più veloce così trovato
						}
						else if(space.getSqrType(nextStep) == SqrType.EMPTY) {//controlla se lo square è vuoto
							if(isNewStep(nextStep)) {//controlla che lo step non sia già presente negli step presenti in steps
								steps.add(nextStep);//aggiunge lo square salvato agli steps
								goodPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
								goodPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
								tmpPaths.add((LinkedList<Coord>) goodPath.clone());//si aggiunge il path così ottenuto all'insieme dei path aggiornati
							}
						}						
					}
					//west
					if(space.squareExist(tmpSqr.goWest())) {//prende le coordinate dello square, le manda a west e controlla che esista uno square con quelle coordinate in space
						nextStep = tmpSqr.goWest();//se esiste lo Square a quelle coordinate lo salva in nextStep
						if(nextStep.isEquals(goalCoord)) {//lo step successivo è il goal quindi si è trovatoss uno dei path più veloce
							shortestPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
							shortestPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
							return shortestPath;//si restituisce il path più veloce così trovato
						}
						else if(space.getSqrType(nextStep) == SqrType.EMPTY) {//controlla se lo square è vuoto
							if(isNewStep(nextStep)) {//controlla che lo step non sia già presente negli step presenti in steps
								steps.add(nextStep);//aggiunge lo square salvato agli steps
								goodPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
								goodPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
								tmpPaths.add((LinkedList<Coord>) goodPath.clone());//si aggiunge il path così ottenuto all'insieme dei path aggiornati
							}
						}						
					}
					//east
					if(space.squareExist(tmpSqr.goEast())) {//prende le coordinate dello square, le manda a east e controlla che esista uno square con quelle coordinate in space
						nextStep = tmpSqr.goEast();//se esiste lo Square a quelle coordinate lo salva in nextStep
						if(nextStep.isEquals(goalCoord)) {//lo step successivo è il goal quindi si è trovatoss uno dei path più veloce
							shortestPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
							shortestPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
							return shortestPath;//si restituisce il path più veloce così trovato
						}
						else if(space.getSqrType(nextStep) == SqrType.EMPTY) {//controlla se lo square è vuoto
							if(isNewStep(nextStep)) {//controlla che lo step non sia già presente negli step presenti in steps
								steps.add(nextStep);//aggiunge lo square salvato agli steps
								goodPath = (LinkedList<Coord>) path.clone();//copia il path su cui si sta lavorando
								goodPath.add(nextStep);//si aggiunge lo square su cui si sta lavorando
								tmpPaths.add((LinkedList<Coord>) goodPath.clone());//si aggiunge il path così ottenuto all'insieme dei path aggiornati
							}
						}						
					}					
				}
				paths = (LinkedHashSet<LinkedList<Coord>>) tmpPaths.clone();//finiti di controllare tutti i path che si possono originare delle head dei paths che si avevano prima, ora sostituisce il vecchio insieme dei path con quello nuovo	
				tmpPaths.clear();

			}
			else {//se non ci sono altri percorsi vuol dire che non esiste un percorso che porta dallo start al goal
				return null;
			}
			
		}
		
		return null;
		
	}
	
	public boolean isNewStep(Coord step) { //controlla che lo step non sia già stato attraversato
		for(Coord sqr: steps) {
			if(sqr.isEquals(step)) {
				return false;
			}
		}
		return true;
	}
	

}
