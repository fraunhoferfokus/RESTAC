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
 * Last modified on 13.09.2005
 * Created on 13.09.2005
 */

package de.fhg.fokus.restac.httpx.core.common;

/**
 * Stores the status code and reason phrase of an HTTP response as defined in RFC 2616.
 * 
 * @author Philippe Bößling
 * @author Stefan Föll
 * @see <a> href="http://www.ietf.org/rfc/rfc2616.txt" target="_blank">RFC 2616</a>
 */
public class HTTPStatus {

	/* public constant fields */
	public final static HTTPStatus CONTINUE 				= new HTTPStatus(100, "Continue");
	public final static HTTPStatus SWITCHING_PROTOCOL 		= new HTTPStatus(101, "Switching Protocol");
	public final static HTTPStatus OK 						= new HTTPStatus(200, "OK");
	public final static HTTPStatus CREATED 					= new HTTPStatus(201, "Created");
	public final static HTTPStatus ACCEPTED 				= new HTTPStatus(202, "Accepted");
	public final static HTTPStatus NON_AUTHORATIVE 			= new HTTPStatus(203, "Non-Authorative Information");
	public final static HTTPStatus NO_CONTENT 				= new HTTPStatus(204, "No Content");
	public final static HTTPStatus RESET_CONTENT 			= new HTTPStatus(205, "Reset Content");
	public final static HTTPStatus PARTIAL_CONTENT 			= new HTTPStatus(206, "Partial Content");
	public final static HTTPStatus MULTIPLE_CHOICES 		= new HTTPStatus(300, "Multiple Choices");
	public final static HTTPStatus MOVED_PERM 				= new HTTPStatus(301, "Moved Permanently");
	public final static HTTPStatus FOUND 					= new HTTPStatus(302, "Found");
	public final static HTTPStatus SEE_OTHER 				= new HTTPStatus(303, "See Other");
	public final static HTTPStatus NOT_MODIFIED 			= new HTTPStatus(304, "Not Modified");
	public final static HTTPStatus USE_PROXY 				= new HTTPStatus(305, "Use Proxy");
	public final static HTTPStatus CODE_MONKEYS 			= new HTTPStatus(306, "Code Monkeys");
	public final static HTTPStatus TEMP_REDIRECT 			= new HTTPStatus(307, "Temporary Redirect");
	public final static HTTPStatus BAD_REQUEST 				= new HTTPStatus(400, "Bad Request");
	public final static HTTPStatus UNAUTHORIZED 			= new HTTPStatus(401, "Unauthorized");
	public final static HTTPStatus PAYMENT_REQUIRED 		= new HTTPStatus(402, "Payment Required");
	public final static HTTPStatus FORBIDDEN 				= new HTTPStatus(403, "Forbidden");
	public final static HTTPStatus NOT_FOUND 				= new HTTPStatus(404, "Not Found");
	public final static HTTPStatus METHOD_NOT_ALLOWED 		= new HTTPStatus(405, "Method Not Allowed");
	public final static HTTPStatus NOT_ACCEPTABLE 			= new HTTPStatus(406, "Not Acceptable");
	public final static HTTPStatus PROXY_AUTHENTICATION 	= new HTTPStatus(407, "Proxy Authentication Required");
	public final static HTTPStatus REQUEST_TIMEOUT 			= new HTTPStatus(408, "Request Timeout");
	public final static HTTPStatus CONFLICT 				= new HTTPStatus(409, "Conflict");
	public final static HTTPStatus GONE 					= new HTTPStatus(410, "Gone");
	public final static HTTPStatus LENGTH_REQUIRED 			= new HTTPStatus(411, "Length Required");
	public final static HTTPStatus PRECONDITION_FAILED 		= new HTTPStatus(412, "Precondition Failed");
	public final static HTTPStatus ENTITY_TOO_LARGE 		= new HTTPStatus(413, "Request Entity Too Large");
	public final static HTTPStatus URI_TOO_LONG				= new HTTPStatus(414, "Request-URI Too Long");
	public final static HTTPStatus UNSUPPORTED_TYPE 		= new HTTPStatus(415, "Unsupported Media Type");
	public final static HTTPStatus RANGE_NOT_SATISFIABLE 	= new HTTPStatus(416, "Requested Range Not Satisfiable");
	public final static HTTPStatus EXPECTATION_FAILED 		= new HTTPStatus(417, "Expectation Failed");
	public final static HTTPStatus INTERNAL_ERROR 			= new HTTPStatus(500, "Internal Server Error");
	public final static HTTPStatus NOT_IMPLEMENTED 			= new HTTPStatus(501, "Not Implemented");
	public final static HTTPStatus BAD_GATEWAY 				= new HTTPStatus(502, "Bad Gateway");
	public final static HTTPStatus SERVICE_UNAVAILABLE	 	= new HTTPStatus(503, "Service Unavailable");
	public final static HTTPStatus GATEWAY_TIMEOUT 			= new HTTPStatus(504, "Gateway Timeout");
	public final static HTTPStatus VERSION_NOT_SUPPORTED 	= new HTTPStatus(505, "HTTP Version Not Supported");
	
