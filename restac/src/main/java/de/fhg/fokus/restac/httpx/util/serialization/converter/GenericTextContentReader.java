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

/**
 * Reads from stream and returns content as string.
 * 
 * @author Anna Kress
 *
 */
public class GenericTextContentReader {
	
	protected HTTPXInputStream in;
	
	public GenericTextContentReader(HTTPXInputStream in) throws ContentConvertingException {
		this.in = in;
	}

	public String read() throws ContentConvertingException {
		byte[] body = in.read();

		if(body.length == 0)
			return null;
		
		return GenericTextConverter.byteArrayToContent(body, in.getContentCharset());
	}

	public String readBuffered()  throws ContentConvertingException{
		byte[] body = in.readBuffered();

		if(body.length == 0)
			return null;
		
		return GenericTextConverter.byteArrayToContent(body, in.getContentCharset());
	}
}
