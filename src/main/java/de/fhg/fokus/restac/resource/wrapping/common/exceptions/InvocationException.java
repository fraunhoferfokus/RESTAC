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
 * Created on 26.09.2005
 */

package de.fhg.fokus.restac.resource.wrapping.common.exceptions;

/**
 * Signals that an error occured while trying to invoke a method on a proxy node.
 * 
 * @author Philippe B��ling
 */
public class InvocationException extends ProxyException {
	
	/**
	 * Constructs a new empty <code>InvocationException</code>.
	 */
	public InvocationException() {
		super();
	}

	/**
	 * Construct a new <code>InvocationException</code> with the specified message.
	 * 
	 * @param message	the message of the exception
	 */
	public InvocationException(String message) {
		super(message);
	}
	
}
