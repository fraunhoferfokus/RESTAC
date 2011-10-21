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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;

public class TextPlainConverter extends ContentConverter {
	
	private final static Logger LOGGER = Logger.getLogger(TextPlainConverter.class);
	
	static public String byteArrayToContent(byte[] body, String charset) throws ContentConvertingException {

		try {
			return new String(body, charset);
		} catch(UnsupportedEncodingException e) {
			LOGGER.warn("Unsupported Content-Encoding: '" + charset + "'. Using charset: '" + Charset.defaultCharset().displayName() + "' instead.");
			return new String(body);
		}
	}
	
	static public byte[] contentToByteArray(String text, String charset)  throws IOException {
		
		byte[] bytes = new byte[0];
		
		try {
			bytes = text.getBytes(charset);
		}

		catch(UnsupportedEncodingException e) {
			LOGGER.error(e);
		}

		return bytes;
	}

	
}
