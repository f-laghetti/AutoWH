package autoMag.shelves;

import java.io.Serializable;

public enum ShelfStatus implements Serializable {
	
	EMPTY, RESERVED, INPROGRESS, FULL, COLLECTRESERVED, COLLECTING;

	/*
	 * EMPTY 			= vuota
	 * RESERVED 		= vuota e prenotata da una transazione per depositare merce
	 * INPROGRESS 		= un transporter è stato incaricato di depositare della merce in questa shelf
	 * FULL 			= occupata
	 * COLLECTRESERVED 	= occupata e prenotata da una transazione per prelevarne la merce
	 * COLLECTING		= un transporter è stato incaricato di prelevare la merce da questa shelf
	 */
}
