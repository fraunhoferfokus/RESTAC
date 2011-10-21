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

package de.fhg.fokus.restac.resource.core.client;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.omg.PortableInterceptor.LOCATION_FORWARD;

import de.fhg.fokus.restac.httpx.core.common.HTTPStatus;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessage;
import de.fhg.fokus.restac.httpx.core.common.HTTPXActionMessageTransceiver;
import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.core.common.HTTPXStatusMessage;
import de.fhg.fokus.restac.httpx.core.common.ParameterList;
import de.fhg.fokus.restac.httpx.core.common.Path;
import de.fhg.fokus.restac.httpx.core.communication.exceptions.HTTPXProtocolViolationException;
import de.fhg.fokus.restac.httpx.util.serialization.converter.TextPlainContentReader;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXInputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXOutputStream;
import de.fhg.fokus.restac.httpx.util.streams.HTTPXStreamFactory;
import de.fhg.fokus.restac.resource.core.common.Resource;
import de.fhg.fokus.restac.resource.core.common.exceptions.AccessForbiddenException;
import de.fhg.fokus.restac.resource.core.common.exceptions.BadContentException;
import de.fhg.fokus.restac.resource.core.common.exceptions.ConflictException;
import de.fhg.fokus.restac.resource.core.common.exceptions.InternalException;
import de.fhg.fokus.restac.resource.core.common.exceptions.NoContentException;
import de.fhg.fokus.restac.resource.core.common.exceptions.NotFoundException;
import de.fhg.fokus.restac.resource.core.common.exceptions.RequestTimeoutException;
import de.fhg.fokus.restac.resource.core.common.exceptions.ResourceException;
import de.fhg.fokus.restac.resource.core.common.exceptions.UnsupportedContentTypeException;
 
/**
 * A local proxy for a remote <code>Resource</code> object.
 * 
 * @author Stefan Föll; Anna Kress
 * @see NodeProxy
 */
public class ResourceProxy implements Resource {
	
	/** The URL of the resource connected to this proxy. */
	private URL url;
	
	/** 
	 * The connection used to send calls to the proxy to the <code>Resource</code> 
	 * identified by the URL.
	 */
	private HTTPXActionMessageTransceiver transceiver;
	
	
	/**
	 * Constructs a new local proxy for a remote <code>Resource</code> located at
	 * the given URL using the given connection.
	 * 
	 * @param url			the URL of the resource
	 * @param connection	the connection to send calls on the stub to the resource
	 */
	public ResourceProxy(URL url, HTTPXActionMessageTransceiver transceiver) {
		this.transceiver= transceiver;
		this.url=url;
	}
	
	/**
	 * Constructs a new local proxy for a remote <code>Resource</code> located at
	 * the given URL.
	 * 
	 * @param url	the URL of the resource
	 */
	public ResourceProxy(URL url) {
		this.url = url;
		this.transceiver = HTTPXActionMessageTransceiver.getInstance();
	}
	
	/**
	 * Returns the <code>String</code> representation of the URL of this resource proxy.
	 * 
	 * @return	the <code>String</code> representation of the URL of this resource proxy
	 * @see 	de.fhg.fokus.rest2.resource.common.Resource#getUniformIdentifier()
	 */
	public String getUniformIdentifier() {
		return url.getPath();
	}
	
