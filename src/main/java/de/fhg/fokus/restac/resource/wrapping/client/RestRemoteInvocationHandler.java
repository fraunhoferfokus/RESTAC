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

package de.fhg.fokus.restac.resource.wrapping.client;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.core.dispatcher.HTTPXActionMessageDispatcher;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentReader;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentWriter;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXChunkedOutputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXInputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXStreamFactory;
import de.fhg.fokus.restac.resource.wrapping.common.WrapperConstants;
import de.fhg.fokus.restac.resource.wrapping.common.exceptions.InvocationException;
import de.fhg.fokus.restac.resource.wrapping.serialization.ParserFunction;
import de.fhg.fokus.restac.resource.wrapping.serialization.SerializerFunction;


/**
	 * Implements an invocation handler which handles method invocations on 
	 * remote resources.
	 * 
	 * @author Stefan Föll
	 *
	 */
	public class  RestRemoteInvocationHandler implements InvocationHandler {
		
		private final static Logger LOGGER = Logger.getLogger(RestRemoteInvocationHandler.class);
		
		private HTTPXActionMessageDispatcher outboundDispatcher;
		
		private URL resourceURL;
		
	/**
	 * Constructs a new <code>InvocationHandler</code> which maps
	 * method invocation on a local proxy to a REST based communication pattern
	 * 
	 * @param resourceUrl the URL identifying the remote resource to which
	 *                            REST invocations are delegated to
	 */
	public RestRemoteInvocationHandler(URL resourceUrl, HTTPXActionMessageDispatcher outboundDispatcher) {
			this.resourceURL = resourceUrl;
			this.outboundDispatcher = outboundDispatcher;
		}
			
		/** 
		 * Method invocation on a local proxy is delegated to this method.
		 *
		 *@param proxy the proxy object on which the method was invoked
		 *@param 
		 */
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				return invoke(method.getName(), args, method.getReturnType());
			} catch (Exception e){
				LOGGER.error("Method delegate for method " + method.getName() + " failed.");
				throw new Exception("Method delegate for method " + method.getName() + " failed.", e);
			}
		}
					
		/**
		 * Performs a remote method invocation for a method call on this local proxy. 
		 * 
	 	 * @param method		the method to be invoked
	 	 * @param arguments		the arguments of the method
	 	 * @param resultType	the result type of the method
	 	 * @return				the result of the method invocation
	 	 */		 
		private Object invoke(String method, Object[] arguments, Class<?> resultType) throws InvocationException {
	  		//ResourceProxy resource = null;
	  		Map<String, String> allParams = new HashMap<String, String>();
	  		Map<String, String> param;
			SerializerFunction serializer=SerializerFunction.getInstance();
			char prefix = WrapperConstants.PARAMETER_NAME_DEFAULT_PREFIX.charAt(0);//97; //'a'
			if (arguments == null) arguments = new Object[0];
			for (int i = 0; i < arguments.length; ++i) {
				param = getPrefixedMap(serializer.serialize(arguments[i].getClass(), arguments[i]), prefix);
				allParams.putAll(param);
				prefix++;
			}

			Map<String, String> responseContent = new HashMap<String, String>();
			ParameterList paramQuery = new ParameterList();
				// hässliches "casten" ...
				for(Iterator<String> iter = allParams.keySet().iterator(); iter.hasNext();) {
					String name = iter.next();
					String value = allParams.get("name");
					paramQuery.setParameter(name, value);
				}
//			 hässliches "casten" 2...
				Path path = new Path(this.resourceURL.getPath());
			

		try {
				if (ParserFunction.getInstance().isParseable(resultType)) {
					if(method.startsWith(WrapperConstants.PREFIX_GET)) { //invocation of a getter

						path.addToken(method.substring(3, method.length()));
						
						HTTPXActionMessage request = new HTTPXActionMessage("GET", this.resourceURL.getProtocol(), 
							this.resourceURL.getHost(), new Integer(this.resourceURL.getPort()), path, paramQuery, null, null);
			
						
						HTTPXStatusMessage response;
						try {
							response = outboundDispatcher.deliverSynchronous(request);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							response=null;
						}
						
						if(response.getInputStream() != null) {
							HTTPXInputStream responseInstream =  HTTPXStreamFactory.getHTTPXInputStream(response);
							
							
							if (responseInstream.getContentType() != null && responseInstream.getContentType().equals(HTTPXConstants.TYPE_APP_URLENCODED)) {
								// read and parse content of stream:
								
								UrlEncodedContentReader rd = new UrlEncodedContentReader(responseInstream);
								responseContent = (Map<String, String>)rd.readBuffered();

							}
		 	                else {
		 	                   	LOGGER.error("Client response content type either not set or cannot be processed: " + responseInstream.getContentType());
		 	                   	throw new InvocationException("response content cannot be processed - only url encoded content is possible");
		 	                }

	 	                
						}
							else
								throw new InvocationException("response content was empty");

					}
					else if(method.startsWith(WrapperConstants.PREFIX_SET) && (resultType.equals(Void.class) || resultType.equals(Void.TYPE))) { // invocation of a setter
						path.addToken(method.substring(3, method.length()));
						
						HTTPXChunkedOutputStream out = new HTTPXChunkedOutputStream(HTTPXConstants.TYPE_APP_URLENCODED, HTTPXConstants.DEFAULT_CHARSET);
						
						UrlEncodedContentWriter wr = new UrlEncodedContentWriter(out);
						
						wr.writeBuffered(allParams);
						
						HTTPXActionMessage request = out.constructHTTPXActionMessage("PUT", this.resourceURL.getProtocol(), 
							this.resourceURL.getHost(), new Integer(this.resourceURL.getPort()).intValue(), path, null, new HashMap<String, String>());
			
						try {
							HTTPXStatusMessage response = outboundDispatcher.deliverSynchronous(request);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null; //put doesn't return anything
						//if (response.getStatusCode().equals((new Integer(HTTPStatus.OK.getCode())).toString())){
							
						//TODO status messages auslesen bei problemfall...}
						
					}
					else { //invocation of a normal method 
						path.addToken(method);
						
						HTTPXActionMessage request;
						
						HTTPXChunkedOutputStream out = new HTTPXChunkedOutputStream(HTTPXConstants.TYPE_APP_URLENCODED, HTTPXConstants.DEFAULT_CHARSET);
						
							UrlEncodedContentWriter wr = new UrlEncodedContentWriter(out);
							wr.writeBuffered(allParams);
							
							request = out.constructHTTPXActionMessage("POST", this.resourceURL.getProtocol(), 
									this.resourceURL.getHost(), new Integer(this.resourceURL.getPort()).intValue(), path, null, new HashMap<String, String>());
						

						
						HTTPXStatusMessage response;
						try {
							response = outboundDispatcher.deliverSynchronous(request);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							response=null;
						}
						
						if(response.getInputStream() != null) {
							HTTPXInputStream responseInstream = HTTPXStreamFactory.getHTTPXInputStream(response);
							
							
							if (responseInstream.getContentType().equals(HTTPXConstants.TYPE_APP_URLENCODED)) {
								UrlEncodedContentReader rd = new UrlEncodedContentReader(responseInstream);
								responseContent = (Map<String, String>)rd.readBuffered();
							}
		 	                else {
		 	                   	LOGGER.error("Client response content " + responseInstream.getContentType() + " cannot be processed");
		 	                   	throw new InvocationException("response content cannot be processed - only url encoded content is possible");
		 	                }

	 	                
						}
							else
								throw new InvocationException("Response content was empty");
					}
			}
				
				else if (resultType.isInterface() && !method.startsWith(WrapperConstants.PREFIX_SET)) {
					//TODO}
				}
				
		} catch (ContentConvertingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (HTTPXProtocolViolationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
//				else if (resultType.isInterface() && !method.startsWith(WrapperConstants.PREFIX_SET)) {
//					if (method.startsWith(WrapperConstants.PREFIX_GET))method = method.substring(3);
//					resource = (ResourceProxy)node.getChild(method).getResource(allParams);
//					resource.head();
//					URL resourceUrl = ((NodeProxy)node).getResourceURL();
//					try {
//						URL newUrl = new URL(resourceUrl.getProtocol(),resourceUrl.getHost(),resourceUrl.getPort(), resourceUrl.getPath() + "/" + method);
//						return ObjectLookupService.getInstance().createRemoteProxy(newUrl,resultType);
//					}
//					catch (MalformedURLException e) {
//						LOGGER.error("Creating new RestProxy failed because of incorrect URL for path: " + resourceUrl.getPath() + "/" + method);
//						throw new InvocationException("Creating new RestProxy failed because of incorrect URL for path: " + resourceUrl.getPath() + "/" + method);
//						}
//				}
//				else throw new InvocationException("No parser found for result type " + resultType.getName());
//			}
//			catch (MovedPermanentlyException e1) {
//				String urlString = e1.getURL();
//				try {
//					URL resourceUrl = new URL(urlString);
//					((NodeProxy)node).setResourceURL(resourceUrl);
//			    	return invoke(method, arguments, resultType);
//				}
//				catch (MalformedURLException e2) {
//					LOGGER.error("Http Moved received for request - creating request to new Location: " + urlString + " failed beacause of malformed URL Exception");
//					throw new InvocationException("Http Moved received for request - creating request to new Location: " + urlString + " failed beacause of malformed URL Exception");
//				} 
//			}
//			catch(ResourceException e) {
//				throw new InvocationException(e.getMessage());
//			}

		
              	return ParserFunction.getInstance().parse(resultType, responseContent);

			}
				
		/**
		 * Sets the prefixes on a name-value map for serialization.
		 * 
		 * @param input		a name-value map
		 * @param prefix	the prefix to set on all elements of the name-value map 
		 * @return			the name-value map with prefixes
		 */
		private Map<String, String> getPrefixedMap(Map<String, String> input, char prefix){
			Map<String, String> result = new HashMap<String, String>();
			for (final Iterator<String> i = input.keySet().iterator(); i.hasNext();){
				String name = i.next();
				String value = input.get(name);
				result.put(prefix + WrapperConstants.PARAMETER_NAME_SEPARATOR + name, value);
			}
			return result;
		}

	}