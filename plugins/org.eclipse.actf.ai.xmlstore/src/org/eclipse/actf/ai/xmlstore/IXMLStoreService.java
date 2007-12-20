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

import java.io.File;



public interface IXMLStoreService {
    IXMLSelector getSelectorWithDocElem(String name, String iri);
    IXMLSelector getSelectorWithIRI(String iri);
    // IXMLSelector getSelectorWithXPath(String xpath);
    IXMLStore getRootStore();
    
    IXMLEditableInfo newUserXML(String namespaceURI, String qualifiedName, String targetUriPattern) throws XMLStoreException;
    boolean exportMetadata(IXMLInfo info, File file);
    boolean importMetadata(File file);
    boolean exportAllAnnotations(File dest);
}
