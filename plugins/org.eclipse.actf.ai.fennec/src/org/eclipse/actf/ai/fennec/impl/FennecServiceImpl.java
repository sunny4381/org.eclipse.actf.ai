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

import org.eclipse.actf.ai.fennec.IFennecEntry;
import org.eclipse.actf.ai.fennec.IFennecService;
import org.eclipse.actf.ai.fennec.FennecException;
import org.eclipse.actf.ai.fennec.FennecInterruptedException;
import org.eclipse.actf.ai.fennec.treemanager.IAccessKeyList;
import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerException;
import org.eclipse.actf.ai.query.IQuery;
import org.eclipse.actf.ai.query.QueryService;
import org.eclipse.actf.model.dom.dombycom.AnalyzedResult;
import org.eclipse.actf.model.dom.dombycom.IDocumentEx;
import org.eclipse.actf.model.dom.dombycom.IFlashNode;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.eclipse.actf.util.vocab.IProposition;
import org.eclipse.actf.util.vocab.Vocabulary;
import org.w3c.dom.Element;


public class FennecServiceImpl implements IFennecService {
    private FennecMetadata rootMetadata;

    private int status;

    private TreeItemFennec lastItem;

    private IDocumentEx document;

    private Element root;

    private AnalyzedResult analyzedResult;

    Element getDocumentElement() {
        return root;
    }

    private void initDefaultMetadata() {
        FennecMode rootMode = new FennecMode(FennecMode.TYPE_SIMPLE);
        FennecMode nextMode = new FennecMode(FennecMode.TYPE_ATTACH, FennecMode.TRIGGER_ALWAYS, true, false, false);
        // /HTML/FRAMESET/FRAME/HTML/BODY
        //IQuery q = QueryService.createFromXPath("/HTML/BODY|/HTML/FRAMESET//FRAME/HTML/BODY");
        //IQuery q = QueryService.createFromXPath("//HTML/BODY");
        IQuery q = QueryService.createFromXPath("/HTML");
        FennecMetadata[] mds = new FennecMetadata[1];
        mds[0] = FennecRecombinantMetadata.newAttach(this, q, rootMode, nextMode, null);
        rootMetadata = new FennecGroupMetadata(this, null, rootMode, mds);
    }

    public FennecServiceImpl(IFennecEntry entry, IDocumentEx document) throws FennecException {
        this.document = document;
        this.root = document.getDocumentElement();
        try {
            FennecDOMReader reader = new FennecDOMReader(this);
            rootMetadata = reader.parse(entry);
            this.status = UNINIT;
        } catch (Exception e) {
            throw new FennecException("Failed to load Fennec data.", e);
        }
    }

    public FennecServiceImpl(IDocumentEx document) {
        this.document = document;
        this.root = document.getDocumentElement();
        initDefaultMetadata();
    }

    public int getStatus() {
        return status;
    }

    public int analyze() throws FennecException {
        analyzedResult = new AnalyzedResult();
        if (root instanceof INodeEx) {
            cachedVideoControl = null;
            cachedSoundControl = null;
            analyzedResult = ((INodeEx) root).analyze(analyzedResult);
        }
        return ITreeManager.NOACTION;
    }

    private ISoundControl cachedSoundControl;

    private IVideoControl cachedVideoControl;

    private IAccessKeyList cachedAccessKeyList;
    
    private IFlashNode[] cachedFlashTopNodes;

    public int initialize() throws FennecException {
        if (analyzedResult == null) {
            analyze();
        }
        lastItem = rootMetadata.buildRootItem();
        if (lastItem == null) {
            throw new FennecException("Failed to initialize", null);
        }
        status = NORMAL;
        return ITreeManager.MOVED;
    }

    public ITreeItem getLastTreeItem() {
        return lastItem;
    }

    private int update(ITreeItem target, int trigger) throws FennecException {
        TreeItemFennec targetItem = (TreeItemFennec) target;
        if (targetItem.getParent() == null) {
            return initialize();
        } else {
            targetItem = targetItem.expand(trigger);
            if (targetItem == null) {
                return ITreeManager.NOACTION;
            }
        }
        if (targetItem == null) {
            status = UNINIT;
            throw new FennecInterruptedException("Lost my way"); // $ NON-NLS-1
        }
        lastItem = targetItem;
        return ITreeManager.MOVED;
    }

    public int moveUpdate(ITreeItem target) throws FennecException {
        return update(target, FennecMode.TRIGGER_MOVE);
    }

    public int moveUpdate(ITreeItem target, boolean update) throws FennecException {
        if (update) {
            return update(target, FennecMode.TRIGGER_MOVE);
        } else {
            return update(target, FennecMode.TRIGGER_MOVE | FennecMode.TRIGGER_WITHOUTCHANGE);
        }
    }

    public int clickUpdate(ITreeItem target) throws FennecException {
        return update(target, FennecMode.TRIGGER_CLICK) | ITreeManager.CLICKED;
    }

