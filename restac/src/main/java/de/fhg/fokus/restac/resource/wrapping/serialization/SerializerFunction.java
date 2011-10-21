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

import java.lang.reflect.Array;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.common.Path;


/**
 * Represents a simple manager for all known serializers. 
 * 
 * @author David Linner
 * @author Stefan Föll
 * @see Serializer
 */
public class SerializerFunction {
	
	private final static Logger LOGGER = Logger.getLogger(SerializerFunction .class);
	
	/** The singleton instance of <code>SerializerFunction</code>. */
	private static SerializerFunction instance = new SerializerFunction();
	
	/** The map of registered serializers. */
	private Map<Class, Serializer> serializers; 
	
	/**
	 * Constructs a new empty <code>SerializerFunction</code>.
	 */
	private SerializerFunction(){
		serializers = Collections.synchronizedMap(new HashMap<Class, Serializer>());
	}
	
	/**
	 * Returns the singletong instance of <code>SerializerFunction</code>.
	 * 
	 * @return	the singletong instance of <code>SerializerFunction</code>
	 */
	public static SerializerFunction getInstance(){
		return instance;
	}
	
	/**
	 * Checks whether a serialization for a object of the given class is possible 
	 * 
	 * @param preferredType 	type that indicates a pattern for serialization
	 * @return true 			if a serialization is possible, false otherwise
	 */
	public boolean isSerializable(Class preferredType){
		Serializer serializer = serializers.get(preferredType);
		if (serializer != null || preferredType.isArray())	return true;
		return false;
	}
	
	/**
	 * Creates for the preferred type a serialization for the given object.
	 *  
	 * @param preferredType	type that indicates a pattern for serialization
	 * @param obj 			an instance to serialize
	 * @return				the serialization of the object
	 */
	public Map<String, String> serialize(Class preferredType, Object obj){
		Serializer serializer = serializers.get(preferredType);
		if (serializer != null){
			return serializer.serialize(obj);
		}
		if (preferredType.isArray())
			return new ArraySerializer().serialize(obj);
		
		/* The following code in comments is for serializing objects whose types
		 * are implementing the List, Set or Map interface. 
		 * Because of missing information on their element types this works only for String element types: 
		 * Set<String>,Map<String,String>,List<String>
		 * */
		
		/*		
 		else if (java.util.Map.class.isAssignableFrom(preferredType)){
			return new MapSerializer().serialize(obj);			
		}
		else if (java.util.List.class.isAssignableFrom(preferredType)){
			return new ListSerializer().serialize(obj);	
		}
		else if (java.util.Set.class.isAssignableFrom(preferredType)){
			return new SetSerializer().serialize(obj);	
		}
		*/
			
		return null;
	} 
	
	/**
	 * Adds a new serializer for a certain type of object.
	 * 
	 * @param type			the type of the object
	 * @param serializer	the serializer to be added
	 */
	public void addSerializer(Class type, Serializer serializer){
		serializers.put(type, serializer);
	}
	
	/**
	 * Adding some serializers for basic types.
	 */
	static {
		SerializerFunction writer = SerializerFunction.getInstance();
		
		Serializer simpleSerializer = new Serializer() {
			public Map<String, String> serialize(Object object) {
				Map<String, String> result = new HashMap<String, String>();
				result.put("value", object.toString());
				return result;
			}
		};
		writer.addSerializer(String.class, simpleSerializer);
		
		writer.addSerializer(Character.TYPE, simpleSerializer);
		writer.addSerializer(Character.class, simpleSerializer);

		writer.addSerializer(Double.TYPE, simpleSerializer);
		writer.addSerializer(Double.class, simpleSerializer);

		writer.addSerializer(Float.TYPE, simpleSerializer);
		writer.addSerializer(Float.class, simpleSerializer);

		writer.addSerializer(Boolean.TYPE, simpleSerializer);
		writer.addSerializer(Boolean.class, simpleSerializer);

		writer.addSerializer(Byte.TYPE, simpleSerializer);
		writer.addSerializer(Byte.class, simpleSerializer);

		writer.addSerializer(Integer.TYPE, simpleSerializer);
		writer.addSerializer(Integer.class, simpleSerializer);
		
		writer.addSerializer(Short.TYPE, simpleSerializer);
		writer.addSerializer(Short.class, simpleSerializer);
		
		writer.addSerializer(Long.TYPE, simpleSerializer);
		writer.addSerializer(Long.class, simpleSerializer);	

		//Serializer for URLs
		Serializer urlSerializer = new Serializer() {
			public Map<String, String> serialize(Object object) {
				Map<String, String> result = new HashMap<String, String>();
				result.put("url",((URL)object).toExternalForm());
				return result;
			}		
		};
		
		writer.addSerializer(URL.class, urlSerializer);	
		
		Serializer pathSerializer = new Serializer() {
			public Map<String, String> serialize(Object object) {
				Map<String, String> result = new HashMap<String, String>();
				result.put("path",((Path)object).getString());
				return result;
			}		
		};
		
		writer.addSerializer(Path.class, pathSerializer);	
	
	}
	
