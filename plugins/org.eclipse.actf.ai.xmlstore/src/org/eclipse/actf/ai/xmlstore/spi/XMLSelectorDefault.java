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

import org.eclipse.actf.ai.xmlstore.IXMLSelector;

public abstract class XMLSelectorDefault implements IXMLSelector {
    public String getDocumentElementName() {
        return null;
    }

    public String getDocumentElementNS() {
        return null;
    }

    public String getURI() {
        return null;
    }
}

