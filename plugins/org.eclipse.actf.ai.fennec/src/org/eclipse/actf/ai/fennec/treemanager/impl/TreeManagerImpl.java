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
package org.eclipse.actf.ai.fennec.treemanager.impl;

import java.util.ArrayList;

import org.eclipse.actf.ai.fennec.INVM3Service;
import org.eclipse.actf.ai.fennec.NVM3Exception;
import org.eclipse.actf.ai.fennec.NVM3InterruptedException;
import org.eclipse.actf.ai.fennec.treemanager.IAccessKeyList;
import org.eclipse.actf.ai.fennec.treemanager.ILocation;
import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerException;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerInterruptedException;
import org.eclipse.actf.model.dom.dombycom.IFlashNode;
import org.eclipse.actf.util.vocab.IProposition;
import org.eclipse.actf.util.vocab.Vocabulary;


/**
 * The main plugin class to be used in the desktop.
 */
public class TreeManagerImpl implements ITreeManager {
    private INVM3Service nvm3Service;

    public TreeManagerImpl(INVM3Service nvm3Service) {
        this.nvm3Service = nvm3Service;
    }

    public int getLevel() throws TreeManagerException {
        int i = 0;
        ITreeItem ita = getActiveItem();
        if (ita == null)
            return 0;
        while (true) {
            ita = ita.getParent();
            if (ita == null)
                break;
            i++;
        }
        return i;
    }

    public ITreeItem getActiveItem() throws TreeManagerException {
        return nvm3Service.getLastTreeItem();
    }

    public ITreeItem[] getSiblings() throws TreeManagerException {
        ITreeItem item = getActiveItem();
        if (item == null)
            return null;
        ITreeItem parent = item.getParent();
        if (parent == null) {
            ITreeItem[] itas = new ITreeItem[1];
            itas[0] = getActiveItem();
            return itas;
        }
        return parent.getChildItems();
    }

    private void initNVM3Service() throws TreeManagerException {
        if (nvm3Service.getStatus() == INVM3Service.NORMAL)
            return;
        try {
            nvm3Service.initialize();
        } catch (NVM3InterruptedException e) {
            throw new TreeManagerInterruptedException(ITreeManager.UNDONE, e.getMessage(), e);
        } catch (NVM3Exception e) {
            throw new TreeManagerException(ITreeManager.ERROR, e.getMessage(), e);
        }
    }

    public int initialize() throws TreeManagerException {
        initNVM3Service();
        return ITreeManager.NOACTION;
    }

    private int moveUpdate(ITreeItem target, boolean update) throws TreeManagerException {
        try {
            return nvm3Service.moveUpdate(target, update);
        } catch (NVM3InterruptedException e) {
            throw new TreeManagerInterruptedException(ITreeManager.UNDONE, "Failed to update by move.", e);
        } catch (NVM3Exception e) {
            throw new TreeManagerException(ITreeManager.ERROR, "Failed to update by move.", e);
        }
    }

    private int clickUpdate(ITreeItem target) throws TreeManagerException {
        try {
            return nvm3Service.clickUpdate(target);
        } catch (NVM3InterruptedException e) {
            throw new TreeManagerInterruptedException(ITreeManager.UNDONE, "Failed to update by click.", e);
        } catch (NVM3Exception e) {
            throw new TreeManagerException(ITreeManager.ERROR, "Failed to update by click.", e);
        }
    }

    private int gotoRoot() throws TreeManagerException {
        ITreeItem parent = getActiveItem().getParent();
        if (parent == null)
            return ITreeManager.NOACTION;
        while (gotoParent() != ITreeManager.NOACTION)
            ;
        return ITreeManager.MOVED | ITreeManager.LEVEL_CHANGED;
    }

    public int stay() throws TreeManagerException {
        initNVM3Service();
        return getActiveItem().stay();
    }

    public int click(boolean doClick) throws TreeManagerException {
        initNVM3Service();
        ITreeItem item = getActiveItem();
        if (doClick)
            item.doClick();
        return clickUpdate(item);
    }

    public int gotoParent() throws TreeManagerException {
        initNVM3Service();
        ITreeItem parent = getActiveItem().getParent();
        if (parent != null) {
            int st = moveUpdate(parent, true);
            if ((st & ITreeManager.MOVED) != 0)
                st |= ITreeManager.LEVEL_CHANGED;
            return st;
        }
        return ITreeManager.NOACTION;
    }

