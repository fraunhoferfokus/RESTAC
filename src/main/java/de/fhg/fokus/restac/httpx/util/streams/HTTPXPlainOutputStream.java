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

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.util.serialization.converter.ContentConverter;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;

/**
 * Output Stream with methods for sending plain (noch chunked) http message bodies. 
 * 
 * @author Anna Kress
 *
 */
public class HTTPXPlainOutputStream extends HTTPXOutputStream {

	private Integer contentLength = null; 
	
//	public HTTPXPlainOutputStream(Object obj, String contentType, String contentCharset) throws ContentConvertingException{
//		super(null, contentType + ";" + "charset=" + contentCharset);
//		writeContent(obj);
//	}

	public HTTPXPlainOutputStream(String contentType, String contentCharset) {
		super(null, contentType + ";" + "charset=" + contentCharset);
	}

	/**
	 * Writes payload as byte array into
	 * input stream. Sets content length according to 
	 * length of array.
	 */
	@Override
	public void write(final byte[] array){
		
		final PipedOutputStream pipeOut = new PipedOutputStream();
		
		try {
			this.inputStream = new PipedInputStream(pipeOut, array.length);
			pipeOut.write(array);
			pipeOut.close();
			this.contentLength = new Integer(array.length);
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Writes object into
	 * input stream by serializing it into a byte array. 
	 * @see ContentConverter
	 * Sets content length according to 
	 * length of array.
	 */
//	@Override
//	public void writeContent(Object obj) throws ContentConvertingException {
//		try {
//			byte[] array;
//			
//			if (this.contentType.equals(HTTPXConstants.TYPE_APP_URLENCODED))
//				array=UrlEncodedConverter.contentToByteArray((Map<String, String>)obj, this.contentCharset);
//			else if (this.contentType.equals(HTTPXConstants.TYPE_TXT_PLAIN))
//				array=TextPlainConverter.contentToByteArray((String)obj, this.contentCharset);
//			else if (this.contentType.equals(HTTPXConstants.TYPE_HTML_XML))
//				array=HtmlXmlConverter.contentToByteArray((Document)obj, this.contentCharset);
//			else if (this.contentType.equals(HTTPXConstants.TYPE_TXT_XML))
//				array=TextXmlConverter.contentToByteArray((Document)obj, this.contentCharset);
//			else
//				throw new ContentConvertingException("Content Type not supported.");
//			
//			this.write(array);
//
//		} catch (IOException e) {
//			throw new ContentConvertingException("Content could not be processed: " + e);
//		}
//	}
	
	@Override
	public void writeBuffered(byte[] array) throws ContentConvertingException {
		write(array);		
	}

//	@Override
//	public void writeBufferedContent(Object obj) throws ContentConvertingException {
//		writeContent(obj);
//	}
	
	@Override
	public HTTPXStatusMessage constructHTTPXStatusMessage(int statusCode, String statusMessage, String protocol, Map<String, String> headers) {
		
		if(this.contentLength > 0) {
			if (headers == null)
				headers = new HashMap<String, String>();
			
			headers.put(HTTPXConstants.CONTENT_LENGTH, this.contentLength.toString());
			headers.put(HTTPXConstants.CONTENT_TYPE, getContentType() + "; charset=" + getContentCharset());
		}
		return new HTTPXStatusMessage(statusCode, statusMessage, protocol, headers, this.getInputStream());
	}
	
	@Override
	public HTTPXActionMessage constructHTTPXActionMessage(String method, String protocol, String host, int port, Path path, ParameterList query, Map<String, String> headers) {
		
		if(this.contentLength > 0) {
			if (headers == null)
				headers = new HashMap<String, String>();
			
			headers.put(HTTPXConstants.CONTENT_LENGTH, this.contentLength.toString());
			headers.put(HTTPXConstants.CONTENT_TYPE, getContentType() + "; charset=" + getContentCharset());
		}
		return new HTTPXActionMessage(method, protocol, host, port, path, query, headers, this.getInputStream());
	}

}
