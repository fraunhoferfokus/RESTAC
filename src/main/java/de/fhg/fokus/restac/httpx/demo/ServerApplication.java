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

package de.fhg.fokus.restac.httpx.demo;

import java.io.IOException;

import de.fhg.fokus.restac.httpx.core.common.HTTPStatus;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessageHandleImpl;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXPlainActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXSynActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;


/**
 * @author Murat Ates
 * Just sample code to demonstrate how a plain / sync / async handler is implemented.
 */
public class ServerApplication implements HTTPXPlainActionMessageHandler, HTTPXSynActionMessageHandler{

	private HTTPXActionMessageFilter filter;
	HTTPXStatusMessage response = new HTTPXStatusMessage();//for async handler

	/**
	 * Contructs a new <code>ServerApplication</code>
	 */
	public ServerApplication(HTTPXActionMessageDispatcher dispatcher, HTTPXActionMessageFilter filter){
		this.filter = filter;
		dispatcher.addActionMessageHandler(this.filter, this);
	}
	
	public HTTPXStatusMessage handleSyn(final HTTPXActionMessage request) throws HTTPXProtocolViolationException, IOException {

		//do something with the request...
		
		//TODO
		
		// do something with the response...	
		HTTPXStatusMessage res = new HTTPXStatusMessage();

		//TODO
		
		return res;
	}

	public HTTPXStatusMessageHandle handleAsyn(HTTPXActionMessage request) {

		HTTPXStatusMessageHandle stMsgHandle = new HTTPXStatusMessageHandleImpl();
		
		// remember unique_id of request, use it to set header of response
		// so that TCPServerManagedActionMessageDispatcher can map the id to correct socket
		String responseID = request.getHeader(HTTPXConstants.UNIQUE_ID);
		response.setHeader(HTTPXConstants.UNIQUE_ID, responseID);

		createAsyncResponse();//execute this in a new thread to create an response asynchronously
		
		return stMsgHandle;
	}
	
	public HTTPXStatusMessage createAsyncResponse(){

		//TODO: fill the response

		return this.response;
	}

	public void handlePlain(HTTPXActionMessage request) throws IOException {
		// do something with the request...
		//TODO
		
	}
}
