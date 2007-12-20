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

package org.eclipse.actf.ai.xmlstore.spi;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLUtil {
    private static DocumentBuilderFactory domFactory;
    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        if (domFactory == null) {
            domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);            
        }
        return domFactory.newDocumentBuilder();
    }

    public static XMLReader newXMLReader() throws SAXException {
        return XMLReaderFactory.createXMLReader();
    }
}
