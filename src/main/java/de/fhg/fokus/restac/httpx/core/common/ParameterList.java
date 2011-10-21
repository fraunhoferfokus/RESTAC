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

package de.fhg.fokus.restac.httpx.core.common;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Name-value pairs which contain the query of the request. Parameters in the query
 * without value are saved as name with value <code>null</code>.
 * 
 * @author Murat Ates
 * 
 */
public class ParameterList {
	
	private HashMap<String, LinkedList<String>> parameters = new HashMap<String, LinkedList<String>>();
	
	public ParameterList() {
	}
	
	/**
	 * Constructs a new parameter list from an url encoded query string
	 * 
	 * @param query
	 */
	public ParameterList(String query) {
		
		if(query == null)
			return;
		
		String[] fields = query.split("&");
		//don't allow body=null and body.length>0 and body==""
		if (fields!=null && fields.length > 0 && !(fields.length==1 && fields[0].equals(""))){

			String name, value;
			int idx;
			for(int i=0; i<fields.length; i++) {
				idx = fields[i].indexOf('=');
				if (idx!=-1) {
					name= fields[i].substring(0, idx).trim();
					value=fields[i].substring(idx+1).trim();
					try {
						name = URLDecoder.decode(name, HTTPXConstants.DEFAULT_CHARSET);
						value = URLDecoder.decode(value, HTTPXConstants.DEFAULT_CHARSET);
					}
					catch(UnsupportedEncodingException e) {
						this.parameters = new HashMap<String, LinkedList<String>>();
					}
					LinkedList<String> valueList = this.parameters.get(name);
					
					if(valueList == null)
						this.parameters.put(name, new LinkedList<String>());
					else {
						valueList.add(value);
						this.parameters.put(name, valueList);
					}
						
						
				}
				else {
					this.parameters.put(fields[i],null);
				}
			}
		}
	}
	
	/**
	 * Sets the name of the <code>ParameterList</code> with
	 * value <code>null</code>.
	 * 
	 * @param name	the name of the query
	 */
	public void setParameter(String name){
		this.setParameter(name, null);
	}
	
	/**
	 * Sets the name of the query with value.
	 * 
	 * @param name		the name of the query
	 * @param value		the value of the query
	 */
	public void setParameter(String name, String value){
		if (parameters.containsKey(name)){
			parameters.get(name).add(value);
		} else {
			LinkedList<String> values = new LinkedList<String>();
			values.add(value);
			parameters.put(name, values);
		}
	} 
	
	/**
	 * Returns true if the query with name exists.
	 * 
	 * @param name	the name of the query
	 * @return		true if exist
	 */
	public boolean isSet(String name){
		return parameters.containsKey(name);
	}
	
	/**
	 * Remove the query.
	 * 
	 * @param name		the name of the query
	 * @param value		the value of the query
	 */
	public void removeParameter(String name, String value){
		//FIXME: if there is no other value, remove the name anyway ???
		if (parameters.containsKey(name)){
			parameters.get(name).remove(value);
		} 
	}
	
	/**
	 * Remove all querys with the name.
	 * 
	 * @param name	name of the query
	 */
	public void removeAllParameters(String name){
		parameters.remove(name);
	}
	
	/**
	 * Returns the first value of the query.
	 * 
	 * @param name	the name of the query
	 * @return		the key
	 */
	public String getFirstParameter(String name){
		if (parameters.containsKey(name) && parameters.get(name).size() > 0){
			return parameters.get(name).getFirst();
		} 
		return null;
	}
	
	/**
	 * Returns the query value at the Position <code>index</code> .
	 * 
	 * @param name	the name of the query
	 * @param index	the index
	 * @return		the key
	 */
	public String getParameter(String name, int index){
		if (parameters.containsKey(name) && index < parameters.get(name).size()){
			return parameters.get(name).get(index);
		} 
		return null;
	}
	
	/**
	 * Returns all values to a given key.
	 * 
	 * @param name	the name of the query
	 * @return		the <code>List<String></code>
	 */
	public List<String> getAllParameters(String name){
		return parameters.get(name);
	}

	/**
	 * Returns a <code>Set<String></code> with all keys.
	 * 
	 * @return	the keys
	 */
	public Set<String> getAllKeys(){
		return parameters.keySet();
	}
	
	/**
	 * Returns all name-value pairs as a String
	 * 
	 * @return	the <code>String</code> representation
	 */
	public String getAllAsString(){
		String result = "";
		
		for (String key : parameters.keySet()) {
			List<String> valueList = parameters.get(key);
			
			if (result.length() > 0) {
				result += "&";
			}
			for (Iterator<String> value = valueList.iterator(); value.hasNext();) {
				result += key + HTTPXConstants.NAME_VALUE_SEPARATOR + value.next() + (value.hasNext() ? "&" : HTTPXConstants.EMPTY);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the size of the parameter list
	 * 
	 * @return	size as <code>int</code>
	 */
	public int getSize() {
		return parameters.size();
	}
}
