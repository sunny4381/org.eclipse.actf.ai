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

package org.eclipse.actf.ai.xmlstore.nvdl.dispatcher;

import org.xml.sax.Attributes;

/**
 * The <code>NVDLPlaceHolderAttributes</code> is the special
 * attribute class for placeholders.
 */
class NVDLPlaceHolderAttributes implements Attributes {
    private String ns;
    private String localName;

    public int getLength() {
        // ns and localName
        return 2;
    }

    public String getURI(int index) {
        switch (index) {
        case 0: case 1:
            return "";
        }
        return null;
    }

    public String getLocalName(int index) {
        switch (index) {
        case 0:
            return "ns";
        case 1:
            return "localName";
        }
        return null;
    }

    public String getQName(int index) {
        return getLocalName(index);
    }

    public String getType(int index) {
        switch (index) {
        case 0: case 1:
            return "CDATA";
        }
        return null;
    }

    public String getValue(int index) {
        switch (index) {
        case 0:
            return ns;
        case 1:
            return localName;
        }
        return null;
    }

    public int getIndex(String uri, String localName) {
        if ((uri == null) || (uri.length() != 0)) return -1;
        if ("ns".equals(localName)) {
            return 0;
        } else if ("localName".equals(localName)) {
            return 1;
        }
        return -1;
    }

    public int getIndex(String qName) {
        if ("ns".equals(qName)) {
            return 0;
        } else if ("localName".equals(qName)) {
            return 1;
        }
        return -1;
    }

    public String getType(String uri, String localName) {
        return getType(getIndex(uri, localName));
    }

    public String getType(String qName) {
        return getType(getIndex(qName));
    }

    public String getValue(String uri, String localName) {
        if ((uri == null) || (uri.length() != 0)) return null;
        if ("ns".equals(localName)) {
            return ns;
        } else if ("localName".equals(localName)) {
            return localName;
        }
        return null;
    }

    public String getValue(String qName) {
        if ("ns".equals(qName)) {
            return ns;
        } else if ("localName".equals(qName)) {
            return localName;
        }
        return null;
    }
    
    NVDLPlaceHolderAttributes(String ns, String localName) {
        this.ns = ns;
        this.localName = localName;
    }
}
