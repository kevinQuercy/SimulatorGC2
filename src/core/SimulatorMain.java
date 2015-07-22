package core;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;

/** @file
 * 
 * main function, starting point of the simulator application
 *
 * TTTTTTESSSST
 */

public class SimulatorMain {
	private static Logger LOGGER = Logger.getLogger(SimulatorMain.class.getName());

	public static void main(String[] args) {
		
		// default server location
		String server_host = "localhost";
		int server_port = 10000;
		
		if (args.length == 2)
		{
			// override with parameters from command line
			server_host = args[0];
			server_port = Integer.parseInt(args[1]);
		}
		
		Socket socket = null;

		LOGGER.info("Connecting to "+server_host+":"+server_port);
		try {
			socket = new Socket(server_host, server_port);
		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE, "Unknown host", e);
			return;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Socket error", e);
			return;
		}
			
		XMLSocket xmlsocket = new XMLSocket(socket);
		
		// prepare request to server
		Element rootReq = new Element("request");
		Document request = new Document(rootReq);
		Element eltReqType = new Element("request_type");
		eltReqType.setText("CONTAINER_REPORT");
		rootReq.addContent(eltReqType);

		// send request
		LOGGER.info("Sending request CONTAINER_REPORT");
		xmlsocket.write(request);

		// get server response
		Document response = xmlsocket.read();

		if (response == null) {
			LOGGER.severe("No response received");
		} else {
    		// check response type
    		Element rootResp = response.getRootElement();
    		String responseType = rootResp.getChild("response_type").getTextNormalize().toUpperCase();
    		LOGGER.info("Server response: "+responseType);
		}
		
		xmlsocket.close();
	}

}
