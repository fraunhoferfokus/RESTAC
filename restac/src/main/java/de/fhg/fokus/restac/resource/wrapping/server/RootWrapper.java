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
 * Created on Sep 8, 2005
 */
package de.fhg.fokus.restac.resource.wrapping.server;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.resource.core.common.Node;
import de.fhg.fokus.restac.resource.core.common.Resource;
import de.fhg.fokus.restac.resource.core.handler.HTTPXActionMessageFilterResourceImpl;
import de.fhg.fokus.restac.resource.core.handler.ResourceMessageHandler;
import de.fhg.fokus.restac.resource.wrapping.annotations.RestResourceMethod;
import de.fhg.fokus.restac.resource.wrapping.common.WrapperConstants;

/**
 * A resource message handler that allows to wrap Java objects to resources.
 * 
 * @author David Linner
 * changed by Anna Kress 01.06.07
 * @see ResourceMessageHandler
 */
public class RootWrapper extends ResourceMessageHandler {

	private final static Logger LOGGER = Logger.getLogger(RootWrapper.class); 
	
	/** The wrapped object. */
	private Object object;

	/**
	 * Constructs a new root wrapper with the specified Restlet context and wrapped
	 * object.
	 * 
	 * @param context	the context of this Restlet
	 * @param object	the wrapped object
	 */
	public RootWrapper(HTTPXActionMessageFilterResourceImpl filter, Object object) {
		super(filter);
		this.object = object;
	}

	/**
	 * Returns the child node of this node with the specified name.
	 * 
	 * @param name	the name of the child node to be returned
	 * @return		the child node
	 */
	public Node getChild(String name) {

		try {
			Method [] methods = object.getClass().getMethods(); 
			
			for (int i = 0; i < methods.length; i++){
				if (methods[i].getName().equals(WrapperConstants.PREFIX_GET + name) ||
					methods[i].getName().equals(WrapperConstants.PREFIX_SET + name) ||
					methods[i].getName().equals(name)){
					
					if(methods[i].isAnnotationPresent(RestResourceMethod.class)) //if method is annotated for remote access
						return new InvocationWrapper(this, object, name); // TODO eigentlich könnte man mit einer vorhandenen instanz arbeiten ???
				} 
			}
			
			LOGGER.warn("Requested method with base name '" + name + "' could not be retrieved.");				
		} catch (SecurityException e) {
			LOGGER.error("The security level of the method '" + WrapperConstants.PREFIX_GET + name + "()' is to high.",e);
		} catch (IllegalArgumentException e) {
			LOGGER.error("Invocation context of method '" + WrapperConstants.PREFIX_GET + name + "()' was invalid.", e);
		}
		return null;
	}

	/**
	 * Returns the root resource connected to this node.
	 * 
	 * @return	the root resource connected to this node
	 */
	public Resource getResource(ParameterList query) {
		return null;
	} 
}
