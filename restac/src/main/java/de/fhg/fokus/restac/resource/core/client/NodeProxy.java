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
 * Created on 22.09.2005
 */
package de.fhg.fokus.restac.resource.core.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageTransceiver;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.resource.core.common.Node;
import de.fhg.fokus.restac.resource.core.common.Resource;

/**
 * A local proxy for a remote <code>Node</code> object.
 * 
 * @author Stefan Foell
 * @see Node
 * @see ResourceProxy
 */
public class NodeProxy {

	private final static Logger LOGGER = Logger.getLogger(NodeProxy.class);
	
	/** The URL of the resource connected to this proxy. */
	private URL url;
	
	/** 
	 * The connection used to send calls on the proxy to the <code>Node</code> identified
	 * by the URL.
	 */
	private HTTPXActionMessageTransceiver transceiver = null;
	
	
	/**
	 * Constructs a new local proxy for a remote <code>Node</code> located at the 
	 * given URL. 
	 * 
	 * @param url	the URL of the resource
	 */
	public NodeProxy(URL url) {
		this.url = url;
	    this.transceiver = HTTPXActionMessageTransceiver.getInstance();
	}
	
	/**
	 * Constructs a new local proxy for a remote <code>Node</code> located at the
	 * given URL using the given connection.
	 *
	 * @param url			the URL of the resource
	 * @param connection	the connection to send calls to the stub to the resource
	 */
	public NodeProxy(URL url, HTTPXActionMessageTransceiver transceiver) {
	    this.transceiver = transceiver;
		this.url = url;
	}
		
	/**
	 * Returns the parent <code>Node</code> of this <code>Node</code>.
	 * 
	 * @return 	the parent <code>Node</code> of this <code>Node</code>
	 * @see 	de.fhg.fokus.rest2.resource.common.Node#getParent()
	 */
	public NodeProxy getParent(){
		try {
			Path path = new Path(url.getPath());
			if (path.hasPrevious()) {
					path.removeLastToken();
					return new NodeProxy(new URL(url.getProtocol(),url.getHost(),url.getPort(),path.getString()), this.transceiver);
			}
		}
		catch (MalformedURLException e) {
			LOGGER.error(e);
		}
		return null;
	}
	
	/**
	 * Returns a child <code>Node</code> of this node with the specified name.
	 * 
	 * @return	a child <code>Node</code> of this node with the specified name
	 * @see 	de.fhg.fokus.rest2.resource.common.Node#getChild(java.lang.String)
	 */
	public NodeProxy getChild(String name){
		try {
			Path path = new Path(url.getPath());
			String[] words = name.split("/");
			if (words.length > 0) {
				for(int i = 0; i < words.length; i++) {
					if (!words[i].equals(""))
					path.addToken(words[i]); 
				}
			}
			return new NodeProxy(new URL(url.getProtocol(),url.getHost(),url.getPort(),path.getString()), this.transceiver);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e);
		}
		return null;
	}
	
	/**
	 * Returns a child <code>Node</code> of this node with the indicated child path.
	 * 
	 * @return	a child <code>Node</code> of this node with the indicated child path
	 * @see 	de.fhg.fokus.rest2.resource.common.Node#getChild(java.lang.String)
	 */
	public NodeProxy getChild(Path child){
		try {
			Path path = new Path(url.getPath());
			while (child.hasNext() ){
				child.next();
				path.addToken(child.getCurrentToken());
			}
		
			return new NodeProxy(new URL(url.getProtocol(),url.getHost(),url.getPort(),path.getString()), this.transceiver);
		}
		catch (MalformedURLException e) {
			LOGGER.error(e);
		}
		return null;
	}

	/**
	 * Returns the resource associated with the <code>Node</code>.
	 * 
	 * @return	the resource associated with the <code>Node</code>
	 * @see 	de.fhg.fokus.rest2.resource.common.Node#getResource(java.util.Map)
	 */
	public ResourceProxy getResource(ParameterList query){
		if(query != null && query.getSize() > 0) {												
			String queryString = "";
			for (final Iterator<String> i = query.getAllKeys().iterator(); i.hasNext();) {
				String name = i.next();
				String value = query.getFirstParameter(name);
				queryString += name;
				if(value != null) {
					queryString += HTTPXConstants.NAME_VALUE_SEPARATOR + value + (i.hasNext() ? "&" : HTTPXConstants.EMPTY) ; // query can contain parameter without value
				}
			}
			try {
				URL resourceUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath() + "?" + queryString);
				return new ResourceProxy(resourceUrl, transceiver);
			}
			catch(MalformedURLException e) {
				LOGGER.error("Could not create resourceUrl.", e);
				return new ResourceProxy(url, transceiver);
			}
		}
		else
			return new ResourceProxy(url,transceiver);	
	}
		
	/**
	 * Sets the URL of this node proxy.
	 * 
	 * @param url	the URL of this node proxy
	 */
	public void setResourceURL(URL url) {
		this.url=url;
	}
	
	/**
	 * Returns the URL of this node proxy.
	 * 
	 * @return	the URL of this node proxy
	 */
	public URL getResourceURL() {
		return url;
	}
	
	
}