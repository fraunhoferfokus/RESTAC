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

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;

/**
 * Output Stream with methods for sending chunks. 
 * 
 * @author Anna Kress
 *
 */
public class HTTPXChunkedOutputStream extends HTTPXOutputStream {
	
	private final static Logger LOGGER = Logger.getLogger(HTTPXChunkedOutputStream.class);
	private PipedOutputStream pipeOut;
	
	/**
	 * Constructor for HTTPXChunkedOutputStream.
	 * 
	 * @param contentType - type of content
	 * @param contentCharset - charset to be used for serializing content
	 */
	public HTTPXChunkedOutputStream(String contentType, String contentCharset){
		super(null, contentType + ";" + "charset=" + contentCharset);
		
		try {
			//initialize stream if necessary
			if(this.getInputStream() == null) {
				pipeOut =  new PipedOutputStream();
				this.inputStream = new PipedInputStream(pipeOut, this.getMaxChunkSize());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for HTTPXChunkedOutputStream.
	 * 
	 * @param contentType - type of content
	 * @param contentCharset - charset to be used for serializing content
	 * @param maxChunkSize - maximum chunk size 
	 */
	public HTTPXChunkedOutputStream(String contentType, String contentCharset, int maxChunkSize){
		super(null, contentType + ";" + "charset=" + contentCharset);
		
		this.setMaxChunkSize(maxChunkSize);
		
		try {
			//initialize stream if necessary
			if(this.getInputStream() == null) {
				pipeOut =  new PipedOutputStream();
				this.inputStream = new PipedInputStream(pipeOut, this.getMaxChunkSize()); 
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor for HTTPXChunkedOutputStream.
	 * 
	 * @param obj - object to be serialized and written to stream. The object is broken
	 * down into chunks according to maxChunkSize
	 * @param contentType - type of content
	 * @param contentCharset - charset to be used for serializing content
	 * 
	 * @throws ContentConvertingException if content could not be serialized
	 */
//	public HTTPXChunkedOutputStream(Object obj, String contentType, String contentCharset) throws ContentConvertingException{
//		super(null, contentType + ";" + "charset=" + contentCharset);
//		try {
//			//initialize stream
//			pipeOut =  new PipedOutputStream();
//			this.inputStream = new PipedInputStream(pipeOut);
//			writeBufferedContent(obj);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
//	public HTTPXChunkedOutputStream(int maxChunkSize, Object obj, String contentType, String contentCharset)  throws ContentConvertingException {
//		super(null, contentType + ";" + "charset=" + contentCharset);
//		
//		this.setMaxChunkSize(maxChunkSize);
//
//		try {
//			//initialize stream
//			pipeOut =  new PipedOutputStream();
//			this.inputStream = new PipedInputStream(pipeOut, this.getMaxChunkSize());
//			writeBufferedContent(obj);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * Writes one chunk into stream: Takes the chunk content (chunk-data) as parameter,
	 * packages content into chunk according to RFC2616 (see below), and writes chunk into stream. 
	 * <p>
	 * If a byte array with size zero is given, the chunk is taken to be the last-chunk:
	 * Method writes chunk size and closes stream to free up resources.
	 * <p>
	 * Method blocks if size of array is greater than maxChunkSize!
	 * <p>
	 * Attention: chunk-extension and trailer are not supported so far and are ignored!
	 * <p>
	 * Chunks according to RFC2616:
	 * <p>
	 * Chunked-Body = *chunk last-chunk trailer CRLF
	 * <p>
	 * chunk = chunk-size [ chunk-extension ] CRLF chunk-data CRLF 
	 * <p>
	 * chunk-size = 1*HEX last-chunk = 1*("0") [ chunk-extension ] CRLF
	 * <p>
	 * chunk-extension= *( ";" chunk-ext-name [ "=" chunk-ext-val ] )
	 * <p>
	 * chunk-ext-name = token chunk-ext-val = token | quoted-string 
	 * <p>
	 * chunk-data = chunk-size(OCTET) 
	 * <p>
	 * trailer = *(entity-header CRLF)
	 * <p>
	 * @param array - content of chunk (chunk-data) to be written to stream as byte array.
	 */
	@Override
	public void write(byte[] array){
		
		// get chunk length
		int chunk_size = array.length;
		
		String chunkSize = Integer.toHexString(chunk_size);

		if(chunk_size + chunkSize.getBytes().length > this.getMaxChunkSize())
			LOGGER.warn("Chunk too big for internal buffer! Adjust maxChunkSize or write smaller byte array, otherwise method" +
					" will block, if it is not executed in a thread!");
		
		// write chunk
		try {	
			//write chunk header
			pipeOut.write(chunkSize.getBytes()); // write chunk size
			pipeOut.write(HTTPXConstants.CRLF.getBytes()); // write delimiter "\r\n"

			//TODO write chunk extension? extensions are not supported so far...
			LOGGER.debug("Wrote header, header has size " + chunkSize.getBytes().length);
			LOGGER.debug("size of content will be " + array.length);
			LOGGER.debug("max chunk size is " + this.getMaxChunkSize());
			
			//write chunk data
			if (chunk_size > 0) {
				pipeOut.write(array); // write chunk data if available
				LOGGER.debug("wrote body");
				pipeOut.write(HTTPXConstants.CRLF.getBytes()); // write delimiter "\r\n"
				LOGGER.debug("wrote delimiter");
			}
			else //chunk size = 0 means write last chunk without body
			{
				//TODO: if trailer available, write trailer and write chunkSizeDelimiter
				
				pipeOut.write(HTTPXConstants.CRLF.getBytes()); // write delimiter "\r\n";
				LOGGER.debug("wrote delimiter");
				pipeOut.write(HTTPXConstants.CRLF.getBytes()); // write delimiter "\r\n";
				LOGGER.debug("wrote delimiter");
				
				//close stream
				pipeOut.close();
				LOGGER.debug("Wrote last chunk and closed stream");
			}		
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}


	/**
	 * Writes byte array into stream, starts a new thread. 
	 * Byte array is splitted into chunks according to maxChunkSize.
	 * <p>
	 * @param array - payload to be chunked and written to stream. A byte array with size zero
	 * indicates end of payload and closes stream to free up resources.
	 */
	@Override
	public void writeBuffered(final byte[] array) throws ContentConvertingException, IOException{
		
		if(array.length == 0)
			write(array);
		
		new Thread(new Runnable(){
			public void run(){
			// break array down into chunks, max chunk size is by default 2048 byte
			int sizeLeft = array.length;

			int nextChar = 0;
			byte[] buffer;
			
			while (sizeLeft > 0) {
				if (sizeLeft >= getMaxChunkSize()) {
					buffer = new byte[getMaxChunkSize()];

					while (nextChar < getMaxChunkSize()) {
						// copy next 2048 Byte
						buffer[nextChar] = array[nextChar];
						nextChar++;
					}

					write(buffer); // write next chunk with data
				} else {
					buffer = new byte[sizeLeft];
					while (nextChar < sizeLeft){
						buffer[nextChar] = array[nextChar];
					nextChar++;
				}
					write(buffer); // write last chunk with data
					write(new byte[0]); // write zero size chunk to close stream
				}
				sizeLeft -= buffer.length;
			}
			}
		}).start();
		
	}
	
	/*
	 * Serializes and writes object into stream as ONE chunk. User has to take 
	 * care whether the serialized object size is appropriate for one chunk. 
	 * <p>
	 * Useful if application which is going to receive the chunk can handle it independently 
	 * of other chunks. If this is not the case and the user prefers the object to 
	 * be splitted according to maxChunkSize(), writeBufferedContent()
	 * can be used.
	 * <p>
	 * Does NOT close stream by default, only when Object=null is given
	 * <p>
	 * @param array - payload to be chunked and written to stream. Object=null indicates
	 * end of streaming and closes stream to free up resources.
	 */
//	@Override
//	public void writeContent(Object obj) throws ContentConvertingException {
//		try {
//			byte[] array;
//
//			if(obj == null)
//				array = new byte[0];
//				
//			else if (this.contentType.equals(HTTPXConstants.TYPE_APP_URLENCODED))
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
//			writeBuffered(array); //do not close the stream because further objects could be written to it
//			
//		} catch (IOException e) {
//			throw new ContentConvertingException("Content could not be processed: " + e);
//		}
//		
//	}
	
//	/
//	 * Serializes and writes object into stream. Serialized object is splitted into
//	 * chunks according to maxChunkSize(). 
//	 * <p>
//	 * Note that no care is taken whether or not the content of one chunk makes sense independently
//	 * of other chunks (and could therefore be handled independently by the aplication on the other side). 
//	 * Useful e.g. if just a big object the size of which is known in advance should not be send
//	 * as one chunk but broken down in smaller pieces. 
//	 * Then on the other side readBufferedContent() should be used
//	 * which reads all the chunks, puts them together, parses the content and returns the complete object.
//	 * <p>
//	 * Closes stream after last chunk was written.
//	 * <p>
//	 * @param obj - object to be serialized and send.
//	 * @throws ContentConvertingException - indicates that object could not be serialized
//	 */
//	@Override
//	public void writeBufferedContent(Object obj) throws ContentConvertingException {
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
//			writeBuffered(array);
//			write(new byte[0]); // write zero length chunk to close the stream
//			
//		} catch (IOException e) {
//			throw new ContentConvertingException("Content could not be processed: " + e);
//		}
//		
//	}

	
	
	@Override
	public HTTPXActionMessage constructHTTPXActionMessage(String method, String protocol, String host, int port, Path path, ParameterList query, Map<String, String> headers) {
		if(this.getInputStream() != null) {
			if (headers == null)
				headers = new HashMap<String, String>();
		
			headers.put(HTTPXConstants.CONTENT_TYPE, getContentType() + "; charset=" + getContentCharset());
			headers.put(HTTPXConstants.TRANSFER_ENCODING, HTTPXConstants.CHUNKED);
			headers.remove(HTTPXConstants.CONTENT_LENGTH); // no content length should be set if chunked transfer encoding is used
		}
		return new HTTPXActionMessage(method, protocol, host, port, path, query, headers, this.getInputStream());
	}
	
	@Override
	public HTTPXStatusMessage constructHTTPXStatusMessage(int statusCode, String statusMessage, String protocol, Map<String, String> headers) {
		
		if(this.getInputStream() != null) {
			if (headers == null)
				headers = new HashMap<String, String>();
			
			headers.put(HTTPXConstants.CONTENT_TYPE, getContentType() + "; charset=" + getContentCharset());
			headers.put(HTTPXConstants.TRANSFER_ENCODING, HTTPXConstants.CHUNKED);
			headers.remove(HTTPXConstants.CONTENT_LENGTH); // no content length should be set if chunked transfer encoding is used
		}
		return new HTTPXStatusMessage(statusCode, statusMessage, protocol, headers, this.getInputStream());
	}

}
