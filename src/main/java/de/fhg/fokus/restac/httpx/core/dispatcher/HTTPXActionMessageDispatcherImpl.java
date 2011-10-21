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

/* Created on 09.04.2007 */

package de.fhg.fokus.restac.httpx.core.dispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPStatus;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter;
import de.fhg.fokus.restac.httpx.core.common.HTTPXManagedActionMessageDispatcherInfo;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXAsynActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXPlainActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXSynActionMessageHandler;

/**
 * Implementation of the <code>HTTPXActionMessageDispatcher</code> interface.
 * 
 * @author Murat Ates
 *
 * @see HTTPXActionMessageDispatcher
 */
public class HTTPXActionMessageDispatcherImpl implements HTTPXActionMessageDispatcher {

	private final static Logger LOGGER = Logger.getLogger(HTTPXActionMessageDispatcherImpl.class);
	
	/* Handle registered <code>HTTPXActionMessageHandler</code> and <code>HTTPXActionMessageFilter</code> */
	private List<Tuple> registration;
	
	/**
	 * memorise registered inbound <code>HTTPXManagedActionMessageDispatcher</code>,
	 * in order to forward registered <code>HTTPXActionMessageHandler</code> objects.
	 */
	private List<HTTPXManagedActionMessageDispatcher> manageDispatcherList = new ArrayList<HTTPXManagedActionMessageDispatcher>();
	
	/**
	 * Inner Class for efficient handling a <code>HTTPXActionMessageFilter</code> and the 
	 * corresponding <code>HTTTPXActionMessageHandler</code>. 
	 * 
	 * @author Murat Ates 
	 *
	 */
	private class Tuple{
		private HTTPXActionMessageFilter filter;
		private HTTPXActionMessageHandler handler;
		
		private Tuple(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler){
			this.filter = filter;
			this.handler = handler;
		}
		
//		@Override
//		public boolean equals(Object obj){
//			boolean result = false;
//			
//			if (obj == null) {
//				result = false;
//			}else if (obj == this) {
//				result = true;
//			}else if (! obj.getClass().equals(this.getClass())) {
//				result = false;
//			}else {
//				Tuple tuple = (Tuple)obj;
//				result = this.filter.equals(tuple.filter) && this.handler.equals(tuple.handler);
//			}
//			return result;
//		}
	}
	
