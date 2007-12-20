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

public interface IXMLInfo {
    Node getRootNode() throws XMLStoreException;
    
    void setContentHandler(ContentHandler h);
    void startSAX() throws XMLStoreException, SAXException;
    void reset() throws XMLStoreException;

    String getDocumentation();
    boolean isUserEntry();
    int getPriority();
    
}
