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
package org.eclipse.actf.ai.fennec.impl;

import java.util.Iterator;
import java.util.List;

import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.query.IQuery;
import org.w3c.dom.NodeList;


class NVM3SimpleMetadata extends NVM3Metadata {
    static public final int TYPE_ALT_TEXT = 1;
    static public final int TYPE_DESCRIPTION = 2;
    static public final int TYPE_HEADER = 3;

    private short headingLevel;
    private int type;

    private List stringFormat;

    public short getHeadingLevel(ITreeItem item) {
        return headingLevel;
    }
    
    public String getString(ITreeItem item) {
        StringBuffer buf = new StringBuffer();
        if (stringFormat == null) return null;
        Iterator it = stringFormat.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof String) {
                buf.append((String) o);
            } else if (o instanceof IQuery) {
                IQuery q = (IQuery) o;
                NodeList nl = query(q, item);
                String str = NodeUtil.extractString(nl);
                buf.append(str);
            }
        }
        return buf.toString();
    }

    public String getAltText(ITreeItem item) {
        if (type == TYPE_ALT_TEXT) return getString(item);
        return "";
    }

    public String getDescription(ITreeItem item) {
        if (type == TYPE_DESCRIPTION) return getString(item);
        return "";
    }

    static NVM3SimpleMetadata newHeader(NVM3ServiceImpl nvm3Service,
                                        IQuery q, NVM3Mode mode, short level) {
        NVM3SimpleMetadata md = new NVM3SimpleMetadata(nvm3Service, q, mode);
        md.type = TYPE_HEADER;
        md.headingLevel = level;
        return md;
    }

    static NVM3SimpleMetadata newAltText(NVM3ServiceImpl nvm3Service,
                                         IQuery q, NVM3Mode mode, List stringFormat) {
        NVM3SimpleMetadata md = new NVM3SimpleMetadata(nvm3Service, q, mode);
        md.type = TYPE_ALT_TEXT;
        md.stringFormat = stringFormat;
        return md;
    }

    static NVM3SimpleMetadata newDescription(NVM3ServiceImpl nvm3Service,
                                             NVM3Mode mode, IQuery q, List stringFormat) {
        NVM3SimpleMetadata md = new NVM3SimpleMetadata(nvm3Service, q, mode);
        md.type = TYPE_DESCRIPTION;
        md.stringFormat = stringFormat;
        return md;
    }

    private NVM3SimpleMetadata(NVM3ServiceImpl nvm3Service,
                               IQuery q, NVM3Mode mode) {
        super(nvm3Service, q, mode);
    }

    List expand(TreeItemNVM3 pItem, int trigger) throws NVM3Exception {
        return null;
    }
}

