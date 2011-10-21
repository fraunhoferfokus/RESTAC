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
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageTransceiver;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.util.serialization.converter.TextXmlContentReader;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXStreamFactory;

/**
 * In this sample code the RESTAC framework is used to access a web service with a REST 
 * interface. The FlickrSearchServiceWrapper accesses the photo search service
 * offered by the online photo sharing application Flickr (http://www.flickr.com/).
 * For details of Flickr's photo search service API see: 
 * http://www.flickr.com/services/api/flickr.photos.search.html
 * 
 * @author Steffen Krüssel
 * @author Anna Kress
 */

public class FlickrSearchServiceWrapper {

	public FlickrSearchServiceWrapper() {
		
		// 1. Start transceiver
		HTTPXActionMessageTransceiver transceiver = HTTPXActionMessageTransceiver.getRunningInstance();

		// 2. Get outbound dispatcher
		HTTPXActionMessageDispatcher outbound = transceiver.getOutboundDispatcher();
	
		// 3. Set up request
		String host = "api.flickr.com";
		Map<String, String> header = new HashMap<String, String>();
		header.put("Host", host);
		
		int port = 80;

		Path path = new Path("services/rest"); //Flickr's REST services
		
		ParameterList query = new ParameterList();
		query.setParameter("method", "flickr.photos.search"); //Flickr's REST endpoint URL 
		query.setParameter("api_key", "insert_your_API_key_here"); //insert your Flickr API key here
		query.setParameter("tags", "snowflake"); //some search criteria
		query.setParameter("machine_tag_mode", "any"); //some search criteria

		HTTPXActionMessage request = new HTTPXActionMessage(HTTPXConstants.GET, HTTPXConstants.HTTP, 
															host, port, path, query, header, null);
		
		HTTPXStatusMessage response = null;
		
		try {
			// 4. send request
			response = outbound.deliverSynchronous(request);
		} catch (HTTPXProtocolViolationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (response == null) return;
		
		// 5. Parse response into a w3c.dom.document
		try {
			Document content = (Document) new TextXmlContentReader(
					HTTPXStreamFactory.getHTTPXInputStream(response)).readBuffered();
			
			// 6. Do something with the document...
			
		} catch (ContentConvertingException e) {
			e.printStackTrace();
		}

		return;
	}

	public static void main(String[] args) {
		new FlickrSearchServiceWrapper();
	}
}

