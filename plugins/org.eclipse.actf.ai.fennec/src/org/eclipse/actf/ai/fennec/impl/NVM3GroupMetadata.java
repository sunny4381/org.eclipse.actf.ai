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


class NVM3GroupMetadata extends NVM3BundleMetadata {
    
    public String getAltText(ITreeItem item) {
        String r = super.getAltText(item);
        if (r == null) return null;
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

    NVM3GroupMetadata(NVM3ServiceImpl nvm3Service,
                      IQuery q, NVM3Mode mode,
                      NVM3Metadata[] childMds) {
        super(nvm3Service, q, mode, childMds);
    }

}

