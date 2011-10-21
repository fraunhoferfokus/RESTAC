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
package de.fhg.fokus.restac.resource.wrapping.server;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentReader;
import de.fhg.fokus.restac.httpx.util.serialization.converter.UrlEncodedContentWriter;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXChunkedOutputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXInputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXOutputStream;
import de.fhg.fokus.restac.resource.core.common.Get;
import de.fhg.fokus.restac.resource.core.common.Head;
import de.fhg.fokus.restac.resource.core.common.Node;
import de.fhg.fokus.restac.resource.core.common.Post;
import de.fhg.fokus.restac.resource.core.common.Put;
import de.fhg.fokus.restac.resource.core.common.Resource;
import de.fhg.fokus.restac.resource.core.common.exceptions.BadContentException;
import de.fhg.fokus.restac.resource.core.common.exceptions.InternalException;
import de.fhg.fokus.restac.resource.core.common.exceptions.NotFoundException;
import de.fhg.fokus.restac.resource.core.common.exceptions.ResourceException;
import de.fhg.fokus.restac.resource.core.common.exceptions.UnsupportedContentTypeException;
import de.fhg.fokus.restac.resource.wrapping.annotations.RestResourceMethod;
import de.fhg.fokus.restac.resource.wrapping.common.WrapperConstants;
import de.fhg.fokus.restac.resource.wrapping.serialization.ParserFunction;
import de.fhg.fokus.restac.resource.wrapping.serialization.SerializerFunction;

/**
 * The InvocationWrapper is the core class of the wrapping model. Its major function is to map the information
 * applied by a certain request on a resource to method-invocation on a java-object. It asserts that 
 * contents of PUT and POST operations are represented as name-value-pairs. This concept originates from
 * the first implementation of the REST-Layer. For details see the diploma thesis of Witold Drytkiwicz.
 *     
 * @author David Linner
 * 
 * changed by Anna Kress 01.06.07
 */
public class InvocationWrapper implements Node, Put, Post, Get, Head {

	private final static Logger LOGGER = Logger.getLogger(InvocationWrapper.class); 
	
	/** The wrapped object. */
	private Object object;
	
	/** The name of the resource. */
	private String token;
	
	/** The parent node. */
	private Node parent;
	
	/** The query parameters for a request on this <code>InvocationWrapper</code>. */
	private Map<String, String> query;
	
	/**
	 * Constructs a new invocation wrapper with the specified parent node, object to
	 * be wrapped and name.
	 * 
	 * @param parent	the parent node
	 * @param object	the object to be wrapped
	 * @param token		the name of the object
	 */
	public InvocationWrapper(Node parent, Object object, String token){
		this.parent = parent;
		this.object = object;
		this.token = token;
	}
	
	/**
	 * Returns the parent node of this node.
	 * 
	 * @return	the parent node of this node
	 */
	public Node getParent(){
		return parent;
	}
	
