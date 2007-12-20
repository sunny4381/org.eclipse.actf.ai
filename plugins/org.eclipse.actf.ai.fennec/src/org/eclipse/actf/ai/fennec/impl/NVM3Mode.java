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

import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.fennec.NVM3InterruptedException;
import org.eclipse.actf.ai.fennec.autotranslator.AutoTranslator;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


// TODO I'd like to make it package-local.
public class NVM3Mode {
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

    private NVM3RecombinantMetadata baseMetadata;

    private List<NVM3Metadata> belongingMetadata;

    private HashMap<Node, ArrayList<NVM3Metadata>> metadataMap;

    void addMetadata(NVM3Metadata md) {
        if (false) {
            // A mode manages its belonging metadata in order to manage substate, but
            // currently it is not used...  So now this part is disabled.
            if (belongingMetadata == null) {
                belongingMetadata = new ArrayList<NVM3Metadata>();
            }
            belongingMetadata.add(md);
        }
    }

    void setBaseMetadata(NVM3RecombinantMetadata md) {
        this.baseMetadata = md;
    }

    boolean changed(TreeItemNVM3 pItem, int trigger) {
        // TODO:
        // return true;
        if ((trigger & TRIGGER_WITHOUTCHANGE) != 0)
            return false;
        if (changeless)
            return false;
        return true;
    }

    private TreeItemNVM3 currentTopItem;

    TreeItemNVM3 buildItem(Node n, TreeItemNVM3 pItem) {
        currentTopItem = pItem;
        TreeItemNVM3 item = AutoTranslator.translate(this, pItem, n);
        if (pItem != null)
            pItem.markRefreshedChild();
        return item;
    }

    TreeItemNVM3 buildItemContinued(Node n, TreeItemNVM3 item) {
        currentTopItem = null;
        TreeItemNVM3 newItem = AutoTranslator.translateContinued(this, item, n);
        if (newItem != null)
            newItem.markRefreshedChild();
        return newItem;
    }

    private NVM3Metadata[] topMds;

    private NVM3Metadata[] initMetadataMap(NVM3Metadata[] mds, Node n) {
        if (topMds != null) return topMds;

        metadataMap = new HashMap<Node, ArrayList<NVM3Metadata>>();
        ArrayList<NVM3Metadata> mdList = new ArrayList<NVM3Metadata>();
        for (int i = 0; i < mds.length; i++) {
            if (mds[i].hasTargets()) {
                registMetadata(metadataMap, mds[i], n);
            } else {
                mdList.add(mds[i]);
            }
        }
        topMds = mdList.toArray(new NVM3Metadata[mdList.size()]);
        return topMds;
    }

    public TreeItemNVM3 generateItem(TreeItemNVM3 pItem, Node n) {
        NVM3Metadata[] cmds = null;

        NVM3Metadata[] mds = baseMetadata.getChildMetadata();
        if (mds != null) {
            if (currentTopItem == pItem) {
                // This item is top.
                cmds = initMetadataMap(mds, n);
            } else {
                initMetadataMap(mds, n);
                ArrayList<NVM3Metadata> aMeta = metadataMap.get(n);
                if (aMeta != null) {
                    cmds = (NVM3Metadata[]) aMeta.toArray(new NVM3Metadata[aMeta.size()]);
                }
            }
        }
        NVM3Metadata md = NVM3GeneratedMetadata.generate(baseMetadata, this, n, cmds);

        return TreeItemNVM3.newTreeItem(md, pItem, n);
    }

    private void registMetadata(HashMap<Node, ArrayList<NVM3Metadata>> metaMap, NVM3Metadata meta, Node base) {
        NodeList list = meta.query(base);
        
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            ArrayList<NVM3Metadata> aMeta = metaMap.get(node);
            if (aMeta == null) {
                aMeta = new ArrayList<NVM3Metadata>();
                metaMap.put(node, aMeta);
            }
            if (meta instanceof NVM3BundleMetadata) {
                NVM3Metadata[] m = ((NVM3BundleMetadata) meta).childMetadata;
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

    private List autoAttach(TreeItemNVM3 pItem, NodeList nl, int len, int trigger) throws NVM3Exception {
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
            TreeItemNVM3 newItem = buildItem(n, pItem);
            if (newItem != null)
                l.add(newItem);
        }
        if (l.size() == 0) {
            if (this.trigger == TRIGGER_CLICK) {
                throw new NVM3InterruptedException("Could not attach the expected nodes.");
            }
            return null;
        }
        if (waitContents && (l.size() == 0)) {
            throw new NVM3InterruptedException("Contents have not been prepared yet.");
        }
        return l;
    }

    private List manualAttach(TreeItemNVM3 pItem, NodeList nl, int len, int trigger) throws NVM3Exception {
        List l = new ArrayList(len);

        NVM3Metadata[] childMds = baseMetadata.getChildMetadata();
        for (int j = 0; j < len; j++) {
            for (int i = 0; i < childMds.length; i++) {
                NVM3Metadata md = childMds[i];
                List l2 = md.buildItems(pItem, nl.item(j), trigger);
                if (l2 != null) {
                    if (l2.size() > 0) {
                        l.addAll(l2);
                    } else if (waitContents) {
                        throw new NVM3InterruptedException("Contents have not been prepared yet.");
                    }
                }
            }
        }

        return l;
    }

    private List unwrap(TreeItemNVM3 pItem, NodeList nl, int len, int trigger) {
        //TODO
        return null;
    }

    // TODO:
    private HashMap cachedResult = new HashMap();

    List expand(TreeItemNVM3 pItem, Node baseNode, int trigger) throws NVM3Exception {
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
                    throw new NVM3InterruptedException("Could not attach the expected nodes.");
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

    NVM3Mode(int type, int trigger, boolean automatic, boolean changeless, boolean waitContents) {
        this.type = type;
        this.trigger = trigger;
        this.automatic = automatic;
        this.changeless = changeless;
        this.waitContents = waitContents;
    }

    NVM3Mode(int type) {
        this.type = type;
        this.trigger = TRIGGER_ALWAYS;
        this.automatic = false;
        this.changeless = false;
        this.waitContents = false;
    }
}
