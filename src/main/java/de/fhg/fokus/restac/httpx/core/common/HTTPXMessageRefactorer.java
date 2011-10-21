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

/* Created on 08.03.2007 */

package de.fhg.fokus.restac.httpx.core.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;

/**
 * TODO: Documentation
 * 
 * @author Murat Ates
 *
 */
public class HTTPXMessageRefactorer {

	private final static Logger LOGGER = Logger.getLogger(HTTPXMessageRefactorer.class);
	private final static int BUFFER_SIZE = 512;
	
	/***
	 * Parses the status line of a HTTP response from an <code>InputStream</code> and creates
	 * a <codeHTTPXStatusMessage</code>.
	 * 
	 * @param stream	the <code>InputStream</code> with response	
	 * @return			the <code>HTTPXStatusMessage</code>
	 * @throws HTTPXProtocolViolationException	if status line doesn't conform with the syntax
	 */
	public HTTPXStatusMessage createStatusMessageFromStream(InputStream stream) throws HTTPXProtocolViolationException{
		HTTPXStatusMessage statusMessage = new HTTPXStatusMessage();
		
		int currentCharacter = -1;
		int previousCharacter = -1;
//		StringWriter sw = new StringWriter();
		StringBuilder statusLine = new StringBuilder();
//		String statusLine = null;
				
		try {
			while((currentCharacter = stream.read()) != -1) {
				if(previousCharacter == 13 && currentCharacter == 10)
					break;
				if(currentCharacter != 13 && currentCharacter != 10){
					statusLine.append((char)currentCharacter);
				}
				previousCharacter = currentCharacter;
			}
		}
		catch(IOException e) {
			LOGGER.error(e);
		}
		
		if (statusLine.length() != 0) 			// is not empty?
		{
			// split status line at trailing white spaces
			String[] words = statusLine.toString().split("\\s", 3);
			// does status line contain three elements (http-version, status-code, reason-phrase)?
			if (words.length < 3) {
				LOGGER.error("The Statusline doesn't contain 3 Elements (http-version, status-code, reason-phrase) " + statusLine.toString());
				throw new HTTPXProtocolViolationException("Status line does not conform with the required syntax: HTTP/Version SP Status-Code SP Reason-Phrase");
			}
			
			//split protocol and protocolversion
			String[] tmp = words[0].split("/");
			if (tmp.length == 2) {
				statusMessage.setProtocol(tmp[0]);
				statusMessage.setVersion(tmp[1]);
			} else {
				LOGGER.error("The Statusline doesn't contain the Protocol and/or the Protocolversion (e.g. HTTP/1.1" + words[0]);
				throw new HTTPXProtocolViolationException("Status line does not conform with the required syntax: HTTP/Version SP Status-Code SP Reason-Phrase");
			}
			
			statusMessage.setStatusCode(Integer.parseInt(words[1]));
			statusMessage.setReasonPhrase(words[2]);
		}
		
		this.parseHeader(stream, statusMessage);			//parse the header
		statusMessage.setInputStream(stream);
		
		return statusMessage;
	}
	
