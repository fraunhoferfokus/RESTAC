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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import de.fhg.fokus.restac.httpx.core.common.Path;

/**
 * Simple function that retrieves a java object for a list of name-value pairs, if an adqquate
 * mapping can be found.  
 * 
 * @author David Linner
 * @author Stefan Föll
 * @see Parser
 */
public class ParserFunction {
	
	/** The singleton instance of <code>ParserFunction</code>. */
	private static ParserFunction instance = new ParserFunction();
	
	/** The map of registered parsers. */
	private Map<Class, Parser> parsers; 
	
	/**
	 * Constructs a new empty <code>ParserFunction</code>.
	 */
	private ParserFunction(){
		parsers = Collections.synchronizedMap(new HashMap<Class, Parser>());
	}
	
	/**
	 * Returns the singletong instance of <code>ParserFunction</code>.
	 * 
	 * @return	the singletong instance of <code>ParserFunction</code>
	 */
	public static ParserFunction getInstance(){
		return instance;
	}
	
	/**
	 * Checks wether an  object of the given class can be assembled
	 * 
	 * @param expectedType 	type that indicates a pattern for assembling
	 * @return 				true if it is possible to assemble an object of the given 
	 * 						type, false otherwise
	 */
	public boolean isParseable(Class expectedType){
		Parser parser = parsers.get(expectedType);
		if (parser != null || expectedType.isArray())	return true;
		return false;
	}
	
	/**
	 * Tries to assemble an object from an assign list of name-value pairs. 
	 * 
	 * @param expectedType	type that indicates a pattern for assembling
	 * @param values		name-value pairs to be assembled to an object
	 * @return 				the object or null if parsing failed
	 */
	public Object parse(Class expectedType, Map<String, String> values){
		Parser primitiveParser = parsers.get(expectedType);
		if (primitiveParser != null){
			return primitiveParser.parse(values);
		}
		
		try {
			
			if (expectedType.isArray())
				return new ArrayParser().parse(expectedType, values);
			
/*			
 *         The following code in comments is for assembling objects whose types
 *         are implementing the List, Set or Map interface. Because of missing information on
 *         their element types this works only for String element types: 
 *         Set<String>,Map<String,String>,List<String>
 * 
 * 			else if (java.util.Map.class.isAssignableFrom(expectedType)){
				return new MapParser().parse(expectedType, values);			
			}
			else if (java.util.List.class.isAssignableFrom(expectedType)){
				return new ListParser().parse(expectedType, values);	
			}
			
			else if (java.util.Set.class.isAssignableFrom(expectedType)) {
				return new SetParser().parse(expectedType,values);
			}
			
			*/

		}
		catch (Exception e){ }
			
		return null;
	} 
	
		
	/**
	 * Adds a new parser for a certain type to this <code>ParserFunction</code>.
	 * 
	 * @param type		type of the parser
	 * @param parser	the parser to be added
	 */
	public void addParser(Class type, Parser parser){
		parsers.put(type, parser);
	}
	
	/**
	 * Add some parsers for basic types here. Later parsers may be loaded reflective.
	 */
	static {
		ParserFunction reader = ParserFunction.getInstance();
		
		//adding parser for strings
		reader.addParser(String.class, new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					return value != null ? value : name;
				}
				return null;
			}
		});
		
		//adding parser for integer types
		Parser intParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						return new Integer(Integer.parseInt((value != null ? value : name)));
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		reader.addParser(Integer.class, intParser);
		reader.addParser(Integer.TYPE, intParser);
		
		//adding parser for long types
		Parser longParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						return new Long(value != null ? value : name);
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		
		reader.addParser(Long.class, longParser);
		reader.addParser(Long.TYPE, longParser);
		
		//adding parser for short types
		Parser shortParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						return new Short(value != null ? value : name);
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		reader.addParser(Short.class, shortParser);
		reader.addParser(Short.TYPE,    shortParser);
				
		//adding parser for float types
		Parser floatParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						return new Float(value != null ? value : name);
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		reader.addParser(Float.class, floatParser);
		reader.addParser(Float.TYPE,  floatParser);
		
		//adding parser for double types
		Parser doubleParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						return new Double(value != null ? value : name);
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		reader.addParser(Double.class, doubleParser);
		reader.addParser(Double.TYPE,  doubleParser);
		
		//adding parser for byte types
		Parser byteParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						return new Byte(value != null ? value : name);
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		
		reader.addParser(Byte.class, byteParser);
		reader.addParser(Byte.TYPE,  byteParser);
		
		//adding parser for boolean types
		Parser booleanParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						//return new Boolean(entry.getValue() != null ? entry.getValue() : entry.getName());
						if(value != null) {
							if(value.equalsIgnoreCase("true")) return new Boolean(true);
							if(value.equalsIgnoreCase("false")) return new Boolean(false);
						}
						else new Boolean(true); // single parameter without value interpreted as true
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		
		reader.addParser(Boolean.class, booleanParser);
		reader.addParser(Boolean.TYPE,  booleanParser);
		
		//adding parser for character types
		Parser characterParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size() == 1){
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					try{
						return new Character(value != null ? value.charAt(0) : name.charAt(0));
					}catch(NumberFormatException e){
						//nothing please
					}
				}
				return null;
			}
		};
		
		reader.addParser(Character.class, characterParser);
		reader.addParser(Character.TYPE,  characterParser);
		
		//adding parser for void types
		Parser voidParser = new Parser() {
			public Object parse(Map<String, String> params) {
				return null;
			}
		};
		
		reader.addParser(Void.class, voidParser);
		reader.addParser(Void.TYPE,  voidParser);
		
		//Parser for NamingServiceAnnouncement-Messages
