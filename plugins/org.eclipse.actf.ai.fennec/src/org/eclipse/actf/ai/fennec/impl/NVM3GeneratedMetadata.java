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

import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.w3c.dom.Node;


class NVM3GeneratedMetadata extends NVM3BundleMetadata {
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
    List expand(TreeItemNVM3 pItem, int trigger) throws NVM3Exception {
        ITreeItem[] childItems = pItem.getChildItems();
        if (!mode.changed(pItem, trigger) && pItem.hasAlreadyChildRefreshed()) {
            return Arrays.asList(childItems);
        } else {
            TreeItemNVM3 bItem = mode.buildItemContinued(node, pItem);
            if ((bItem != null) && bItem.hasChild()) {
                return Arrays.asList(bItem.getChildItems());
            }
        }
        return null;
    }

    public static NVM3GeneratedMetadata generate(NVM3RecombinantMetadata metadata, NVM3Mode mode, Node node,
            NVM3Metadata[] childMds) {
        return new NVM3GeneratedMetadata(metadata.nvm3Service, mode, node, childMds);
    }

    private NVM3GeneratedMetadata(NVM3ServiceImpl nvm3Service, NVM3Mode mode, Node node, NVM3Metadata[] childMds) {
        super(nvm3Service, mode, node, childMds);
        this.node = node;
    }

    public static NVM3Metadata generate(NVM3GeneratedMetadata meta, NVM3GeneratedMetadata meta2) {
        NVM3Metadata[] childMds = new NVM3Metadata[meta.childMetadata.length+meta2.childMetadata.length];
        for(int i=0; i<meta.childMetadata.length; i++)
            childMds[i] = meta.childMetadata[i];
        for(int i=0; i<meta2.childMetadata.length; i++)
            childMds[meta.childMetadata.length+i] = meta2.childMetadata[i];
        return new NVM3GeneratedMetadata(meta.nvm3Service, meta.mode, meta.node, childMds);
    }

}