	public final static HTTPStatus[] ALL= {HTTPStatus.CONTINUE, HTTPStatus.SWITCHING_PROTOCOL,HTTPStatus.OK, HTTPStatus.CREATED, HTTPStatus.ACCEPTED,
											HTTPStatus.NON_AUTHORATIVE, HTTPStatus.NO_CONTENT, HTTPStatus.RESET_CONTENT, HTTPStatus.PARTIAL_CONTENT,
											HTTPStatus.MULTIPLE_CHOICES, HTTPStatus.MOVED_PERM, HTTPStatus.FOUND, HTTPStatus.SEE_OTHER, HTTPStatus.NOT_MODIFIED,
											HTTPStatus.USE_PROXY, HTTPStatus.CODE_MONKEYS, HTTPStatus.TEMP_REDIRECT, HTTPStatus.BAD_REQUEST, HTTPStatus.UNAUTHORIZED,
											HTTPStatus.PAYMENT_REQUIRED, HTTPStatus.FORBIDDEN, HTTPStatus.NOT_FOUND, HTTPStatus.METHOD_NOT_ALLOWED,
											HTTPStatus.NOT_ACCEPTABLE, HTTPStatus.PROXY_AUTHENTICATION, HTTPStatus.REQUEST_TIMEOUT, HTTPStatus.CONFLICT,
											HTTPStatus.GONE, HTTPStatus.LENGTH_REQUIRED, HTTPStatus.PRECONDITION_FAILED, HTTPStatus.ENTITY_TOO_LARGE,
											HTTPStatus.URI_TOO_LONG, HTTPStatus.UNSUPPORTED_TYPE, HTTPStatus.RANGE_NOT_SATISFIABLE, HTTPStatus.EXPECTATION_FAILED,
											HTTPStatus.INTERNAL_ERROR,HTTPStatus.NOT_IMPLEMENTED, HTTPStatus.BAD_GATEWAY, HTTPStatus.SERVICE_UNAVAILABLE,
											HTTPStatus.GATEWAY_TIMEOUT, HTTPStatus.VERSION_NOT_SUPPORTED};
	
	public final static HTTPStatus[] ALL_USED = { HTTPStatus.FORBIDDEN, HTTPStatus.BAD_REQUEST, HTTPStatus.INTERNAL_ERROR, HTTPStatus.BAD_REQUEST,
											 HTTPStatus.NOT_FOUND, HTTPStatus.MOVED_PERM, HTTPStatus.REQUEST_TIMEOUT, HTTPStatus.CONFLICT};
		
	/**
	 * The HTTPStatus code of the <code>HTTPStatus</code> object.
	 */
	private int code;
	
	/**
	 * The reason phrase of the <code>HTTPStatus</code> object. 
	 */
	private String message;
	
	/**
	 * Constructs a new <code>HTTPStatus</code> object with the specified code and message.
	 * 
	 * @param code		the HTTPStatus code
	 * @param message	the reason phrase
	 */
	public HTTPStatus(int code, String message) {
		this.code = code;
		this.message = message;
	} // HTTPStatus()
	
	/**
	 * Constructs a new <code>HTTPStatus</code> object with the specified code and message.
	 * 
	 * @param code		the HTTPStatus code
	 * @param message	the reason phrase
	 */
	public HTTPStatus(String code, String message) {
		this.code = Integer.parseInt(code);
		this.message = message;
	} // HTTPStatus()

	/**
	 * Returns the HTTPStatus code.
	 * 
	 * @return 	the HTTPStatus code.
	 */
	public int getCode() {
		return code;
	} // getCode()

	/**
	 * Sets the HTTPStatus code.
	 * 
	 * @param code 	the HTTPStatus code
	 */
	public void setCode(int code) {
		this.code = code;
	} // setCode()

	/**
	 * Returns the reason phrase.
	 * 
	 * @return 	the reason phrase
	 */
	public String getMessage() {
		return message;
	} // getMessage()

	/**
	 * Sets the reason phrase.
	 * 
	 * @param message 	the reason phrase
	 */
	public void setMessage(String message) {
		this.message = message;
	} // setMessage()
	
	/** 
	 * Returns the <code>String</code> representation of a <code>HTTPStatus</code> object.
	 * 
	 * @return 	the <code>String</code> representation of a <code>HTTPStatus</code> object
	 */
	public String getString() {
		return code + " " + message;
	} // getString()
	
} // HTTPStatus