    public int gotoFirstChild() throws TreeManagerException {
        initNVM3Service();
        ITreeItem active = getActiveItem();
        ITreeItem[] childItems = active.getChildItems();
        if (childItems.length == 0)
            return ITreeManager.NOACTION;
        int st = moveUpdate(childItems[0], true);
        if ((st & ITreeManager.MOVED) != 0)
            st |= ITreeManager.LEVEL_CHANGED;
        return st;
    }

    private int gotoLastChild() throws TreeManagerException {
        initNVM3Service();
        ITreeItem active = getActiveItem();
        ITreeItem[] childItems = active.getChildItems();
        if (childItems.length == 0)
            return ITreeManager.NOACTION;
        int st = moveUpdate(childItems[childItems.length - 1], true);
        if ((st & ITreeManager.MOVED) != 0)
            st |= ITreeManager.LEVEL_CHANGED;
        return st;
    }

    private int gotoSiblingIndex(int idx) throws TreeManagerException {
        initNVM3Service();
        ITreeItem active = getActiveItem();
        ITreeItem[] siblingItems = getSiblings();
        if ((idx < 0) || (siblingItems.length <= idx) || (active.equals(siblingItems[idx])))
            return ITreeManager.NOACTION;
        int st = moveUpdate(siblingItems[idx], true);
        return st;
    }

    private int getActiveIndex() throws TreeManagerException {
        initNVM3Service();
        return getActiveItem().getNth();
    }

    public int gotoStartOfSiblings() throws TreeManagerException {
        return gotoSiblingIndex(0);
    }

    public int gotoEndOfSiblings() throws TreeManagerException {
        initNVM3Service();
        ITreeItem[] siblingItems = getSiblings();
        int st = moveUpdate(siblingItems[siblingItems.length - 1], true);
        return st | ITreeManager.MOVED;
    }

    public int gotoPreviousSibling() throws TreeManagerException {
        int idx = getActiveIndex();
        return gotoSiblingIndex(idx - 1);
    }

    public int gotoNextSibling() throws TreeManagerException {
        int idx = getActiveIndex();
        return gotoSiblingIndex(idx + 1);
    }

    public int gotoStartOfPage() throws TreeManagerException {
        ITreeItem item = getCurrentRootItem();
        int st;
        st = moveUpdate(item, true);
        return ITreeManager.NOACTION;

        /*
         ITreeItem[] childItems = item.getChildItems();
         int st;
         if ((childItems != null) && (childItems.length > 0)) {
         st = moveUpdate(childItems[0], true);
         } else {
         st = moveUpdate(item, true);
         }
         if ((st & ITreeManager.MOVED) != 0)
         st |= ITreeManager.LEVEL_CHANGED;
         return st;*/
    }

    public int gotoEndOfPageForFind() throws TreeManagerException {
        // TODO
        int st;
        this.gotoStartOfPage();
        do {
            st = this.gotoEndOfSiblings();
        } while (this.gotoFirstChild() != ITreeManager.NOACTION);

        if ((st & ITreeManager.MOVED) != 0)
            st |= ITreeManager.LEVEL_CHANGED;
        return st;
    }

    public int gotoEndOfPage() throws TreeManagerException {
        // TODO
        int st;
        this.gotoStartOfPage();
        do {
            st = this.gotoEndOfSiblings();
        } while (this.gotoFirstChild() != ITreeManager.NOACTION);

        ITreeItem item = getActiveItem();
        while (!Vocabulary.hasReadingContent().eval(item)) {
            st = this.traverse(true);
            item = getActiveItem();
        }

        if ((st & ITreeManager.MOVED) != 0)
            st |= ITreeManager.LEVEL_CHANGED;
        return st;
    }

    public int gotoPreviousLine() throws TreeManagerException {
        // TODO
        return ITreeManager.NOACTION;
    }

    public int gotoNextLine() throws TreeManagerException {
        // TODO
        return ITreeManager.NOACTION;
    }

