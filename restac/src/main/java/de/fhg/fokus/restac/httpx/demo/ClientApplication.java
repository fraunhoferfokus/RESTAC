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

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandler;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;

/**
 * @author Murat Ates
 * Just sample code to demonstrate how a plain / sync / async client is implemented.
 */
public class ClientApplication implements HTTPXStatusMessageHandler{
	
	private final static Logger LOGGER = Logger.getLogger(ClientApplication.class);
	
	private HTTPXActionMessage request;
	private HTTPXActionMessageDispatcher outboundProxy;
	
	public ClientApplication(HTTPXActionMessageDispatcher outboundProxy, HTTPXActionMessage request) {
		this.outboundProxy = outboundProxy;
		this.request = request;
	}
	
	public void sendAsync()throws IOException{
		HTTPXStatusMessageHandle hdl = outboundProxy.deliverAsynchronous(request);
		if (hdl != null) {
			hdl.addStatusMessageHandler(this);
		}else
			LOGGER.debug("Handler is NULL /n Can't register !");
	}
	
	public void sendSync(){
		HTTPXStatusMessage resp = null;
		try {
			if (request.getProtocol() == HTTPXConstants.HTTP) {
				resp = outboundProxy.deliverSynchronous(request);
				
			}else if (request.getProtocol() == HTTPXConstants.HTTPU || request.getProtocol() == HTTPXConstants.HTTPMU) {
				outboundProxy.deliverPlain(request);
			}
		} catch (HTTPXProtocolViolationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error(e);
		}
		
		if (resp != null) {
			LOGGER.debug(resp.toString());
			
		}
	}
	
	public void sendPlain() throws IOException{
		LOGGER.debug("Send Plain");
		outboundProxy.deliverPlain(request);
		
	}
	
	public void handle(HTTPXStatusMessage message) {
		if (message != null) {
			LOGGER.debug(message.toString());
		}
	}
}
