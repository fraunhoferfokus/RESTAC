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


/* Created on 04.04.07 */

package de.fhg.fokus.restac.httpx.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcherImpl;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXManagedActionMessageDispatcher;

/**
 * TODO: Documentation
 * 
 * @author Murat Ates
 * 
 */
public class HTTPXActionMessageTransceiver {
	private final static Logger LOGGER = Logger.getLogger(HTTPXActionMessageTransceiver.class);
	
	/* A singleton instance of <code>HTTPXActionMessageTransceiver</code> */
	private static HTTPXActionMessageTransceiver instance = null;
	
	private HTTPXActionMessageDispatcher outboundProxy;
	private HTTPXActionMessageDispatcher inboundProxy;
	
	private List<HTTPXManagedActionMessageDispatcher> dispatcherList;
	
	private boolean started = false;
	private static String DEFAULT_CONF_FILE = "/transceiver.properties";
	
	/**
	 * Constructs a new <code>HTTPXActionMessageTransceiver</code>
	 *
	 */
	public HTTPXActionMessageTransceiver(){
		outboundProxy = new HTTPXActionMessageDispatcherImpl();
		inboundProxy = new HTTPXActionMessageDispatcherImpl();
		dispatcherList = new ArrayList<HTTPXManagedActionMessageDispatcher>();
		
		instance = this;
	}
	
	/**
	 * Initialize and start the System.
	 * 
	 * @param config	the configuration file with all full named dispatchers
	 */
	public void start(String config){
		final String strOutbound = "outbound";
		final String strInbound = "inbound";
		final String strBoth = "inboundoutbound";
		
		Properties properties;
		
		LOGGER.debug("Loading Message Dispatchers");
		properties = new Properties();
		InputStream is = this.getClass().getResourceAsStream(config);

		try {
			if (is != null) {
				properties.load(is);
				
				Set<Map.Entry<Object, Object>> tmpSet = properties.entrySet();
				Iterator<Map.Entry<Object, Object>> it = tmpSet.iterator();
				while (it.hasNext()) {
					Map.Entry<Object, Object> element = it.next();					
					String className = (String)element.getValue();
					HTTPXManagedActionMessageDispatcher managedDispatcher = null;
					
					try {
						Class managedDefinition = HTTPXActionMessageTransceiver.class.getClassLoader().loadClass(className);
						//get the parameterized constructor
						Constructor constructor = managedDefinition.getConstructor(HTTPXActionMessageDispatcher.class);
						String key = ((String)element.getKey()).toLowerCase();
						//Create the object
						if (key.startsWith(strBoth)) {
							managedDispatcher = (HTTPXManagedActionMessageDispatcher)constructor.newInstance(outboundProxy);
							((HTTPXActionMessageDispatcherImpl)inboundProxy).addDispatcher(managedDispatcher);
							
						}else if (key.startsWith(strInbound)) {
							managedDispatcher = (HTTPXManagedActionMessageDispatcher)constructor.newInstance(inboundProxy);
							// to forward the registration of the message Handler
							// required only for inbound dispatcher
							((HTTPXActionMessageDispatcherImpl)inboundProxy).addDispatcher(managedDispatcher);
							
						} else if(key.startsWith(strOutbound)){
							managedDispatcher = (HTTPXManagedActionMessageDispatcher)constructor.newInstance(outboundProxy);
						}
						
						if (managedDispatcher != null) {	
							managedDispatcher.start();
							LOGGER.debug(className + " - is Created."); 
							dispatcherList.add(managedDispatcher);
						}
					} catch(ClassNotFoundException e) {
						LOGGER.error(e);						
					} catch (Exception e) {
						LOGGER.error(e);
						e.printStackTrace();
					}
				}
				
				this.started = true;
			
			}else
				LOGGER.info("Configfile '" +config+ "' could not be found");
			
		} catch (IOException e) {
			LOGGER.error("An error occured while loading properties",e);
			e.printStackTrace();
		}
		finally{
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				LOGGER.error("An error occured while closing the Inputsream to the properties configuration file ",e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Start the Transceiver. Use default config file.
	 * 
	 */
	public void start(){
		start(HTTPXActionMessageTransceiver.DEFAULT_CONF_FILE);
	}
	
	/**
	 * 
	 * @return true if transceiver was already started, false otherwise
	 */
	public boolean started() {
		return this.started;
	}
	
	public static synchronized HTTPXActionMessageTransceiver getInstance(){
		if (instance == null) {
			instance = new HTTPXActionMessageTransceiver();
		}
		return instance;
	}
	
	/**
	 * Instantiates a new instance of transceiver if none is instantiated so far
	 * and starts it if it is not started yet.
	 * 
	 * @return a running (singleton) instance of transceiver.
	 */
	public static synchronized HTTPXActionMessageTransceiver getRunningInstance(){
		
		if(!HTTPXActionMessageTransceiver.getInstance().started)
			instance.start(HTTPXActionMessageTransceiver.DEFAULT_CONF_FILE);
		
		return instance;
	}
	
	/**
	 * Deinitialize and shutdown the System. 
	 *
	 */
	public void shutdown(){
		for (HTTPXManagedActionMessageDispatcher dispatcher : dispatcherList) {
			dispatcher.shutdown();
		}
	}
	
	/**
	 * Returns the inbound proxy dispatcher.
	 * 
	 * @return	the inbound dispatcher
	 */
	public HTTPXActionMessageDispatcher getInboundDispatcher(){
		return inboundProxy;
	}
	
	/**
	 * Returns the outbound proxy dispatcher.
	 * 
	 * @return	the outbound dispatcher
	 */
	public HTTPXActionMessageDispatcher getOutboundDispatcher(){
		return outboundProxy;
	}
	
	/**
	 * 
	 * 
	 * @param queue
	 */
	public void addInboundDispatcher(HTTPXManagedActionMessageDispatcher queue){
		((HTTPXActionMessageDispatcherImpl)inboundProxy).addDispatcher(queue);
	}
	
	/**
	 * 
	 * 
	 * @param queue
	 */
	protected void addOutboundDispatcher(HTTPXManagedActionMessageDispatcher queue){
		//TODO: implement
	}
	
//	static {
//		HTTPXActionMessageTransceiver tranceiver = HTTPXActionMessageTransceiver.getInstance();
//		
//		tranceiver.start("/tranceiver.properties");
//	}
}
