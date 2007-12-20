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

import org.eclipse.actf.ai.xmlstore.nvdl.model.NVDLConst;
import org.xml.sax.Attributes;


/**
 * The <code>NVDLSlotNodeAttributes</code> is the special
 * attribute class for slot-node.
 */
class NVDLSlotNodeAttributes implements Attributes {
    private final String id;
    private final String qName;

    public int getLength() {
        // ns and localName
        return 1;
    }

    public String getURI(int index) {
        if (index == 0) {
            // return NVDLConst.INSTANCE_REC_NS;
            return "";
        }
        return null;
    }

    public String getLocalName(int index) {
        if (index == 0) {
            return NVDLConst.SLOT_NODE_ID_ATTR;
        }
        return null;
    }

    public String getQName(int index) {
        if (index == 0) {
            return qName;
        }
        return null;
    }

    public String getType(int index) {
        if (index == 0) {
            return "CDATA";
        }
        return null;
    }

    public String getValue(int index) {
        if (index == 0) {
            return id;
        }
        return null;
    }

    public int getIndex(String uri, String localName) {
        if (!(NVDLConst.INSTANCE_REC_NS.equals(uri))) return -1;
        if (NVDLConst.SLOT_NODE_ID_ATTR.equals(localName)) {
            return 0;
        }
        return -1;
    }

    public int getIndex(String qName) {
        if (this.qName.equals(qName)) {
            return 0;
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
        if (!(NVDLConst.INSTANCE_REC_NS.equals(uri))) return null;
        if (NVDLConst.SLOT_NODE_ID_ATTR.equals(localName)) {
            return id;
        }
        return null;
    }

    public String getValue(String qName) {
        if (this.qName.equals(qName)) {
            return id;
        }
        return null;
    }
    
    NVDLSlotNodeAttributes(String qName, String id) {
        this.qName = qName;
        this.id = id;
    }
}
