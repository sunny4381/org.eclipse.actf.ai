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
import org.w3c.dom.NodeList;


abstract class FennecMetadata {
    protected final FennecServiceImpl fennecService;
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
            baseNode = fennecService.getDocumentElement();
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

    protected final FennecMode mode;

    public abstract String getAltText(ITreeItem item);
    public abstract String getDescription(ITreeItem item);
    public abstract short getHeadingLevel(ITreeItem item);

    List buildItems(TreeItemFennec baseItem, Node baseNode, int trigger) throws FennecException {
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
                TreeItemFennec newItem = TreeItemFennec.newTreeItem(this, baseItem, n);
                if (newItem != null) {
                    result.add(newItem);
                }
            }
            return result;
        } else if (this instanceof FennecBundleMetadata) {
            ArrayList result = new ArrayList(1);
            TreeItemFennec newItem = TreeItemFennec.newTreeItem(this, baseItem, null);
            if (newItem != null) {
                result.add(newItem);
            }
            return result;
        }
        return null;
    }

    TreeItemFennec buildRootItem() throws FennecException {
        List items = buildItems(null, null, FennecMode.TRIGGER_MOVE);
        if ((items == null) || (items.size() == 0)) {
            return null;
        }
        if (items.size() == 1) {
            TreeItemFennec root = (TreeItemFennec) items.get(0);
            return root.expand(FennecMode.TRIGGER_MOVE);
        } else {
            TreeItemFennec root = TreeItemFennec.newTreeItem(this, null, fennecService.getDocumentElement());
            if (root == null) return null;
            root.setChildItems(items);
            return root;
        }
    }

    abstract List expand(TreeItemFennec pItem, int trigger) throws FennecException;

    protected FennecMetadata(FennecServiceImpl fennecService,
                           IQuery q, FennecMode mode) {
        this.fennecService = fennecService;
        this.query = q;
        if (q != null) {
            this.hasTarget = q.hasTarget();
        } else {
            this.hasTarget = false;
        }
        this.mode = mode;
        this.mode.addMetadata(this);
    }

    protected FennecMetadata(FennecServiceImpl fennecService,
                           FennecMode mode,
                           Node node) {
        this.fennecService = fennecService;
        this.query = null;
        this.hasTarget = false;
        this.mode = mode;
        this.mode.addMetadata(this);
    }

}

