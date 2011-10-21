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

package de.fhg.fokus.restac.resource.wrapping.annotations;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;

/**
* Marker annotation for restac-resources.
* Should be used for each class which is supposed to be exported as a resource for remote access.
* <p>
* Following parameters can optionally be specified for a resource 
* (default values are used in case no specification is given):
* <p>
* protocol - resource accepts only messages following this protocol, for example http, default is: http
* host - resource accepts messages only from this host (use "" for any host), default is: any host
* port - resource accepts messages only from this port (use "" for any port), default is: any port
* path - resource can be accessed by using this path, default is: class name
* query - resource accepts only this query in a message (URL-encoded query, use "" for any query), default is: any query
* <p>
* Additionally, the user should annotate which methods are to be exported for remote access, see RestacResourceMethod.
* 
* @see RestacResourceMethod
* @author Anna Kress
* */
@Target( { TYPE } ) // user should only annotate classes
@Retention( java.lang.annotation.RetentionPolicy.RUNTIME ) // annotation visible to jre
public @interface RestResource {

public String protocol() default HTTPXConstants.HTTP; //default is http
public String host() default ""; // default should be null, it seems we can only use "" to indicate this ???
public int port() default 0; // default is 0
public String path() default ""; // default should be null, it seems we can only use "" to indicate this ???
public String query() default ""; // default should be null, it seems we can only use "" to indicate this ???
}