	    /**
	     * Serializiation class for <code>Array</code> objects.
	     * 
	     * @author Stefan Föll
	     */
	    private class ArraySerializer implements Serializer {
	    	
	    	/** 
	    	 * Serializes an <code>Array</code> into a map of name-value pairs.
	    	 *
	    	 * @param object	the array to be serialized
	    	 * @return			a name-value map representing the array
	    	 */
	    	public Map<String, String> serialize(Object object) {
	    		Map<String, String> result = new HashMap<String, String>();
	    		return serializeArray(object,result,"array");
	    	}
	    	
	    	/**
	    	 * Serializes and array into a map of name-value pairs.
	    	 * 
	    	 * @param array		the array to be serialized
	    	 * @param result	the name-value map representing the array
	    	 * @param name		the name of the array
	    	 * @return			a name-value map representing the array
	    	 */
	    	private Map<String, String> serializeArray(Object array, Map<String, String> result, String name ) {
	    		 
	    		for (int i=0;i<Array.getLength(array);++i) {
	    			Object element=Array.get(array,i);
	    			Class cls=element.getClass();
	    			String new_name=name+"["+i+"]";
	    			if (cls.isArray()) {
	    			serializeArray(element,result,new_name);
	    			}
	    			else if (serializers.get(cls)!=null){
	    				Map<String, String> elements = (serializers.get(cls)).serialize(element);
	    				if (elements.size() == 1){
	    					String entryName = elements.keySet().iterator().next();
	    					String entryValue = elements.get(entryName);
	    					String value = entryValue != null ? entryValue : entryName;
	    					result.put(new_name,value);
	    				}
	    				else LOGGER.debug("Cannot serialize array of complex elemnent types");
	    				
	    			}
	    			else return null;
	    				
	    		}
				return result;
			}
	    	
	    }
	    
	    /**
	     *  Serializiation class for <code>Map</code> objects.
	     * 
	     * @author Stefan Föll
	     */
	    private class MapSerializer implements Serializer {
	    	
	    	/** 
	    	 * Serializes a <code>Map</code> into a name-value map.
	    	 * 
	    	 * @param object	the map to be serialized
	    	 * @return			a name-value map representing the map
	    	 */
	    	public Map<String, String> serialize(Object object) {
	    		Map map = (Map)object;
	    		Map<String, String> result = new HashMap<String, String>();
	    		for (Iterator iter=map.entrySet().iterator();iter.hasNext();) {
	    			Map.Entry entry=(Map.Entry)iter.next();
	    			result.put(entry.getKey().toString(),entry.getValue().toString());
	    		}
				return result;
			}
	    	
	    }
	    
	    /**
	     * Serializiation class for <code>List</code> objects.
	     * 
	     * @author Stefan Föll
	     */
	    private class ListSerializer implements Serializer {
	    	
	    	/**
	    	 * Serializes a <code>List</code> into a name-value map.
	    	 * 
	    	 * @param object	the list to be serialized
	    	 * @return			a name-value map representing the list
	    	 */
	    	public Map<String, String> serialize(Object object) {
	    		List list = (List)object;
	    		Map<String, String> result = new HashMap<String, String>();
	    		for (Iterator iter=list.iterator();iter.hasNext();) {
	    			Object element=iter.next();
	    			result.put("list",element.toString());
	    		}
				return result;
			}
	    	
	    }
	    
	    /**
	     * Serializiation class for <code>Set</code> objects.
	     * 
	     * @author Stefan Föll
	     */
	    private class SetSerializer implements Serializer {	
	    	
	    	/** 
	    	 * Serializes a <code>Set</code> into a name-value map.
	    	 * 
	    	 * @param object	the set to be serialized
	    	 * @return			a name-value map representing the set
	    	 */
	    	public Map<String, String> serialize(Object object) {
	    		Set set = (Set)object;
	    		Map<String, String> result = new HashMap<String, String>();
	    		for (Iterator iter=set.iterator();iter.hasNext();) {
	    			Object element=iter.next();
	    			result.put("set",element.toString());
	    		}
				return result;
				
			}
	    	
	    }
	    
	    
}
