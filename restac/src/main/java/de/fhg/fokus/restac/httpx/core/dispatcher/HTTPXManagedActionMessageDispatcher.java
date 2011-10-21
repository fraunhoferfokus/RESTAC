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

/* Created on 27.03.07 */

package de.fhg.fokus.restac.httpx.core.dispatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter;
import de.fhg.fokus.restac.httpx.core.common.HTTPXManagedActionMessageDispatcherInfo;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXAsynActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXPlainActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXSynActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.dispatcher.client.TCPClientManagedActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.core.dispatcher.server.TCPServerManagedActionMessageDispatcher;

/**
 * TODO: Documentation
 * 
 * @author Murat Ates
 * @see TCPServerManagedActionMessageDispatcher
 * @see TCPClientManagedActionMessageDispatcher
 * @see UDPUServerManagedActionMessageDispatcher
 * @see UDPUClientManagedActionMessageDispatcher
 * @see UDPMUServerManagedActionMessageDispatcher
 * @see UDPMUClientManagedActionMessageDispatcher
 */
public abstract class HTTPXManagedActionMessageDispatcher implements HTTPXActionMessageDispatcher, HTTPXSynActionMessageHandler, HTTPXAsynActionMessageHandler, HTTPXPlainActionMessageHandler, HTTPXManagedActionMessageDispatcherInfo{
	
	/* <code>List</code> with all registered <code>HTTTPXActionMessageHandler</code>,
	 * <code>HTTPXActionMessageFilter</code>
	 */
	protected List<Tuple> registration;
	
	/**
	 * Inner Class to handle efficient a <code>HTTPXActionMessageFilter</code> and the 
	 * corresponding <code>HTTTPXActionMessageHandler</code>. 
	 * 
	 * @author Murat Ates 
	 */
	protected class Tuple{
		public HTTPXActionMessageFilter filter;
		public HTTPXActionMessageHandler handler;
		
		protected Tuple(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler){
			this.filter = filter;
			this.handler = handler;
		}
	}
	
	/**
	 * Constructs a new <code>HTTPXManagedActionMessageDispatcher</code> with the specified attributes.
	 * 
	 * @param dispatcher	the proxy dispatcher 
	 */
	public HTTPXManagedActionMessageDispatcher(HTTPXActionMessageDispatcher dispatcher) {
		registration = new ArrayList<Tuple>();
	}

	/**
	 * Initialize and start all registered Dispatcher.
	 *
	 */
	public abstract void start();
	
	/**
	 * Shutdown and deinitialize all registered Dispatcher.
	 *
	 */
	public abstract void shutdown();
	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#addActionMessageHandler(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter, de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXActionMessageHandler)
	 */
	public void addActionMessageHandler(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler){
		synchronized (registration) {
			if (filter != null && handler != null) {
				registration.add(new Tuple(filter, handler));
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deleteActionMessageHandler(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter, de.fhg.fokus.restac.httpx.core.communication.HTTPXActionMessageHandler)
	 */
	public void deleteActionMessageHandler(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler){
		synchronized (registration) {
			for (ListIterator<Tuple> iter = registration.listIterator(); iter.hasNext();) {
				Tuple element = iter.next();
				if (element.filter.equals(filter) && element.handler.equals(handler)) {
					iter.remove();
					break;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deliverAsynchronous(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage)
	 */
	public final HTTPXStatusMessageHandle deliverAsynchronous(HTTPXActionMessage message) throws IOException{
		return null;
	}

	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deliverSynchronous(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage)
	 */
	public final HTTPXStatusMessage deliverSynchronous(HTTPXActionMessage message) throws HTTPXProtocolViolationException, IOException{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#deliverPlain(de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage)
	 */
	public final void deliverPlain(HTTPXActionMessage message) throws IOException{
		
	}
	
	/*(non-Javadoc)
	 * @see de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher#getManagedDispatcherList()
	 */
	public final List<HTTPXManagedActionMessageDispatcherInfo> getManagedDispatcherList(){
		return null;
	}
}
