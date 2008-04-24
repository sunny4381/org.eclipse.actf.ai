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

import org.eclipse.actf.ai.fennec.IFennecService;
import org.eclipse.actf.util.vocab.IProposition;

/**
 * ITreeManager interface defines high level methods of Fennec tree
 * representation. It is created using {@link IFennecService}, and it provides
 * function to manage the tree representation by using {@link IFennecService}
 * 
 * All goto**** methods are for updating the current position. These methods try
 * to update the current position to "****" which is readable by the
 * application. These method will return {@link ITreeManager#NOACTION} if the
 * updating is failed.
 */
public interface ITreeManager {
	// Status code.
	/**
	 * The tree is no changed, the tree did no action.
	 */
	int NOACTION = 0;
	/**
	 * The current position is moved.
	 */
	int MOVED = 1 << 0;
	/**
	 * The level of the current position is changed.
	 */
	int LEVEL_CHANGED = 1 << 1;
	/**
	 * It is not used.
	 */
	int TRANSFERRED = 1 << 2;
	/**
	 * The element of the current position is clicked.
	 */
	int CLICKED = 1 << 3;
	/**
	 * The element of the current position is changed.
	 */
	int CHANGED = 1 << 4;
	/**
	 * The parent of the element of the current position is changed.
	 */
	int PARENT_CHANGED = 1 << 5;
	/**
	 * An element is found in searching or something.
	 */
	int FOUND = 1 << 6;
	/**
	 * A {@link TreeManagerInterruptedException} is thrown.
	 */
	int UNDONE = 1 << 8;
	/**
	 * A {@link TreeManagerException} is thrown.
	 */
	int ERROR = 1 << 16;

	// Status API
	/**
	 * @return the tree level of the current position.
	 * @throws TreeManagerException
	 */
	int getLevel() throws TreeManagerException;

	/**
	 * @return the current item of the document.
	 * @throws TreeManagerException
	 */
	ITreeItem getActiveItem() throws TreeManagerException;

	/**
	 * Used for media control. It will be subject to change.
	 * 
	 * @return
	 * @throws TreeManagerException
	 */
	ISoundControl getSoundControl() throws TreeManagerException;

	/**
	 * Used for media control. It will be subject to change.
	 * 
	 * @return
	 * @throws TreeManagerException
	 */
	IVideoControl getVideoControl() throws TreeManagerException;

	/**
	 * Used for access key.
	 * 
	 * @return
	 * @throws TreeManagerException
	 */
	IAccessKeyList getAccessKeyList() throws TreeManagerException;

	/**
	 * @return the siblings of the current item.
	 * @throws TreeManagerException
	 */
	ITreeItem[] getSiblings() throws TreeManagerException;

	/**
	 * initialize. API.
	 * 
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int initialize() throws TreeManagerException;

	// Action APIs

	/**
	 * Tentative API. Do not use it currently.
	 * 
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int stay() throws TreeManagerException;

	/**
	 * @param doClick
	 *            the children of the current item is updated when false. false
	 *            is used when retrying because of interruption of operation.
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int click(boolean doClick) throws TreeManagerException;

	// Tree Navigation API
	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoParent() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoFirstChild() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoStartOfSiblings() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoEndOfSiblings() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoPreviousSibling() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoNextSibling() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoStartOfPage() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoEndOfPage() throws TreeManagerException;

	/**
	 * @param back
	 *            the direction of the traversing. true = backward, false =
	 *            forward.
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int traverse(boolean back) throws TreeManagerException;

	// Search functions. (Extension)

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoPreviousLine() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoNextLine() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoStartOfLine() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoEndOfLine() throws TreeManagerException;

	/**
	 * It searches a element matched with the <i>proposition</i>. All tree
	 * items after the current position is tested to be matched in forward
	 * direction. If the element matched with the <i>proposition</i> is found
	 * then the current position is changed to the element or the current
	 * position is not changed. If the searching reaches the end of the page
	 * then the searching continues from the start of the page to the current
	 * position.
	 * 
	 * @param proposition
	 *            the instance of IProposition to be used for searching.
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int findNext(IProposition proposition) throws TreeManagerException;

	/**
	 * This method is backward direction version of
	 * {@link #findNext(IProposition)}.
	 * 
	 * @param proposition
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int findPrevious(IProposition proposition) throws TreeManagerException;

	/**
	 * @param target
	 *            the anchor name to be searched.
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int skipToAnchor(String target) throws TreeManagerException;

	// Table Navigation API.

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoLeftCell() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoRightCell() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoUpCell() throws TreeManagerException;

	/**
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoDownCell() throws TreeManagerException;

	/**
	 * @return the information of the current table which includes the current
	 *         position.
	 * @throws TreeManagerException
	 */
	int getActiveTableInfo() throws TreeManagerException;

	/**
	 * @return the information of the current table cell.
	 * @throws TreeManagerException
	 */
	int getActiveTableCellInfo() throws TreeManagerException;

	// Optional API.
	/**
	 * @return the root item of the document.
	 * @throws TreeManagerException
	 */
	ITreeItem getCurrentRootItem() throws TreeManagerException;

	// Experimental API.
	/**
	 * This method is used when the analysis is failed because of load timing.
	 * 
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int analyze() throws TreeManagerException;

	/**
	 * @return the ILocation instance of the current position. the instance will
	 *         be used for {@link #moveToLocation(ILocation)}
	 * @throws TreeManagerException
	 */
	ILocation getCurrentLocation() throws TreeManagerException;

	/**
	 * @param location
	 *            the location to which the current position is moved.
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int moveToLocation(ILocation location) throws TreeManagerException;

	// 
	/**
	 * All tree items of the tree representation by the Fennec metadata are
	 * expanded at once.
	 * 
	 * @return the root item of the document.
	 * @throws TreeManagerException
	 */
	ITreeItem expandWholeTree() throws TreeManagerException;

	/**
	 * Change the current position to the end of page without readable checking.
	 * 
	 * @return the status of this operation.
	 * @throws TreeManagerException
	 */
	int gotoEndOfPageForFind() throws TreeManagerException;

	/**
	 * Execute the repairing function of the Flash objects.
	 * 
	 * @throws TreeManagerException
	 */
	void repairFlash() throws TreeManagerException;
}
