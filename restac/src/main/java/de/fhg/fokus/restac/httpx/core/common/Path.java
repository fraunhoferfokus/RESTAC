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
 * Last modified on: 13.09.2005
 * Created on: 05.09.2005
 */
package de.fhg.fokus.restac.httpx.core.common;

import java.util.List;
import java.util.ArrayList;

/**
 * Stores a path.
 * 
 * @author Philippe Bößling
 */
public class Path {

	/**
	 * <code>List</code> of <code>String</code>, which contains the elements of a path.
	 */
	private List<String> tokens = null;
	
	/**
	 * Pointer to the current element of the path.
	 */
	private int current = -1;
	
	/**
	 * Creates a new <code>Path</code> with the specified tokens.
	 * 
	 * @param tokens	the tokens which build up the path
	 */
	public Path(List<String> tokens) {
		this.tokens = tokens;
	} // Path()
	
	/**
	 * Creates a new <code>Path</code> by parsing a <code>String</code> containing the
	 * path, e.g. /cup/color/getColor.
	 * 
	 * @param path	the <code>String</code> containing the path
	 */
	public Path(String path) {
		if(path.indexOf("/") > -1) {
			String[] words = path.split("/");
			if(words.length > 0) {
				int i;
				if(path.indexOf("/") == 0) {
					//path starts with '/'	
					tokens = new ArrayList<String>(words.length - 1);
					i = 1;
				}
				else {
					//path does not start with '/'
					tokens = new ArrayList<String>(words.length);
					i = 0;
				}
				for(; i < words.length; i++) {
					tokens.add(words[i]); 
				}
			} else { //root path -> create empty list
				tokens = new ArrayList<String>(0);
			}
		}
		else { 	//path contains no '/' -> whole string considered
				//as one token
			tokens = new ArrayList<String>(1);
			tokens.add(path);
		}
	} // Path()
	
	/**
	 * Returns the <code>List</code> of <code>String</code>, which contains the elements 
	 * of the path.
	 * 
	 * @return 	<code>List</code> of <code>String</code>, which contains the elements 
	 * 			of the path
	 */
	public List getTokens() {
		return tokens;
	} // getTokens()

