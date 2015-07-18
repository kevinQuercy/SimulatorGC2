package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/** @file
 * 
 * main function, starting point of the simulator application
 *
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
    	SAXBuilder sxb = new SAXBuilder();

		try {
			LOGGER.info("Connecting to "+server_host+":"+server_port);
			socket = new Socket(server_host, server_port);
			
			// prepare request to server
			Element rootReq = new Element("request");
			Document request = new Document(rootReq);
			Element eltReqType = new Element("request_type");
			eltReqType.setText("CONTAINER_REPORT");
			rootReq.addContent(eltReqType);

			// send request
			LOGGER.info("Sending request CONTAINER_REPORT");
    		XMLOutputter xmlOutput = new XMLOutputter(Format.getCompactFormat());
    		xmlOutput.output(request, socket.getOutputStream());
    		socket.getOutputStream().write('\n'); // empty line to indicate end of request

			// get server response (buffer lines until an empty line is found)
    		BufferedReader buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    		StringBuilder xmlRequest = new StringBuilder();
    		while (true)
    		{
    			String line = buffRead.readLine();
    			if (line == null || line.isEmpty())
    				break;
    			xmlRequest.append(line);
    		}
    		Document response = sxb.build(new StringReader(xmlRequest.toString()));
    		
    		// check response type
    		Element rootResp = response.getRootElement();
    		String responseType = rootResp.getChild("response_type").getTextNormalize().toUpperCase();
    		LOGGER.info("Server response: "+responseType);
    		
		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE, "Unknown host", e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Error during connection to server", e);
		} catch (JDOMException e) {
			LOGGER.log(Level.SEVERE, "Invalid XML", e);
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error while closing socket", e);
			}
		}
	}

}
