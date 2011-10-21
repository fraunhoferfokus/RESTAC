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
import java.io.StringWriter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXAbstractMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;

/**
 * Input Stream with methods for receiving chunks. 
 * 
 * @author Anna Kress
 *
 */
public class HTTPXChunkedInputStream extends HTTPXInputStream {

public HTTPXChunkedInputStream(HTTPXAbstractMessage message) {
		super(message.getInputStream(),  message.getHeader(HTTPXConstants.CONTENT_TYPE));
	}

	private final static Logger LOGGER = Logger.getLogger(HTTPXChunkedInputStream.class);

	private List<String> trailerHeader; //TODO trailer not supported so far...
	
	/**
	 * Reads chunk-data of next chunk.
	 * <p>
	 * Attention: chunk-extension and trailer are not supported so far and simply ignored.
	 * <p>
	 * Chunks according to RFC2616:
	 * <p>
	 * Chunked-Body = *chunk last-chunk trailer CRLF
	 * <p>
	 * chunk = chunk-size [ chunk-extension ] CRLF chunk-data CRLF 
	 * chunk-size = 1*HEX last-chunk = 1*("0") [ chunk-extension ] CRLF
	 * chunk-extension= *( ";" chunk-ext-name [ "=" chunk-ext-val ] )
	 * chunk-ext-name = token chunk-ext-val = token | quoted-string chunk-data =
	 * chunk-size(OCTET) trailer = *(entity-header CRLF)
	 * <p>
	 * @return payload=chunk-data of next chunk as raw array which is extracted from the chunk according to chunk-size.
	 * Returns array with length 0 if no chunk could be read anymore. 
	 */
	@Override
	public byte[] read(){
	
		int currentCharacter = -1;
		int previousCharacter = -1;
		StringWriter chunkHeader = new StringWriter();

		try {
			while((currentCharacter = this.inputStream.read()) != -1) {
				if(previousCharacter == 13 && currentCharacter == 10) //means "\r\n"
					break;
				if(currentCharacter != 13 && currentCharacter != 10)
					chunkHeader.append((char)currentCharacter); 
				previousCharacter = currentCharacter;
			}
		
		StringTokenizer tokenizer = new StringTokenizer(chunkHeader.toString(), ";"); // header can contain chunk extensions delimited by ";"
			
		int chunk_size = (Integer.decode("0x" + tokenizer.nextToken())).intValue(); //decode chunk size
		// TODO what about chunk extensions? we ignore them so far...
		
		LOGGER.debug("Read chunk size " + chunk_size);
		
		byte[] chunk_data = new byte[chunk_size];
		
		if(chunk_size != 0) {
			this.inputStream.read(chunk_data);
		
			this.inputStream.read(); // remove CRLF from stream, would also be a good idea to check if we get CRLF...
			this.inputStream.read();
		}
		else //chunk size = 0
		{
			//TODO read trailer? trailer is not supported so far...
			int nextByte = -1;
			
			//ignore trailer, just take everything from stream
			//TODO this implementation is probably buggy and will need another look at...
			
			if ((nextByte = this.inputStream.read()) != 13) {
				while(nextByte != -1) {
					if((nextByte = this.inputStream.read()) != 13)
						continue;
						if((nextByte = this.inputStream.read()) != 10)
							continue;
							if((nextByte = this.inputStream.read()) != 13)
								continue;
								if((nextByte = this.inputStream.read()) != 10)
									break; 
								else
									break;//looking for to consecutive "\r\n" which means end of streaming
				}
			}
			
			//remove last LF from stream
			else {
				this.inputStream.read();
			}
		}
		
		return chunk_data;
		
		} catch (IOException e) {
			return new byte[0];
		}
		 catch (NoSuchElementException e) {
				return new byte[0];
		}
	}

	/**
	 * Reads and parses content of next chunk into an object. Useful if the 
	 * application can use the content of this chunk independently of other chunks in stream.
	 * <p>
	 * Attention: chunk-extension and trailer are not supported so far and just ignored.
	 * 
	 * @return parsed payload of next chunk which is extracted from the chunk.
	 * Returns null if last chunk with size 0 was read.
	 * 
	 * @see read()
	 */
//	@Override
//	public Object readContent() throws ContentConvertingException {
//		byte[] body;
//		
//		body = read();
//
//		if (body.length == 0)
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

	/**
	 * Reads chunks (means chunk-data) until a zero length chunk is read indicating end of stream.
	 * Concatenates all chunks and returns the complete chunk payload as raw byte array.
	 * <p>
	 * Attention: chunk-extension and trailer are not supported so far and just ignored.
	 * 
	 * @return payload of all chunks as raw byte array
	 * 
	 * @see read()
	 */
	@Override
	public byte[] readBuffered() {

		byte[] payload = new byte[0];
		byte[] payloadSoFar = new byte[0];
		byte[] nextChunkPayload = new byte[0];
		
	    //concatenate arrays until we get complete payload
		while((nextChunkPayload = read()).length > 0) {
			payload = new byte[( payloadSoFar.length + nextChunkPayload.length)];
			System.arraycopy(payloadSoFar, 0, payload, 0, payloadSoFar.length);
			System.arraycopy(nextChunkPayload, 0, payload, payloadSoFar.length, nextChunkPayload.length);
			payloadSoFar = payload;
		}
		
		return payload;
	}

	/**
	 * Reads chunks from stream (means chunk-data) until a zero length chunk is read indicating end of stream.
	 * Concatenates all chunks into a raw byte array, parses the array into object and returns object.
	 * <p>
	 * Attention: chunk-extension and trailer are not supported so far and just ignored.
	 * 
	 * @return object extracted and parsed from chunks
	 * 
	 * @see readBuffered()
	 */
//	@Override
//	public Object readContentBuffered() throws ContentConvertingException {
//		
//		byte[] body = readBuffered();
//		
//		if (body.length == 0)
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



}
