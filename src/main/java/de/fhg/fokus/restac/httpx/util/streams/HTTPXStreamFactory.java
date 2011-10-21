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

import de.fhg.fokus.restac.httpx.core.common.HTTPXAbstractMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;

public class HTTPXStreamFactory {
	
	/**
	 * Returns the appropriate type of input stream for reading the content of this message, that means
	 * either plain or chunked input stream according
	 * to the Transfer-Encoding header field of message.
	 * 
	 * @param message
	 * 
	 * @return either HTTPXChunkedInputStream or HTTPXPlainInputStream according
	 * to the Transver-Coding header field of message.
	 */
	public static HTTPXInputStream getHTTPXInputStream(HTTPXAbstractMessage message) {
		if(message.getInputStream() == null)
			return null;
		
		if (message.getHeader(HTTPXConstants.TRANSFER_ENCODING) != null &&
				message.getHeader(HTTPXConstants.TRANSFER_ENCODING).equals(HTTPXConstants.CHUNKED))
			return new HTTPXChunkedInputStream(message);
		else
			return new HTTPXPlainInputStream(message);
	}

	/**
	 * Returns the appropriate type of output stream according to length of object and
	 * the max supported chunk length (stored in HTTPXConstants.MAX_CHUNK_SIZE): if size of serialized object is 
	 * <= MAX_CHUNK_SIZE, the object is send in a plain input stream, 
	 * otherwise it is chunked and send over a chunked input stream
	 * 
	 * @param obj - object to be serialized
	 * @param contentType - type of object
	 * @param contentCharset - charset to be used for serialization
	 * 
	 * @return either HTTPXChunkedInputStream or HTTPXPlainInputStream 
	 * 
	 * @throws ContentConvertingException if content could not be serialized
	 * @throws IOException
	 */
//	public static HTTPXOutputStream getHTTPXOutputStream(Object obj, String contentType, String contentCharset) throws ContentConvertingException, IOException {
//
//		// there is no other way to find out which stream is appropriate
//		// than to serialize the object into a byte array just to test how 
//		// long the array will be...
//		byte[] array;
//
//		if (contentType.equals(HTTPXConstants.TYPE_APP_URLENCODED))
//			array=UrlEncodedConverter.contentToByteArray((Map<String, String>)obj, contentCharset);
//		else if (contentType.equals(HTTPXConstants.TYPE_TXT_PLAIN))
//			array=TextPlainConverter.contentToByteArray((String)obj, contentCharset);
//		else if (contentType.equals(HTTPXConstants.TYPE_HTML_XML))
//			array=HtmlXmlConverter.contentToByteArray((Document)obj, contentCharset);
//		else if (contentType.equals(HTTPXConstants.TYPE_TXT_XML))
//			array=TextXmlConverter.contentToByteArray((Document)obj, contentCharset);
//		else
//			throw new ContentConvertingException("Content Type not supported.");
//		
//		if(array.length > HTTPXConstants.MAX_CHUNK_SIZE)
//			return new HTTPXChunkedOutputStream(obj, contentType, contentCharset);
//		else
//			return new HTTPXPlainOutputStream(obj, contentType, contentCharset);
//	}

	/**
	 * 
	 * @param obj - object to be serialized
	 * @param maxChunkSize - maximum size to be used for chunks: if size of serialized object is <= maxChunkSize, the 
	 * object is send in a plain input stream, otherwise it is chunked and send over a chunked input stream
	 * 
	 * @param contentType - type of object
	 * @param contentCharset - charset to be used for serialization
	 * 
	 * @return either HTTPXChunkedInputStream or HTTPXPlainInputStream according to length of object and
	 * the given max chunk length
	 * 
	 * @throws ContentConvertingException if content could not be serialized
	 * @throws IOException
	 */
//	public static HTTPXOutputStream getHTTPXOutputStream(Object obj, int maxChunkSize, String contentType, String contentCharset) throws ContentConvertingException, IOException {
//
//		// there is no other way to find out which stream is appropriate
//		// than to serialize the object into a byte array just to test how 
//		// long the array will be...
//		byte[] array;
//
//		if (contentType.equals(HTTPXConstants.TYPE_APP_URLENCODED))
//			array=UrlEncodedConverter.contentToByteArray((Map<String, String>)obj, contentCharset);
//		else if (contentType.equals(HTTPXConstants.TYPE_TXT_PLAIN))
//			array=TextPlainConverter.contentToByteArray((String)obj, contentCharset);
//		else if (contentType.equals(HTTPXConstants.TYPE_HTML_XML))
//			array=HtmlXmlConverter.contentToByteArray((Document)obj, contentCharset);
//		else if (contentType.equals(HTTPXConstants.TYPE_TXT_XML))
//			array=TextXmlConverter.contentToByteArray((Document)obj, contentCharset);
//		else
//			throw new ContentConvertingException("Content Type not supported.");
//		
//		if(array.length > maxChunkSize)
//			return new HTTPXChunkedOutputStream(maxChunkSize, obj, contentType, contentCharset);
//		else
//			return new HTTPXPlainOutputStream(obj, contentType, contentCharset);
//	}
	
}