    public int gotoStartOfLine() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int gotoEndOfLine() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    private int findItem(boolean back, IProposition proposition) throws TreeManagerException {
        try {
            ITreeItem item;
            int[] pos = getActivePos();
            int level = pos.length;
            int rc;
            if (back) {
                // First, traverse to the next item in order to skip the items
                // connectable with the current item.
                rc = traverse(true);
                item = getActiveItem();
                if (proposition.eval(item)) {
                    rc |= ITreeManager.FOUND;
                } else {
                    rc = nvm3Service.searchBackward(proposition);
                }
            } else {
                // Likewise.
                rc = traverse(false);
                item = getActiveItem();
                if (proposition.eval(item)) {
                    rc |= ITreeManager.FOUND;
                } else {
                    rc = nvm3Service.searchForward(proposition);
                }
            }
            if ((rc & ITreeManager.FOUND) == 0) {
                setActivePos(pos);
                return rc;
            }
            item = getActiveItem();
            if (back) {
                // Skip to the interval start.
                ITreeItem siblings[] = getSiblings();
                int idx = item.getNth();
                int st = intervalStart(siblings, idx);
                if (idx != st) {
                    rc |= moveUpdate(siblings[st], false);
                }
                item = getActiveItem();
            }
            if (!Vocabulary.hasReadingContent().eval(item)) {
                traverse(false);
            }
            if (level != getLevel()) {
                rc |= ITreeManager.LEVEL_CHANGED;
            }
            return rc;
        } catch (NVM3InterruptedException e) {
            throw new TreeManagerInterruptedException(ITreeManager.UNDONE, "Failed to search.", e);
        } catch (NVM3Exception e) {
            throw new TreeManagerException(ITreeManager.ERROR, "Failed to search.", e);
        }
    }

    private int[] getActivePos() throws TreeManagerException {
        int[] ret = new int[getLevel()];
        ITreeItem current = getActiveItem();
        int count = ret.length;
        while (count > 0) {
            ret[count - 1] = current.getNth();
            count--;
            current = current.getParent();
        }
        return ret;
    }

    private int setActivePos(int[] indexes) throws TreeManagerException {
        gotoRoot();
        ITreeItem item;
        int st = ITreeManager.NOACTION;
        for (int i = 0; i < indexes.length; i++) {
            item = getActiveItem();
            ITreeItem[] children = item.getChildItems();
            int idx = indexes[i];
            if ((children == null) || (idx >= children.length))
                break;
            st |= moveUpdate(children[idx], true);
        }
        return st;
    }

    public int findNext(IProposition proposition) throws TreeManagerException {
        return findItem(false, proposition);
    }

    public int findPrevious(IProposition proposition) throws TreeManagerException {
        return findItem(true, proposition);
    }

    private int traverseForward(ITreeItem active) throws TreeManagerException {
        ITreeItem[] childItems = active.getChildItems();
        if (childItems.length != 0) {
            int st = moveUpdate(childItems[0], false);
            return st;
        }
        int idx = active.getNth();
        ITreeItem[] siblings = getSiblings();
        if ((idx >= 0) && ((idx + 1) < siblings.length)) {
            int st = moveUpdate(siblings[idx + 1], false);
            return st;
        }

        for (int upCount = 1;; upCount++) {
            active = getActiveItem();
            ITreeItem parent = active.getParent();
            if (parent == null) {
                for (; upCount > 0; upCount--) {
                    gotoLastChild();
                }
                return ITreeManager.NOACTION;
            }
            if (moveUpdate(parent, false) == ITreeManager.NOACTION) {
                throw new TreeManagerException(ITreeManager.ERROR, "Internal Error.");
            }
            idx = getActiveIndex();
            siblings = getSiblings();
            if ((idx >= 0) && ((idx + 1) < siblings.length)) {
                int st = moveUpdate(siblings[idx + 1], false);
                return st;
            }
        }
    }

    private int traverseBackward() throws TreeManagerException {
        int idx = getActiveIndex();
        if (idx <= 0) {
            ITreeItem active = getActiveItem();
            ITreeItem parent = active.getParent();
            if (parent == null)
                return ITreeManager.NOACTION;
            int st = moveUpdate(parent, false);
            return st;
        }
        ITreeItem[] siblings = getSiblings();
        int st = moveUpdate(siblings[idx - 1], false);

        for (;;) {
            ITreeItem active = getActiveItem();
            ITreeItem[] childItems = active.getChildItems();
            if (childItems.length == 0)
                break;
            st |= moveUpdate(childItems[0], false) | ITreeManager.LEVEL_CHANGED;
            siblings = getSiblings();
            if (siblings.length > 1) {
                st |= moveUpdate(siblings[siblings.length - 1], false);
            }
        }
        return st;
    }

    private int intervalStart(ITreeItem[] siblings, int st) {
        for (st = st - 1;; st--) {
            if (st < 0)
                return 0;
            if (!Vocabulary.isConnectable().eval(siblings[st]))
                return st + 1;
        }
    }

