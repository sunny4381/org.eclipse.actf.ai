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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.query.IQuery;
import org.w3c.dom.Node;


abstract class NVM3BundleMetadata extends NVM3Metadata {
    private static final NVM3Metadata[] emptyChildMetadata = new NVM3Metadata[0];
    protected final NVM3Metadata[] childMetadata;

    protected NVM3BundleMetadata(NVM3ServiceImpl nvm3Service,
                                 IQuery q, NVM3Mode mode, NVM3Metadata[] mds) {
        super(nvm3Service, q, mode);
        if (mds == null) {
            mds = emptyChildMetadata;
        }
        this.childMetadata = mds;
    }

    protected NVM3BundleMetadata(NVM3ServiceImpl nvm3Service,
                                NVM3Mode mode,
                                Node node,
                                NVM3Metadata[] mds) {
        super(nvm3Service, mode, node);
        if (mds == null) {
            mds = emptyChildMetadata;
        }
        this.childMetadata = mds;
    }

    @Override
    List expand(TreeItemNVM3 pItem, int trigger) throws NVM3Exception {
        ArrayList ret = new ArrayList();
        for (int i = 0; i < childMetadata.length; i++) {
            NVM3Metadata cmd = childMetadata[i];
            List ret2 = cmd.buildItems(pItem, null, trigger);
            if (ret2 != null) ret.addAll(ret2);
        }
        return ret;
    }


    NVM3Metadata[] getChildMetadata() {
        return childMetadata;
    }

    // CoR pattern.
    @Override
    public String getAltText(ITreeItem item) {
        for (int i = 0; i < childMetadata.length; i++) {
            if (childMetadata[i].hasTargets()) continue;
            if (!(childMetadata[i] instanceof NVM3SimpleMetadata)) continue;
            String ret = childMetadata[i].getAltText(item);
            if (ret == null) return null;
            if (ret.length() > 0) return ret;
        }
        return "";
    }

    @Override
    public String getDescription(ITreeItem item) {
        for (int i = 0; i < childMetadata.length; i++) {
            if (childMetadata[i].hasTargets()) continue;
            if (!(childMetadata[i] instanceof NVM3SimpleMetadata)) continue;
            String ret = childMetadata[i].getDescription(item);
            if (ret == null) return null;
            if (ret.length() > 0) return ret;
        }
        return "";
    }

    @Override
    public short getHeadingLevel(ITreeItem item) {
        for (int i = 0; i < childMetadata.length; i++) {
            if (childMetadata[i].hasTargets()) continue;
            if (!(childMetadata[i] instanceof NVM3SimpleMetadata)) continue;
            short ret = childMetadata[i].getHeadingLevel(item);
            if (ret == -1) return -1;
            if (ret > 0) return ret;
        }
        return 0;
    }
}

