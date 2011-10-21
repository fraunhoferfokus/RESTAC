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
import java.io.InputStream;
import java.util.Map;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;

public abstract class HTTPXOutputStream extends HTTPXStream {

	private int maxChunkSize = HTTPXConstants.MAX_CHUNK_SIZE;
	
	public HTTPXOutputStream(InputStream inputStream, String contentTypeHeader) {
		super(inputStream, contentTypeHeader);
	}
	
	/**
	 * 
	 * @see HTTPXPlainOutputStream or HTTPXChunkedOutputStream
	 */
	abstract public void write(byte[] array) throws IOException;// write to input stream
	
	/**
	 * 
	 * @see HTTPXPlainOutputStream or HTTPXChunkedOutputStream
	 */
	abstract public void writeBuffered(byte[] array) throws ContentConvertingException, IOException;
	
//	/**
//	 * 
//	 * @see HTTPXPlainOutputStream or HTTPXChunkedOutputStream
//	 */
//	abstract public void writeContent(Object obj) throws ContentConvertingException;
//	
//	/**
//	 * 
//	 * @see HTTPXPlainOutputStream or HTTPXChunkedOutputStream
//	 */
//	abstract public void writeBufferedContent(Object obj) throws ContentConvertingException;
	 
	/**
	 * Constructs a new httpx status message according to type of stream (either plain or chunked input stream).
	 * Adds especially the necessary headers for the streams.
	 * 
	 * @param statusCode - status code of message
	 * @param statusMessage - status message
	 * @param protocol - protocol to be used
	 * @param headers - headers so far (method just appends necessary headers like Content-Type to headers and does
	 * not overwrite them, the only exception is Content-Length which is deleted if stream is chunked according to RFC2616)
	 * 
	 * @return HTTPXStatusMessage
	 */
	public abstract HTTPXStatusMessage constructHTTPXStatusMessage(int statusCode, String statusMessage,
			String protocol, Map<String, String> headers);

	/**
	 * Constructs a new httpx action message according to type of stream (either plain or chunked input stream).
	 * Adds especially the necessary headers for the streams.
	 * 
	 * @param method - POST, GET, PUT, DELETE, HEAD
	 * @param protocol - protocol to be used
	 * @param host - destination host
	 * @param port - dest port
	 * @param path - dest path
	 * @param query - dest query
	 * @param headers - headers so far (method just appends necessary headers like Content-Type to headers and does
	 * not overwrite them, the only exception is Content-Length which is deleted if stream is chunked according to RFC2616)
	 * 
	 * @return HTTPXActionMessage
	 */
	public abstract HTTPXActionMessage constructHTTPXActionMessage(String method, String protocol, 
			String host, int port, Path path, ParameterList query, Map<String, String> headers);


	public int getMaxChunkSize() {
		return maxChunkSize;
	}

	protected void setMaxChunkSize(int maxChunkSize) {
		this.maxChunkSize = maxChunkSize;
	}
}
