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

package de.fhg.fokus.restac.httpx.util.serialization.converter;

import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXInputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXOutputStream;

public abstract class ContentReader {
	
	protected HTTPXInputStream in;
	
	/**
	 * Constructor for content reader. Throws ContentConvertingException if content reader
	 * does not support content type of given httpx input stream.
	 * 
	 * @param in - httpx input stream from which to read 
	 * @param myContentType - content type supported by reader implementation
	 * 
	 * @throws ContentConvertingException
	 */
	protected ContentReader(HTTPXInputStream in, String myContentType) throws ContentConvertingException {
		
		if(in.getContentType() == null || !in.getContentType().equals(myContentType) ||
				in.getContentCharset() == null) {
			throw new ContentConvertingException("Output stream has either not an appropriate content type or charset equals null.");			
		}
		else {
			this.in = in;
		}
	}
	
	/**
	 * Reads from httpx input stream. If input stream is a httpx plain input stream,
	 * returns parsed content of httpx message. If input stream is a httpx chunked input stream
	 * returns parsed content of next chunk. Returns null if stream is closed or last chunk was read.
	 * 
	 * @return parsed object - data type of object is determined by reader implementation
	 * @throws ContentConvertingException
	 */
	public abstract Object read() throws ContentConvertingException;
	
	/**
	 * Reads from httpx input stream. If input stream is a httpx plain input stream,
	 * returns parsed content of httpx message. If input stream is a httpx chunked input stream
	 * reads all chunks from stream until zero size chunk is read, 
	 * parses chunks into one object and returns object.
	 * Returns null if stream is closed or last chunk was read.
	 * 
	 * @return parsed object - data type of object is determined by reader implementation
	 * @throws ContentConvertingException
	 */
	public abstract Object readBuffered() throws ContentConvertingException;	
}
