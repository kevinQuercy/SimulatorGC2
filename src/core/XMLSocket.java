package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/** @file
 *
 * Wrap a socket for XML communication.
 * XMLSocket takes as input / output an XML Document (tree) and do the necessary
 * conversion from / to XML string on the socket
 *
 * @note An empty text line is used to indicate the end of the XML message.
 * It is managed internally in read() and write() functions.
 */

public class XMLSocket {
	private static Logger LOGGER = Logger.getLogger(XMLSocket.class.getName());

	Socket socket;
	SAXBuilder sxb;

	public XMLSocket(Socket socket) {
		super();
		this.socket = socket;
		sxb = new SAXBuilder();
	}
	
	// Get one XML message from socket
	public Document read() {
		// buffer lines until an empty line is found == end of XML message
		BufferedReader buffRead;
		StringBuilder xmlRequest = new StringBuilder();
		
		try {
			buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (true)
			{
				String line = buffRead.readLine();
				if (line == null || line.isEmpty())
					break;
				xmlRequest.append(line);
			}
		} catch (IOException e) {
			close();
			LOGGER.log(Level.SEVERE, "Socket error", e);
			return null;
		}

		if (xmlRequest.length() == 0)
		{
			close();
			LOGGER.info("Socket has been closed");
			return null;
		}
		
		Document msg = null;
		try {
			msg = sxb.build(new StringReader(xmlRequest.toString()));
		} catch (JDOMException e) {
			LOGGER.log(Level.SEVERE, "XML error", e);
			close();
			return null;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "IO error", e);
			close();
			return null;
		}
		return msg;
	}
	
	// Send one XML message on the socket
	public void write(Document msg) {
		XMLOutputter xmlOutput = new XMLOutputter(Format.getCompactFormat());
		try {
			xmlOutput.output(msg, socket.getOutputStream());
			socket.getOutputStream().write('\n'); // empty line to indicate end of request
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, "Socket error", e);
			close();
		}
	}
	
	public void close() {
		if (socket != null)
		{
			try {
				socket.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error while closing socket", e);
			}
			socket = null;
		}
	}
	
	public boolean isClosed() {
		return socket == null;
	}
}