	/**
	 * Sets the <code>List</code> of <code>String</code>, which contains the elements of 
	 * the path
	 * 
	 * @param tokens 	<code>List</code> of <code>String</code> which contains the 
	 * 					elements of the path
	 */
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	} // setTokens()	
	
	/**
	 * Returns the pointer to the current element of the path.
	 * 
	 * @return 	pointer to the current element of the path
	 */
	public int getCurrent() {
		return current;
	} // getCurrent()

	/**
	 * Sets the pointer pointer to the current element of the path.
	 * 
	 * @param current 	pointer to the current element of the path
	 */
	public void setCurrent(int current) {
		if(current <= countTokens()) this.current = current;
	} // setCurrent()

	/**
	 * Adds a token to the end of the path.
	 * 
	 * @param token	the token to be added to the path
	 */
	public void addToken(String token) {
		if (token!=null) {
			if (token.charAt(0)==('/'))
			     token=token.substring(1);
			if (!token.equals(HTTPXConstants.EMPTY))tokens.add(token);
		}
	
	} // addToken()
	
	/**
	 * Removes the last token from the path.
	 */
	public void removeLastToken() {
		if (tokens.size() >= 1) {
			tokens.remove(tokens.size() - 1);
		}
	} // removeLastToken()
	
	/**
	 * Checks whether there is a next element in the path after the current element.
	 * 
	 * @return 	true, if there is a next element in the path after the current element
	 */
	public boolean hasNext() {
		return (countTokens() > 0 && current < tokens.size() - 1);
	} // hasNext()
	
	/**
	 * Checks whether there is a previous element in the path before the current element.
	 * 
	 * @return 	true, if there is a previous element in the path before the current element
	 */
	public boolean hasPrevious() {
		return current > 0;
	} // hasPrevious()
	
	/**
	 * Sets the <code>current</code> pointer on the first element of the path.
	 */
	public void first() {
		if(countTokens() > 0) current = 0;
	} // first()
	
	/**
	 * Sets the <code>current</code> pointer on the last element of the path.
	 */
	public void last() {
		if(countTokens() > 0) current = tokens.size() - 1 ;
	} // last()
	
	/**
	 * Sets the <code>current</code> pointer on the next element of the path.
	 */
	public void next() {
		if(hasNext()) current++;
	} // next()
	
	/**
	 * Sets the <code>current</code> pointer on the previous element of the path. 
	 */
	public void previous() {
		if(hasPrevious()) current--;
	} // previous()
	
	/**
	 * Returns the current token.
	 * 
	 * @return 	the current token
	 */
	public String getCurrentToken() {
		if(countTokens() > 0) return (String)tokens.get(current);
		else return null;
	} // getCurrentToken()
	
	/**
	 * Returns the path from the root to the current element.
	 * 
	 * @return 	the path from the root to the current element
	 */
	public String getCurrentPath() {
		if(countTokens() == 0) return "/";
		String currentPath = "";
		for(int i = 0; i <= current; i++) {
			currentPath += "/" + tokens.get(i);
		}
		return currentPath;
	} // getCurrentPath()

	/**
	 * Subtracts a path from this path. First the method checks whether this path
	 * starts with the path to be subtracted. Then the beginning of this part which 
	 * matches the given path is cut of and the rest is being returned, 
	 * e.g. /a/b/c/d - /a/b = /c/d. Returns null, if this path does not start with 
	 * the given path.  
	 * 
	 * @param path		the path to be subtracted from this path
	 * @return			the orinal path minus the given path
	 */
	public Path subtractPath(Path path) {
		if(startsWith(path)) {
			List<String> newTokens = new ArrayList<String>(countTokens() - path.countTokens());
			for(int i = path.countTokens(); i < countTokens(); i++) {
				newTokens.add(tokens.get(i));
			}
			return new Path(newTokens);
		}
		return null;
	} // subtractPath()
	
	/**
	 * Checks whether this path starts with the given path.
	 * 
	 * @param path	the given path
	 * @return		true if this path starts with the given path
	 */
	public boolean startsWith(Path path) {
		if(path.countTokens() <= this.countTokens()) {
			int oldCurrent = current;
			path.first();
			this.first();
			for(int i = 0; i < path.countTokens(); i++) {
				if(path.getCurrentToken().equals(this.getCurrentToken())) {
					path.next();
					this.next();
				}
				else { 
					current = oldCurrent;
					return false;
				}
			}
			/*
			while(path.hasNext()) {
				if(path.getCurrentToken().equals(this.getCurrentToken())) {
					path.next();
					this.next();
				}
				else {
					current = oldCurrent;
					return false;
				}
			}
			*/
			current = oldCurrent;
			return true;
		}
		return false;
	} // startsWith()
	
	/**
	 * Checks whether this path starts with the given path.
	 * 
	 * @param path	the given path
	 * @return		true if this path starts with the given path
	 */
	public boolean startsWith(String path) {
		if (path == null)
			return true;
		else
			return startsWith(new Path(path));
	} // startsWith()
	
	/**
	 * Returns the number of the tokens in the path.
	 * 
	 * @return	the number of the tokens in the path
	 */
	public int countTokens() {
		if(tokens != null) return tokens.size();
		else return 0;
	} // countTokens()
	
	/**
	 * Returns the <code>String</code> representation of the path.
	 * 
	 * @return 	the <code>String</code> representation of the path
	 */
	public String getString() {
		String path = "";
		int oldCurrent = current;
		last();
		path += getCurrentPath();
		current = oldCurrent;
		return path;
	} // getString()
	
	/**
	 * Indicates whether some other object is "equal to" this one.
	 * 
	 * @param object 	the reference object with which to compare. 
	 * @return 			<i>true</i> if this object is the same as the obj argument; <i>false</i> otherwise.
	 *
	 */
	public boolean equals(Object object) {
		if (object instanceof Path)
			return this.getString().equals(((Path)object).getString());	
		return false;
	}
	
	/** 
	 * Returns a hash code value for this object
	 * 
	 * @return a hash code value for this object.
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.getString().hashCode();
	}
	
	public static void main(String[] args) {
		Path p=new Path("/a/b");
		p.addToken("/");
		
//		Path a = new Path("/a/b/c/d");
//		Path b = new Path("/a/z");
		System.out.println(p.getString());
		p.removeLastToken();
		System.out.println(p.getString());
		p.removeLastToken();
		System.out.println(p.getString());
	}
	
} 
