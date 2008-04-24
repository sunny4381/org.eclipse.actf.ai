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
import java.util.HashMap;
import java.util.List;

import org.eclipse.actf.ai.fennec.FennecException;
import org.eclipse.actf.ai.fennec.FennecInterruptedException;
import org.eclipse.actf.ai.fennec.autotranslator.AutoTranslator;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


// TODO I'd like to make it package-local.
public class FennecMode {
    static public final int TYPE_SIMPLE = 0;

    static public final int TYPE_ATTACH = 1;

    static public final int TYPE_UNWRAP = 2;

    static public final int TYPE_BUILT = 2;

    static public final int TRIGGER_MOVE = 1 << 0;

    static public final int TRIGGER_CLICK = 1 << 1;

    static public final int TRIGGER_KEEP = 1 << 2;

    static public final int TRIGGER_WITHOUTCHANGE = 1 << 3;

    static public final int TRIGGER_UNWRAP = 1 << 4;

    static public final int TRIGGER_ALWAYS = (TRIGGER_MOVE
                                              | TRIGGER_CLICK
                                              | TRIGGER_KEEP
                                              | TRIGGER_UNWRAP);

    private final int type;

    private final int trigger;

    private final boolean automatic;

    private final boolean changeless;

    private final boolean waitContents;

    private FennecRecombinantMetadata baseMetadata;

    private List<FennecMetadata> belongingMetadata;

    private HashMap<Node, ArrayList<FennecMetadata>> metadataMap;

    void addMetadata(FennecMetadata md) {
        if (false) {
            // A mode manages its belonging metadata in order to manage substate, but
            // currently it is not used...  So now this part is disabled.
            if (belongingMetadata == null) {
                belongingMetadata = new ArrayList<FennecMetadata>();
            }
            belongingMetadata.add(md);
        }
    }

    void setBaseMetadata(FennecRecombinantMetadata md) {
        this.baseMetadata = md;
    }

    boolean changed(TreeItemFennec pItem, int trigger) {
        // TODO:
        // return true;
        if ((trigger & TRIGGER_WITHOUTCHANGE) != 0)
            return false;
        if (changeless)
            return false;
        return true;
    }

    private TreeItemFennec currentTopItem;

    TreeItemFennec buildItem(Node n, TreeItemFennec pItem) {
        currentTopItem = pItem;
        TreeItemFennec item = AutoTranslator.translate(this, pItem, n);
        if (pItem != null)
            pItem.markRefreshedChild();
        return item;
    }

    TreeItemFennec buildItemContinued(Node n, TreeItemFennec item) {
        currentTopItem = null;
        TreeItemFennec newItem = AutoTranslator.translateContinued(this, item, n);
        if (newItem != null)
            newItem.markRefreshedChild();
        return newItem;
    }

    private FennecMetadata[] topMds;

    private FennecMetadata[] initMetadataMap(FennecMetadata[] mds, Node n) {
        if (topMds != null) return topMds;

        metadataMap = new HashMap<Node, ArrayList<FennecMetadata>>();
        ArrayList<FennecMetadata> mdList = new ArrayList<FennecMetadata>();
        for (int i = 0; i < mds.length; i++) {
            if (mds[i].hasTargets()) {
                registMetadata(metadataMap, mds[i], n);
            } else {
                mdList.add(mds[i]);
            }
        }
        topMds = mdList.toArray(new FennecMetadata[mdList.size()]);
        return topMds;
    }

    public TreeItemFennec generateItem(TreeItemFennec pItem, Node n) {
        FennecMetadata[] cmds = null;

        FennecMetadata[] mds = baseMetadata.getChildMetadata();
        if (mds != null) {
            if (currentTopItem == pItem) {
                // This item is top.
                cmds = initMetadataMap(mds, n);
            } else {
                initMetadataMap(mds, n);
                ArrayList<FennecMetadata> aMeta = metadataMap.get(n);
                if (aMeta != null) {
                    cmds = (FennecMetadata[]) aMeta.toArray(new FennecMetadata[aMeta.size()]);
                }
            }
        }
        FennecMetadata md = FennecGeneratedMetadata.generate(baseMetadata, this, n, cmds);

        return TreeItemFennec.newTreeItem(md, pItem, n);
    }

