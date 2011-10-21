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

/* Created on 01.04.2007 */

package de.fhg.fokus.restac.httpx.core.common;

import java.io.InputStream;
import java.util.Map;

/**
 * TODO: Documentation
 * 
 * @author Murat Ates
 *
 */
public class HTTPXStatusMessage extends HTTPXAbstractMessage {
	
	private int statusCode;
	private String reasonPhrase;
	
	/**
	 * Constructs a new <code>HTTPXStatusMessage</code>.
	 *
	 */
	public HTTPXStatusMessage(){
		super();
	}
		
	/**
	 * Constructs a new <code>HTTPXStatusMessage</code> with the given arguments.
	 * 
	 * @param statusCode	the status code of the message
	 * @param reasonPhrase	the reason phrase of the message (e.g. 'http')
	 * @param protocol		the protocol of the resource
	 * @param headers		the headers of the HTTP response
	 * @param in			the <code>InputStream</code> of the message
	 */
	public HTTPXStatusMessage(int statusCode, String reasonPhrase, String protocol, Map<String, String> headers, InputStream in){
		super(protocol, headers, in);
		this.reasonPhrase = reasonPhrase;
		this.statusCode = statusCode;
	}

	/** 
	 * Returns the status code of the response.
	 * 
	 * @return	the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Sets the status code of the message.
	 * 
	 * @param statusCode	the status code
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Sets the reason phrase of the message.
	 * 
	 * @param reasonPhrase 	the reason phrase
	 */
	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	/**
	 * Returns the reason phrase of the message.
	 * 
	 * @return 	the reason phrase
	 */
	public String getReasonPhrase() {
		return reasonPhrase;
	}
	
	@Override
	public String toString(){
		return (this.statusCode + " " + this.reasonPhrase + "\n" + this.getHeadersAsString());
	}
}