//		Parser announcementParser = new Parser() {
//			public Object parse(Map<String, String> params) {
//			     if (params.size() == 3) {
//			    	 try {
//			    		 URL url = new URL(params.get(NamingConstants.ADDRESS));
//			    		 int life_time = Integer.parseInt(params.get(NamingConstants.LIFE_TIME));
//			    		 VisibilityGroup visibilityGroup = new VisibilityGroup(params.get(NamingConstants.VISIBILITY_GROUP));
//			    		 return new NamingServiceAnnouncement(url, life_time, visibilityGroup);
//			    	 }
//			    	 catch (MalformedURLException e) {
//			    		//nothing please
//			    	}
//			    	catch (NumberFormatException e) {
//			    	   // nothing please
//			    	}
//			    	catch(InvalidMulticastGroupException e) {
//			    		
//			    	}
//			     }
//			     return null;
//			}
//		};
//		
//		//adding parser for NamingServiceAnnouncement-Messages
//		reader.addParser(NamingServiceAnnouncement.class,announcementParser);
//		
		Parser urlParser = new Parser() {
			public Object parse(Map<String, String> params) {
			     if (params.size()==1) {
			    	 String name = params.keySet().iterator().next();
						String value = params.get(name);
						try{
							return new URL(value != null ? value : name);
						} catch (MalformedURLException e) {
				    		//nothing please
				    	}
			     }
					return null;
			}
		};
		
//		adding parser for URL Type
		reader.addParser(URL.class,urlParser);
		
		Parser pathParser = new Parser() {
			public Object parse(Map<String, String> params) {
				if (params.size()==1) {
					String name = params.keySet().iterator().next();
					String value = params.get(name);
					return new Path(value != null ? value : name);
				}
				return null;
			}
		};
		