    private int searchForwardChildren(int idx, TreeItemFennec item, IProposition proposition) throws FennecException {
        int st;
        item = item.expandChildItems(FennecMode.TRIGGER_KEEP);
        ITreeItem[] childItems = item.getChildItems();
        for (int i = idx; i < childItems.length; i++) {
            TreeItemFennec cItem = (TreeItemFennec) childItems[i];
            if (proposition.eval(cItem)) {
                return moveUpdate(cItem) | ITreeManager.FOUND;
            }
            st = searchForwardChildren(0, cItem, proposition);
            if ((st & ITreeManager.FOUND) != 0)
                return st;
        }
        return ITreeManager.NOACTION;
    }

    private int searchForwardInternal(int idx, TreeItemFennec item, IProposition proposition) throws FennecException {
        int st = searchForwardChildren(idx, item, proposition);
        if ((st & ITreeManager.FOUND) != 0)
            return st;
        TreeItemFennec pItem = (TreeItemFennec) item.getParent();
        if (pItem == null)
            return ITreeManager.NOACTION;
        st = searchForwardInternal(item.getNth() + 1, pItem, proposition);
        if ((st & ITreeManager.FOUND) != 0)
            return st;

        return ITreeManager.NOACTION;
    }

    public ITreeItem[] getSiblings() throws TreeManagerException {
        ITreeItem parent = lastItem.getParent();
        if (parent == null) {
            ITreeItem[] itas = new ITreeItem[1];
            itas[0] = lastItem;
            return itas;
        }
        return parent.getChildItems();
    }

    public int searchForward(IProposition proposition) throws FennecException {
        return searchForwardInternal(0, lastItem, proposition);
    }

    private int searchBackwardInternal(boolean first, TreeItemFennec item, IProposition proposition) throws FennecException {
        TreeItemFennec pItem = (TreeItemFennec) item.getParent();
        if (pItem == null)
            return ITreeManager.NOACTION;
        int nth = item.getNth() - 1;
        if (nth < 0) {
            if (proposition.eval(pItem)) {
                return moveUpdate(pItem) | ITreeManager.FOUND;
            }
            return searchBackwardInternal(false, pItem, proposition);
        }
        if (first) {
            pItem = pItem.expandChildItems(FennecMode.TRIGGER_KEEP);
        }
        ITreeItem[] siblings = pItem.getChildItems();
        if (nth >= siblings.length) {
            nth = siblings.length - 1;
        }
        item = (TreeItemFennec) siblings[nth];

        while (true) {
            item = item.expandChildItems(FennecMode.TRIGGER_KEEP);
            ITreeItem[] childItems = item.getChildItems();
            if (childItems.length == 0)
                break;
            item = (TreeItemFennec) childItems[childItems.length - 1];
        }
        if (proposition.eval(item)) {
            return moveUpdate(item) | ITreeManager.FOUND;
        }
        return searchBackwardInternal(false, item, proposition);
    }

    public int searchBackward(IProposition predicate) throws FennecException {
        return searchBackwardInternal(true, lastItem, predicate);
    }

    public ISoundControl getSoundControl() {
        if (cachedSoundControl == null) {
            cachedSoundControl = TreeItemSoundControl.newTreeItemSoundControl(analyzedResult);
        }
        return cachedSoundControl;
    }

    public IVideoControl getVideoControl() {
        if (cachedVideoControl == null) {
            cachedVideoControl = TreeItemVideoControl.newTreeItemVideoControl(analyzedResult, this);
        }
        return cachedVideoControl;
    }
    
    public IAccessKeyList getAccessKeyList() {
        if (cachedAccessKeyList == null) {
            cachedAccessKeyList = TreeItemAccessKeyList.newAccessKeyList(analyzedResult);
        }
        return cachedAccessKeyList;
    }

    public IFlashNode[] getFlashTopNodes() {
        if (cachedFlashTopNodes == null) {
            cachedFlashTopNodes = analyzedResult.getFlashTopNodes();
        }
        return cachedFlashTopNodes;
    }

    private void expandWholeTreeInternal(TreeItemFennec item) throws FennecException {
        item = item.expandChildItems(FennecMode.TRIGGER_KEEP);
        ITreeItem[] childItems = item.getChildItems();
        for (int i = 0; i < childItems.length; i++) {
            expandWholeTreeInternal((TreeItemFennec) childItems[i]);
        }
    }

    private int skipToNode(Element e) throws FennecException {
        initialize();
        return searchForward(Vocabulary.nodeLocation(e, false));
    }

    public int skipToAnchor(String target) throws FennecException {
        Element el = document.getTargetElement(target);
        if (el != null)
            return skipToNode(el);
        return ITreeManager.NOACTION;
    }

    public ITreeItem expandWholeTree() throws FennecException {
        initialize();
        if (lastItem instanceof TreeItemFennec) {
            expandWholeTreeInternal((TreeItemFennec) lastItem);
        }
        return lastItem;
    }

}
