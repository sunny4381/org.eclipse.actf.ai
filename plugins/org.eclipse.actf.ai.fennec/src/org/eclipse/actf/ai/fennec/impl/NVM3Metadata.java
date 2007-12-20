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
import org.w3c.dom.NodeList;


abstract class NVM3Metadata {
    protected final NVM3ServiceImpl nvm3Service;
    private final IQuery query;

    private final boolean hasTarget;
    public boolean hasTargets() {
        return hasTarget;
    }

    private static class EmptyNodeList implements NodeList {
        public Node item(int index) {
            return null;
        }

        public int getLength() {
            return 0;
        }
    }
    private static final NodeList emptyNodeList = new EmptyNodeList();

    NodeList query(IQuery q, Node baseNode) {
        if ((q == null) || (!q.hasTarget())) return emptyNodeList;
        return q.query(baseNode);
    }

    NodeList query(IQuery q, ITreeItem baseItem) {
        if ((q == null) || (!q.hasTarget())) return emptyNodeList;
        Node baseNode;
        for (baseNode = null;
            (baseItem != null) && (baseNode == null);
             baseItem = baseItem.getParent()) {
            baseNode = (Node) baseItem.getBaseNode();
        }
        if (baseNode == null) {
            baseNode = nvm3Service.getDocumentElement();
        }
        if (baseNode == null) return emptyNodeList;
        return q.query(baseNode);
    }

    NodeList query(Node baseNode) {
        return query(this.query, baseNode);
    }

    NodeList query(ITreeItem baseItem) {
        return query(this.query, baseItem);
    }

    protected final NVM3Mode mode;

    public abstract String getAltText(ITreeItem item);
    public abstract String getDescription(ITreeItem item);
    public abstract short getHeadingLevel(ITreeItem item);

    List buildItems(TreeItemNVM3 baseItem, Node baseNode, int trigger) throws NVM3Exception {
        if (hasTargets()) {
            NodeList nl;
            if (baseNode != null) {
                nl = query(baseNode);
            } else {
                nl = query(baseItem);
            }
            int len = nl.getLength(); 
            ArrayList result = new ArrayList(len);
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                TreeItemNVM3 newItem = TreeItemNVM3.newTreeItem(this, baseItem, n);
                if (newItem != null) {
                    result.add(newItem);
                }
            }
            return result;
        } else if (this instanceof NVM3BundleMetadata) {
            ArrayList result = new ArrayList(1);
            TreeItemNVM3 newItem = TreeItemNVM3.newTreeItem(this, baseItem, null);
            if (newItem != null) {
                result.add(newItem);
            }
            return result;
        }
        return null;
    }

    TreeItemNVM3 buildRootItem() throws NVM3Exception {
        List items = buildItems(null, null, NVM3Mode.TRIGGER_MOVE);
        if ((items == null) || (items.size() == 0)) {
            return null;
        }
        if (items.size() == 1) {
            TreeItemNVM3 root = (TreeItemNVM3) items.get(0);
            return root.expand(NVM3Mode.TRIGGER_MOVE);
        } else {
            TreeItemNVM3 root = TreeItemNVM3.newTreeItem(this, null, nvm3Service.getDocumentElement());
            if (root == null) return null;
            root.setChildItems(items);
            return root;
        }
    }

    abstract List expand(TreeItemNVM3 pItem, int trigger) throws NVM3Exception;

    protected NVM3Metadata(NVM3ServiceImpl nvm3Service,
                           IQuery q, NVM3Mode mode) {
        this.nvm3Service = nvm3Service;
        this.query = q;
        if (q != null) {
            this.hasTarget = q.hasTarget();
        } else {
            this.hasTarget = false;
        }
        this.mode = mode;
        this.mode.addMetadata(this);
    }

    protected NVM3Metadata(NVM3ServiceImpl nvm3Service,
                           NVM3Mode mode,
                           Node node) {
        this.nvm3Service = nvm3Service;
        this.query = null;
        this.hasTarget = false;
        this.mode = mode;
        this.mode.addMetadata(this);
    }

}

