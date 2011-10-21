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

package de.fhg.fokus.restac.resource.core.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPStatus;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.core.communication.handler.HTTPXSynActionMessageHandler;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXInputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXOutputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXStreamFactory;
import de.fhg.fokus.restac.resource.core.common.Delete;
import de.fhg.fokus.restac.resource.core.common.Get;
import de.fhg.fokus.restac.resource.core.common.Head;
import de.fhg.fokus.restac.resource.core.common.Node;
import de.fhg.fokus.restac.resource.core.common.Post;
import de.fhg.fokus.restac.resource.core.common.Put;
import de.fhg.fokus.restac.resource.core.common.Resource;
import de.fhg.fokus.restac.resource.core.common.exceptions.AccessForbiddenException;
import de.fhg.fokus.restac.resource.core.common.exceptions.ResourceException;

/**
 * An abstract synchronous message handler that allows to process requests on a resource tree structure.
 * (For a sample implementation see MyResourceMessageHandler in package de.fhg.fokus.restac.resource.demo)
 * 
 * Last changed: 01.06.07 by Anna Kress
 * 
 * @see HTTPXSynActionMessageHandler
 * @see Node
 */
public abstract class ResourceMessageHandler implements HTTPXSynActionMessageHandler, Node {
	
	private final static Logger LOGGER = Logger.getLogger(ResourceMessageHandler.class);
	
	/** The filter of this inbound handler */
	protected HTTPXActionMessageFilterResourceImpl filter = null;

	/** The inbound dispatcher where this handler is registered. */
	protected HTTPXActionMessageDispatcher inboundDispatcher = null;
	
	/**
	 * Constructs a new resource inbound handler with given filter.
	 * 
	 * @param filter - the action message filter for this handler
	 */
	public ResourceMessageHandler (HTTPXActionMessageFilterResourceImpl filter){
		this.filter = filter;
	}

	/**
	 * Registers inbound handler with the given inbound dispatcher.
	 * 
	 * @param dispatcher - the inbound dispatcher
	 */
	public void register(HTTPXActionMessageDispatcher inboundDispatcher) {
		
		if(this.inboundDispatcher != null){
			LOGGER.warn("Handler already registered with an inbound dispatcher!");
			return;
		}
		this.inboundDispatcher = inboundDispatcher;
		this.inboundDispatcher.addActionMessageHandler(this.filter, this);
	}	

	/**
	 * Unregisters inbound handler.
	 * 
	 */
	public void unregister() {
		this.inboundDispatcher.deleteActionMessageHandler(filter, this);
	}
	
	/**
	 * Services a request on a resource tree structure.
	 * 
	 * @param request -	the request to be processed
	 * @return status message - message to be send as response to request
	 */
	public HTTPXStatusMessage handleSyn(HTTPXActionMessage request) {

		Node node = this;

		// get child node of resource tree according to the path
		Path path = request.getPath();
		path = path.subtractPath(new Path(this.filter.getPath())); // remove root path
		 
		while (path.hasNext() && node != null){
			path.next();
			node = node.getChild(path.getCurrentToken()); 
		}
		
		// get response 
		HTTPStatus responseStatus = null;		
		HTTPXOutputStream response = null;
		
		Map<String, String> responseHeaders = new HashMap<String, String>();
		
		if (node == null){
			responseStatus = HTTPStatus.NOT_FOUND;
		} else {
			Resource resource = node.getResource(request.getQuery());
			if (resource != null){
				try{
					if (request.getMethod().equals(HTTPXConstants.GET) && resource instanceof Get){

						try {
							response = ((Get)resource).get();
							
							if (response == null)
								responseStatus = HTTPStatus.NO_CONTENT;
							else {
								//set response status
								responseStatus = HTTPStatus.OK;
								//build headers
							}
						}
						catch(AccessForbiddenException e) {
							responseStatus = HTTPStatus.FORBIDDEN;
						}

					} else if (request.getMethod().equals(HTTPXConstants.PUT) && resource instanceof Put) {
						HTTPXInputStream requestStream;

							requestStream = HTTPXStreamFactory.getHTTPXInputStream(request);
							
							((Put)resource).put(requestStream);
								responseStatus = HTTPStatus.OK;

					} else if (request.getMethod().equals(HTTPXConstants.POST) && resource instanceof Post) {
						try {
							HTTPXInputStream requestStream;

								requestStream = HTTPXStreamFactory.getHTTPXInputStream(request);
		
							response = ((Post)resource).post(requestStream);

							if (response == null)
								responseStatus = HTTPStatus.NO_CONTENT; // A 204 response MUST not have body. 
							else {
								responseStatus = HTTPStatus.OK;
							}
						}
						catch(AccessForbiddenException e) {
							responseStatus = HTTPStatus.FORBIDDEN;
						}
						catch (NumberFormatException e) {
							responseStatus = HTTPStatus.NO_CONTENT;					
						}

					} else if (request.getMethod().equals(HTTPXConstants.HEAD) && resource instanceof Head) {
						((Head)resource).head(); 
						responseStatus = HTTPStatus.OK;
					} else if (request.getMethod().equals(HTTPXConstants.DELETE) && resource instanceof Delete) {
						((Delete)resource).delete();
						responseStatus = HTTPStatus.OK;
					}else {
						// header field 'Allowed' must be set, when status code 405
						Class<?>[] interfaces = resource.getClass().getInterfaces(); // get implemented inferfaces
						String allowedMethods = null;
						for(int i = 0; i < interfaces.length; i++) {
							if(allowedMethods != null) allowedMethods += ",";
							else allowedMethods = "";
							String interf = interfaces[i].getName().substring(interfaces[i].getName().lastIndexOf('.') + 1).toUpperCase();
							if(interf.equals(HTTPXConstants.GET)   ||
							   interf.equals(HTTPXConstants.PUT)   ||
							   interf.equals(HTTPXConstants.HEAD)  ||
							   interf.equals(HTTPXConstants.POST)  ||
							   interf.equals(HTTPXConstants.DELETE)
							   ) {
								allowedMethods += interf;
							}
						}
						responseStatus = HTTPStatus.METHOD_NOT_ALLOWED;
						responseHeaders.put(HTTPXConstants.ALLOW, allowedMethods);
					} 
				} catch (ResourceException e) {
					responseStatus = e.getStatus();
				}
				
			} else {
				responseStatus = HTTPStatus.NOT_FOUND;
			}
		}

		LOGGER.debug("Responding to request.");

		if(response != null)
			return response.constructHTTPXStatusMessage(responseStatus.getCode(), responseStatus.getMessage(), HTTPXConstants.HTTP, responseHeaders);
		else
			return new HTTPXStatusMessage(responseStatus.getCode(), responseStatus.getMessage(), HTTPXConstants.HTTP, responseHeaders, null);
		 
	}
	
	/**
	 * Returns <code>null</code> as this node has no parent ;o).
	 * 
	 * @return	<code>null</code>
	 */
	public Node getParent() {
		return null;
	}
	
}