	/**
	 * Retrieves a child node of this node. Since every <code>InvocationWrapper</code> addresses a pair of an object
	 * and a token, this implementation tries to find a get-Method for the applied token with no parameters and
	 * returns a new <code>InvocationWrapper</code> that contains the resulting object of the method-invocation and the
	 * child-name. 
	 * 
	 * @param name 	the root of a method-name (e.g. 'Value' for 'getValue' or 'setValue')
	 * @return 		a new node wrapping another object and the passed name as method-identifier   
	 */
	public Node getChild(String name) {
		Class cls = object.getClass();
		try {
			Method method = cls.getMethod(WrapperConstants.PREFIX_GET + token, null);
			
			if (method != null && method.isAnnotationPresent(RestResourceMethod.class)){
				if (method.getReturnType().isPrimitive()){
					return null;
				} else {
					return new InvocationWrapper(this, method.invoke(object, null), name);
				}
			} else {
				LOGGER.warn("Requested method '" + WrapperConstants.PREFIX_GET + name + "()' could not be retrieved.");				
			}
		} catch (SecurityException e) {
			LOGGER.error("The security level of the method '" + WrapperConstants.PREFIX_GET + name + "()' is to high.",e);
		} catch (NoSuchMethodException e) {
			LOGGER.warn("Requested method '" + WrapperConstants.PREFIX_GET + name + "()' does not exist.");
		} catch (IllegalArgumentException e) {
			LOGGER.error("Invocation context of method '" + WrapperConstants.PREFIX_GET + name + "()' was invalid.", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("Invocation context of method '" + WrapperConstants.PREFIX_GET + name + "()' was invalid.", e);
		} catch (InvocationTargetException e) {
			LOGGER.error("Invocation of the method '" + WrapperConstants.PREFIX_GET + name + "()' on the target object of type'" + object.getClass().getName() + "' failed.", e);
		}
		return null;
	}
	
	/**
	 * Gets a generic instance of a resource, that implements according to the class of the wrapped object and the method-identifier 
	 * assigned to this wrapper-instance, <code>Put</code>, <code>Get</code>, <code>Head</code>, or <code>Post</code> interfaces. 
	 * If for example the wrapped object implements a 'getValue'-method and the method-identifier of this wrapper is 'Value', the 
	 * resource will implement the <code>Get</code>-interface.  
	 *    
	 * @return An implementation of <i>Resource</i>.  
	 */
	public Resource getResource(ParameterList query) {

		// hässliches "casten" ...
		for(Iterator<String> iter = query.getAllKeys().iterator(); iter.hasNext();) {
			String name = iter.next();
			String value = query.getFirstParameter(name);
			this.query.put(name, value);
		}
		
		Resource resource = null;
		Class [] classes = getResourceInterfaces(object.getClass(), token);
		
		if (classes != null){
			resource = (Resource)Proxy.newProxyInstance(
	     		Resource.class.getClassLoader(),
				classes,
                new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						try {
							return method.invoke(InvocationWrapper.this, args);
						} catch (Exception e){
							LOGGER.error("Delegate failed!");
							throw e.getCause();
						}
						
					}
				});
		}
		return resource;
	}	
		
	/**
	 * Analyzes what types of http methods can be supported by this resource. 
	 * 
	 * @return The array of interfaces the resource needs to implement.
	 */
	private Class[] getResourceInterfaces(Class cls, String name){
		
		Set<Class> interfaces = new HashSet<Class>();
		Method [] methods = cls.getMethods(); 
		for (int i = 0; i < methods.length; i++){
			if (methods[i].getName().equals(WrapperConstants.PREFIX_GET + name) && 
					methods[i].isAnnotationPresent(RestResourceMethod.class)){
				interfaces.add(Get.class);
				interfaces.add(Head.class);
			} else if (methods[i].getName().equals(WrapperConstants.PREFIX_SET + name) && 
					methods[i].isAnnotationPresent(RestResourceMethod.class)){
				interfaces.add(Put.class);
				interfaces.add(Post.class);
			} else if (methods[i].getName().equals(name) && 
					methods[i].isAnnotationPresent(RestResourceMethod.class)){
				interfaces.add(Post.class);
			} 
		}
		
		return interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : null;
	}
	
	/**
	 * Lazy search that tries to find a mapping of a set of name-value pairs to a list 
	 * of classes. If the search is successful the method will be invoked. 
	 * 
	 * @param params 				a map of name value pairs, received during a request
	 * @param isCheckOnly 			defines whether a found method should be executed or not 
	 * @return 						the return value of the invokated method
	 * @throws CallFailedException 	if no matching method can be found
	 */	
	private Object call(String methodName, Map<String, String> params, boolean isCheckOnly) throws CallFailedException, NoMatchFoundException  {
		
		ParserFunction reader = ParserFunction.getInstance();
		Vector<Object> parsedParams; 
		
		// get possible type combinations
		Class[][] options = getTypeSet(methodName, object.getClass()); 
		// get a list of name-value maps, wherein each map represents a certain instance 
		Map<String, String> [] separated = splitByNamePrefix(params);

		// Search all possible type combinations for a match with the given sets of name-value-pairs
		// if a candidate list of instances is found, take the method the combination is reffering to and
		// invoke it.

		//instead of searching a matrix of type combinations, a kind of search tree would
		//be fine, especially for methods with many overloads 
		for (int i = 0; i < options.length; i++){
			if (options[i].length == separated.length){
				parsedParams = new Vector<Object>(); 
				
				// check if parseble
				for (int j = 0; j < options[i].length; j++){
					Object candidate = reader.parse(options[i][j], separated[j]);
					if (candidate != null){
						parsedParams.add(candidate);
					} else {
						break; 
					}
				}
				
				// if candidate combination is found, try to invoke method
				if (parsedParams.size() == separated.length){
					if (isCheckOnly) return null;
					try {
						Method meth = object.getClass().getMethod(methodName, options[i]);
						return meth.invoke(object, parsedParams.toArray());
					} catch (SecurityException e) {
						LOGGER.error("Method '" + methodName + "' not accessible due to its protection level.");
					} catch (NoSuchMethodException e) {
						LOGGER.error("Found method '" + methodName + "' cannot be resolved.");
					} catch (IllegalArgumentException e) {
						LOGGER.error("Method '" + methodName + "' was called with wrong arguments.");
					} catch (IllegalAccessException e) {
						LOGGER.error("Method '" + methodName + "' cannot be accessed.");
					} catch (InvocationTargetException e) {
						LOGGER.error("Method '" + methodName + "' failed to execute.", e);
					}
					throw new CallFailedException("Method '" + methodName + "' failed to execute."); 
				}
			}
		}
		throw new NoMatchFoundException("No matching signature for method '" + methodName + "' found."); 
	}
	
	/**
	 * Splits a map of name value pairs dependent on prefixes used within the names to
	 * a list of multiple maps while removing the prefixes. 
	 * Prefixes are used to separate the list of name-value pairs within a request
	 * according to the data objects the name-value pairs represent. Normally the separator is a dot.
	 *    
	 * @param params 	the map to split
	 * @return 			the list of splitt maps 
	 */
	private  Map<String, String>[] splitByNamePrefix(Map<String, String> params) {
		Vector<Map<String, String>> result = new Vector<Map<String, String>>();
		
		// Escape this procedure if no parameters were applied.  
		if (params == null) return new HashMap[0];
		
		// Bring all name-value-pairs into a lexicographical order   
		Map<String, String> sortedEntries = Collections.synchronizedMap(new TreeMap<String, String>());
		for(Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
			String name = iter.next();
			String value = params.get(name);
			sortedEntries.put(name, value);
		}
		params = sortedEntries; 
		
		// Try to separate all name-value.pairs according to their name prefixes into
		// single name-value map. A missing name-prefix is always regarded as new name-prefix 
		// and hence a new map only containing on pair created.
		Map<String, String> currentMap = null;
		String currentPrefix = null; 
		for(Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
			String name = iter.next();
			String value = params.get(name);
			int sepIndex = name.indexOf(WrapperConstants.PARAMETER_NAME_SEPARATOR);
			if(sepIndex > -1) {
				String newPrefix = name.substring(0, sepIndex);
				if(!newPrefix.equals(currentPrefix)) {
					if(currentMap != null) {
						result.add(currentMap);
					}
					currentMap = new HashMap<String, String>();
					currentPrefix = newPrefix;
				}
				currentMap.put(name.substring(sepIndex + 1), value);
			}
			else {
				LOGGER.warn("The parameter name '" + name + "' does not match the preferred name-space encoding (prefix.name=value). This may prevent the request from being processed correctly.");
				if(currentMap != null) {
					result.add(currentMap);
				}
				currentMap = new HashMap<String, String>();
				currentMap.put(name, value);
			}
		}
		if(currentMap != null) {
			result.add(currentMap);
		}
		
		return result.toArray(new HashMap[result.size()]);
	}

	/**
	 * Retrieves the list of types from all overloaded implementations of the given method.
	 * 
	 * @param methodName 	name of the method the overloads should be checked for
	 * @return				list of parameter types of the overloaded implementations
	 * 						of the method identified by the name
	 */
	private Class[][] getTypeSet(String methodName, Class cls){
		Vector<Class[]> result = new Vector<Class[]>();
		Method [] methods = cls.getMethods();
		for (int i = 0; i < methods.length; i++){
			if (methods[i].getName().equals(methodName) && methods[i].isAnnotationPresent(RestResourceMethod.class)){
				result.add(methods[i].getParameterTypes());
			}
		}
		return result.toArray(new Class[result.size()][]);
	}
	
	
	/**
	 * Implements a <code>GET</code> request on a Java object by searching a matching get-method to 
	 * delegate the request to. 
	 * 
	 * @return						the content of the body of the response
	 * @throws ResourceException	if an error occured while processing the request
	 * 								on this resource
	 * @throws BadContentException 
	 */
	public HTTPXOutputStream get() throws ResourceException{
		try {
			Object result = call(WrapperConstants.PREFIX_GET + token, query, false);
			if (result != null){
				Map<String, String>  map = SerializerFunction.getInstance().serialize(result.getClass(), result);
				if (map != null){
					HTTPXChunkedOutputStream out = new HTTPXChunkedOutputStream(HTTPXConstants.TYPE_APP_URLENCODED, HTTPXConstants.DEFAULT_CHARSET);
					
					UrlEncodedContentWriter wr = new UrlEncodedContentWriter(out);
					
					wr.writeBuffered(getPrefixedMap(map));
					
					return out;
				
				} else 
					throw new BadContentException("Cannot process the result of the request. Please do not request it again.");
			}			
		} catch (CallFailedException e) {
			LOGGER.error("Getting parameterized resource failed. " + e.getMessage());
			throw new InternalException("Request could not be processed successfully.");
		} catch (NoMatchFoundException e) {
			LOGGER.debug("Parameterized resource was not found.");
			throw new NotFoundException("No resource found for the given parameters.");
		} catch (ContentConvertingException e) {
			throw new InternalException("Request could not be processed successfully.");
		}
		return null;
	}
	
	/**
	 * Implements a <code>HEAD</code> request on a Java object by searching a matching get-method to 
	 * delegate the request to. 
	 *
	 * @throws ResourceException	if an error occured while processing the request
	 * 								on this resource
	 */
	public void head() throws ResourceException {
		try {
			call(WrapperConstants.PREFIX_GET + token, query, true);	
		} catch (CallFailedException e) {
			LOGGER.error("Heading parameterized resource failed. " + e.getMessage());
			throw new InternalException("Request could not be processed successfully.");
		} catch (NoMatchFoundException e) {
			LOGGER.debug("Parameterized resource was not found.");
			throw new NotFoundException("No resource found for the given parameters.");
		}
	}
	
	/**
	 * Implements a <code>PUT</code> request on a Java object by searching a matching set-method to 
	 * delegate the request to. 
	 * 
	 * @param content				the content to be put
	 * @throws ResourceException	if an error occured while processing the request
	 * 								on this resource
	 * @throws ContentConvertingException 
	 * @throws ContentConvertingException 
	 * @throws  
	 * @throws BadContentException 
	 * @throws UnsupportedContentTypeException 
	 */
	public void put(HTTPXInputStream in) throws ResourceException{
		//parse inputstream into a content container
		
		//TODO: Do anything with query parameters?
		if (in != null && in.getContentType() != null && in.getContentType().equals(HTTPXConstants.TYPE_APP_URLENCODED)){

			try {
				// read and parse content of stream:
				
				UrlEncodedContentReader rd = new UrlEncodedContentReader(in);
				
				Map<String, String>  map = (Map<String, String>)rd.readBuffered();

				call(WrapperConstants.PREFIX_SET + token, map, false);	
			} catch (CallFailedException e) {
				LOGGER.error("Matching method cannot not be executed.", e);
				throw new InternalException("Putting new content failed. An adequate method was found, but the invocation caused a problem.");
			} catch (NoMatchFoundException e) {
				LOGGER.debug("No match found for the requested resource and the given content.");
				throw new BadContentException("Putting new content failed. No adequate method found to process service request.");
			} catch (ContentConvertingException e) {
				LOGGER.error("Matching method cannot not be executed.", e);
				throw new InternalException("Putting new content failed. An adequate method was found, but the invocation caused a problem.");
			}
		} else {
			throw new UnsupportedContentTypeException("Content type not supported by this resource.");
		}	
	}

	/**
	 * Implements a <code>POST</code> request on a Java object by searching a matching set-method or prefixless method to 
	 * delegate the request to. 
	 * 
	 * @param content				the content of the body of the request
	 * @return						the content of the body of the response
	 * @throws ResourceException	if an error occured while processing the request
	 * 								on this resource
	 * @throws ContentConvertingException 
	 * @throws IOException 
	 * @throws BadContentException 
	 * @throws UnsupportedContentTypeException 
	 * @throws IOException 
	 */
	public HTTPXOutputStream post(HTTPXInputStream in) throws ResourceException{
		//parse inputstream into a content container
		
		//TODO: Do anything with query parameters?
		if (in != null && in.getContentType() != null &&  in.getContentType().equals(HTTPXConstants.TYPE_APP_URLENCODED)){
			Map<String, String> map;
			try {
				// read and parse content of stream:
				
				UrlEncodedContentReader rd = new UrlEncodedContentReader(in);
				
				map = (Map<String, String>)rd.readBuffered();
			} catch (ContentConvertingException e1) {
				throw new InternalException("Converting failed.");
			}

			// first try for a service method
			Object result = null;
			try {
				result =  call(token, map, false);	
			} catch (NoMatchFoundException e) {
				LOGGER.debug("Finding a method '" + token + "' failed. Searching for a method '" + WrapperConstants.PREFIX_SET + token + "' instead.");
				// second try for a set-method
				try {
					result =  call(WrapperConstants.PREFIX_SET + token, map, false);	
				} catch (CallFailedException e2) {
					LOGGER.error("Matching method '" + WrapperConstants.PREFIX_SET + token + "' failed to execute." + e2.getMessage() );
					throw new InternalException("Content could not be processed successfully.");
				} catch (NoMatchFoundException e2) {
					LOGGER.debug("No method '" + WrapperConstants.PREFIX_SET + token + "' found for this request.");
					/*ContentContainer wsdl = getWSDL(WrapperConstants.PREFIX_SET);
					if(wsdl != null) return wsdl;
					else*/ throw new BadContentException("Content could not be processed successfully.");
				}
			} catch (CallFailedException e) {
				LOGGER.error("Matching method '" + token + "' cannot not be executed." + e.getMessage());
				throw new InternalException("Posting new content failed. An adequate method was found, but the invocation caused a problem.");
			}
			
			if (result != null){
				Map<String, String> output = SerializerFunction.getInstance().serialize(result.getClass(), result);
				if (output != null){
					try {
						HTTPXChunkedOutputStream out = new HTTPXChunkedOutputStream(HTTPXConstants.TYPE_APP_URLENCODED, HTTPXConstants.DEFAULT_CHARSET);
						
						UrlEncodedContentWriter wr = new UrlEncodedContentWriter(out);
						
						wr.writeBuffered(getPrefixedMap(output));
						
						return out;
						
					
					} catch (ContentConvertingException e) {
						throw new InternalException(e.getMessage());
					}
				} else {
					LOGGER.error("No writer found for an object of type '" + result.getClass().getName() + "', result cannot be serialized.");
				}
			}			
		} else {
			throw new UnsupportedContentTypeException("Content type not supported by this resource.");
		}	
		return null;
	}
	
	/**
	 * Adds prefixes to the names of a name-value map.
	 * 
	 * @param input	the name-value map to be prefixed
	 * @return		the prefixed name-value map
	 */
	private  Map<String, String> getPrefixedMap(Map<String, String>  input){
		Map<String, String> result = new HashMap<String, String>();
		for (final Iterator<String> i = input.keySet().iterator(); i.hasNext();){
			String name = i.next();
			String value = input.get(name);
			result.put(WrapperConstants.PARAMETER_NAME_DEFAULT_PREFIX + 
					WrapperConstants.PARAMETER_NAME_SEPARATOR + name, value);
		}
		
		return result;
	}

	/**
	 * Returns the uniform identifier of this resource.
	 * 
	 * @return 	the uniform identifier of this resource
	 */
	public String getUniformIdentifier() {
		// get resource identifier, but take care not to create to much processing load especially if the parent node
		// is an InvocationWrapper
		Node parent = getParent(); 
		return (parent instanceof InvocationWrapper ? 
				((InvocationWrapper)parent).getUniformIdentifier() : 
					parent.getResource(null).getUniformIdentifier()) + WrapperConstants.PATH_NAME_SEPARATOR + token;
	}
	
	/**
	 * Internal exception that is thrown if a method processing a reuqest failed to execute.
	 *  
	 * @author David Linner
	 * @see java.lang.Exception
	 */
	private class CallFailedException extends Exception {

		/** The serial version UID of this exception. */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Constructs a new empty <code>CallFailedException</code>.
		 */
		public CallFailedException() {
			super();
		}
		
		/**
		 * Constructs a new <code>CallFailedException</code> with the specified message.
		 * 
		 * @param message	the message of this exception
		 */
		public CallFailedException(String message) {
			super(message);
		}
		
		/**
		 * Constructs a new <code>CallFailedException</code> from the specified
		 * <code>Throwable</code>
		 * 
		 * @param cause	the cause of this exception
		 */
		public CallFailedException(Throwable cause) {
			super(cause);
		}
		
		/**
		 * Constructs a new <code>CallFailedException</code> from the specified
		 * message and cause.
		 * 
		 * @param message	the message of this exception
		 * @param cause		the cause of this exception
		 */
		public CallFailedException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	/**
	 * Internal exception that indicates that no match for a request addressing 
	 * a certain mathod could be found. 
	 * 
	 * @author David Linner
	 * @see java.lang.Exception
	 */
	private class NoMatchFoundException extends Exception {
		
		/** The serial version UID of this exception. */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Constructs a new empty <code>NoMatchFoundException</code>.
		 */
		public NoMatchFoundException() {
			super();
		}
		
		/**
		 * Constructs a new <code>NoMatchFoundException</code> with the specified message.
		 * 
		 * @param message	the message of this exception
		 */
		public NoMatchFoundException(String message) {
			super(message);
		}
		
		/**
		 * Constructs a new <code>NoMatchFoundException</code> from the specified
		 * <code>Throwable</code>
		 * 
		 * @param cause	the cause of this exception
		 */
		public NoMatchFoundException(Throwable cause) {
			super(cause);
		}
		
		/**
		 * Constructs a new <code>NoMatchFoundException</code> from the specified
		 * message and cause.
		 * 
		 * @param message	the message of this exception
		 * @param cause		the cause of this exception
		 */
		public NoMatchFoundException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
