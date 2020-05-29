package autoMag.transaction;

public enum TransactionStatus {
	OPEN, INPROGRESS, DELIVERED, CLOSED;
	
	/*
	 * OPEN 		=	accetta ma non ancora in fase di lavorazione
	 * INPROGRESS 	=	la richiesta di scarico della merce � stata accettata
	 * DELIVERED 	=	la merce � stata consegnata ma non ancora distribuita all'interno
	 * CLOSED 		=	completata 
	 * */

}