	/**
	 * Sends a <code>GET</code> request to the resource.
	 * 
	 * @return						the body of the response to the request.
	 * @throws ResourceException	if an error occured while trying to process the
	 * 								request at the resource
	 */
	public HTTPXInputStream get() throws ResourceException {
		
		HTTPXStatusMessage response;
		
		try {
			HTTPXActionMessage request =new HTTPXActionMessage("GET", this.url.getProtocol(), 
				this.url.getHost(), this.url.getPort(), new Path(url.toString()), new ParameterList(this.url.getQuery()), null, null);

			// get response
			try {
				response = this.transceiver.getOutboundDispatcher().deliverSynchronous(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response=null;
			}
		
		} catch (HTTPXProtocolViolationException e) {
			response = new HTTPXStatusMessage(HTTPStatus.REQUEST_TIMEOUT.getCode(), HTTPStatus.REQUEST_TIMEOUT.getMessage(), null, null, null);
		}
		
	    checkResponse(response);

	    return response!=null ? HTTPXStreamFactory.getHTTPXInputStream(response) : null;
		
	}
	
	/**
	 * Sends a <code>HEAD</code> request to the resource.
	 * 
	 * @throws ResourceException	if an error occured while trying to process the
	 * 								request at the resource
	 */
	public void head() throws ResourceException {

		HTTPXStatusMessage response;
		
		try {
			HTTPXActionMessage request =new HTTPXActionMessage("HEAD", this.url.getProtocol(), 
				this.url.getHost(), this.url.getPort(), new Path(url.toString()), new ParameterList(this.url.getQuery()), null, null);

			// get response
			try {
				response = this.transceiver.getOutboundDispatcher().deliverSynchronous(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response=null;
			}
		
		} catch (HTTPXProtocolViolationException e) {
				response = new HTTPXStatusMessage(HTTPStatus.REQUEST_TIMEOUT.getCode(), HTTPStatus.REQUEST_TIMEOUT.getMessage(), null, null, null);
			}
		
		checkResponse(response);
	}
	
	/**
	 * Sends a <code>POST</code> request to the resource.
	 * 
	 * @param content				the body of the request
	 * @return						the body of the response to the request.
	 * @throws ResourceException	if an error occured while trying to process the
	 * 								request at the resource
	 */
	public HTTPXInputStream post(HTTPXOutputStream content) throws ResourceException {
		
		HTTPXStatusMessage response;
		
		try {
			
			HTTPXActionMessage request;

			if (content!=null) {
			request = content.constructHTTPXActionMessage("POST", this.url.getProtocol(), 
				this.url.getHost(), this.url.getPort(), new Path(this.url.getFile()), new ParameterList(this.url.getQuery()), null);
			} else {
				request=new HTTPXActionMessage("POST", url.getProtocol(), url.getHost(), url.getPort(), new Path(this.url.getFile()) , new ParameterList(this.url.getQuery()), null, null);
			}

			// get response
			try {
				response = this.transceiver.getOutboundDispatcher().deliverSynchronous(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response=null;
			}
		}
		
		 catch (HTTPXProtocolViolationException e) {
				response = new HTTPXStatusMessage(HTTPStatus.REQUEST_TIMEOUT.getCode(), HTTPStatus.REQUEST_TIMEOUT.getMessage(), null, null, null);
			}
		
		checkResponse(response);
		
	    return response!=null ? HTTPXStreamFactory.getHTTPXInputStream(response) : null;
	}
	
	/**
	 * Sends a <code>PUT</code> request to the resource.
	 * 
	 * @param content				the body of the request
	 * @throws ResourceException	if an error occured while trying to process the
	 * 								request at the resource
	 */
	public void put(HTTPXOutputStream content) throws ResourceException {
		
		HTTPXStatusMessage response;
		
		try {
			
			HTTPXActionMessage request;
			
			if (content!=null) {
				request =content.constructHTTPXActionMessage("PUT", this.url.getProtocol(), 
						this.url.getHost(), this.url.getPort(), new Path(url.toString()), new ParameterList(this.url.getQuery()), null);
				} else {
					request=new HTTPXActionMessage("PUT", url.getProtocol(), url.getHost(), url.getPort(), new Path(url.toString()) , new ParameterList(this.url.getQuery()), null, null);
				}

			// get response
			try {
				response = this.transceiver.getOutboundDispatcher().deliverSynchronous(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response=null;
			}
		}
		
		 catch (HTTPXProtocolViolationException e) {
				response = new HTTPXStatusMessage(HTTPStatus.REQUEST_TIMEOUT.getCode(), HTTPStatus.REQUEST_TIMEOUT.getMessage(), null, null, null);
			}
		//if (isRedirect())  new ResourceProxy(url,transceiver).put(content);
		checkResponse(response);
	}
	
	/** 
	 * Sends a <code>DELETE</code> request to the resource. 
	 *
	 * @throws ResourceException	if an error occured while trying to process the
	 * 								request at the resource
	 */
	public void delete() throws ResourceException {
		HTTPXStatusMessage response;
		
		try {
			HTTPXActionMessage request =new HTTPXActionMessage("DELETE", this.url.getProtocol(), 
				this.url.getHost(), this.url.getPort(), new Path(url.toString()), new ParameterList(this.url.getQuery()), null, null);

			// get response
			try {
				response = this.transceiver.getOutboundDispatcher().deliverSynchronous(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response=null;
			}
		}
		
		 catch (HTTPXProtocolViolationException e) {
				response = new HTTPXStatusMessage(HTTPStatus.REQUEST_TIMEOUT.getCode(), HTTPStatus.REQUEST_TIMEOUT.getMessage(), null, null, null);
			}
		 
		//if (isRedirect()) new ResourceProxy(url,transceiver).delete();
		checkResponse(response);
	}

	
	/**
	 * Checks the response HTTPStatus to see whether the processing of the request at the
	 * resource was successful.
	 * 
	 * @throws ResourceException	if an error occured while trying to process the
	 * 								request at the resource 
	 */
	private void checkResponse(HTTPXStatusMessage response) throws ResourceException {
	  if (response!=null) {
		 
		  int responseCode=response.getStatusCode();
		 
		 String msg;

		 if (response.getHeader(HTTPXConstants.CONTENT_TYPE) != null &&
				 response.getHeader(HTTPXConstants.CONTENT_TYPE).equals(HTTPXConstants.TYPE_TXT_PLAIN)) {
		
				 // get either plain or chunked input stream
				 HTTPXInputStream in = HTTPXStreamFactory.getHTTPXInputStream(response);
					
				 TextPlainContentReader rd;
				try {
					rd = new TextPlainContentReader(in);
					 msg = (String)rd.readBuffered();
				} catch (ContentConvertingException e) {
					msg="";
				}
		 }
		 else msg="";
		 
		 for (int i=0;i<HTTPStatus.ALL_USED.length;++i) {
	       if (HTTPStatus.ALL_USED[i].getCode()==responseCode) {
	    	   if      (responseCode==404)  throw new NotFoundException(msg);
	    	   else if (responseCode ==415) throw new UnsupportedContentTypeException(msg);
	    	   else if (responseCode ==500) throw new InternalException(msg);
	    	   else if (responseCode ==400) throw new BadContentException(msg);
	    	   else if (responseCode ==403) throw new AccessForbiddenException(msg);
	    	   else if (responseCode == 408) throw new RequestTimeoutException(msg);
	    	   else if (responseCode == 409) throw new ConflictException(msg);
	    	   else if (responseCode == 204) throw new NoContentException(msg);
	    	   else  {
	    		   final HTTPStatus status=HTTPStatus.ALL_USED[i];
	    		   throw new ResourceException(status.getMessage()) {
					@Override
					public de.fhg.fokus.restac.httpx.core.common.HTTPStatus getStatus() {
						return  status;
					}
	    		   };
	    	   }
	       }
	    }
	  }
	}

	
	
}
