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

package org.eclipse.actf.ai.xmlstore.nvdl.util;

import org.xml.sax.Attributes;

/**
 * The <code>EmptyAttributes</code> is the special
 * attribute class to descrive empty attributes.
 */
public class EmptyAttributes implements Attributes {
    private static final EmptyAttributes instance = new EmptyAttributes();
    public static EmptyAttributes getInstance() {
        return instance;
    }

    public int getLength() {
        return 0;
    }

    public String getURI(int index) {
        return null;
    }

    public String getLocalName(int index) {
        return null;
    }

    public String getQName(int index) {
        return null;
    }

    public String getType(int index) {
        return null;
    }

    public String getValue(int index) {
        return null;
    }

    public int getIndex(String uri, String localName) {
        return -1;
    }

    public int getIndex(String qName) {
        return -1;
    }

    public String getType(String uri, String localName) {
        return null;
    }

    public String getType(String qName) {
        return null;
    }

    public String getValue(String uri, String localName) {
        return null;
    }

    public String getValue(String qName) {
        return null;
    }
    
    private EmptyAttributes() {
    }
}
