/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.xmlstore;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * IXMLInfo represents a metadata or config XML file, which contains &lt;meta&gt;
 * information.
 */
public interface IXMLInfo {
	/**
	 * @return the document element of the XML file.
	 * @throws XMLStoreException
	 */
	Node getRootNode() throws XMLStoreException;

	/**
	 * @param handler the content handler to be used for parsing.
	 */
	void setContentHandler(ContentHandler handler);

	/**
	 * Starts SAX parsing using the content handler specified by {@link #setContentHandler(ContentHandler)}
	 * @throws XMLStoreException
	 * @throws SAXException
	 */
	void startSAX() throws XMLStoreException, SAXException;

	/**
	 * Reset the SAX parsing.
	 * @throws XMLStoreException
	 */
	void reset() throws XMLStoreException;

	/**
	 * @return the documentation text declared in the &lt;meta&gt; information.
	 */
	String getDocumentation();

	/**
	 * @return whether the XML file is for user annotation or not.
	 */
	boolean isUserEntry();

	/**
	 * @return the priority value declared in the &lt;meta&gt; information.
	 */
	int getPriority();

}
