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

import java.util.List;

import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerException;
import org.eclipse.actf.ai.query.QueryService;
import org.eclipse.actf.model.dom.dombycom.IElementEx;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.eclipse.actf.model.dom.dombycom.ISelectElement;
import org.eclipse.actf.model.dom.dombycom.IElementEx.Position;
import org.eclipse.actf.util.vocab.AbstractTerms;
import org.eclipse.actf.util.vocab.IEvalTarget;
import org.eclipse.actf.util.vocab.Vocabulary;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


// TODO I'd like to make it package-local.
public class TreeItemNVM3 implements ITreeItem {
    static private final TreeItemNVM3[] emptyChild = new TreeItemNVM3[0];

    private Node baseNode;

    private ITreeItem parent;

    private ITreeItem[] childItems;

    private NVM3Metadata metadata;

    private boolean hasAlreadyChildRefreshed;

    private int nth;

    private TreeItemTerms terms;

    // for terms
    int distance = 0;
    
    public static TreeItemNVM3 newTreeItem(NVM3Metadata metadata, ITreeItem parent, Node baseNode) {
        if (baseNode == null) {
            // TODO
            return new TreeItemNVM3(metadata, parent, null, new TreeItemTerms(null));
        }
        if (!(baseNode instanceof IEvalTarget)) return null;
        IEvalTarget evalTarget = (IEvalTarget) baseNode;
        return new TreeItemNVM3(metadata, parent, baseNode, new TreeItemTerms(evalTarget)); 
    }

    protected TreeItemNVM3(NVM3Metadata metadata,
                           ITreeItem parent, Node baseNode,
                           TreeItemTerms terms) {
        this.terms = terms;
        this.metadata = metadata;
        this.parent = parent;
        this.baseNode = baseNode;
        this.childItems = emptyChild;
        this.nth = 0;
    }
    

    public AbstractTerms getTerms() {
        return terms;
    }

    public ITreeItem getParent() {
        return parent;
    }

    public int getNth() {
        return nth;
    }

    private void setAsRoot() {
        this.parent = null;
    }

    private void setParent() {
        for (int i = 0; i < childItems.length; i++) {
            TreeItemNVM3 c = (TreeItemNVM3) childItems[i];
            c.parent = this;
            c.nth = i;
        }
    }

    public void setChildItems(List items) {
        if (items == null) {
            this.childItems = emptyChild;
        } else {
            this.childItems = (ITreeItem[]) items.toArray(emptyChild);
        }
        setParent();
    }

    public void setChildItems(ITreeItem[] items) {
        if (items == null) {
            this.childItems = emptyChild;
        } else {
            this.childItems = items;
        }
        setParent();
    }

    public void appendChildItems(ITreeItem[] items) {
        if (items == null)
            return;
        ITreeItem[] newItems = new ITreeItem[childItems.length + items.length];
        int i, j;
        for (i = 0; i < childItems.length; i++) {
            newItems[i] = childItems[i];
        }
        for (j = 0; j < items.length; i++, j++) {
            TreeItemNVM3 c = (TreeItemNVM3) items[j];
            c.parent = this;
            newItems[i] = c;
        }
        this.childItems = newItems;
    }

    public boolean hasChild() {
        return (childItems.length > 0);
    }

    public void forceParent(ITreeItem parent) {
        this.parent = parent;
    }

    void markRefreshedChild() {
        hasAlreadyChildRefreshed = true;
    }

    boolean hasAlreadyChildRefreshed() {
        return hasAlreadyChildRefreshed;
    }

    TreeItemNVM3 expandChildItems(int trigger) throws NVM3Exception {
        List newChildItems = metadata.expand(this, trigger);
        setChildItems(newChildItems);
        return this;
    }

    private Node getNearestNodeInternal(int idx) {
        if (baseNode != null) return baseNode;
        for (; idx < childItems.length; idx++) {
            TreeItemNVM3 item = (TreeItemNVM3) childItems[idx];
            return item.getNearestNodeInternal(0);
        }
        if (parent == null) return null;
        return ((TreeItemNVM3) parent).getNearestNodeInternal(getNth());
    }

    Node getNearestNode() {
        return getNearestNodeInternal(0);
    }

