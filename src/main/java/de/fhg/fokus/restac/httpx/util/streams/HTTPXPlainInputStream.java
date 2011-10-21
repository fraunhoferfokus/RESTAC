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

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXAbstractMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;

/**
 * Input Stream with methods for receiving plain (noch chunked) http message bodies. 
 * 
 * @author Anna Kress
 *
 */
public class HTTPXPlainInputStream extends HTTPXInputStream  {
	
	private final static Logger LOGGER = Logger.getLogger(HTTPXPlainInputStream.class);
	
	private Integer contentLength = null; 
	
	public HTTPXPlainInputStream(HTTPXAbstractMessage message){
		super(message.getInputStream(), message.getHeader(HTTPXConstants.CONTENT_TYPE));
		
		if(message.getHeader(HTTPXConstants.CONTENT_LENGTH) != null)
			this.contentLength = new Integer(message.getHeader(HTTPXConstants.CONTENT_LENGTH));
		else 
			this.contentLength = 0;
	}
	
	/**
	 * @return payload of message as byte array.
	 * Returns array with length 0 if no payload is 
	 * available.
	 */
	@Override
	public byte[] read(){
		
		byte[] bytes = new byte[0];
		
	

		if(this.contentLength > 0) {
			
			bytes = new byte[this.contentLength];
			
			int read_bytes = 0;
			
			try {
				
				while (read_bytes < contentLength) {
				
					int read_tmp=this.getInputStream().read(bytes,read_bytes,contentLength-read_bytes);
					
					if (read_tmp==-1) break;
					
					read_bytes+=read_tmp;
					
				}
			}
			catch(IOException e) {
				LOGGER.error(e);
				bytes = new byte[0];
			}
		}		
		return bytes;	 //read from stream only once
	}


	/**
	 * @return the parsed object from payload of message by 
	 * applying the appropriate converter
	 * 
	 * @see ContentConverter
	 * 
	 * @throws ContentConvertingException if payload could not be parsed
	 */
//	@Override
//	public Object readContent() throws ContentConvertingException {
//
//		byte[] body = read();
//
//		if(body.length == 0)
//			return null;
//		
//		if(this.contentType.equals(HTTPXConstants.TYPE_APP_URLENCODED))
//			return UrlEncodedConverter.byteArrayToContent(body, this.contentCharset);
//		else if(this.contentType.equals(HTTPXConstants.TYPE_TXT_PLAIN))
//			return TextPlainConverter.byteArrayToContent(body, this.contentCharset);
//		else if(this.contentType.equals(HTTPXConstants.TYPE_HTML_XML))
//			return HtmlXmlConverter.byteArrayToContent(body, this.contentCharset);
//		else if(this.contentType.equals(HTTPXConstants.TYPE_TXT_XML))
//			return TextXmlConverter.byteArrayToContent(body, this.contentCharset);
//		else throw new ContentConvertingException("Content Type not supported.");
//	}

	/** Just a dummy method for convenient use of HTTPXStreamFactory. Calls directly read().
	 * More interesting things are going on in a method with the same name in HTTPXChunkedInputStream! 
	 * 
	 * @see HTTPXStreamFactory
	 * @see HTTPXChunkedInputStream
	 * */
	@Override
	public byte[] readBuffered(){
		return read();
	}


	/** Just a dummy method for convenient use of HTTPXStreamFactory. Calls directly readContent().
	 * More interesting things are going on in a method with the same name in HTTPXChunkedInputStream!
	 * 
	 * @see HTTPXStreamFactory
	 * @see HTTPXChunkedInputStream
	 * 
	 * @throws ContentConvertingException if content could not be parsed. 
	 * */
//	@Override
//	public Object readContentBuffered() throws ContentConvertingException {
//		return readContent();
//	}

}
