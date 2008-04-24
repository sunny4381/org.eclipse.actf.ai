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

import java.util.Arrays;
import java.util.List;

import org.eclipse.actf.ai.fennec.FennecException;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.w3c.dom.Node;


class FennecGeneratedMetadata extends FennecBundleMetadata {
    private final Node node;
    
    @Override
    public String getAltText(ITreeItem item) {
        String r = super.getAltText(item);
        if (r == null) return null;
        if (r.length() > 0)
            return r;
        if (node instanceof INodeEx) {
            return ((INodeEx) node).extractString();
        }
        return "";
    }

    @Override
    public String getDescription(ITreeItem item) {
        return super.getDescription(item);
    }

    public short getHeadingLevelByMetadata(ITreeItem item) {
        return super.getHeadingLevel(item);
    }

    @Override
    public short getHeadingLevel(ITreeItem item) {
        short r = super.getHeadingLevel(item);
        if (r > 0)
            return r;
        if (r == -1)
            return -1;
        if (node instanceof INodeEx) {
            return ((INodeEx) node).getHeadingLevel();
        }
        return 0;
    }

    @Override
    List expand(TreeItemFennec pItem, int trigger) throws FennecException {
        ITreeItem[] childItems = pItem.getChildItems();
        if (!mode.changed(pItem, trigger) && pItem.hasAlreadyChildRefreshed()) {
            return Arrays.asList(childItems);
        } else {
            TreeItemFennec bItem = mode.buildItemContinued(node, pItem);
            if ((bItem != null) && bItem.hasChild()) {
                return Arrays.asList(bItem.getChildItems());
            }
        }
        return null;
    }

    public static FennecGeneratedMetadata generate(FennecRecombinantMetadata metadata, FennecMode mode, Node node,
            FennecMetadata[] childMds) {
        return new FennecGeneratedMetadata(metadata.fennecService, mode, node, childMds);
    }

    private FennecGeneratedMetadata(FennecServiceImpl fennecService, FennecMode mode, Node node, FennecMetadata[] childMds) {
        super(fennecService, mode, node, childMds);
        this.node = node;
    }

    public static FennecMetadata generate(FennecGeneratedMetadata meta, FennecGeneratedMetadata meta2) {
        FennecMetadata[] childMds = new FennecMetadata[meta.childMetadata.length+meta2.childMetadata.length];
        for(int i=0; i<meta.childMetadata.length; i++)
            childMds[i] = meta.childMetadata[i];
        for(int i=0; i<meta2.childMetadata.length; i++)
            childMds[meta.childMetadata.length+i] = meta2.childMetadata[i];
        return new FennecGeneratedMetadata(meta.fennecService, meta.mode, meta.node, childMds);
    }

}
