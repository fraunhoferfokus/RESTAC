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

/* Created on 27.02.2007 */

package de.fhg.fokus.restac.httpx.core.common;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores an HTTP message as defined in RFC 2616. Subclasses are <code>HTTPXActionMessage</code> 
 * and <code>HTTPXStatusMessage</code>.
 * 
 * @author Murat Ates
 * @see HTTPXActionMessage
 * @see HTTPXStatusMessage
 *
 */
public abstract class HTTPXAbstractMessage {
	private final static String DEFAULT_VERSION = "1.1";
	
	/** The <code>InputStream</code> which contain the Body of the HTTP Message. */
	private InputStream in;
	
	/** The headers of the HTTP message, stored as name-value pairs. */
	private Map<String, String> headers;
	
	/** The version of the HTTP protocol, default is <code>HTTP/1.1</code>. */
	private String protocol;
	
	/** The version of the requested resource */
	private String version;
	
	/**
	 * Constructs a new <code>HTTPXAbstractMessage</code>.
	 */
	public HTTPXAbstractMessage(){
		this(HTTPXConstants.HTTP, null, null);
	}
	
	/**
	 * Constructs a new <code>HTTPXAbstractMessage</code> with the specified attributes.
	 * 
	 * @param protocol	the protocol of the HTTP message
	 * @param headers	the headers of the HTTP message
	 * @param in		the <code>InputStream</code> of the HTTP message
	 */
	public HTTPXAbstractMessage(String protocol, Map<String, String> headers, InputStream in){
		if (headers == null) {
			headers = new HashMap<String, String>();
		}
		this.headers = headers;
		this.in = in;
		this.protocol = protocol;
		this.version = DEFAULT_VERSION;
	}
	
	/**
	 * Returns the value of a header field.
	 * 
	 * @param name	the name of the header field.
	 * @return		the value of the header field.
	 */
	public String getHeader(String name){
		return headers.get(name);
	}
	
	/**
	 * Returns the protocol of the HTTP message, e.g. <code>HTTP / HTTPU / HTTPMU</code>.
	 * 
	 * @return 	the protocol of the message.
	 */
	public String getProtocol() {
		return protocol;
	}
	
	/**
	 * Returns the header of the HTTP message.
	 * 
	 * @return	the <code>Map</code> containing the Header fields.
	 */
	public Map<String, String> getHeaders(){
		return headers;
	}
	
	/**
	 * Returns the header as a String representation.
	 * 
	 * @return	the header as a String
	 */
	public String getHeadersAsString(){
		StringBuilder result = new StringBuilder();
		
		for (Map.Entry<String, String> elem	: this.headers.entrySet()) {
			result.append(elem.getKey()).append(HTTPXConstants.HEADER_NAME_SEPARATOR).append(" ").append(elem.getValue()).append("\n");
		}
		return result.toString();
	}
	
	/**
	 * Returns the <code>InputStream</code> of the HTTP message.
	 * 
	 * @return	the <code>InputStream</code> containing the Body.
	 */
	public InputStream getInputStream(){
		return in;
	}
	
	/**
	 * Sets a header field of a HTTP message 
	 * 
	 * @param name	the name of the header field.
	 * @param value	the value of the header field.
	 */
	public void setHeader(String name, String value){
		headers.put(name, value);
	}

	/**
	 * Removes the header field from the headers of the HTTP message specified by the
	 * name.
	 * 
	 * @param name	the name of the header to be removed.
	 * @return		true, if the field was removed successful. 
	 */
	public boolean unsetHeader(String name){
		return headers.remove(name) != null;
	}
	
	/**
	 * Sets the protocol of the HTTP message, e.g. <code>HTTP / HTTPU / HTTPMU</code>.
	 *  
	 * @param protocol 	the protocol of the HTTP message.
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	/**
	 * Sets the <code>InputStream</code> of the HTTP message.
	 * 
	 * @param in	the <code>InputStream</code> containing the Body
	 */
	public void setInputStream(InputStream in){
		this.in = in;
	}
	
	/**
	 * Returns the version of the HTTP protocol.
	 * 
	 * @return	the version of the protocol.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version of the HTTP protocol.
	 * Default value is '1.1'.
	 * 
	 * @param version 	the version of the HTTP protocol.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
}
