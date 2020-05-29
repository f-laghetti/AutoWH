package request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DoGet {
	
	public static void main(String arg[]) {
		System.out.println("invio richiesta POST");
		try{
			sendPOST("http://localhost:8000/", "CONNECT&c3:76:2f:0b:70:18&13&T&3"); //alettone pultio
			sendPOST("http://localhost:8000/", "CONNECT&c3:10:2a:28:10:45&22&T&3"); 
			//sendPOST("http://localhost:8000/", "DELIVER_SHELF&0&15&6&18|0/1/2$19|0/1/2");
			sendPOST("http://localhost:8000/", "RETRIEVE_SHELF&0&15&6&18|0/1/2$19|0/1/2");
			
			Thread.sleep(10000);
			
			//sendPOST("http://localhost:8000/", "DELIVER_SHELF&0&15&6&18|0/1/2$19|0/1/2");
			
			/*sendPOST("http://localhost:8000/", "RETRIEVE_SHELF&1&16&6&13|0/1/2$13|0/1/2");
			sendPOST("http://localhost:8000/", "ComandoVolontariamenteErrato");*/
		}
		catch (ConnectException e) {
			System.out.println("Connessione rifiutata. Il server potrebbe essere spento o non raggiungibile.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

		
	public static String sendPOST(String url, String content) throws IOException {
		
		URL URLGet = new URL(url);
		
		HttpURLConnection conn = (HttpURLConnection) URLGet.openConnection();
		
		conn.setRequestMethod("POST");
		
		conn.setRequestProperty("Content-type", "text/html");
		
		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		
		OutputStream out = conn.getOutputStream();
        out.write(content.getBytes("UTF-8"));
        out.close();
        
        StringBuffer responseBuffer = new StringBuffer();
		
		int responseCode = conn.getResponseCode();
		System.out.println("POST request response code :: " + responseCode);
		
		if(responseCode == HttpURLConnection.HTTP_OK) { //SUCCESSO
			
			//legge e stampa la risposta ricevuta
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String inputLine;
			
			
			while ((inputLine = in.readLine()) != null) {
				responseBuffer.append(inputLine);
			}
			
			in.close();
			
			System.out.println(responseBuffer.toString());
			
			System.out.println("POST request IS SUCCESSFUL");
		}
		else {//FAILURE
			System.out.println("POST request FAILED");
		}
		
		return responseBuffer.toString();
		
	}
}