    private TreeItemNVM3 autoUnwrap(int trigger) throws NVM3Exception {
        TreeItemNVM3 item = this;

        for (;;) {
            TreeItemNVM3 parent = (TreeItemNVM3) item.getParent();
            if (parent == null) {
                item.setChildItems(emptyChild);
                return item;
            }
            int idx = item.getNth();
            List newSiblings = parent.metadata.expand(parent, (NVM3Mode.TRIGGER_KEEP
                                                               | NVM3Mode.TRIGGER_UNWRAP));
            if (newSiblings != null) {
                int newSize = newSiblings.size();
                if (newSize > 0) {
                    if (idx >= newSize) idx = newSize - 1;
                    item = (TreeItemNVM3) newSiblings.get(idx);

                    item = item.expandChildItems(trigger);
                    // success. Set parent's childItems.
                    parent.setChildItems(newSiblings);
                    return item;
                }
            }
            item = parent;
        }
    }

    TreeItemNVM3 expand(int trigger) throws NVM3Exception {
        TreeItemNVM3 parent = (TreeItemNVM3) getParent();
        if (parent != null) {
            int idx = getNth();
            List newSiblings = parent.metadata.expand(parent, NVM3Mode.TRIGGER_KEEP);
            if (newSiblings == null) {
                return parent.autoUnwrap(trigger);
            }
            int newSize = newSiblings.size();
            if (newSize == 0) {
                return parent.autoUnwrap(trigger);
            }
            if (idx >= newSize) {
                idx = newSize - 1;
            }
            TreeItemNVM3 newTarget = (TreeItemNVM3) newSiblings.get(idx);
            newTarget = newTarget.expandChildItems(trigger);
            // success. Set parent's childItems.
            parent.setChildItems(newSiblings);
            return newTarget;
        } else {
            List newChildItems = metadata.expand(this, trigger);
            setChildItems(newChildItems);
            this.setAsRoot();
            return this;
        }
    }

    public ITreeItem[] getChildItems() {
        return childItems;
    }

    public String getUIString() {
        if (metadata != null) {
            String r = metadata.getAltText(this);
            if (r == null)
                return "";
            if (r.length() > 0)
                return r;
        }
        if (baseNode instanceof INodeEx) {
            INodeEx node2 = (INodeEx) baseNode;
            return node2.extractString();
        }
        return "";
    }

    public String getDescription() {
        if (metadata != null) {
            String r = metadata.getDescription(this);
            if (r == null)
                return "";
            if (r.length() > 0)
                return r;
        }
        return "";
    }

    public String getNodeString() {
        if (baseNode != null) {
            return baseNode.getNodeName();
        }
        return "No Node";
    }

    public short getHeadingLevel() {
        if (metadata != null)
            return metadata.getHeadingLevel(this);
        else
            return 0;
    }

    public String getLinkURI() {
        if (baseNode instanceof INodeEx) {
            INodeEx node2 = (INodeEx) baseNode;
            return node2.getLinkURI();
        }
        return null;
    }

    public Object getBaseNode() {
        return baseNode;
    }
    
    public NVM3Metadata getMetadata() {
        return metadata;
    }

    public int doClick() throws TreeManagerException {
        if (baseNode instanceof INodeEx) {
            ((INodeEx) baseNode).doClick();
            return ITreeManager.CLICKED;
        }
        return ITreeManager.NOACTION;
    }

    public int stay() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int highlight() throws TreeManagerException {
        if (baseNode instanceof INodeEx) {
            ((INodeEx) baseNode).highlight();
        }
        return ITreeManager.NOACTION;
    }

    public int unhighlight() throws TreeManagerException {
        if (baseNode instanceof INodeEx) {
            ((INodeEx) baseNode).unhighlight();
        }
        return ITreeManager.NOACTION;
    }

    public boolean setFocus() {
        if (baseNode instanceof INodeEx) {
            ((INodeEx) baseNode).setFocus();
        }
        return false;
    }

    // !FN!
    public boolean isInputable() {
        if (baseNode instanceof IEvalTarget) {
            return Vocabulary.isInputable().eval((IEvalTarget) baseNode);
        }
        return false;
    }

