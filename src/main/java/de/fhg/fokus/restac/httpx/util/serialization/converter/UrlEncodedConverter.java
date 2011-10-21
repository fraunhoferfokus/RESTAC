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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;


/**
 */
public class UrlEncodedConverter extends ContentConverter {
	
	private final static Logger LOGGER = Logger.getLogger(UrlEncodedConverter.class);
	
	static public Map<String, String> byteArrayToContent(byte[] body, String charset) throws ContentConvertingException {

		String bodyString = null;

		try {
			bodyString = new String(body, charset);
		}
		catch(UnsupportedEncodingException e) {
			LOGGER.warn("Unsupported Content-Encoding: '" + charset + "'. Using charset: '" + Charset.defaultCharset().displayName() + "' instead.");
			bodyString = new String(body);
		}
		
		String[] fields = bodyString.split("&");
		//don't allow body=null and body.length>0 and body==""
		if (fields!=null && fields.length > 0 && !(fields.length==1 && fields[0].equals(""))){
			Map<String, String> m = new HashMap<String, String>();
			String name, value;
			int idx;
			for(int i=0; i<fields.length; i++) {
				idx = fields[i].indexOf('=');
				try {
					if (idx!=-1) {
						name= URLDecoder.decode(fields[i].substring(0, idx).trim(),HTTPXConstants.DEFAULT_CHARSET);
						value=URLDecoder.decode(fields[i].substring(idx+1).trim(),HTTPXConstants.DEFAULT_CHARSET);
						m.put(name, value);
					}
					else {
						m.put(URLDecoder.decode(fields[i], HTTPXConstants.DEFAULT_CHARSET),null);
					}
				}catch(UnsupportedEncodingException e) {
					throw new ContentConvertingException("Encoding must be " + HTTPXConstants.DEFAULT_CHARSET, e);
				} 
			}
			return m;
		}
		else if (bodyString.equals("")) return new HashMap<String, String>();
		else throw new ContentConvertingException("URL encoding schema does not conform with the required syntax. parameter1=value1&parameter2=value&...");
		
	}
	
	static public byte[] contentToByteArray(Map<String, String> content, String charset) throws IOException  {
		
		byte[] bytes = new byte[0];
		
		if(content == null) return bytes;
		
		String urlEncoded = "";
		if(content != null) {
			final Iterator<String> iterator = content.keySet().iterator();
			String name;
			String value;
			while(iterator.hasNext()) {
				name=iterator.next();
				value = content.get(name);
				urlEncoded += URLEncoder.encode(name,charset);

				if(value != null) {
					try {
						value = URLEncoder.encode(value, charset);
					}
					catch(UnsupportedEncodingException e) {
						
					}
					urlEncoded += "=" + value;
				}

				if(iterator.hasNext()) urlEncoded += "&";
			}
		}
		try {
			bytes = urlEncoded.getBytes(charset);
		}

		catch(UnsupportedEncodingException e) {
			LOGGER.error(e);
		}
		
		return bytes;
	}
	

}