//		adding parser for URL Type
		reader.addParser(Path.class,pathParser);
		
	
	}
	
	/**
	 * Parser class for assembling an object of type <code>Map<String></code>.
	 * Assuming elements of type <code>String</code> because of missing information on the required element type.
	 * 
	 * @author Stefan Föll
	 */
	private class MapParser {
		
		/**
		 * Assembles an object of type <code>Map</code>
		 * 
		 * @param expectedType the type conform to the <code>Map</code>type
		 * @param values the name-value map to parse from
		 * @return the assembled <code>Map</code> object
		 * @throws Exception creating an instance of the expected type failed
		 */
		public Object parse(Class expectedType, Map<String, String> values) throws Exception {
			Map<String, String> map;
			if (expectedType == java.util.Map.class) 
				map = new HashMap<String, String>();
			else 
				map = (Map<String, String>) expectedType.newInstance();
			for(Iterator<String> iter = values.keySet().iterator(); iter.hasNext();) {
				String name = iter.next();
				String value = values.get(name);
				map.put(name, value);
			}
			return null;
		}		
		
	}
	
	/**
	 * Parser class for assembling an object of type <code>List<String></code>.
	 * Assuming elements of type <code>String</code> because of missing information on the required element type.
	 * 
	 * @author Stefan Föll
	 */
	private class ListParser {
		
		/**
		 * Assembles an object of type <code>List/code>
		 * 
		 * @param expectedType the type conform to the <code>List</code>type
		 * @param values the name-value map to parse from
		 * @return the assembled <code>List</code> object
		 * @throws Exception creating an instance of the expected type failed
		 */
		public Object parse(Class expectedType, Map<String, String> values) throws Exception {
			List<String> list;
			if(expectedType == java.util.List.class) 
				list = new ArrayList<String>();
			list = (List<String>)expectedType.newInstance();
			for(Iterator<String> iter = values.keySet().iterator(); iter.hasNext();) {
				String name = iter.next();
				String value = values.get(name);
			    list.add((value != null) ? value : name) ;
			}
			return null;
		}
	}
		
	/**
	 * Parser class for assembling an object of type <code>Set<String></code>.
	 * Assuming elements of type <code>String</code> because of missing information on the required element type.
	 * 
	 * @author Stefan Föll
	 */
	private class SetParser {
		
			/**
			 * Assembles an object of type <code>Set</code>
			 * 
			 * @param expectedType the type conform to the <code>Set</code>type
			 * @param values the name-value map to parse from
			 * @return the assembled <code>Set</code> object
			 * @throws Exception creating an instance of the expected type failed
			 */
			public Object parse(Class expectedType, Map<String, String> values) throws Exception {
				Set<String> set;
				if(expectedType == java.util.Set.class) 
					set = new HashSet<String>();
				set = (Set<String>)expectedType.newInstance();
				for(Iterator<String> iter = values.keySet().iterator(); iter.hasNext();) {
					String name = iter.next();
					String value = values.get(name);
				    set.add((value != null) ? value : name);
				}
				return null;
			}		
		
	}
	
	/**
	 * 
	 * Parser class for assembling an object of type <code>Array</code>.
	 * By means of Java Reflection the array object is created with the required array's element type. 
	 * The process assumes all array entries contained in the map of name value pairs, no entry may be missing.
	 * 
	 * @author Stefan Föll
	 */
	private class ArrayParser {
		
		/**
		 * Assembles an object of type <code>Array</code>
		 * 
		 * @param expectedType 	the dynamic type the assembled object shall have
		 * @param nameValueMap	the name-value map to parse from
		 * @return 				the assembled object
		 */
		public Object parse(Class expectedType, Map<String, String> nameValueMap) throws Exception {
			
			//check if names of name-value pairs match the required pattern
			Pattern arrayPattern = Pattern.compile("(\\w*\\.?\\w*)(\\[\\d+\\])+");
			Class arrayElementType = expectedType.getComponentType();
			for(Iterator<String> iter = nameValueMap.keySet().iterator(); iter.hasNext();) {
				String name = iter.next();
				if (!arrayPattern.matcher(name).matches())
					throw new Exception("Array identifier does not conform with the required syntax");
			}
		
			//sort the map with the name-value pairs lexicographically over the names
			if(nameValueMap.size() > 1) {
				Map<String, String> sortedEntries = Collections.synchronizedMap(new TreeMap<String, String>());
				for(Iterator<String> iter = nameValueMap.keySet().iterator(); iter.hasNext();) {
					String name = iter.next();
					String value = nameValueMap.get(name);
					sortedEntries.put(name, value);
				}
				nameValueMap = sortedEntries; 
			}
			
			//get array size
			int size = getArraySizeForThisDimension(nameValueMap);
			
			//cut off prefix and array index from names in map and parse array from that map 
			Object array = Array.newInstance(arrayElementType, size);
			for(int i = 0; i < size; ++i){	//why ++i instead of i++?
				Map<String, String> map = fillMap(nameValueMap, i);
				Object o = ParserFunction.this.parse(arrayElementType, map);
				Array.set(array, i, o);
			}	
			
			return array;
		}
		
		/**
		 * Creates a name-value map with all required entries for this position.
		 * The returned map is parsed in turn to obtain an object which can be set as entry on the current array's position
		 * 
		 * @param entries 	the array of name-value pairs to examine
		 * @param position 	the current position in the array to set an object 
		 * @return 			name-value map with all required entries for this position.
		 */
		private Map<String, String> fillMap(Map<String, String> nameValueMap, int position ) {
			Map<String, String> map = new HashMap<String, String>();
			int begin, end, compare;
			for(Iterator<String> iter = nameValueMap.keySet().iterator(); iter.hasNext();) { //int i = 0; i < entries.length;++i) {
				String name = iter.next();
				begin = name.indexOf('[');
				end =  name.indexOf(']');
				compare = Integer.parseInt(name.substring(begin + 1, end).trim());
				if(compare == position) 
					map.put(name.substring(end + 1).trim(), nameValueMap.get(name));
			}
			return map;
		}
		
		 /**
		  * Gets the Array size for the current dimension.
		  * Therefore the method returns the max value within the first bracket pair of all names
		  * 
		 * @param entries 	the array of name-value pairs to examine
		 * @return 			the array size for this dimension 
		 */
		private int getArraySizeForThisDimension(Map<String, String> nameValueMap) {
			int size = -1;
			int begin, end, newSize;
			
			for(Iterator<String> iter  = nameValueMap.keySet().iterator(); iter.hasNext();) {
				String name = iter.next();
				begin = name.indexOf('[');
				end = name.indexOf(']');
				newSize = Integer.parseInt(name.substring(begin + 1, end).trim());
				if(newSize > size)
					size = newSize;
			}
			return size + 1;
		}

	}
}
