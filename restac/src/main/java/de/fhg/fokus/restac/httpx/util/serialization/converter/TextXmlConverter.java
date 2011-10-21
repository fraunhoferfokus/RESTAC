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

package de.fhg.fokus.restac.httpx.util.serialization.converter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.fhg.fokus.restac.httpx.core.common.HTTPXConstants;
import de.fhg.fokus.restac.httpx.util.serialization.exceptions.ContentConvertingException;

public class TextXmlConverter extends ContentConverter{
	
	private final static Logger LOGGER = Logger.getLogger(TextXmlConverter.class);
	
	private static final String XML_VERSION        = "1.0";
	private static final String XML_ENCODING       = HTTPXConstants.DEFAULT_CHARSET;
	
	static public Document byteArrayToContent(byte[] body, String charset) throws ContentConvertingException {

		Document document = null;
		DocumentBuilder dBuilder = null;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		}
		catch(ParserConfigurationException e) {}
		try  {
			try {
				document = dBuilder.parse(new InputSource(new StringReader(new String(body, charset))));
			}
			catch(UnsupportedEncodingException e) {
				LOGGER.warn("Unsupported Content-Encoding: '" + charset + "'. Using charset: '" + Charset.defaultCharset().displayName() + "' instead.");
				document = dBuilder.parse(new InputSource(new StringReader(new String(body))));
			}
		}
		catch(Exception e) {
			throw new ContentConvertingException("Content of body is no valid XML.", e);
		}
		
		return document;
	}
	
	static public byte[] contentToByteArray(Document document, String charset) throws IOException {
		byte[] bytes = new byte[0];
		
		if(document == null) return null;

		try {
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			transformer.setOutputProperty(OutputKeys.VERSION,XML_VERSION);
			transformer.setOutputProperty(OutputKeys.ENCODING,XML_ENCODING ); //TODO: Use default charset or given charset here?
			transformer.transform(source, result);
			try {
				bytes = sw.toString().getBytes(charset);
			}
			catch(UnsupportedEncodingException e) {
				LOGGER.error(e);
			}
		}
		catch(TransformerConfigurationException e) {
			LOGGER.error(e);
		}
		catch(TransformerException e) {
			LOGGER.error(e);
		}

		return bytes;
	}
}