    private int intervalEnd(ITreeItem[] siblings, int end) {
        for (; end < siblings.length; end++) {
            if (!Vocabulary.isConnectable().eval(siblings[end]))
                return end;
        }
        return end - 1;
    }

    public int traverse(boolean back) throws TreeManagerException {
        initNVM3Service();
        ITreeItem orig = getActiveItem();
        ITreeItem current = orig;
        int level = getLevel();
        int rc = ITreeManager.NOACTION;

        if (back) {
            do {
                rc = traverseBackward();
                current = getActiveItem();
            } while ((rc != ITreeManager.NOACTION)
                    && (!Vocabulary.hasReadingContent().eval(current)));
            ITreeItem siblings[] = getSiblings();
            int idx = current.getNth();
            int st = intervalStart(siblings, idx);
            if (idx != st) {
                rc |= moveUpdate(siblings[st], false);
            }
        } else {
            ITreeItem siblings[] = getSiblings();
            int idx = current.getNth();
            int end = intervalEnd(siblings, idx);
            current = siblings[end];
            do {
                rc = traverseForward(current);
                current = getActiveItem();
            } while ((rc != ITreeManager.NOACTION)
                    && (!Vocabulary.hasReadingContent().eval(current)));
        }

        if (!orig.equals(current)) {
            // rc |= ITreeManager.MOVED;
            if (level != getLevel()) {
                rc |= ITreeManager.LEVEL_CHANGED;
            }
        }

        return rc;
    }

    public String[] getActiveStrings() throws TreeManagerException {
        initNVM3Service();
        ArrayList al = new ArrayList();
        ITreeItem[] siblings = getSiblings();
        for (int i = getActiveIndex(); i < siblings.length; i++) {
            ITreeItem current = siblings[i];
            String str = current.getUIString();
            if (str.length() > 0) {
                al.add(str);
            }
            ITreeItem[] childItems = current.getChildItems();
            if (childItems.length > 0)
                break;
        }
        return (String[]) al.toArray(new String[0]);
    }

    public int gotoLeftCell() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int gotoRightCell() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int gotoUpCell() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int gotoDownCell() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getActiveTableInfo() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getActiveTableCellInfo() throws TreeManagerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public ITreeItem getCurrentRootItem() throws TreeManagerException {
        initNVM3Service();
        ITreeItem parent, current;
        current = getActiveItem();
        while (true) {
            parent = current.getParent();
            if (parent == null)
                break;
            current = parent;
        }
        return current;
    }

    public ISoundControl getSoundControl() throws TreeManagerException {
        initNVM3Service();
        return nvm3Service.getSoundControl();
    }

    public IVideoControl getVideoControl() throws TreeManagerException {
        initNVM3Service();
        return nvm3Service.getVideoControl();
    }

    public IAccessKeyList getAccessKeyList() throws TreeManagerException {
        initNVM3Service();
        return nvm3Service.getAccessKeyList();
    }

    public int analyze() throws TreeManagerException {
        initNVM3Service();
        try {
            return nvm3Service.analyze();
        } catch (NVM3Exception e) {
            throw new TreeManagerException(ITreeManager.ERROR, "Failed to analyze", e);
        }
    }

    public ILocation getCurrentLocation() throws TreeManagerException {
        initNVM3Service();
        return new LocationImpl(getActivePos());
    }

    public int moveToLocation(ILocation location) throws TreeManagerException {
        initNVM3Service();
        LocationImpl loc = (LocationImpl) location;
        int[] pos = loc.getPos();
        return setActivePos(pos);
    }

    public ITreeItem expandWholeTree() throws TreeManagerException {
        initNVM3Service();
        try {
            return nvm3Service.expandWholeTree();
        } catch (NVM3Exception e) {
            throw new TreeManagerException(ITreeManager.ERROR, "Failed to expand the whole tree", e);
        }
    }

    public int skipToAnchor(String target) throws TreeManagerException {
        initNVM3Service();
        try {
            return nvm3Service.skipToAnchor(target);
        } catch (NVM3Exception e) {
            throw new TreeManagerException(ITreeManager.ERROR, "Failed to find the target in the anchors", e);
        }
    }
    
    public void repairFlash() throws TreeManagerException {
        initNVM3Service();
        IFlashNode[] node = nvm3Service.getFlashTopNodes();
        for (int i = 0; i < node.length; i++) {
            node[i].repairFlash();
        }
    }
}
