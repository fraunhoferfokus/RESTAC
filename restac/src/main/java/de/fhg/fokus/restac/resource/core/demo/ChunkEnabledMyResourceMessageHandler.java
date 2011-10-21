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

package de.fhg.fokus.restac.resource.core.demo;

import java.util.HashMap;
import java.util.Map;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageTransceiver;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentReader;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentWriter;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXChunkedOutputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXInputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXOutputStream;
import de.fhg.fokus.restac.resource.core.common.Node;
import de.fhg.fokus.restac.resource.core.common.Post;
import de.fhg.fokus.restac.resource.core.common.Resource;
import de.fhg.fokus.restac.resource.core.common.exceptions.ResourceException;
import de.fhg.fokus.restac.resource.core.common.exceptions.UnsupportedContentTypeException;
import de.fhg.fokus.restac.resource.core.handler.HTTPXActionMessageFilterResourceImpl;
import de.fhg.fokus.restac.resource.core.handler.ResourceMessageHandler;

/**
 * Sample implementation of the ResourceMessageHandler. ChunkEnabledMyResourceMessageHandler
 * is registered at root path "math" and manages HTTP POST requests to the resource "add".
 * The resource "add" reads the content of the request (url-enconded numbers)
 * and returns the sum of the numbers to the requester. Handler sends response
 * using chunked encoding!
 * 
 * Last changed: 01.06.07 by Anna Kress
 */
public class ChunkEnabledMyResourceMessageHandler extends
		ResourceMessageHandler {

	public ChunkEnabledMyResourceMessageHandler(HTTPXActionMessageFilterResourceImpl filter) {
		super(filter);
	}
	
	// Traversing departing from the root node to the node representing
	// <rootPath>/add
	public Node getChild(String name) {
		if (name.equals("add"))
			return new MathNode();
		return null;
	}

	// There is supposed to be no resource connected to the path
	// <rootPath>
	public Resource getResource(ParameterList query) {
		return null;
	}

	// Node, on which the path <rootPath>/add is mapped
	public class MathNode implements Node {

		// Returns the parent node
		public Node getParent() {
			return ChunkEnabledMyResourceMessageHandler.this;
		}

		// There shall be no nodes for further path segments
		public Node getChild(String name) {
			return null;
		}

		// Returns the resource connected to the path <rootPath>/add
		public Resource getResource(ParameterList query) {
			return new MathResource();
		}
	}

	public class MathResource implements Resource, Post {

		// Defines the URI of the resource to have the form
		// „<rootPath>/add“
		public String getUniformIdentifier() {
			return filter.getPath() + "/add";
		}

		// A request with HTTP POST shall result in an addition, all
		// other methods shall not be supported
		public HTTPXOutputStream post(HTTPXInputStream in) throws ResourceException {
			
			HTTPXInputStream inStream = in;
			
			HTTPXChunkedOutputStream responseStream = null;
			
			if(!inStream.getContentType().equals(HTTPXConstants.TYPE_APP_URLENCODED))
				throw new UnsupportedContentTypeException("Content Type not supported.");
				
			// get content and do adding stuff for values contained in content
			Map<String, String> content;
			
			try {
				UrlEncodedContentReader rd = new UrlEncodedContentReader(in);

				content = (Map<String, String>)rd.readBuffered();
				
				Integer result = new Integer(0);
				
				for ( String number : content.values() ) 
				{
					result += new Integer(number);
				}
				
				// build response
				Map<String, String> res = new HashMap<String, String>();
				res.put("result", result.toString());
				
				responseStream = new HTTPXChunkedOutputStream(HTTPXConstants.TYPE_APP_URLENCODED, HTTPXConstants.DEFAULT_CHARSET);

				UrlEncodedContentWriter wr = new UrlEncodedContentWriter(responseStream);
				
				wr.writeBuffered(res);
		
			} catch (ContentConvertingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return responseStream;
		}
	}
	
	public static void main(String[] args) {
		
		// get inbound dispatcher where handler will be registered
		HTTPXActionMessageTransceiver transceiver = HTTPXActionMessageTransceiver.getInstance();
		transceiver.start("/transceiver.properties");
		HTTPXActionMessageDispatcher inboundDispatcher = transceiver.getInboundDispatcher();
		
		// handler will be registered with root path "math"
		ChunkEnabledMyResourceMessageHandler myHandler = new ChunkEnabledMyResourceMessageHandler
												(new HTTPXActionMessageFilterResourceImpl(HTTPXConstants.HTTP, null, 0, "/math", null));	
		
		// register new handler
		myHandler.register(inboundDispatcher);	
	}
}
