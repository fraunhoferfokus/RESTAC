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

/**
 * 
 */
package de.fhg.fokus.restac.httpx.core.common;

import org.apache.log4j.Logger;

/**
 * @author Murat Ates
 *
 */
public class HTTPXActionMessageFilterImpl implements HTTPXActionMessageFilter {
private final static Logger LOGGER = Logger.getLogger(HTTPXActionMessageFilter.class);
	
	private String protocol;
	private String host;
	private int port;
	private String path;
	private String query;
	
	/**
	 * Constructs a new <code>HTTPXActionMessageFilter</code>.
	 */
	private HTTPXActionMessageFilterImpl(){
		this(null, null, 0, null, null);
	}
	
	/**
	 * Constructs a new <code>HTTPXActionMessageFilter</code> with the specified attributes.
	 * 
	 * @param protocol	the protocol of the filter
	 * @param host		the host of the filter
	 * @param port		the port of the filter
	 * @param path		the path of the filter
	 * @param query		the query of the filter
	 */
	public HTTPXActionMessageFilterImpl(String protocol, String host, int port, String path, String query){
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.path = path;
		this.query = query;
	}
	

	/**
	 * Compare the given arguments with the filter.
	 * 
	 * @param protocol		the protocol of the filter
	 * @param host			the host of the filter
	 * @param port			the port of the filter
	 * @param path			the path of the filter
	 * @param query			the query of the filter
	 * @return				<code>true</code> if the argument match; false otherwise
	 */
	public boolean doesPass(String protocol, String host, int port, String path, String query){
		boolean bProtocol = this.protocol == null ? true : this.protocol.equalsIgnoreCase(protocol);
		boolean bHost = this.host == null ? true : this.host.equalsIgnoreCase(host);
		boolean bPort = this.port == 0 ? true : this.port == port;
//		boolean bPath = this.path == null ? true : path.toLowerCase().startsWith(this.path.toLowerCase());
		boolean bPath = this.path == null ? true : this.path.equalsIgnoreCase(path);
		boolean bQuery = this.query == null ? true : this.query.equalsIgnoreCase(query);
		
		boolean result = bProtocol && bHost && bPort && bPath && bQuery; 
		
//		if (result) {
//			LOGGER.debug(result);
//			LOGGER.debug("comparing:\n" + 
//					"prot " + protocol + "\n" + 
//					"host " + host + "\n" +
//					"port " + port + "\n" +
//					"path " + path + "\n" + 
//					"query " + query + "\n");
//			LOGGER.debug("filter:\n" +
//					"prot " + this.protocol + "\n" + 
//					"host " + this.host + "\n" + 
//					"port " + this.port + "\n" + 
//					"path " + this.path + "\n" + 
//					"query " + this.query + "\n");
//		}
		return result;
	}
	
	
	public boolean doesPass(String protocol, String host, int port, Path path, String query){
		return this.doesPass(protocol, host, port, path.getString(), query);
	}
	
	@Override
	public String toString(){
		String protocol = this.protocol == null ? "" : this.protocol + "://";
		String host = this.host == null ? "" : this.host;
		String port = this.port == 0 ? "" :  ":" + this.port; 
		String path = this.path == null ? "" : "/" + this.path;
		String query = this.query == null ? "" : "?" + this.query;
		
		return new StringBuilder(super.toString() + "\n" + protocol + host + port + path + query).toString();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		
		if (obj == null) {
			result = false;
		}else if (obj == this) {
			result = true;
		}else if (! obj.getClass().equals(this.getClass())) {
			result = false;
		}else {
			HTTPXActionMessageFilterImpl filter = (HTTPXActionMessageFilterImpl)obj;
			result =   this.host == null ? this.host == filter.host : this.host.equalsIgnoreCase(filter.host) 
					&& this.path == null ? this.path == filter.path : this.path.equalsIgnoreCase(filter.path)
					&& this.port == filter.port
					&& this.protocol == null ? this.protocol == filter.protocol : this.protocol.equalsIgnoreCase(filter.protocol)
					&& this.query == null ? this.query == filter.query : this.query.equalsIgnoreCase(filter.query) ;
		}
		return result;
	}

	/**
	 * Returns the registered filter path.
	 * 
	 * @return	the path attribute
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the registered filter protocol.
	 * 
	 * @return	the protocol attribute
	 */
	public String getProtocol() {
		return protocol;
	}
}
