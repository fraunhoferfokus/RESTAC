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

/* Created on 15.04.2007 */

package de.fhg.fokus.restac.httpx.core.dispatcher.server;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPStatus;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXMessageRefactorer;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXAsynActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXSynActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXManagedActionMessageDispatcher;

/**
 * TODO: Documentation
 * 
 * @author Murat Ates
 *
 */
public class TCPServerManagedActionMessageDispatcher extends HTTPXManagedActionMessageDispatcher implements Runnable, HTTPXStatusMessageHandler{

	private final static Logger LOGGER = Logger.getLogger(TCPServerManagedActionMessageDispatcher.class);
	private final static int DEFAULT_PORT = 2048;
	
	/** The Serversocket for the TCP communication */
	private ServerSocket srvSocket;
	
	/** The Port for the TCP communication */
	private int port = DEFAULT_PORT;
	
	private boolean listening;
	
	/* List of MessageID's */
	private List<MessageID> mapID;
	
	/* save the id, socket tupel, to find the corresponding socket */
	class MessageID{
		private String id;
		private Socket socket;
		
		private MessageID(String id, Socket socket){
			this.id = id;
			this.socket = socket;
		}
	}
	
	/**
	 *  Constructs a new <code>TCPServerManagedActionMessageDispatcher</code> with the specified attributes.
	 * 
	 * @param dispatcher	the proxy dispatcher.
	 */
	public TCPServerManagedActionMessageDispatcher(HTTPXActionMessageDispatcher dispatcher) {
		super(dispatcher);
		mapID = new ArrayList<MessageID>();
		
		//do nothing with the dispatcher !
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.HTTPXManagedActionMessageDispatcher#start()
	 */
	@Override
	public void start() {
		LOGGER.info("Start: " + this.getClass().toString());
		
		while (srvSocket == null) {
			try {
				srvSocket = new ServerSocket(port);//ServerSocket(port, -1, InetAddress.getLocalHost());
				port = srvSocket.getLocalPort();
				LOGGER.info("new TCP-Socket on Port: " + port);
				new Thread(this).start();
			} catch (IOException e) {
//				LOGGER.error(port + " : " + e);
//				e.printStackTrace();
				port = 0;
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.HTTPXManagedActionMessageDispatcher#shutdown()
	 */
	@Override
	public void shutdown() {
		LOGGER.info("Shutdown: " + this.getClass().toString());
		
		listening = false;
//		new Thread(this).interrupt();
		if (srvSocket != null) {
			try {
				srvSocket.close();
			} catch (IOException e) {
				LOGGER.error(e);
				e.printStackTrace();
			}
		}
	}
	
	public HTTPXStatusMessage handleSyn(HTTPXActionMessage request) throws HTTPXProtocolViolationException, IOException{
		LOGGER.debug("No Effect");
		return null;
	}

	public HTTPXStatusMessageHandle handleAsyn(HTTPXActionMessage request) throws IOException{
		LOGGER.debug("No Effect");
		return null;
	}
	
	public void handlePlain(HTTPXActionMessage request) {
		LOGGER.debug("No Effect");
	}
	
	public void handle(HTTPXStatusMessage message) throws IOException{
		String uid = message.getHeader(HTTPXConstants.UNIQUE_ID);
		
		if (uid != null && !uid.equals("")) {
			for (ListIterator<MessageID> iter = mapID.listIterator(); iter.hasNext();) {
				MessageID element = iter.next();
				if (element.id.equalsIgnoreCase(uid)) {
					LOGGER.debug("ID with corresponding Socket was found");
					try {
						sendResponse(element.socket, message);
					} finally{
						iter.remove();
						element.socket.close();
					}
					break;
				}
			}
			LOGGER.debug("There is no socket for the " + HTTPXConstants.UNIQUE_ID + "-Header : " + uid);
			
		} else {
			LOGGER.debug("There is no " + HTTPXConstants.UNIQUE_ID + "-Header in the message");
			LOGGER.debug("No socket to send out");
		}
	}

	/**
	 * Send out the response.
	 * 
	 * @param socket		
	 * @param response
	 * @throws IOException
	 */
	private void sendResponse(Socket socket, HTTPXStatusMessage response) throws IOException{
		HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
		
		InputStream in = refactorer.toStream(response);
		OutputStream out = new BufferedOutputStream(socket.getOutputStream()); 

		byte[] buffer = new byte[1024];
		int readBytes;
		while ((readBytes = in.read(buffer)) != -1) {
			out.write(buffer, 0, readBytes);
		}
		//flush the stream
		out.flush();
	}
	
	public void run() {
		listening = true;
		while (listening) {
			try {
				new ConnectionHandler(srvSocket.accept());
			} catch (IOException e) {
				LOGGER.debug(HTTPXConstants.HTTP + "-Server Socket was closed.");
			}
		}
	}
	
	/**
	 * Inner class to handle the request.
	 * 
	 * @author Murat Ates
	 *
	 */
	private class ConnectionHandler implements Runnable{
		private Socket socket;
		
		public ConnectionHandler(Socket socket){
			LOGGER.debug("new tcp-socket accepted");
			this.socket = socket;
			
			//Start the Thread
			Thread t = new Thread(this);
			t.setName(TCPServerManagedActionMessageDispatcher.class.toString() + ": " + this.getClass().toString());
			t.start();
		}

		public void run() {
			HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
			HTTPXActionMessage request = null;
			HTTPXStatusMessage response = new HTTPXStatusMessage(HTTPStatus.NOT_FOUND.getCode(), HTTPStatus.NOT_FOUND.getMessage(), HTTPXConstants.HTTP, null, null);
			boolean bFound = false;
			boolean bError = false;
			
			try {
				request = refactorer.createActionMessageFromStream(socket.getInputStream());

//				TODO: overwrite port and host with the socket information 
				request.setPort(socket.getLocalPort());
				request.setHost(socket.getInetAddress().getHostName());
				
				LOGGER.debug("Request:\r\n" + request.toString());
				
				Path tmpPath = new Path(request.getPath().getString());
				do {
					for (Tuple element : registration) {		
						if (element.filter.doesPass(request.getProtocol(), request.getHost(), request.getPort(), tmpPath, request.getQueryAsString()))
						{	
							if(element.handler instanceof HTTPXSynActionMessageHandler){
								LOGGER.debug("synchronous Handler was found: " + element.handler);

								response = ((HTTPXSynActionMessageHandler)element.handler).handleSyn(request);
								//send the response
								TCPServerManagedActionMessageDispatcher.this.sendResponse(socket, response);									
								
								bFound = true;
								break;
								
							} else if (element.handler instanceof HTTPXAsynActionMessageHandler) {
								LOGGER.debug("asynchronous Handler was found");
								//create a unique-ID
								String uid = new UID().toString();
								request.setHeader(HTTPXConstants.UNIQUE_ID, uid );
								
								HTTPXStatusMessageHandle statusMsgHdle;

								statusMsgHdle = ((HTTPXAsynActionMessageHandler)element.handler).handleAsyn(request);
								//register the handler
								statusMsgHdle.addStatusMessageHandler(TCPServerManagedActionMessageDispatcher.this);
								mapID.add(new MessageID(uid, socket));
								bFound = true;
								break;
							}
						}
					}//for
					
					//TODO: change for a better alternative
					if (!bFound) {
						String strTmp = tmpPath.getString();
						tmpPath.removeLastToken();
						//check wheter path is root
						if (strTmp.equalsIgnoreCase(tmpPath.getString())) {
							break;
						}
					}
				} while (!bFound);
				
//			} catch (HTTPXProtocolViolationException e) {
//				LOGGER.error(e);
//				e.printStackTrace();
//				//create a "400 Bad Request"
//				response = new HTTPXStatusMessage(HTTPStatus.BAD_REQUEST.getCode(), HTTPStatus.BAD_REQUEST.getMessage(), HTTPXConstants.HTTP, null, null);
//			} catch (IOException e) {
//				LOGGER.error(e);
//				e.printStackTrace();
//				//create a "400 Bad Request"
//				response = new HTTPXStatusMessage(HTTPStatus.BAD_REQUEST.getCode(), HTTPStatus.BAD_REQUEST.getMessage(), HTTPXConstants.HTTP, null, null);
			} catch (Exception e) {
				LOGGER.error(e);
				e.printStackTrace();
				//create a "400 Bad Request"
				response = new HTTPXStatusMessage(HTTPStatus.BAD_REQUEST.getCode(), HTTPStatus.BAD_REQUEST.getMessage(), HTTPXConstants.HTTP, null, null);
				bError = true;
			}
				
			if (!bFound || bError) {
				LOGGER.debug("no handler was found");
				try {
					//send a "404 Not Found" / "400 Bad Request" response
					TCPServerManagedActionMessageDispatcher.this.sendResponse(socket, response);
				} catch (IOException e) {
					LOGGER.error(e);
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						LOGGER.error(e);
						e.printStackTrace();
					}
				}
			}
				
		}//run
		
	}//ConnectionHandler

	public String getInetAddress() {
		return this.srvSocket.getInetAddress().getHostAddress();
	}

	public int getPort() {
		return this.port;
	}

	public String getProtocol() {
		return HTTPXConstants.HTTP;
	}
}