	/***
	 * Returns a <code>InputStream</code> containing the serialized form of the HTTP response.
	 * <pre>
	 * HttpRequest = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
	 *               *(message-header CRLF)
	 *               CRLF	
	 *               [message-body]	
	 * </pre>
	 * 
	 * @param message	the <code>HTTPXStatusMessage</code>
	 * @return			the serialized form of the HTTP request
	 */
	public InputStream toStream(final HTTPXStatusMessage message) {
		byte[] serializedForm = new byte[0];
		
//		try {
//			//Serialize status line
//			StringBuilder statusLine = new StringBuilder();
//			statusLine.append(message.getProtocol()).append("/").append(message.getVersion()).append(" ")
//						.append(message.getStatusCode()).append(" ").append(message.getReasonPhrase()).append(HTTPXConstants.CRLF);
//						
//			byte[] statusLineBytes = statusLine.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET); 
//
//			//Serialize headers
//			StringBuilder headerString = new StringBuilder();
//			
//			if(message.getHeaders() != null) {
//				for(final Iterator<String> i = message.getHeaders().keySet().iterator(); i.hasNext();) {
//					String name = i.next();
//					String value = message.getHeader(name);
//					headerString.append(name).append(HTTPXConstants.HEADER_NAME_SEPARATOR)
//									.append(" ").append(value).append(HTTPXConstants.CRLF);
//				}
//			}
//			headerString.append(HTTPXConstants.CRLF);
//			byte[] headerBytes = headerString.toString().getBytes(HTTPXConstants.CRLF);
//			
//			serializedForm = new byte[statusLineBytes.length + headerBytes.length];
//			System.arraycopy(statusLineBytes, 0, serializedForm, 0, statusLineBytes.length);
//			System.arraycopy(headerBytes, 0, serializedForm, statusLineBytes.length, headerBytes.length);
//			
//		}
//		catch(UnsupportedEncodingException e) {
//			LOGGER.error(e);
//		}
		
		serializedForm = toByte(message);
		
		final OutputStream pipeOut = new PipedOutputStream();
		InputStream pipeIn = new PipedInputStream();
		
		//write status line and header
		try {
			((PipedInputStream)pipeIn).connect((PipedOutputStream)pipeOut);
			pipeOut.write(serializedForm);
			pipeOut.flush();
		} catch (IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		
		if (message.getInputStream() != null) {
			new Thread(new Runnable(){
				public void run() {
					writeBody(message.getInputStream(), pipeOut);
				}//run
			}).start();
		}else {
			try {
				pipeOut.close();
			} catch (IOException e) {
				LOGGER.error(e);
				e.printStackTrace();
			}
		}
		
		return pipeIn;
	}
	
	/**
	 * 
	 * 
	 * @param message
	 * @return
	 */
	public byte[] toByte(HTTPXStatusMessage message){
		byte[] serializedForm = new byte[0];
		
		try {
			//Serialize status line
			StringBuilder statusLine = new StringBuilder();
			statusLine.append(message.getProtocol()).append("/").append(message.getVersion()).append(" ")
						.append(message.getStatusCode()).append(" ").append(message.getReasonPhrase()).append(HTTPXConstants.CRLF);
						
			byte[] statusLineBytes = statusLine.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET); 

			//Serialize headers
			StringBuilder headerString = new StringBuilder();
			
			if(message.getHeaders() != null) {
				for(final Iterator<String> i = message.getHeaders().keySet().iterator(); i.hasNext();) {
					String name = i.next();
					String value = message.getHeader(name);
					headerString.append(name).append(HTTPXConstants.HEADER_NAME_SEPARATOR)
									.append(" ").append(value).append(HTTPXConstants.CRLF);
				}
			}
			headerString.append(HTTPXConstants.CRLF);
			byte[] headerBytes = headerString.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET);

			serializedForm = new byte[statusLineBytes.length + headerBytes.length];
			System.arraycopy(statusLineBytes, 0, serializedForm, 0, statusLineBytes.length);
			System.arraycopy(headerBytes, 0, serializedForm, statusLineBytes.length, headerBytes.length);
			
			LOGGER.debug("Statusline: " + "\n" + statusLine.toString());
			if (headerString.length() > 4) {
				LOGGER.debug("Headerstring: " + "\n" + headerString.toString());
			}
			
			LOGGER.debug("Statusline: " + "\n" + statusLine.toString());
			if (headerString.length() > 4) {
				LOGGER.debug("Headerstring: " + "\n" + headerString.toString());
			}
		}
		catch(UnsupportedEncodingException e) {
			LOGGER.error(e);
		}
		
