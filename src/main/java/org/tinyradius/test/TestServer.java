/**
 * $Id: TestServer.java,v 1.6 2006/02/17 18:14:54 wuttke Exp $
 * Created on 08.04.2005
 * 
 * @author Matthias Wuttke
 * @version $Revision: 1.6 $
 */
package org.tinyradius.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.tinyradius.packet.AccessRequest;
import org.tinyradius.packet.AccountingRequest;
import org.tinyradius.packet.CoaRequest;
import org.tinyradius.packet.RadiusPacket;
import org.tinyradius.util.RadiusException;
import org.tinyradius.util.RadiusServer;

/**
 * Test server which terminates after 30 s.
 * Knows only the client "localhost" with secret "testing123" and
 * the user "mw" with the password "test".
 */
public class TestServer {

	/**
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, Exception {
		RadiusServer server = new RadiusServer() {
			// Authorize localhost/testing123
			public String getSharedSecret(InetSocketAddress client) {
				if (client.getAddress().getHostAddress().equals("127.0.0.1")) {
					return "47oeW^cC";
				}
				return null;
			}

			// Authenticate mw
			public String getUserPassword(String userName) {
				if (userName.equals("mw")) {
					return "test";
				}
				return null;
			}

			// Adds an attribute to the Access-Accept packet
			public RadiusPacket accessRequestReceived(AccessRequest accessRequest, InetSocketAddress client) throws RadiusException {
				System.out.println("Received Access-Request:\n" + accessRequest);
				RadiusPacket packet = super.accessRequestReceived(accessRequest, client);
				if (packet == null) {
					System.out.println("Ignore packet.");
				}
				else if (packet.getPacketType() == RadiusPacket.ACCESS_ACCEPT) {
					packet.addAttribute("Reply-Message", "Welcome " + accessRequest.getUserName() + "!");
				}
				else {
					System.out.println("Answer:\n" + packet);
				}
				return packet;
			}

			public RadiusPacket accountingRequestReceived(AccountingRequest accountingRequest, InetSocketAddress client) throws RadiusException {
				System.out.println("Received Accounting-Request:\n" + accountingRequest);
				RadiusPacket packet = super.accountingRequestReceived(accountingRequest, client);
				if (packet == null) {
					System.out.println("Ignore packet.");
				}
				else if (packet.getPacketType() == RadiusPacket.ACCOUNTING_RESPONSE) {
					packet.addAttribute("Reply-Message", "Welcome " + accountingRequest.getUserName() + "!");
				}
				else {
					System.out.println("Answer:\n" + packet);
				}
				return packet;
			}

			public RadiusPacket coaRequestReceived(CoaRequest coaRequest, InetSocketAddress client) throws RadiusException {
				System.out.println("Received Accounting-Request:\n" + coaRequest);
				RadiusPacket packet = super.coaRequestReceived(coaRequest, client);
				if (packet == null) {
					System.out.println("Ignore packet.");
				}
				else if (packet.getPacketType() == RadiusPacket.COA_ACK) {
					packet.addAttribute("Reply-Message", "Welcome " + coaRequest.getUserName() + "!");
				}else if (packet.getPacketType() == RadiusPacket.COA_NAK) {
					packet.addAttribute("Error-Cause","Unsupported-Service");
					packet.addAttribute("Reply-Message", "No valid session");
				}
				else {
					System.out.println("Answer:\n" + packet);
				}
				return packet;
			}



			};
		if (args.length >= 1)
			server.setAuthPort(Integer.parseInt(args[0]));
		if (args.length >= 2)
			server.setAcctPort(Integer.parseInt(args[1]));
		if (args.length >= 3)
			server.setCoaPort(Integer.parseInt(args[2]));

		server.start(true, true,true);

		System.out.println("Server started.");

		Thread.sleep(1000 * 60 * 30);
		System.out.println("Stop server");
		server.stop();
	}

}
