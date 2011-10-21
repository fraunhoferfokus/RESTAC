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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageTransceiver;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentReader;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentWriter;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXInputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXOutputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXPlainOutputStream;
import de.fhg.fokus.restac.resource.core.client.NodeProxy;
import de.fhg.fokus.restac.resource.core.client.ResourceProxy;
import de.fhg.fokus.restac.resource.core.common.exceptions.ResourceException;

public class ResourceProxyClientTestClass {
	private final static Logger LOGGER = Logger.getLogger(ClientTestClass.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		HTTPXActionMessageTransceiver transceiver = HTTPXActionMessageTransceiver.getInstance();
		transceiver.start("/transceiver.properties");

		HTTPXActionMessageDispatcher outboundProxy = transceiver.getOutboundDispatcher();

		//construct message

		Map<String, String> numbers = new HashMap<String, String>();
		
		int i = 0;
		while(i<50) {
			String nextNr = "number" + i;
			numbers.put(nextNr, "1");
			i++;
		}
		
		List<String> path = new ArrayList<String>();
		path.add("math");
		path.add("add");
		
		HTTPXOutputStream out;
		
		Map<String, String> result;
		
		try {
			
			out = new HTTPXPlainOutputStream(HTTPXConstants.TYPE_APP_URLENCODED, HTTPXConstants.DEFAULT_CHARSET);
			
			UrlEncodedContentWriter wr = new UrlEncodedContentWriter(out);
			
			wr.writeBuffered(numbers);
			
			URL url = null;
			
			try {
				url = new URL("http://127.0.0.1:2048/math/add");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			NodeProxy nodeProxy = new NodeProxy(url);

			ResourceProxy resProxy = nodeProxy.getResource(null);
			
			HTTPXInputStream in;
			
			try {
				
				in = resProxy.post(out);
				
				if(in != null && in.getContentType().equals(HTTPXConstants.TYPE_APP_URLENCODED)) {
					
					// read and parse content of stream:
					
					UrlEncodedContentReader rd = new UrlEncodedContentReader(in);
					
					result = (Map<String, String>)rd.readBuffered();
					
					LOGGER.info("Printing response: " + (String)result.get("result"));
				}
				
			} catch (ResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			} catch (ContentConvertingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			catch (NumberFormatException e){
				e.printStackTrace();
			}
		
		transceiver.shutdown();
	}
	
}
