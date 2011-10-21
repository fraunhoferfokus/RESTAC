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

package de.fhg.fokus.restac.httpx.util.streams;

import java.io.InputStream;

import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;

public abstract class HTTPXStream {
	
	protected InputStream inputStream = null;
	protected String contentType = null;
	protected String contentCharset = null;
	
	/**
	 * Constructor for HTTPXStream, sets the input stream and parses the content type header of a
	 * httpx message to extract content type and charset, for format of the content type header see RFC2616
	 * 
	 * @param inputStream
	 * @param contentTypeHeader - header of HTTPX message which contains both, content type and charset
	 */
	public HTTPXStream(InputStream inputStream, String contentTypeHeader) {
		this.inputStream = inputStream;
		
		setContentTypeAndCharset(contentTypeHeader);
	}
	
	/**
	 * Parses the content type header of a httpx message and extracts content type and charset.
	 * For format of the content type header see RFC2616
	 * 
	 * @param contentTypeHeader - content type header where both content type and charset are stored
	 */
	private void setContentTypeAndCharset(String contentTypeHeader) {
		
		if(contentTypeHeader == null){
			this.contentCharset = null;
			this.contentType = null;
			return;
		}
		
		//get content type
		if(contentTypeHeader.contains(";")) 
			this.contentType = contentTypeHeader.substring(0, contentTypeHeader.indexOf(";"));
		else { //no params in content type header, therefore no charset
			this.contentType = contentTypeHeader;
			this.contentCharset = HTTPXConstants.DEFAULT_CHARSET;
			return;
		}
		
		if(contentTypeHeader.contains("charset")) {
			int indexOfCharsetValue = contentTypeHeader.indexOf("charset=") + 8; // start of charset value
			int indexOfEndOfCharsetValue = contentTypeHeader.indexOf(";", indexOfCharsetValue); // are there other params besides of charset?
			
			if(indexOfEndOfCharsetValue == -1)//if there is no other param
				this.contentCharset = contentTypeHeader.substring(indexOfCharsetValue, contentTypeHeader.length());
			else
				this.contentCharset = contentTypeHeader.substring(indexOfCharsetValue, indexOfEndOfCharsetValue); 			
		}
		else
			this.contentCharset = HTTPXConstants.DEFAULT_CHARSET; //no charset given, let's try default charset
		
		return;
	}
		
	public InputStream getInputStream() {
		return inputStream;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getContentCharset() {
		return contentCharset;
	}

}
