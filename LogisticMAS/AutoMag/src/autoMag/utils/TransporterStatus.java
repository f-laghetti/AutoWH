/*
 * enum contenente gli stati che può assumere un transporter
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
	WAITING:		in attesa poiché la posizione che deve attraversare o raggiungere è occupata
	WORKING:		fermo perché sta ultimando il processo di deposito o prelievo della merce
	*/

}
