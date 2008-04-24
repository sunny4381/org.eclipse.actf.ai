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

import org.eclipse.actf.ai.fennec.FennecException;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.query.IQuery;
import org.w3c.dom.Node;


abstract class FennecBundleMetadata extends FennecMetadata {
    private static final FennecMetadata[] emptyChildMetadata = new FennecMetadata[0];
    protected final FennecMetadata[] childMetadata;

    protected FennecBundleMetadata(FennecServiceImpl fennecService,
                                 IQuery q, FennecMode mode, FennecMetadata[] mds) {
        super(fennecService, q, mode);
        if (mds == null) {
            mds = emptyChildMetadata;
        }
        this.childMetadata = mds;
    }

    protected FennecBundleMetadata(FennecServiceImpl fennecService,
                                FennecMode mode,
                                Node node,
                                FennecMetadata[] mds) {
        super(fennecService, mode, node);
        if (mds == null) {
            mds = emptyChildMetadata;
        }
        this.childMetadata = mds;
    }

    @Override
    List expand(TreeItemFennec pItem, int trigger) throws FennecException {
        ArrayList ret = new ArrayList();
        for (int i = 0; i < childMetadata.length; i++) {
            FennecMetadata cmd = childMetadata[i];
            List ret2 = cmd.buildItems(pItem, null, trigger);
            if (ret2 != null) ret.addAll(ret2);
        }
        return ret;
    }


    FennecMetadata[] getChildMetadata() {
        return childMetadata;
    }

    // CoR pattern.
    @Override
    public String getAltText(ITreeItem item) {
        for (int i = 0; i < childMetadata.length; i++) {
            if (childMetadata[i].hasTargets()) continue;
            if (!(childMetadata[i] instanceof FennecSimpleMetadata)) continue;
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
            if (!(childMetadata[i] instanceof FennecSimpleMetadata)) continue;
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
            if (!(childMetadata[i] instanceof FennecSimpleMetadata)) continue;
            short ret = childMetadata[i].getHeadingLevel(item);
            if (ret == -1) return -1;
            if (ret > 0) return ret;
        }
        return 0;
    }
}