    private void registMetadata(HashMap<Node, ArrayList<FennecMetadata>> metaMap, FennecMetadata meta, Node base) {
        NodeList list = meta.query(base);
        
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            ArrayList<FennecMetadata> aMeta = metaMap.get(node);
            if (aMeta == null) {
                aMeta = new ArrayList<FennecMetadata>();
                metaMap.put(node, aMeta);
            }
            if (meta instanceof FennecBundleMetadata) {
                FennecMetadata[] m = ((FennecBundleMetadata) meta).childMetadata;
                for (int j = 0; j < m.length; j++) {
                    if (m[j].hasTargets()) {
                        registMetadata(metaMap, m[j], node);
                    } else {
                        aMeta.add(m[j]);
                    }
                }
            }
        }
    }

    private List autoAttach(TreeItemFennec pItem, NodeList nl, int len, int trigger) throws FennecException {
        List l = new ArrayList(len);

        for (int i = 0; i < len; i++) {
            Node n = nl.item(i);
            /*
             if (n instanceof CacheableNode) {
             CachableNode cn = (CachableNode) cn;
             if (!cn.isChanged()) {
             } else {
             }
             }
             */
            TreeItemFennec newItem = buildItem(n, pItem);
            if (newItem != null)
                l.add(newItem);
        }
        if (l.size() == 0) {
            if (this.trigger == TRIGGER_CLICK) {
                throw new FennecInterruptedException("Could not attach the expected nodes.");
            }
            return null;
        }
        if (waitContents && (l.size() == 0)) {
            throw new FennecInterruptedException("Contents have not been prepared yet.");
        }
        return l;
    }

    private List manualAttach(TreeItemFennec pItem, NodeList nl, int len, int trigger) throws FennecException {
        List l = new ArrayList(len);

        FennecMetadata[] childMds = baseMetadata.getChildMetadata();
        for (int j = 0; j < len; j++) {
            for (int i = 0; i < childMds.length; i++) {
                FennecMetadata md = childMds[i];
                List l2 = md.buildItems(pItem, nl.item(j), trigger);
                if (l2 != null) {
                    if (l2.size() > 0) {
                        l.addAll(l2);
                    } else if (waitContents) {
                        throw new FennecInterruptedException("Contents have not been prepared yet.");
                    }
                }
            }
        }

        return l;
    }

    private List unwrap(TreeItemFennec pItem, NodeList nl, int len, int trigger) {
        //TODO
        return null;
    }

    // TODO:
    private HashMap cachedResult = new HashMap();

    List expand(TreeItemFennec pItem, Node baseNode, int trigger) throws FennecException {
        if ((trigger != TRIGGER_KEEP) && ((trigger & this.trigger) == 0)) {
            return null;
        }
        Object key;
        if (baseNode != null) {
            key = baseNode;
        } else if (pItem == null) {
            key = null;
        } else {
            key = pItem.getBaseNode();
        }
        if (!changed(pItem, trigger)) {
            List l = (List) cachedResult.get(key);
            if (l != null)
                return l;
        }

        NodeList nl;
        if (baseNode != null) {
            nl = baseMetadata.query(baseNode);
        } else {
            nl = baseMetadata.query(pItem);
        }
        int len = nl.getLength();
        List result = null;
        switch (type) {
        case TYPE_ATTACH:
            if (len == 0) {
                if (this.trigger == TRIGGER_CLICK) {
                    throw new FennecInterruptedException("Could not attach the expected nodes.");
                }
                return null;
            }
            if (automatic) {
                result = autoAttach(pItem, nl, len, trigger);
            } else {
                result = manualAttach(pItem, nl, len, trigger);
            }
            cachedResult.put(key, result);
            return result;
        case TYPE_UNWRAP:
            // TODO;
        }

        return result;
    }

    FennecMode(int type, int trigger, boolean automatic, boolean changeless, boolean waitContents) {
        this.type = type;
        this.trigger = trigger;
        this.automatic = automatic;
        this.changeless = changeless;
        this.waitContents = waitContents;
    }

    FennecMode(int type) {
        this.type = type;
        this.trigger = TRIGGER_ALWAYS;
        this.automatic = false;
        this.changeless = false;
        this.waitContents = false;
    }
}