    // !FN!
    public boolean isClickable() {
        if (baseNode instanceof IEvalTarget) {
            return Vocabulary.isClickable().eval((IEvalTarget) baseNode);
        }
        return false;
    }

    // !FN!
    public boolean isImage() {
        if (baseNode instanceof IEvalTarget) {
            return Vocabulary.isImage().eval((IEvalTarget) baseNode);
        }
        return false;
    }

    // !FN!
    public String[] getStillPictureData() {
        if (!(baseNode instanceof INodeEx)) {
            return new String[3];
        }
        return ((INodeEx) baseNode).getStillPictureData();
    }

    public int setText(String text) throws TreeManagerException {
        if (baseNode instanceof INodeEx) {
            ((INodeEx) baseNode).setText(text);
        }
        return ITreeManager.NOACTION;
    }

    public String getText() throws TreeManagerException {
        if (baseNode instanceof INodeEx) {
            return ((INodeEx) baseNode).getText();
        }
        return "";
    }

    public void addMetadata(TreeItemNVM3 item) {
        if (!(this.metadata instanceof NVM3GeneratedMetadata))
            return;
        if (!(item.metadata instanceof NVM3GeneratedMetadata))
            return;

        NVM3GeneratedMetadata meta = (NVM3GeneratedMetadata) this.metadata;
        NVM3GeneratedMetadata meta2 = (NVM3GeneratedMetadata) item.metadata;

        this.metadata = NVM3GeneratedMetadata.generate(meta, meta2);
    }

    
    private Position radioPosition;

    private Position listPosition;

    public int getRadioIndex() {
        if (baseNode instanceof IElementEx) {
            if (radioPosition == null)
                radioPosition = ((IElementEx) baseNode).getRadioPosition();
            if (radioPosition == null)
                return 0;
            return radioPosition.index;
        }
        return 0;
    }

    public int getRadioTotal() {
        if (baseNode instanceof IElementEx) {
            if (radioPosition == null)
                radioPosition = ((IElementEx) baseNode).getRadioPosition();
            if (radioPosition == null)
                return 0;
            return radioPosition.total;
        }
        return 0;
    }

    public int getListIndex() {
        Node current = baseNode;
        do {
            if (current instanceof IElementEx) {
                if (listPosition == null)
                    listPosition = ((IElementEx) current).getListPosition();
                if (listPosition == null)
                    return 0;
                return listPosition.index;
            }
            current = current.getParentNode();
        } while (current != null);
        return 0;
    }

    public int getListTotal() {
        Node current = baseNode;
        do {
            if (current instanceof IElementEx) {
                if (listPosition == null)
                    listPosition = ((IElementEx) current).getListPosition();
                if (listPosition == null)
                    return 0;
                return listPosition.total;
            }
            current = current.getParentNode();
        } while (current != null);
        return 0;
    }

    public String getFormLabel() {
        if (!(baseNode instanceof IElementEx))
            return "";
        Element label = ((IElementEx) baseNode).getFormLabel();
        if (label == null || !(label instanceof INodeEx))
            return "";
        NodeList nl = label.getChildNodes();
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof INodeEx)
                ret.append(((INodeEx) node).extractString());
        }
        return ret.toString();
    }

    public void setSelectedIndices(int[] indices) {
        if (baseNode instanceof ISelectElement) {
            ((ISelectElement) baseNode).setSelectedIndices(indices);
        }
    }

    public int[] getSelectedIndices() {
        if (baseNode instanceof ISelectElement) {
            return ((ISelectElement) baseNode).getSelectedIndices();
        }
        return null;
    }

    public int getOptionsCount() {
        if (baseNode instanceof ISelectElement) {
            return ((ISelectElement) baseNode).getOptionsCount();
        }
        return 0;
    }

    public String getOptionTextAt(int index) {
        if (baseNode instanceof ISelectElement) {
            return ((ISelectElement) baseNode).getOptionTextAt(index);
        }
        return "";
    }

    // For user annotation.
    public Node serializeQuery(Node parent) {
        if (baseNode == null)
            return null;
        return QueryService.serializeQuery(baseNode, parent);
    }
    
    public char getAccessKey() {
        if (baseNode instanceof INodeEx) {
            return ((INodeEx) baseNode).getAccessKey();
        }
        return 0;
    }
}
