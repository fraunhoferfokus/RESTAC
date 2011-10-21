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

/*
 * Last modified on: 13.09.2005
 * Created on: 26.05.2004
 */
package de.fhg.fokus.restac.httpx.core.common;

/**
 * Provides a set of constants used in reference to the HTTP protocol.
 * 
 * @author Witold Drytkiewicz
 * @author Philippe Bößling
 * @see <a href="http://www.ietf.org/rfc/rfc2616.txt" target="_blank">RFC 2616</a>
 */
public interface HTTPXConstants {
	
	/* Methods for HTTP requests */
	final static String GET 	 		= "GET";
	final static String PUT 	 		= "PUT";
	final static String POST 	 		= "POST";
	final static String DELETE 	 		= "DELETE";
	final static String HEAD 	 		= "HEAD";

	/* Protocols */
	final static String HTTP_1_1		= "HTTP/1.1";
	final static String HTTP			= "HTTP";
	final static String HTTPU			= "HTTPU";
	final static String HTTPMU			= "HTTPMU";
		
	/* Charsets */
	final static String ISO_8859_1		= "ISO-8859-1";
	final static String DEFAULT_CHARSET			= ISO_8859_1;
		
	/* Delimiters */
	final static String CRLF			        = "\r\n";
	final static String PATH_SEPARATOR 			= "/";
	final static char   PATH_CHAR_SEPARATOR 	= '/';
	final static String EMPTY 			        = "";
	final static String HEADER_NAME_SEPARATOR   = ":";
	final static String NAME_VALUE_SEPARATOR    = "=";
	
	/* Headers */
	final static String CONTENT_LENGTH  = "Content-Length"; 
	final static String CONTENT_TYPE	= "Content-Type";
	final static String LOCATION		= "Location";
	final static String ALLOW			= "Allow";
	final static String CACHE_CONTROL	= "Cache-Control";
	final static String EXPIRES			= "Expires";
	final static String LAST_MODIFIED	= "Last-Modified";
	final static String HOST			= "Host";
	final static String ACCEPT			= "Accept";
	final static String TRANSFER_ENCODING = "Transfer-Encoding";
	final static String UNIQUE_ID 		= "Unique-ID";
	
	/*Media Range for Accept Header*/
	final static String ALL_MEDIA_TYPES = "*/*";
	
	/* MIME types */
	final static String TYPE_TXT_PLAIN			= "text/plain";
	final static String TYPE_TXT_XML			= "text/xml";
	final static String TYPE_HTML_XML			= "text/html";
	final static String TYPE_APP_URLENCODED		= "application/x-www-form-urlencoded";
	final static String TYPE_APP_XML			= "application/xml";
	final static String TYPE_APP_OCTET			= "application/octet-stream";
	final static String TYPE_AUDIO_MPEG			= "audio/mpeg";
	
	final static String TYPE_ALL_SUPPORTED		= TYPE_TXT_PLAIN + ","
												+ TYPE_TXT_XML + ","
												+ TYPE_HTML_XML + ","
												+ TYPE_APP_URLENCODED + ","
												+ TYPE_APP_XML;
	
	/* Transfer encoding type */
	final static String CHUNKED					= "chunked";
	final static int MAX_CHUNK_SIZE				= 2048;
	
	final static int UDP_PACKET_LENGTH 			= 512;
}
