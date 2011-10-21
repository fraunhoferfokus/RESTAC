/*
 * Copyright (C) 2007 FhG Fokus
 *
 * This file is part of RESTAC, a peer-to-peer Java framework implementing REST.
 *
 * RESTAC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * For a license to use the RESTAC software under conditions
 * other than those described here, please contact David Linner by e-mail at the following addresses:
 *    David.Linner@fokus.fraunhofer.de
 * RESTAC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License and the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * 
 */
package de.fhg.fokus.restac.httpx.core.dispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilterImpl;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXMessageRefactorer;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXPlainActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;

/**
 * @author Murat Ates
 *
 */
public class UDPMUManagedActionMessageDispatcher extends HTTPXManagedActionMessageDispatcher implements Runnable {
	private final static Logger LOGGER = Logger.getLogger(UDPMUManagedActionMessageDispatcher.class);
	
	/** The address of the multicast group to be joined. */
	 public static String DEFAULT_MCASTGROUP = "224.0.0.99";
	
	 /** The port used by the multicast socket	 */
	public static int DEFAULT_MULTICASTSOCKET_PORT = 2221;
	
//	private final int BUFFER_LEN = 512;
	
	/** The address of the multicast group to be joined. */
	private String mCastGroup = DEFAULT_MCASTGROUP;
	
	/** The DatagramSocket */
	private MulticastSocket multicastSocket;
	
	private int port = DEFAULT_MULTICASTSOCKET_PORT;
	private boolean listening;
	
	public UDPMUManagedActionMessageDispatcher(HTTPXActionMessageDispatcher dispatcher) {
		super(dispatcher);
		
		//register the handler on the Proxydispatcher
		dispatcher.addActionMessageHandler(new HTTPXActionMessageFilterImpl(HTTPXConstants.HTTPMU, null, 0, null, null), this);
	}

