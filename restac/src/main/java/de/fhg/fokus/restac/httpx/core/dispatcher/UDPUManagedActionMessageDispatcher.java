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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

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
 * @author mat
 *
 */
public class UDPUManagedActionMessageDispatcher extends	HTTPXManagedActionMessageDispatcher implements Runnable {
	private final static Logger LOGGER = Logger.getLogger(UDPUManagedActionMessageDispatcher.class);
	private final static int DEFAULT_PORT = 1111;
	
	/** The DatagramSocket */
	private DatagramSocket datagramSocket;
	
	private int port = DEFAULT_PORT;
	
	private boolean listening;
	
	
	/**
	 * @param dispatcher
	 */
	public UDPUManagedActionMessageDispatcher(HTTPXActionMessageDispatcher dispatcher) {
		super(dispatcher);
		
//		register the handler on the Proxydispatcher
		dispatcher.addActionMessageHandler(new HTTPXActionMessageFilterImpl(HTTPXConstants.HTTPU, null, 0, null, null), this);
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.communication.common.HTTPXManagedActionMessageDispatcher#start()
	 */
	@Override
	public void start() {
		LOGGER.info("Start: " + this.getClass().toString());
		try {
			// default port
			datagramSocket = new DatagramSocket(port);
		} catch (IOException e) {
//			LOGGER.error(e);
//			e.printStackTrace();
			
			try {
				// random port
				datagramSocket = new DatagramSocket();
			} catch (SocketException e1) {
				LOGGER.error(e1);
				e1.printStackTrace();
			}
		}
		if (datagramSocket != null) {
			port = datagramSocket.getLocalPort();
			new Thread(this).start();
			LOGGER.info("new " + HTTPXConstants.HTTPU + "-Socket on Port: " + port);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.communication.common.HTTPXManagedActionMessageDispatcher#shutdown()
	 */
	@Override
	public void shutdown() {
		LOGGER.info("Shutdown: " + this.getClass().toString());
		listening = false;
		
		if (datagramSocket != null) {
			try {
				datagramSocket.close();
			} catch (Exception e) {
				LOGGER.error(HTTPXConstants.HTTPU + " Socket closed !");
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXSynActionMessageHandler#handleSyn(de.fhg.fokus.restac.httpx.communication.common.HTTPXActionMessage)
	 */
	public HTTPXStatusMessage handleSyn(HTTPXActionMessage request)	throws HTTPXProtocolViolationException {
		return null;
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXAsynActionMessageHandler#handleAsyn(de.fhg.fokus.restac.httpx.communication.common.HTTPXActionMessage)
	 */
	public HTTPXStatusMessageHandle handleAsyn(HTTPXActionMessage request) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXPlainActionMessageHandler#handlePlain(de.fhg.fokus.restac.httpx.communication.common.HTTPXActionMessage)
	 */
	public void handlePlain(HTTPXActionMessage request) throws IOException{
		HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
		InetAddress ia;
		DatagramPacket datagramPacket = null;
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
//			
//			data = sb.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET);
			data = refactorer.toByte(request);
			
			ia = InetAddress.getByName(request.getHost());
			datagramPacket = new DatagramPacket(data, data.length, ia, request.getPort());
			
			//Send the package
			datagramSocket.send(datagramPacket);
			LOGGER.debug(HTTPXConstants.HTTPU + " package is send out");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e);
			e.printStackTrace();
		} 
		
		//close the socket
//		if (datagramSocket != null) {
//			datagramSocket.close();
//		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		DatagramPacket datagramPacket;
		listening = true;
		while (listening) {
			try {
				datagramPacket = new DatagramPacket(new byte[HTTPXConstants.UDP_PACKET_LENGTH], HTTPXConstants.UDP_PACKET_LENGTH);
				datagramSocket.receive(datagramPacket);
				
				new Thread(new ConnectionHandler(datagramPacket));
			} catch (IOException e) {
				LOGGER.debug(HTTPXConstants.HTTPU + "-Server Socket was closed.");
			}
		}
	}

	private class ConnectionHandler implements Runnable{
		private DatagramPacket packet;
		
		public ConnectionHandler(DatagramPacket packet) {
			LOGGER.debug("new " + HTTPXConstants.HTTPU + " package received");
			this.packet = packet;
			
			//Start the Thread
			new Thread(this).start();
		}

		public void run() {
			HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
			HTTPXActionMessage request = null;
			try {
				request = refactorer.createActionMessageFromStream(this.toStream(packet.getData(), packet.getLength()));
			} catch (HTTPXProtocolViolationException e) {
				LOGGER.error(e);
				e.printStackTrace();
			} catch (IOException e){
				LOGGER.error(e);
				e.printStackTrace();
			}
			request.setPort(packet.getPort());
			request.setHost(packet.getAddress().getHostName());
			
			LOGGER.debug(HTTPXConstants.HTTPU + " Request:\r\n" + request.toString());

			boolean bFound = false;
			Path tmpPath = new Path(request.getPath().getString());
			try {
				do {
					for (Tuple element : registration) {
						if ( (element.handler instanceof HTTPXPlainActionMessageHandler) &&
							  element.filter.doesPass(request.getProtocol(), request.getHost(), request.getPort(), tmpPath, request.getQueryAsString())
							){
							LOGGER.debug("void Handler was found : " + element.handler);
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
			}catch (IOException e) {
					LOGGER.error(e);
					e.printStackTrace();
				}
		}
		
		/**
		 * Convert the UDP-Data into a <code>InputStream<\code>. 
		 * 
		 * @param data			the UDP packet
		 * @param len			the length of the data
		 * @return				the <code>InputStream<\code>
		 * @throws IOException
		 */
		private InputStream toStream(byte[] data, int len) throws IOException{
			try {
				OutputStream out = new PipedOutputStream();
				InputStream in = new PipedInputStream((PipedOutputStream)out,len);
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
	}//ConnectionHandler

	public String getInetAddress() {
		return datagramSocket.getLocalAddress().toString();
	}

	public int getPort() {
		return port;
	}

	public String getProtocol() {
		return HTTPXConstants.HTTPU;
	}
	
}//class