		return serializedForm;
	}
	
	private void writeBody(InputStream messageInStream, OutputStream pipeOut){
		InputStream bufIn = new BufferedInputStream(messageInStream);
		OutputStream bufOut = new BufferedOutputStream(pipeOut);
		
		try {
			int readBytes = 0;
			byte[] buffer = new byte[BUFFER_SIZE];
			//write the content
			while ((readBytes = bufIn.read(buffer)) != -1) {
				bufOut.write(buffer, 0, readBytes);
//					LOGGER.debug("Readbytes: " + readBytes);
			}
			bufOut.flush();
		} catch (Exception e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		finally{
			if (bufOut!= null) {
				try {
					bufOut.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
			if (bufIn != null) {
				try {
					bufIn.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
			if (pipeOut != null) {
				try {
					pipeOut.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}
	}
	
	
	/***
	 * <p>
	 * Parses the request line of an HTTP request from an <code>InputStream</code> and creates a
	 * <code>HTTPXActionMessage</code>.
	 * </p>
	 * 
	 * 
	 * @param stream							the <code>InputStream</code> with the request message.
	 * @return 									the deserialized <code>HTTPXActionMessage</code>.
	 * 										
	 * @throws HTTPXProtocolViolationException	if the request line does not conform with the 
	 * 											required syntax: 
	 * 											Method SP Request-URI SP HTTP-Version
	 * 											and if the method in the request line is not supported. 
	 */
	public HTTPXActionMessage createActionMessageFromStream(InputStream stream) throws HTTPXProtocolViolationException {
		HTTPXActionMessage actionMessage = new HTTPXActionMessage();
		
		int currentCharacter = -1;
		int previousCharacter = -1;
//		StringWriter sw = new StringWriter();
		StringBuilder requestLine = new StringBuilder();
//		String requestLine = null;
		
		try {
			while((currentCharacter = stream.read()) != -1) {
				if(previousCharacter == 13 && currentCharacter == 10)
					break;
				if(currentCharacter != 13 && currentCharacter != 10){
//					sw.append((char)currentCharacter);
					requestLine.append((char)currentCharacter);
					
				}
				previousCharacter = currentCharacter;
			}
//			requestLine = sw.toString();
//			requestLine = sb.toString();
		}
		catch(IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
		
//		 is not empty?
		if (requestLine.length() != 0) {
			
			// split request line at trailing white spaces
			String[] words = requestLine.toString().split("\\s", 3); 
			// does request line contain three elements (method, uri, http-version)?
			if (words.length < 3) {
				LOGGER.error("The Statusline doesn't contain 3 Elements (http-version, status-code, reason-phrase)" + requestLine.toString());
				throw new HTTPXProtocolViolationException("Request line does not conform with the required syntax: Method SP Request-URI SP HTTP-Version");
			}
			
			// set method
			String method = words[0];
			
//			if		(method.equals("GET"))		actionMessage.setMethod(HTTPXConstants.GET);
//			else if (method.equals("HEAD"))   	actionMessage.setMethod(HTTPXConstants.HEAD);
//			else if (method.equals("POST"))   	actionMessage.setMethod(HTTPXConstants.POST);
//			else if (method.equals("PUT"))    	actionMessage.setMethod(HTTPXConstants.PUT);
//			else if (method.equals("DELETE")) 	actionMessage.setMethod(HTTPXConstants.DELETE);
//			else throw new MethodNotAllowedException("Method '" + method + "' is not allowed.");
			
			actionMessage.setMethod(method);
			
			// set Host, Port, Path and Query
			String uri = words[1];
			String[] parts = uri.split("://");
			if(parts.length == 2) { 									//Protocol exists in uri
				String[] tmp = parts[0].split("/");
				//split protocol and protocolversion
				if (tmp.length == 2) {
					actionMessage.setProtocol(tmp[0]);
					actionMessage.setVersion(tmp[1]);
				}else {
					LOGGER.error("The Statusline doesn't contain the Protocol and/or the Protocolversion (e.g. HTTP/1.1" + parts[0]);
					throw new HTTPXProtocolViolationException("Request line does not conform with the required syntax: Method SP Request-URI SP HTTP-Version");
				}
				
				uri = parts[1];
			}
			if(uri.indexOf('/') > -1) {
				int pathBegin = uri.indexOf('/');
				if(pathBegin > 0) { 									// Host exists in uri
					int portBegin = uri.indexOf(':');
					if(portBegin > -1) { 								// Port exists in uri
						actionMessage.setHost(uri.substring(0, portBegin));
						actionMessage.setPort(Integer.parseInt(uri.substring(portBegin + 1, pathBegin)));
					} else {
						actionMessage.setHost(uri.substring(0, pathBegin));
					}
				}
				int queryBegin = uri.indexOf('?');
				if(queryBegin > -1) { 									// Query exists in uri
					actionMessage.setPathAsString(uri.substring(pathBegin, queryBegin));
					
					String queryString = uri.substring(queryBegin + 1);	
					if(queryString.length() > 0) {
						ParameterList query = new ParameterList();
						
						String[] queries = queryString.split("\\&");
						int valueBegin;
						for(int i = 0; i < queries.length; i++) {
							valueBegin = queries[i].indexOf('=');
							if(valueBegin > -1) { 						// Parameter has Name and Value
								String[] parameters = queries[i].split("=");
								
								if (parameters.length >= 2) {
									query.setParameter(parameters[0], parameters[1]);
								}else
									query.setParameter(parameters[0]);
								
							} else { 									// parameter has only Name
								query.setParameter(queries[i]);
							}
						}
						actionMessage.setQuery(query);
					}
				} else {
					actionMessage.setPathAsString(uri.substring(pathBegin));
				}
			} else {
				throw new HTTPXProtocolViolationException("Malformed request uri.");
			}
			
			String[] tmp = words[2].split("/");
			//split protocol and protocolversion
			if (tmp.length == 2) {
				actionMessage.setProtocol(tmp[0]);						// set protocol
				actionMessage.setVersion(tmp[1]);
			}else {
				LOGGER.error("The Statusline doesn't contain the Protocol and/or the Protocolversion (e.g. HTTP/1.1" + words[2]);
				throw new HTTPXProtocolViolationException("Request line does not conform with the required syntax: Method SP Request-URI SP HTTP-Version");
			}
			
//			actionMessage.setProtocol(words[2]);						// set protocol
			
			this.parseHeader(stream, actionMessage);
			actionMessage.setInputStream(stream);
		}
		return actionMessage;
	}
	
	 
	/***
	 * <p>
	 * Returns a <code>InputStream</code> containing the serialized form of the HTTP request.
	 * </p>
	 * <pre>
	 * HttpRequest = Method SP Request-URI SP HTTP-Version CRLF
	 *               *(message-header CRLF)	
	 *               CRLF	
	 *               [message-body]
	 * </pre>
	 * @param message		the <code>HTTPXActionMessage</code> wich contains the request
	 * @return				the <code>InputStream</code>
	 */
	public InputStream toStream(final HTTPXActionMessage message) {
		
		byte[] serializedForm = new byte[0];
//		
//		try {
//			
//			//Serialize request line
//			StringBuilder requestLine = new StringBuilder();
//			requestLine.append(message.getMethod()).append(" ");
//			requestLine.append(message.getPathAsString());
//			
//			
//			String queryString = message.getQueryAsString();
//			if (!queryString.equals("")) {
//				requestLine.append("?").append(queryString);
//			}
////			else
////				requestLine += HTTPXConstants.PATH_SEPARATOR;
//			
////			ParameterList queue = message.getQuery();
////			Set<String> fieldList;
////			if ((queue != null) && (!(fieldList = queue.getAllKeys()).isEmpty())) {
////				requestLine += "?";
////				
////				for (String fieldName : fieldList) {
////					List<String> valueList = queue.getAllParameters(fieldName);
////					
////					for (Iterator<String> value = valueList.iterator(); value.hasNext();) {
////						requestLine += fieldName + HTTPXConstants.NAME_VALUE_SEPARATOR + value.next() + (value.hasNext() ? "&" : HTTPXConstants.EMPTY);
////					}
////				}
////			}else
////				requestLine += HTTPXConstants.PATH_SEPARATOR;
//			
//			requestLine.append(" ").append(message.getProtocol()).append("/").append(message.getVersion());
//			requestLine.append(HTTPXConstants.CRLF);
//			byte[] requestLineBytes = requestLine.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET); 
//			
//			//Serialize headers
//			StringBuilder headerString = new StringBuilder();
//			
//			if(message.getHeaders() != null) {
//				for(final Iterator<String> i = message.getHeaders().keySet().iterator(); i.hasNext();) {
//					String name = i.next();
//					String value = message.getHeader(name);
//					headerString.append(name).append(HTTPXConstants.HEADER_NAME_SEPARATOR).append(" ")
//									.append(value).append(HTTPXConstants.CRLF);
//				}
//			}	
//			headerString.append(HTTPXConstants.CRLF);
//			byte[] headersBytes = headerString.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET);
//		
//			serializedForm = new byte[requestLineBytes.length + headersBytes.length];
//			System.arraycopy(requestLineBytes, 0, serializedForm, 0, requestLineBytes.length);
//			System.arraycopy(headersBytes, 0, serializedForm, requestLineBytes.length, headersBytes.length);
//			
//			LOGGER.debug("Requestline: " + requestLine.toString());
//			LOGGER.debug("Headerline: " + headerString.toString());
//		}
//		catch(UnsupportedEncodingException e) {
//			LOGGER.error(e);
//		}
		
		serializedForm = toByte(message);
		InputStream pipeIn = new PipedInputStream();
		final OutputStream pipeOut = new PipedOutputStream();
		try {
			((PipedInputStream)pipeIn).connect((PipedOutputStream)pipeOut);
			pipeOut.write(serializedForm);
			pipeOut.flush();
		} catch (IOException e) {
			LOGGER.error(e);
		}
		
		if (message.getInputStream() != null) {
			//create new Thread
			new Thread(new Runnable(){
				public void run(){
					writeBody(message.getInputStream(), pipeOut);
				}
			}).start();
		}else {
			try {
				pipeOut.close();
			} catch (IOException e) {
				LOGGER.error(e);
				e.printStackTrace();
			}
		}
		
		return pipeIn;
	}
	
	/**
	 * <p>
	 * Returns a <code>InputStream</code> containing the serialized form of the HTTP request.
	 * </p>
	 * <pre>
	 * HttpRequest = Method SP Request-URI SP HTTP-Version CRLF
	 *               *(message-header CRLF)	
	 *               CRLF	
	 *               [message-body]
	 * </pre>
	 * 
	 * 
	 * @param message	the <code>HTTPXActionMessage</code> wich contains the request
	 * @return			the <code>byte</code> Array
	 */
	public byte[] toByte(HTTPXActionMessage message){
		byte[] serializedForm = new byte[0];
		
		try {
			
			//Serialize request line
			StringBuilder requestLine = new StringBuilder();
			requestLine.append(message.getMethod()).append(" ");
			requestLine.append(message.getPathAsString());
			
			
			String queryString = message.getQueryAsString();
			if (!queryString.equals("")) {
				requestLine.append("?").append(queryString);
			}
//			else
//				requestLine += HTTPXConstants.PATH_SEPARATOR;
			
//			ParameterList queue = message.getQuery();
//			Set<String> fieldList;
//			if ((queue != null) && (!(fieldList = queue.getAllKeys()).isEmpty())) {
//				requestLine += "?";
//				
//				for (String fieldName : fieldList) {
//					List<String> valueList = queue.getAllParameters(fieldName);
//					
//					for (Iterator<String> value = valueList.iterator(); value.hasNext();) {
//						requestLine += fieldName + HTTPXConstants.NAME_VALUE_SEPARATOR + value.next() + (value.hasNext() ? "&" : HTTPXConstants.EMPTY);
//					}
//				}
//			}else
//				requestLine += HTTPXConstants.PATH_SEPARATOR;
			
			requestLine.append(" ").append(message.getProtocol()).append("/").append(message.getVersion());
			requestLine.append(HTTPXConstants.CRLF);
			byte[] requestLineBytes = requestLine.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET); 
			
			//Serialize headers
			StringBuilder headerString = new StringBuilder();
			
			if(message.getHeaders() != null) {
				for(final Iterator<String> i = message.getHeaders().keySet().iterator(); i.hasNext();) {
					String name = i.next();
					String value = message.getHeader(name);
					headerString.append(name).append(HTTPXConstants.HEADER_NAME_SEPARATOR).append(" ")
									.append(value).append(HTTPXConstants.CRLF);
				}
			}	
			headerString.append(HTTPXConstants.CRLF);
			byte[] headersBytes = headerString.toString().getBytes(HTTPXConstants.DEFAULT_CHARSET);
		
			serializedForm = new byte[requestLineBytes.length + headersBytes.length];
			System.arraycopy(requestLineBytes, 0, serializedForm, 0, requestLineBytes.length);
			System.arraycopy(headersBytes, 0, serializedForm, requestLineBytes.length, headersBytes.length);
			
			LOGGER.debug("Requestline: " + requestLine.toString());
			LOGGER.debug("Headerline: " + headerString.toString());
		}
		catch(UnsupportedEncodingException e) {
			LOGGER.error(e);
		}
		return serializedForm;
	}
	
	/**
	 * Parses the header fields of an HTTP message from and <code>InputStream</code> and adds theme to the map of headers.
	 * 
	 * @param stream					the <code>InputStream</code>
	 * @param message					the <code>HTTPXAbstractMessage</code>
	 */
	private void parseHeader(InputStream stream, HTTPXAbstractMessage message){
		int currentCharacter = -1;
		int previousCharacter = -1;
		StringBuilder sb = new StringBuilder();
		String headerLine = null;
		int crlfCount = 0;
		
		try {
			currentCharacter = stream.read();
			while(true) { 									//while end of headers not reached
				while(currentCharacter != -1 && !(previousCharacter == 13 && currentCharacter == 10)) { //read header line till CRLF
					if(currentCharacter != 13 && currentCharacter != 10) {
						sb.append((char)currentCharacter);
						crlfCount = 0;
					}
					previousCharacter = currentCharacter;
					currentCharacter = stream.read();
				}
				crlfCount++; 							//increase CRLF counter after end of header line was reached
				if(crlfCount == 2) 						//break if end of headers reached
					break;
				
				//parse headerLine
				headerLine = sb.toString(); 			
				int sep_index = headerLine.indexOf(HTTPXConstants.HEADER_NAME_SEPARATOR);
				if(sep_index > 0) 
					message.setHeader(headerLine.substring(0, sep_index).trim(), headerLine.substring(sep_index +1).trim());
				else
					return;
				
				//Clear StringBuilder
				sb = new StringBuilder();
				previousCharacter = currentCharacter;
				currentCharacter = stream.read();
			}
		}
		catch(IOException e) {
			LOGGER.error(e);
			e.printStackTrace();
		}
	}
}