	@Override
	public void start() {
		//TODO: evlt. LOGGER.info erweitern (Start Client + Server)
		LOGGER.info("Start: " + this.getClass().toString());
		try {
			multicastSocket = new MulticastSocket(port);
			multicastSocket.joinGroup(InetAddress.getByName(mCastGroup));
			multicastSocket.setTimeToLive(15);
			
			new Thread(this).start();
			LOGGER.info("new " + HTTPXConstants.HTTPMU + "-Socket on Port: " + port);
		} catch (IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		LOGGER.debug("Shutdown: " + this.getClass().toString());
		
		listening = false;
		try {
			if (multicastSocket != null) {
				multicastSocket.leaveGroup(InetAddress.getByName(mCastGroup));
			}
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		} 
		finally {
			if (multicastSocket != null) {
				multicastSocket.close();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXSynActionMessageHandler#handleSyn(de.fhg.fokus.restac.httpx.communication.common.HTTPXActionMessage)
	 */
	public HTTPXStatusMessage handleSyn(HTTPXActionMessage request) throws HTTPXProtocolViolationException {
		LOGGER.debug("No Effect");
		return null;
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXAsynActionMessageHandler#handleAsyn(de.fhg.fokus.restac.httpx.communication.common.HTTPXActionMessage)
	 */
	public HTTPXStatusMessageHandle handleAsyn(HTTPXActionMessage request) {
		LOGGER.debug("No Effect");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXPlainActionMessageHandler#handlePlain(de.fhg.fokus.restac.httpx.communication.common.HTTPXActionMessage)
	 */
	public void handlePlain(HTTPXActionMessage request) throws IOException{
		HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
		DatagramPacket datagramPacket;
		byte[] data = new byte[0];
//		InputStream in = refactorer.toStream(request);
		
		try {
//			int curChar;
//			StringBuilder sb = new StringBuilder();
//			try {
//				while ((curChar = in.read()) != -1) {
//					sb.append((char)curChar);
//				}
//			} catch (IOException e) {
//				LOGGER.error(e);
//				e.printStackTrace();
//			}
//			data = sb.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET);
			data = refactorer.toByte(request);
			
			String tmpGroup = null;
			String tmpHost = request.getHost();
			
			// if no host is quoted as multicast group,
			// take the default (DEFAULT_MCASTGROUP) group
			tmpGroup = tmpHost.equals("") ? DEFAULT_MCASTGROUP : tmpHost;
			
			InetAddress ia = InetAddress.getByName(tmpGroup);
			
			// last group is not the same like this group;
			// leave old group and join to the new one
			if (! tmpGroup.equals(mCastGroup)) {
				ia = InetAddress.getByName(tmpGroup);
				// leave old group
				multicastSocket.leaveGroup(InetAddress.getByName(mCastGroup));
				LOGGER.debug("Leave old Multicastgroup: " + mCastGroup);
				
				// join new group
				multicastSocket.joinGroup(ia);
				mCastGroup = tmpGroup;
				LOGGER.debug("Join new Multicastgroup: " + tmpGroup);
			}
			//last group is the same group
			else {
				//do nothing
				
//				ia = InetAddress.getByName(mCastGroup);
//				multicastSocket.leaveGroup(ia);
			}
			
			datagramPacket = new DatagramPacket(data, data.length, ia, request.getPort());
			//Send the package
			multicastSocket.send(datagramPacket);
			LOGGER.debug(HTTPXConstants.HTTPMU + " package is send.");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
	}
	
	public void run() {
		listening = true;
		DatagramPacket datagramPacket;
		
		while (listening) {
			try {
				datagramPacket = new DatagramPacket(new byte[HTTPXConstants.UDP_PACKET_LENGTH], HTTPXConstants.UDP_PACKET_LENGTH);
				multicastSocket.receive(datagramPacket);
				
				new Thread(new ConnectionHandler(datagramPacket));
			}catch (IOException e) {
				LOGGER.debug(HTTPXConstants.HTTPMU + "-Server socket was closed.");
				listening = false;
			}
		}
	}//run

	private class ConnectionHandler implements Runnable{
		private DatagramPacket packet;
				
		public ConnectionHandler(DatagramPacket packet) {
			LOGGER.debug(HTTPXConstants.HTTPMU + " package received");
			this.packet = packet;
			
			//Start the Thread
			Thread t = new Thread(this);
//			t.setName(UDPMUManagedActionMessageDispatcher.class.toString() + ": " + this.getClass().toString());
			t.start();
		}

		public void run() {
			HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
			HTTPXActionMessage request = null;
			
			try {
				request = refactorer.createActionMessageFromStream(toStream(packet.getData(), packet.getLength()));
			} catch (HTTPXProtocolViolationException e) {
				LOGGER.error(e);
				e.printStackTrace();
			} catch (IOException e){
				LOGGER.error(e);
				e.printStackTrace();
			}
			//TODO: overwrite port and host with socket-information
			request.setPort(packet.getPort());
			request.setHost(packet.getAddress().getHostAddress());

			//			LOGGER.debug(HTTPXConstants.HTTPMU + " Request:\r\n" + request.toString());
			
			boolean bFound = false;
			Path tmpPath = new Path(request.getPath().getString());
			try {
				do {
					for (Tuple element : registration) {	
						if ( (element.handler instanceof HTTPXPlainActionMessageHandler) &&
							  element.filter.doesPass(request.getProtocol(), request.getHost(), request.getPort(), tmpPath, request.getQueryAsString())
							){
							LOGGER.debug("plain Handler was found: " + element.handler);
							((HTTPXPlainActionMessageHandler)element.handler).handlePlain(request);
							bFound = true;
							break;
						}
					}//for
					if (!bFound) {
						String strTmp = tmpPath.getString();
						tmpPath.removeLastToken();
						//check wheter path is root
						if (strTmp.equalsIgnoreCase(tmpPath.getString())) {
							break;
						}
					}
				} while (!bFound);
				
				if (! bFound) {
					LOGGER.debug("no handler was found");
				}
			} catch (IOException e) {
//				LOGGER.error(e);
//				e.printStackTrace();
			}
		}
		
		/**
		 * Convert UDP-Data into a <code>InputStream</code>. 
		 * 
		 * @param data			the UDP packet
		 * @param len			the length of the data
		 * @return				the <code>InputStream</code>
		 * @throws IOException	
		 */
		private InputStream toStream(byte[] data, int len) throws IOException{
			try {
				OutputStream out = new PipedOutputStream();
				InputStream in = new PipedInputStream((PipedOutputStream)out, len);
				out.write(data,0,len);
//				out.flush();
				out.close();
				return in;
				}
			catch (IOException e) {
				LOGGER.error("An error occured while building an inputstream out of the request's Datagram buffer data",e);
				throw new IOException(e.getMessage());
			}
		}
	}//ConnectionHanlder class

	public String getInetAddress() {
		try {
			return InetAddress.getByName(this.mCastGroup).toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getPort() {
		return this.port;
	}

	public String getProtocol() {
		return HTTPXConstants.HTTPMU;
	}
	
	public String getGroup(){
		return mCastGroup;
	}
	
	public void setGroup(String group){
		try {
			multicastSocket.leaveGroup(InetAddress.getByName(this.mCastGroup));
			multicastSocket.joinGroup(InetAddress.getByName(group));
		} catch (IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		this.mCastGroup = group;
	}
	
}//class
