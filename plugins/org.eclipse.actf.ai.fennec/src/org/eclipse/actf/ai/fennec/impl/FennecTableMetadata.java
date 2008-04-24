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

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.query.IQuery;
import org.eclipse.actf.model.dom.dombycom.INodeEx;


class FennecTableMetadata extends FennecBundleMetadata {
    private final FennecTableRowMetadata[] rowMds;

    public int getRowSize() {
        return rowMds.length;
    }

    public FennecTableRowMetadata getRow(int idx) {
        return rowMds[idx];
    }
    
    public String getAltText(ITreeItem item) {
        String r = super.getAltText(item);
        if (r.length() > 0) return r;
        // If not specified, extract some string from the node.
        return NodeUtil.extractString(item);
    }

    public short getHeadingLevel(ITreeItem item) {
        short r = super.getHeadingLevel(item);
        if (r > 0) return r;
        if (r == -1) return -1;
        Object baseNode = item.getBaseNode();
        if (baseNode instanceof INodeEx) {
            return ((INodeEx) baseNode).getHeadingLevel();
        }
        return 0;
    }

    FennecTableMetadata(FennecServiceImpl fennecService,
                      IQuery q, FennecMode mode,
                      FennecMetadata[] childMds,
                      FennecTableRowMetadata[] rowMds) {
        super(fennecService, q, mode, childMds);
        this.rowMds = rowMds;
    }

}

