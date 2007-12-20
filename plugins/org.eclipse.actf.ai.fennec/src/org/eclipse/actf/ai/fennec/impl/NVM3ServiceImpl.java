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

import org.eclipse.actf.ai.fennec.INVM3Entry;
import org.eclipse.actf.ai.fennec.INVM3Service;
import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.fennec.NVM3InterruptedException;
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


public class NVM3ServiceImpl implements INVM3Service {
    private NVM3Metadata rootMetadata;

    private int status;

    private TreeItemNVM3 lastItem;

    private IDocumentEx document;

    private Element root;

    private AnalyzedResult analyzedResult;

    Element getDocumentElement() {
        return root;
    }

    private void initDefaultMetadata() {
        NVM3Mode rootMode = new NVM3Mode(NVM3Mode.TYPE_SIMPLE);
        NVM3Mode nextMode = new NVM3Mode(NVM3Mode.TYPE_ATTACH, NVM3Mode.TRIGGER_ALWAYS, true, false, false);
        // /HTML/FRAMESET/FRAME/HTML/BODY
        //IQuery q = QueryService.createFromXPath("/HTML/BODY|/HTML/FRAMESET//FRAME/HTML/BODY");
        //IQuery q = QueryService.createFromXPath("//HTML/BODY");
        IQuery q = QueryService.createFromXPath("/HTML");
        NVM3Metadata[] mds = new NVM3Metadata[1];
        mds[0] = NVM3RecombinantMetadata.newAttach(this, q, rootMode, nextMode, null);
        rootMetadata = new NVM3GroupMetadata(this, null, rootMode, mds);
    }

    public NVM3ServiceImpl(INVM3Entry entry, IDocumentEx document) throws NVM3Exception {
        this.document = document;
        this.root = document.getDocumentElement();
        try {
            NVM3DOMReader reader = new NVM3DOMReader(this);
            rootMetadata = reader.parse(entry);
            this.status = UNINIT;
        } catch (Exception e) {
            throw new NVM3Exception("Failed to load NVM3 data.", e);
        }
    }

    public NVM3ServiceImpl(IDocumentEx document) {
        this.document = document;
        this.root = document.getDocumentElement();
        initDefaultMetadata();
    }

    public int getStatus() {
        return status;
    }

    public int analyze() throws NVM3Exception {
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

    public int initialize() throws NVM3Exception {
        if (analyzedResult == null) {
            analyze();
        }
        lastItem = rootMetadata.buildRootItem();
        if (lastItem == null) {
            throw new NVM3Exception("Failed to initialize", null);
        }
        status = NORMAL;
        return ITreeManager.MOVED;
    }

    public ITreeItem getLastTreeItem() {
        return lastItem;
    }

    private int update(ITreeItem target, int trigger) throws NVM3Exception {
        TreeItemNVM3 targetItem = (TreeItemNVM3) target;
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
            throw new NVM3InterruptedException("Lost my way"); // $ NON-NLS-1
        }
        lastItem = targetItem;
        return ITreeManager.MOVED;
    }

    public int moveUpdate(ITreeItem target) throws NVM3Exception {
        return update(target, NVM3Mode.TRIGGER_MOVE);
    }

    public int moveUpdate(ITreeItem target, boolean update) throws NVM3Exception {
        if (update) {
            return update(target, NVM3Mode.TRIGGER_MOVE);
        } else {
            return update(target, NVM3Mode.TRIGGER_MOVE | NVM3Mode.TRIGGER_WITHOUTCHANGE);
        }
    }

    public int clickUpdate(ITreeItem target) throws NVM3Exception {
        return update(target, NVM3Mode.TRIGGER_CLICK) | ITreeManager.CLICKED;
    }

    private int searchForwardChildren(int idx, TreeItemNVM3 item, IProposition proposition) throws NVM3Exception {
        int st;
        item = item.expandChildItems(NVM3Mode.TRIGGER_KEEP);
        ITreeItem[] childItems = item.getChildItems();
        for (int i = idx; i < childItems.length; i++) {
            TreeItemNVM3 cItem = (TreeItemNVM3) childItems[i];
            if (proposition.eval(cItem)) {
                return moveUpdate(cItem) | ITreeManager.FOUND;
            }
            st = searchForwardChildren(0, cItem, proposition);
            if ((st & ITreeManager.FOUND) != 0)
                return st;
        }
        return ITreeManager.NOACTION;
    }

    private int searchForwardInternal(int idx, TreeItemNVM3 item, IProposition proposition) throws NVM3Exception {
        int st = searchForwardChildren(idx, item, proposition);
        if ((st & ITreeManager.FOUND) != 0)
            return st;
        TreeItemNVM3 pItem = (TreeItemNVM3) item.getParent();
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

    public int searchForward(IProposition proposition) throws NVM3Exception {
        return searchForwardInternal(0, lastItem, proposition);
    }

    private int searchBackwardInternal(boolean first, TreeItemNVM3 item, IProposition proposition) throws NVM3Exception {
        TreeItemNVM3 pItem = (TreeItemNVM3) item.getParent();
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
            pItem = pItem.expandChildItems(NVM3Mode.TRIGGER_KEEP);
        }
        ITreeItem[] siblings = pItem.getChildItems();
        if (nth >= siblings.length) {
            nth = siblings.length - 1;
        }
        item = (TreeItemNVM3) siblings[nth];

        while (true) {
            item = item.expandChildItems(NVM3Mode.TRIGGER_KEEP);
            ITreeItem[] childItems = item.getChildItems();
            if (childItems.length == 0)
                break;
            item = (TreeItemNVM3) childItems[childItems.length - 1];
        }
        if (proposition.eval(item)) {
            return moveUpdate(item) | ITreeManager.FOUND;
        }
        return searchBackwardInternal(false, item, proposition);
    }

    public int searchBackward(IProposition predicate) throws NVM3Exception {
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

    private void expandWholeTreeInternal(TreeItemNVM3 item) throws NVM3Exception {
        item = item.expandChildItems(NVM3Mode.TRIGGER_KEEP);
        ITreeItem[] childItems = item.getChildItems();
        for (int i = 0; i < childItems.length; i++) {
            expandWholeTreeInternal((TreeItemNVM3) childItems[i]);
        }
    }

    private int skipToNode(Element e) throws NVM3Exception {
        initialize();
        return searchForward(Vocabulary.nodeLocation(e, false));
    }

    public int skipToAnchor(String target) throws NVM3Exception {
        Element el = document.getTargetElement(target);
        if (el != null)
            return skipToNode(el);
        return ITreeManager.NOACTION;
    }

    public ITreeItem expandWholeTree() throws NVM3Exception {
        initialize();
        if (lastItem instanceof TreeItemNVM3) {
            expandWholeTreeInternal((TreeItemNVM3) lastItem);
        }
        return lastItem;
    }

}
