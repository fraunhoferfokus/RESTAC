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
import de.fhg.fokus.restac.httpx.util.streams.HTTPXOutputStream;

public abstract class ContentWriter {

	protected HTTPXOutputStream out;
	
	/**
	 * Constructor for content writer. Throws ContentConvertingException if content reader
	 * does not support content type of given httpx input stream.
	 * 
	 * @param in - httpx output stream from which to read 
	 * @param myContentType - content type supported by reader implementation
	 * 
	 * @throws ContentConvertingException
	 */
	protected ContentWriter(HTTPXOutputStream out, String myContentType) throws ContentConvertingException {
		
		if(out.getContentType() == null || !out.getContentType().equals(myContentType) ||
				out.getContentCharset() == null) {
			throw new ContentConvertingException("Output stream has either not an appropriate content type or charset equals null.");			
		}
		else {
			this.out = out;
		}
	}
	
	/**
	 * Serializes object and writes it into httpx output stream. If httpx output stream is chunked,
	 * object will be written into one chunk. In this case a null object should be written at the end
	 * to indicate a zero size chunk in order to close stream. Method can block if size of serialized
	 * object is greater than maximum chunk size (default max chunk size is 2048 byte
	 * but can be changed with a setter method). It is in the responsibility of the programmer to
	 * set max chunk size to an appropriate value or execute the method in a thread.
	 * 
	 * If httpx output stream is plain the stream is closed immediately after writing object.
	 * 
	 * @param obj - object to be serialized and written
	 * @throws ContentConvertingException
	 */
	public abstract void write(Object obj) throws ContentConvertingException ;
	
	/**
	 * Serializes object and writes it into httpx output stream. If httpx output stream is chunked,
	 * object will be splitted into chunks according to maximum chunk size (default max chunk size is 2048 byte
	 * but can be changed with a setter method). A zero size chunk is 
	 * written automatically after the last chunk to close the stream. If httpx output stream is plain,
	 * object is written as content of httpx message.
	 * 
	 * @param obj - object to be serialized and written
	 * @throws ContentConvertingException
	 */
	public abstract void writeBuffered(Object obj) throws ContentConvertingException ;

}
