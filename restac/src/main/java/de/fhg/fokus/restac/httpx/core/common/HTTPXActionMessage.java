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

/* Created on 18.03.2007 */

package de.fhg.fokus.restac.httpx.core.common;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Stores an HTTP Request as defined in RFC 2616. Subclass of <code>HTTPXAbstractMessage</code>.
 * 
 * @author Murat Ates
 * @see <a href="http://www.ietf.org/rfc/rfc2616.txt" target="_blank">RFC 2616</a>
 *
 */
public class HTTPXActionMessage extends HTTPXAbstractMessage {
	private final static int DEFAULT_PORT = 80;
	
	/**
	 * The method of the HTTP request (<code>GET</code>, <code>HEAD</code>, <code>POST</code>,
	 * <code>PUT</code> or <code>DELETE</code>).
	 */
	private String method;
	
	/** The host of the requested resource. */
	private String host;
	
	/** The path of the requested resource. */
	private Path path;
	

	
	/** The query string which is passed to the resource  */
	//TODO: Check keys without values
	private ParameterList query;
	
	/** The port number of the host of the requested resource. */
	private int port;
	
	/**
	 *	Constructs a new <code>HTTPXActionMessage</code> 
	 */
	public HTTPXActionMessage(){
		this(null, null, null, DEFAULT_PORT, new Path("/"), null, null, null);
	}
	
//	/**
//	 * Constructs a new <code>HTTPXActionMessage</code> with the specified attributes
//	 * 
//	 * @param method	the method of the Http request
//	 * @param protocol	the protocol of the requested URL (e.g. 'http')
//	 * @param host		the host of the requested resource
//	 * @param port		the port number of the host of the requested resource
//	 * @param path		the path of the requested resource
//	 * @param query		the query of the request
//	 * @param headers	the headers of the HTTP request
//	 * @param in		the <code>InputStream</code> of the HTTP request
//	 */
//	private HTTPXActionMessage(String method, String protocol, String host, int port, List<String> path, ParameterList query, Map<String, String> headers, InputStream in) {
//		this(method, protocol, host, port, new Path(path), query, headers, in);
//	}

	/**
	 * Constructs a new <code>HTTPXActionMessage</code> with the specified attributes.
	 * 
	 * @param method	the method of the Http request
	 * @param protocol	the protocol of the requested URL (e.g. 'http')
	 * @param host		the host of the requested resource
	 * @param port		the port number of the host of the requested resource
	 * @param path		the path of the requested resource
	 * @param query		the query of the request
	 * @param headers	the headers of the HTTP request
	 * @param in		the <code>InputStream</code> of the HTTP request
	 */
	public HTTPXActionMessage(String method, String protocol, String host, int port, Path path, ParameterList query, Map<String, String> headers, InputStream in) {
		super(protocol, headers, in);

		this.method = method;		
		if (path == null) {
			path = new Path("/");
		}
		this.path = path;
		if (host == null) {
			host = "";
		}
		this.host = host;
		if (query == null) {
			query = new ParameterList();
		}
		this.query = query;
		
		if (port <= 0) {
			port = DEFAULT_PORT;
		}
		this.port = port;
	}
	
	/**
	 * Returns the path of the requested ressource.
	 * 
	 * @return	the path of the requested resource
	 */
	public Path getPath() {
		return path;
	}
	
	/**
	 * Returns the host of the requested resource.
	 * 
	 * @return 	the host of the requested resource
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port number of the host of the requested resource.
	 * 
	 * @return 	the port number of the host of the requested resource 
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the method of the HTTP request (<code>GET</code>, <code>HEAD</code>, 
	 * <code>POST</code>, <code>PUT</code> or <code>DELETE</code>)).
	 * 
	 * @return 	the method of the HTTP request
	 */
	public String getMethod() {
		return method;
	}
	
	/**
	 * Returns the map of name-value pairs, which contain the query of the request.
	 * 
	 * @return	name-value pairs, which contain the query of the request
	 */
	public ParameterList getQuery() {
		return query;
	}

	/**
	 * Returns the query as a String representation.
	 * 
	 * @return	the query string
	 */
	public String getQueryAsString(){
		return this.query.getAllAsString();
	}
	
	/**
	 * Returns the path as a String.
	 * 
	 * @return	the path as a String
	 */
	public String getPathAsString(){
//		final String slash = "/";
//		StringBuilder result = new StringBuilder();
//		
//		for (String element : path) {
//			result.append(slash).append(element);
//		}
//		if (result.length() <= 0) {
//			result.append(slash);
//		} 
//		
//		return result.toString();
		
		return path.getString();
	}
	
	/**
	 * Sets the path of the requested resource.
	 * 
	 * @param path	the String representation of the path
	 */
	public void setPathAsString(String path){
//		String[] tmp = path.split("/");
//		this.path = new ArrayList<String>();
//		
//		for (String token : tmp) {
//			if (token.length() > 0) {
//				this.path.add(token);
//			}
//		}
		
		this.path = new Path(path);
	}
	
	/**
	 * Sets the path of the requested resource.
	 * 
	 * @param path 	the path of the requested resource
	 */
	public void setPath(List<String> path) {
		this.path = new Path(path);
	}
	
	public void setPath(Path path){
		this.path = path;
	}
	
	/**
	 * Sets the host of the requested resource.
	 * 
	 * @param host 	the host of the requested resource
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Sets the port number of the host of the requested resource.
	 * 
	 * @param port 	the port number of the host of the requested resource
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Sets the method of the HTTP request (<code>GET</code>, <code>HEAD</code>, 
	 * <code>POST</code>, <code>PUT</code> or <code>DELETE</code>)).
	 * 
	 * @param method 	the method of the HTTP request 
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Sets the <code>ParameterList</code>, which contain the query of the request.
	 * 
	 * @param query 	the <code>ParameterList</code>, which contain the query of the request
	 */
	public void setQuery(ParameterList query) {
		this.query = query;
	}
	
	@Override
	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("Host: ").append(this.host).append("\n");
		result.append("Path: ").append(this.getPathAsString()).append("\n");
		result.append("Query: ").append(this.getQueryAsString()).append("\n");
		result.append("Port: ").append(this.port).append("\n");
		result.append("Method: ").append(this.method).append("\n");
		result.append("Header:\n").append(this.getHeadersAsString());
		
		return result.toString();
	}
}
