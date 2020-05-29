/*
 * enum contenente gli stati che può assumere una casella all'interno della planimetria
 * */

package pathFinder.squareSpace;

import java.io.Serializable;

public enum SqrType implements Serializable {
	
	CHARGE, CHARGEOCCUPIED, EMPTY, LOADINGDOCK, LOADINGDOCKOCCUPIED, OBSTACLE, OUT, REST, RESTOCCUPIED, SHELF, TMPOCCUPIED, TMPOCCUPIEDC, TMPOCCUPIEDLD, TMPOCCUPIEDR, TRANSPORTER;
	
	/* CHARGE				= zona di ricarica, 
	 * CHARGEOCCUPIED		= zona di ricarica occupata
	 * EMPTY	   			= spazio libero
	 * LOADINGDOCK 			= zona di carico e scarico
	 * LOADINGDOCKOCCUPIED 	= zona di carico e scarico occupata da un transporter
	 * OUT		   			= fuori dalla planimetria
	 * OBSTACLE    			= spazio occupato
	 * REST					= zona di rest di un transporter
	 * RESTOCCUPIED			= zona di rest occupata dal suo transporter
	 * SHELF	   			= spazio occupato da una scaffalatura
	 * TMPOCCUPIED			= spazio prenotato da un transporter che ha richiesto di muoversi in quella casella
	 * TMPOCCUPIEDC			= zona di ricarica prenotata da un transporter che ha richiesto di muoversi in quella casella
	 * TMPOCCUPIEDLD		= loadingdock prenotato da un transporter che ha richiesto di muoversi in quella casella
	 * TMPOCCUPIEDR			= zona di rest prenotata da un transporter che ha richiesto di muoversi in quella casella
	 * TRANSPORTER 			= spazio occupato da un transporter
	 * */
	
}
