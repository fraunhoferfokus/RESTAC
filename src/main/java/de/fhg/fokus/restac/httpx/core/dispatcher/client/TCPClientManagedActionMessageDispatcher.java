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

/* Created on 14.04.2007 */

package de.fhg.fokus.restac.httpx.core.dispatcher.client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilterImpl;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXMessageRefactorer;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessageHandleImpl;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXManagedActionMessageDispatcher;

/**
 * TODO: Documentation
 * 
 * @author Murat Ates
 *
 */

public class TCPClientManagedActionMessageDispatcher extends HTTPXManagedActionMessageDispatcher{
	
	private final static Logger LOGGER = Logger.getLogger(TCPClientManagedActionMessageDispatcher.class);
	
	/**
	 * Constructs a new <code>TCPClientManagedActionMessageDispatcher</code> with the specified attributes.
	 * 
	 * @param dispatcher	the proxy dispatcher.
	 */
	public TCPClientManagedActionMessageDispatcher(HTTPXActionMessageDispatcher dispatcher) {
		super(dispatcher);
		
		//register the handler on the Proxydispatcher
		dispatcher.addActionMessageHandler(new HTTPXActionMessageFilterImpl(HTTPXConstants.HTTP, null, 0, null, null), this);
	}
	
	@Override
	public void start() {
		//Do Nothing
		LOGGER.info("Start: " + this.getClass().toString());
	}

	@Override
	public void shutdown() {
		LOGGER.info("Shutdown : " + this.getClass().toString());
	}

	public HTTPXStatusMessage handleSyn(HTTPXActionMessage message) throws HTTPXProtocolViolationException, IOException {
		HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
		HTTPXStatusMessage response = null;
		Socket socket = null;
		
		try {
			socket = sendRequest(message, socket);
			
			//Receive the Response
			InputStream in = socket.getInputStream();
			
			LOGGER.debug("receive the response");
			try {
				response = refactorer.createStatusMessageFromStream(in);
			} catch (HTTPXProtocolViolationException e) {
//				LOGGER.error(e);
				throw e;
			}
		} catch (IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
			
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					LOGGER.error(e);
					e1.printStackTrace();
				}
			}
			throw e;
		}
		finally {
			// Can't close the socket, the reference
			// of the socket inputStream is assigned 
			// to the response
//			try {
//				if (socket != null)
//					socket.close();
//			} catch (IOException e) {
//				LOGGER.error(e); 
//			}
		}
		
		return response;
	}
	
	public HTTPXStatusMessageHandle handleAsyn(HTTPXActionMessage message){
		HTTPXStatusMessageHandle stMsgHandle = null;
		Socket socket = null;
		try {
			socket = sendRequest(message, socket);
			
			//create a new StatusMessageHandle
			stMsgHandle = new HTTPXStatusMessageHandleImpl();
			new ConnectionHandler(socket, stMsgHandle);
		} catch (IOException e) {
			LOGGER.debug(e);
			e.printStackTrace();
		}
		return stMsgHandle;
	}
	
	public void handlePlain(HTTPXActionMessage request) {
		LOGGER.debug("No Effect");
	}
	
//	 Process the asynchronous message handling
	class ConnectionHandler implements Runnable{
		private Socket socket;
		private HTTPXStatusMessageHandle handle;
		
		private ConnectionHandler(Socket socket, HTTPXStatusMessageHandle handle){
			this.socket = socket;
			this.handle = handle;
			
			//start the Thread
			new Thread(this).start();
		}

		public void run() {
			HTTPXStatusMessage response = null;
			HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
			
			try {
				InputStream in = socket.getInputStream();
				try {
					response = refactorer.createStatusMessageFromStream(in);
				} catch (HTTPXProtocolViolationException e) {
					LOGGER.debug(e);
					e.printStackTrace();
					response = null;
				}
				handle.getStatusMessageHandler().handle(response);
			} catch (IOException e) {
				LOGGER.debug(e);
				e.printStackTrace();
			} finally{
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) 	{
						LOGGER.debug(e);
						e.printStackTrace();
					}
				}
			}
		}//run
	}//ConnectionHandler
	
	public String getInetAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getProtocol() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Initialize the socket and send out the request.
	 * 
	 * @param message	the <code>HTTPXActionMessage</code>
	 * @param socket	
	 * @return			the initialized socket.
	 * @throws IOException
	 */
	private Socket sendRequest(HTTPXActionMessage message, Socket socket) throws IOException{
		HTTPXMessageRefactorer refactorer = new HTTPXMessageRefactorer();
		OutputStream out = null;
		InputStream inStreamMessage = null;
		InetAddress host = InetAddress.getByName(message.getHost());
		
		socket = new Socket(host, message.getPort());
		out = new BufferedOutputStream(socket.getOutputStream());
		inStreamMessage = refactorer.toStream(message);
		
		byte[] buffer = new byte[1024];
		int readBytes;
		
		//shift the stream
		while ((readBytes = inStreamMessage.read(buffer)) != -1) {
			out.write(buffer, 0, readBytes);
		}
		//flush the stream
		out.flush();
		LOGGER.debug("send out the request");
		
		//return the initialized socket
		return socket;
	}
	
}//TCPClientManagedActionMessageDispatcher