	/**
	 * Constructs a new <code>HTTPXActionMessageDispatcherImpl</code>.
	 *
	 */
	public HTTPXActionMessageDispatcherImpl() {
		registration = new ArrayList<Tuple>();
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#addActionMessageHandler(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter, de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXActionMessageHandler)
	 */
	public void addActionMessageHandler(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler) {
		if (filter != null && handler != null){
			registration.add(new Tuple(filter, handler));
			
			for (HTTPXManagedActionMessageDispatcher dispatcher : manageDispatcherList) {
				//forward the registration
				dispatcher.addActionMessageHandler(filter, handler);
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deleteActionMessageHandler(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter, de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXActionMessageHandler)
	 */
	public void deleteActionMessageHandler(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler) {
		if (filter != null && handler != null) {
			registration.remove(new Tuple(filter, handler));
		
			for (HTTPXManagedActionMessageDispatcher dispatcher : manageDispatcherList) {
				//forward the unregistration
				dispatcher.deleteActionMessageHandler(filter, handler);
			}
		}
	}
	
	/**
	 * Add every inbound <code>HTTPXManagedActionMessageDispatcher</code> to forward
	 * all the registered <code>HTTPXActionMessageFilter</code> and
	 * <code>HTTPXActionMessageHandler</code> objects.
	 * 
	 * @param dispatcher	the inbound dispatcher
	 */
	public void addDispatcher(HTTPXManagedActionMessageDispatcher dispatcher){
		this.manageDispatcherList.add(dispatcher);
	}
	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deliverAsynchronous(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage)
	 */
	public HTTPXStatusMessageHandle deliverAsynchronous(HTTPXActionMessage message)throws IOException {
		HTTPXStatusMessageHandle stHandle = null;
		boolean bFound = false;
		
		Path tmpPath = new Path(message.getPath().getString());
		do{
			for (Tuple element : registration) {
				if ( (element.handler instanceof HTTPXAsynActionMessageHandler) &&
					 (element.filter.doesPass(message.getProtocol(), message.getHost(), message.getPort(), tmpPath, message.getQueryAsString()))
					){
					LOGGER.debug("Async handler was found " + element.handler);
					stHandle = ((HTTPXAsynActionMessageHandler)element.handler).handleAsyn(message);
					bFound = true;
					break;
				}
			}
			if (!bFound) {
				String strTmp = tmpPath.getString();
				tmpPath.removeLastToken();
				//check wheter path is root
				if (strTmp.equalsIgnoreCase(tmpPath.getString())) {
					break;
				}
			}
		} while(!bFound);
		
		if (!bFound) {
			LOGGER.info("No Handler was found");
		}
		
		return stHandle;
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deliverSynchronous(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage)
	 */
	public HTTPXStatusMessage deliverSynchronous(HTTPXActionMessage message) throws HTTPXProtocolViolationException, IOException {
		//if no handler was found
		HTTPXStatusMessage status = new HTTPXStatusMessage(HTTPStatus.NOT_FOUND.getCode(), HTTPStatus.NOT_FOUND.getMessage(), 
															message.getProtocol(), null, null);
		boolean bFound = false;
		Path tmpPath = new Path(message.getPath().getString());
		do {
			for (Tuple element : registration) {
				if ( element.handler instanceof HTTPXSynActionMessageHandler	&& 
					 element.filter.doesPass(message.getProtocol(), message.getHost(), message.getPort(), tmpPath, message.getQueryAsString())
					){
					LOGGER.debug("Sync handler was found " + element.handler);
					status = ((HTTPXSynActionMessageHandler)element.handler).handleSyn(message);
					bFound = true;
					break;
				}
			}
			if (!bFound) {
				String strTmp = tmpPath.getString();
				tmpPath.removeLastToken();
				//check wheter path is root
				if (strTmp.equalsIgnoreCase(tmpPath.getString())) {
					break;
				}
			}
		} while (!bFound);
		
		if (!bFound) {
			LOGGER.info("No Handler was found");
		}
		return status;
	}
	
	/**
	 * (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deliverPlain(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage)
	 */
	public void deliverPlain(HTTPXActionMessage message) throws IOException{
		boolean bFound = false;
		
		Path tmpPath = new Path(message.getPath().getString());
		do {
			for (Tuple element : registration) {
				if ( (element.handler instanceof HTTPXPlainActionMessageHandler) &&
					 (element.filter.doesPass(message.getProtocol(), message.getHost(), message.getPort(), tmpPath, message.getQueryAsString()))
					){
					LOGGER.debug("Plain handler was found");
					LOGGER.debug(element.handler);
					((HTTPXPlainActionMessageHandler)element.handler).handlePlain(message);
					bFound = true;
					break;
				}
			}
			if (!bFound) {
				String strTmp = tmpPath.getString();
				tmpPath.removeLastToken();
				//check wheter path is root
				if (strTmp.equalsIgnoreCase(tmpPath.getString())) {
					break;
				}
			}
		} while (!bFound);
		
		if (!bFound) {
			LOGGER.info("No Handler was found");
		}		
	}

	/**
	 * (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#getManagedDispatcherList()
	 */
	public List<HTTPXManagedActionMessageDispatcherInfo> getManagedDispatcherList() {
		List<HTTPXManagedActionMessageDispatcherInfo> resultList = new ArrayList<HTTPXManagedActionMessageDispatcherInfo>();
		
		for (HTTPXManagedActionMessageDispatcher list : manageDispatcherList) {
			HTTPXManagedActionMessageDispatcherInfo tmp = (HTTPXManagedActionMessageDispatcherInfo)list;
			resultList.add(tmp);
		}
		return resultList;
	}
	
	
}
