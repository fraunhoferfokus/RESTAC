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
import java.util.List;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageFilter;
import de.fhg.fokus.restac.httpx.core.common.HTTPXManagedActionMessageDispatcherInfo;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXStatusMessageHandle;

/**
 * The Interface defines methods to deliver synchronous, asynchronous and plain
 * messages (e.g. UDP unicast/multicast ).
 * 
 * @author Murat Ates
 * @see HTTPXActionMessageDispatcherImpl
 * @see HTTPXManagedActionMessageDispatcher
 * @see HTTPXManagedActionMessageDispatcherInfo
 * 
 */
public interface HTTPXActionMessageDispatcher {

	/**
	 * Deliver messages synchronous.
	 * 
	 * @param message	the request message	
	 * @return			the response messge
	 * @throws HTTPXProtocolViolationException
	 * @throws IOException
	 */
	public HTTPXStatusMessage deliverSynchronous(HTTPXActionMessage message) throws HTTPXProtocolViolationException, IOException;
	
	/**
	 * Deliver messages asynchronous.
	 * 
	 * @param message	the request message
	 * @return			the response handle, where the application
	 * 					registeres the handler interface.
	 * @throws IOException
	 */
	public HTTPXStatusMessageHandle deliverAsynchronous(HTTPXActionMessage message) throws IOException;
	
	/**
	 * Deliver messages without a response (e.g. UDP unicast/multicast).
	 * 
	 * @param message	the request message
	 * @throws IOException
	 */
	public void deliverPlain(HTTPXActionMessage message) throws IOException;
	
	/**
	 * Register a message filter with a handler.
	 * 
	 * @param filter	the message filter
	 * @param handler	the appropriate handler
	 */
	public void addActionMessageHandler(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler);
	
	/**
	 * Unregister a message filter with a handler.
	 * 
	 * @param filter	the message filter
	 * @param handler	the appopriata handler
	 */
	public void deleteActionMessageHandler(HTTPXActionMessageFilter filter, HTTPXActionMessageHandler handler);
	
	/**
	 * Returns a <code>List</code> of registered <code>HTTPXManagedActionMessageDispatcher</code> with
	 * information tags about this.
	 * 
	 * @return	a <code>List</code> ManagedDispatcher
	 */
	public List<HTTPXManagedActionMessageDispatcherInfo> getManagedDispatcherList();
}
