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
package org.eclipse.actf.ai.fennec.treemanager;

import org.eclipse.actf.util.vocab.IProposition;


public interface ITreeManager {
    // Status code.
    int NOACTION = 0;
    int MOVED = 1 << 0;
    int LEVEL_CHANGED = 1 << 1;
    int TRANSFERRED = 1 << 2;
    int CLICKED = 1 << 3;
    int CHANGED = 1 << 4;
    int PARENT_CHANGED = 1 << 5;
    int FOUND = 1 << 6;
    int UNDONE = 1 << 8;
    int ERROR = 1 << 16;

    // Status API
    int getLevel() throws TreeManagerException;

    // Returns the active Item.
    ITreeItem getActiveItem() throws TreeManagerException;

    // Used for media control.  It will be subject to change.
    ISoundControl getSoundControl() throws TreeManagerException;

    // Used for media control.  It will be subject to change.
    IVideoControl getVideoControl() throws TreeManagerException;
    
    // Used for accesskey
    IAccessKeyList getAccessKeyList() throws TreeManagerException;

    // Returns the siblings of the active item.
    ITreeItem[] getSiblings() throws TreeManagerException;

    // initialize. API.
    int initialize() throws TreeManagerException;

    // Action APIs

    // Tentative API.  Do not use it currently.
    int stay() throws TreeManagerException;

    // 
    int click(boolean doClick) throws TreeManagerException;

    // Tree Navigation API
    int gotoParent() throws TreeManagerException;

    int gotoFirstChild() throws TreeManagerException;

    int gotoStartOfSiblings() throws TreeManagerException;

    int gotoEndOfSiblings() throws TreeManagerException;

    int gotoPreviousSibling() throws TreeManagerException;

    int gotoNextSibling() throws TreeManagerException;

    int gotoStartOfPage() throws TreeManagerException;

    int gotoEndOfPage() throws TreeManagerException;

    int traverse(boolean back) throws TreeManagerException;

    // Search functions.  (Extension)

    int gotoPreviousLine() throws TreeManagerException;

    int gotoNextLine() throws TreeManagerException;

    int gotoStartOfLine() throws TreeManagerException;

    int gotoEndOfLine() throws TreeManagerException;

    int findNext(IProposition proposition) throws TreeManagerException;

    int findPrevious(IProposition proposition) throws TreeManagerException;

    int skipToAnchor(String target) throws TreeManagerException;

    // Table Navigation API.

    int gotoLeftCell() throws TreeManagerException;

    int gotoRightCell() throws TreeManagerException;

    int gotoUpCell() throws TreeManagerException;

    int gotoDownCell() throws TreeManagerException;

    int getActiveTableInfo() throws TreeManagerException;

    int getActiveTableCellInfo() throws TreeManagerException;

    // Optional API.  
    ITreeItem getCurrentRootItem() throws TreeManagerException;

    // Experimental API.
    int analyze() throws TreeManagerException;
    
    ILocation getCurrentLocation() throws TreeManagerException;
    
    int moveToLocation(ILocation location) throws TreeManagerException;

    // 
    ITreeItem expandWholeTree() throws TreeManagerException;

    int gotoEndOfPageForFind() throws TreeManagerException;

    void repairFlash() throws TreeManagerException;
}
