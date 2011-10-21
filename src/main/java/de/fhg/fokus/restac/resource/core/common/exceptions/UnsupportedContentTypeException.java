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
 * Created on Sep 7, 2005
 * 
 * Last changed: 01.06.07 by Anna Kress
 * 
 */
package de.fhg.fokus.restac.resource.core.common.exceptions;

import de.fhg.fokus.restac.httpx.core.common.HTTPStatus;



/**
 * Signals that the content-type of the body of a request could not be
 * processed. 
 * 
 * @author David Linner
 * @see ResourceException
 */
public class UnsupportedContentTypeException extends ResourceException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 536453554192351460L;

	/**
	 * Constructs a new <code>UnsupportedContentTypeException</code> with the specified message.
	 * 
	 * @param message	the message
	 */
	public UnsupportedContentTypeException(String message) {
		super(message);
	}

	/**
	 *Returns the status connected to this exception.
	 *
	 * @return	the status connected to this exception
	 */
	public HTTPStatus getStatus(){
		return HTTPStatus.UNSUPPORTED_TYPE;
	} 
	
}
