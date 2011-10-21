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
 */
package de.fhg.fokus.restac.resource.wrapping.serialization;

import java.util.Map;

/**
 * Implements to processing of a list of name value pairs to create an instance
 * of a certain type from it. A parser MUST NOT be implemented to act tolerant. If the
 * set of given name-value pairs cannot processed exactly, the parser is required 
 * to fail.
 *  
 * @author David Linner
 * @see ParserFunction
 */
public interface Parser {
	
	/**
	 * Parses a list of name-values and finally creates an object from it. 
	 * 
	 * @param params	the name-value pairs to be parsed
	 * @return			the Java object created from the name-value pairs
	 */
	public Object parse(Map<String, String> params);
}
