package autoMag.utils;

//descrive lo scopo del work order
public enum WorkOrderType {

	TOSHELF, TOTRANSACTOR, TOSHELFCOLLECT, TOTRANSACTORDELIVER, TOREST, TOCHARGE
	
	/*
	 TOSHELF				viaggio verso una scaffalatura per depositare merce
	 TOTRANSACTOR			viaggio verso un transactor per prelevare della merce da esso	 
	 TOSHELFCOLLECT			viaggio verso una scaffalatura per prelevare merce
	 TOTRANSACTORDELIVER	viaggio verso un transactor per consegnargli della merce
	 TOREST					viaggio verso la zona di rest
	 TOCHARGE				viaggio verso un charger
	 * */
	
}
