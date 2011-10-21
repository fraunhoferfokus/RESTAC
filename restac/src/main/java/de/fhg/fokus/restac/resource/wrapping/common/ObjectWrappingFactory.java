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

package de.fhg.fokus.restac.resource.wrapping.common;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.resource.core.handler.HTTPXActionMessageFilterResourceImpl;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageTransceiver;
import de.fhg.fokus.restac.resource.wrapping.annotations.RestResource;
import de.fhg.fokus.restac.resource.wrapping.annotations.RestResourceMethod;
import de.fhg.fokus.restac.resource.wrapping.client.RestRemoteInvocationHandler;
import de.fhg.fokus.restac.resource.wrapping.server.RootWrapper;

/**
 * With an <code>ObjectWrappingFactory</code> developers can easily provide Java objects
 * as remote REST services, and use such services through object wrappers.
 * 
 * @author Philippe Bößling
 * @author Anna Kress - changed on 01.06.07
 */
public class ObjectWrappingFactory {
	
	private final static Logger LOGGER = Logger.getLogger(ObjectWrappingFactory.class);

	private HTTPXActionMessageTransceiver transceiver;
	
	private HashMap<String, RegistrationInfo> registeredObjects; // all registered objects so far

	public ObjectWrappingFactory() {
		// get inbound dispatcher where handler will be registered
		transceiver = HTTPXActionMessageTransceiver.getInstance();
		transceiver.start("/transceiver.properties");
		this.registeredObjects = new HashMap<String, RegistrationInfo>();
	}

	/**
	 * Registers an object as remote REST service. The object
	 * should be annotated as rest resource. All methods to 
	 * be exported for remote access should be annotated as resource methods.
	 * 
	 * @param object -	the object
	 * 
	 * @see RestResource
	 * @see RestResourceMethod
	 * 
	 */
	public void registerObject(Object object){
		RestResource annotation = object.getClass().getAnnotation(RestResource.class);
	
		String host = annotation.host().equals("") ? null : annotation.host();
		String path = annotation.path().equals("") ? WrapperConstants.PATH_NAME_SEPARATOR + object.getClass().getSimpleName() : WrapperConstants.PATH_NAME_SEPARATOR + annotation.path();
		String query = annotation.query().equals("") ? null : annotation.query();
		
		HTTPXActionMessageFilterResourceImpl filter = new HTTPXActionMessageFilterResourceImpl(annotation.protocol(), host,
				annotation.port(), path, query);
		
		// instantiate and register new root wrapper
		RootWrapper rootWrapper = new RootWrapper(filter, object);	
		this.registeredObjects.put(path, new RegistrationInfo(filter, rootWrapper));
		this.transceiver.getInboundDispatcher().addActionMessageHandler(filter, rootWrapper);
	}
	
	/**
	 * Unregisters object.
	 * 
	 * @param object
	 */
	public void unregisterObject(Object object){
		RestResource annotation = object.getClass().getAnnotation(RestResource.class);
		String path = annotation.path().equals("") ? WrapperConstants.PATH_NAME_SEPARATOR + object.getClass().getSimpleName() : WrapperConstants.PATH_NAME_SEPARATOR + annotation.path();
		RegistrationInfo info = this.registeredObjects.get(path);
		if(info != null) {
			this.transceiver.getInboundDispatcher().deleteActionMessageHandler(info.filter, info.rootWrapper);
			this.registeredObjects.remove(path);
		}	
	}
	
	/**
	 * Creates a client side object wrapper for the given interface class.
	 * 
	 * @param url		url of object to be accessed
	 * @param objClass	the interface class of the object
	 * @return			a client side object wrapper
	 */ 
	public Object getObjectWrapper(URL url, Class iface) { 
 		Class[] ifaces  = {iface};
		ClassLoader	cloader = iface.getClassLoader();
		Object proxy = Proxy.newProxyInstance(cloader, ifaces, new RestRemoteInvocationHandler(url, 
				transceiver.getOutboundDispatcher()));
		return proxy;
	}
	
	
	// class holds information about a registered object, so that it can be
	// unregistered again
	private class RegistrationInfo {
		public HTTPXActionMessageFilterResourceImpl filter;
		public RootWrapper rootWrapper;
		
		RegistrationInfo(HTTPXActionMessageFilterResourceImpl filter, RootWrapper rootWrapper) {
			this.filter = filter;
			this.rootWrapper = rootWrapper;
		}
	}
	
}
