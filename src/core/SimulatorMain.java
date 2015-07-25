package core;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;

import data.ContainerSimu;

/** @file
 * 
 * main function, starting point of the simulator application
 *
 */

public class SimulatorMain {
	private static Logger LOGGER = Logger.getLogger(SimulatorMain.class.getName());

	public static void main(String[] args) {
		// default settings
		int nbContainers = 50;
		String server_host = "localhost";
		int server_port = 10000;
		
		if (args.length >= 1)
		{
			// override with parameters from command line
			nbContainers = Integer.parseInt(args[0]);
			if (args.length == 3) {
				server_host = args[1];
				server_port = Integer.parseInt(args[2]);
			}
		}
		
		new SimulatorMain(nbContainers, server_host, server_port).run();
	}
	
	private int nbContainers;
	private String server_host;
	private int server_port;
	private List<ContainerSimu> containers;
	private Random random;
	
	private SimulatorMain(int nbContainers, String server_host, int server_port) {
		this.nbContainers = nbContainers;
		this.server_host = server_host;
		this.server_port = server_port;
		
		// create containers for simulation
		containers = new ArrayList<>();
		for (int i = 0; i < this.nbContainers; i++)
			containers.add(new ContainerSimu(i));
		
		// create random generator
		random = new Random(1); // fix seed to reproduce random pattern
	}
	
	private void run() {
		// 1/ fill randomly all containers
		for (ContainerSimu containerSimu: containers)
			containerSimu.randomFill(random);
		
		// 2/ report state of each container to controller
		for (ContainerSimu containerSimu: containers)
			containerReport(containerSimu);
	}
	
	private void addFieldInt(Element eltRoot, String fieldname, int value) {
		Element elt = new Element(fieldname);
		elt.setText(String.valueOf(value));
		eltRoot.addContent(elt);
	}
	
	private void containerReport(ContainerSimu containerSimu) {
		Socket socket = null;

		LOGGER.info("Container #"+containerSimu.getContainerId()+" connecting to "+server_host+":"+server_port);
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
		
		// add container data
		Element eltContRep = new Element("container_report");
		rootReq.addContent(eltContRep);
		addFieldInt(eltContRep, "id", containerSimu.getContainerId());
		addFieldInt(eltContRep, "weight", containerSimu.getWeight());
		addFieldInt(eltContRep, "volume", containerSimu.getVolume());
		addFieldInt(eltContRep, "volumemax", containerSimu.getVolumeMax());
		
		// send request
		LOGGER.info("Container #"+containerSimu.getContainerId()+" sending request CONTAINER_REPORT");
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
