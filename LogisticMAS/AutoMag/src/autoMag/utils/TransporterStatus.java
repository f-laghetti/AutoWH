/*
 * enum contenente gli stati che pu� assumere un transporter
 * */


package autoMag.utils;

public enum TransporterStatus {
	
	BACKSTART, NOTREADY, UNUSED, MOVING, CHARGING, WAITING, WORKING;
	
	/*
	BACKSTART		sta ritornando verso il suo punto di partenza
	NOTREADY:		in attesa che il warehouse lo accetti 		
	UNUSED: 		fermo in attesa di istruzioni
	MOVING: 		in movimento verso la destinazione
	CHARGING: 		nella postazione di carica
	WAITING:		in attesa poich� la posizione che deve attraversare o raggiungere � occupata
	WORKING:		fermo perch� sta ultimando il processo di deposito o prelievo della merce
	*/

}
