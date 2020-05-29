package autoMag.httpManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Request {
	
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
