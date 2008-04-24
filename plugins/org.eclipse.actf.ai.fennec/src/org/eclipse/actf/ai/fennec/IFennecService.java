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
package org.eclipse.actf.ai.fennec;

import org.eclipse.actf.ai.fennec.treemanager.IAccessKeyList;
import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.model.dom.dombycom.IFlashNode;
import org.eclipse.actf.util.vocab.IProposition;

/**
 * IFennecService interface defines the methods to manage a document with a
 * Fennec metadata which provides another tree presentation of the document. The
 * service has a position, and the tree presentation is dynamically changed by
 * updating the position. When the position is tried to be updated then the
 * result code of ITreeManager is returned.
 * 
 * @see ITreeManager#NOACTION
 * @see ITreeManager#MOVED
 * @see ITreeManager#CLICKED
 * @see ITreeManager#FOUND
 */
public interface IFennecService {
	/**
	 * The service is not initialized.
	 */
	int UNINIT = 0;
	/**
	 * The service is available.
	 */
	int NORMAL = 1;
	/**
	 * The service is in error.
	 */
	int ERROR = 1 << 16;

	/**
	 * @return the status of the service.
	 * @see #UNINIT
	 * @see #NORMAL
	 * @see #ERROR
	 */
	int getStatus();

	/**
	 * @return the ITreeItem instance of the position on the service.
	 */
	ITreeItem getLastTreeItem();

	/**
	 * It returns collected sound objects in the document.
	 * 
	 * @return the {@link ISoundControl} instance of the document tree.
	 * @see ISoundControl
	 */
	ISoundControl getSoundControl();

	/**
	 * It returns collected video objects in the document.
	 * 
	 * @return the {@link IVideoControl} instance of the document tree.
	 */
	IVideoControl getVideoControl();

	/**
	 * It returns collected access keys declared in the document.
	 * 
	 * @return the {@link IAccessKeyList} instance of the document tree.
	 */
	IAccessKeyList getAccessKeyList();

	/**
	 * It returns Flash objects in the document.
	 * 
	 * @return the array of the {@link IFlashNode} instances of the document
	 *         tree.
	 */
	IFlashNode[] getFlashTopNodes();

	/**
	 * Initialize the service. It creates the root element of the tree
	 * presentation of the document, and analyzes the document to collect
	 * objects such as sounds, videos, and access keys in the document.
	 * 
	 * @return {@link ITreeManager#MOVED} is returned.
	 * @throws FennecException
	 *             it is thrown if the initialization is failed.
	 */
	int initialize() throws FennecException;

	/**
	 * The position will be updated to the specified ITreeItem. You can update
	 * the position by traversing from the position, {@link #getLastTreeItem()}
	 * which has a parent and some children. This method is the shortcut of
	 * moveUpdate(target, true).
	 * 
	 * @param target
	 *            the new position of the tree presentation.
	 * @return the result code of the updating.
	 * @throws FennecException
	 * @see #moveUpdate(ITreeItem, boolean)
	 * @see ITreeManager#NOACTION
	 * @see ITreeManager#MOVED
	 * @see ITreeManager#CLICKED
	 * @see ITreeManager#FOUND
	 */
	int moveUpdate(ITreeItem target) throws FennecException;

	/**
	 * The position will be updated to the specified ITreeItem. If the <i>update</i>
	 * is false then the children of the current ITreeItem are not updated, the
	 * children are cached instances. It is valid in static document, but if the
	 * original document is changed dynamically the children are invalid.
	 * 
	 * @param target
	 *            the new position of the tree presentation.
	 * @param update
	 *            whether updating the children of the target or not.
	 * @return the result code of the updating.
	 * @throws FennecException
	 * @see ITreeManager#NOACTION
	 * @see ITreeManager#MOVED
	 * @see ITreeManager#CLICKED
	 * @see ITreeManager#FOUND
	 */
	int moveUpdate(ITreeItem target, boolean update) throws FennecException;

	/**
	 * The position will be updated. This method should be used after clicking
	 * the target. If the target is disappear by the clicking the position will
	 * be changed to proper position.
	 * 
	 * @param target
	 *            the target ITreeItem which was clicked.
	 * @return the result code of the updating.
	 * @throws FennecException
	 * @see ITreeManager#NOACTION
	 * @see ITreeManager#MOVED
	 * @see ITreeManager#CLICKED
	 * @see ITreeManager#FOUND
	 */
	int clickUpdate(ITreeItem target) throws FennecException;

	/**
	 * It searches the element which is matched with the specified IProposition
	 * in forward direction. This service tests
	 * {@link IProposition#eval(org.eclipse.actf.util.vocab.IEvalTarget)} with
	 * all elements from the current position in forward direction. If the
	 * matching is succeeded then it returns.
	 * 
	 * @param prop
	 *            the instance of IProposition to be used for the searching.
	 * @return
	 * @return the result code of the updating.
	 * @throws FennecException
	 * @see ITreeManager#NOACTION
	 * @see ITreeManager#MOVED
	 * @see ITreeManager#CLICKED
	 * @see ITreeManager#FOUND
	 * @see IProposition
	 */
	int searchForward(IProposition prop) throws FennecException;

	/**
	 * It searches the element which is matched with the specified IProposition
	 * in backward direction.
	 * 
	 * @param prop
	 * @return the result code of the updating.
	 * @throws FennecException
	 * @see ITreeManager#NOACTION
	 * @see ITreeManager#MOVED
	 * @see ITreeManager#CLICKED
	 * @see ITreeManager#FOUND
	 * @see #searchForward(IProposition)
	 */
	int searchBackward(IProposition prop) throws FennecException;

	/**
	 * It analyzes the document to collect objects such as sounds, videos, and
	 * access keys in the document. This analysis is executed in the
	 * {@link #initialize()}. So it is used when the analysis is failed because
	 * of loading timing or something.
	 * 
	 * @return it returns ITreeManager#NOACTION
	 * @throws FennecException
	 */
	int analyze() throws FennecException;

	/**
	 * It searches the anchor element specified by <i>target</i>.
	 * 
	 * @param target
	 *            the anchor name to be searched.
	 * @return the result code of the updating.
	 * @throws FennecException
	 * @see ITreeManager#NOACTION
	 * @see ITreeManager#MOVED
	 * @see ITreeManager#CLICKED
	 * @see ITreeManager#FOUND
	 */
	int skipToAnchor(String target) throws FennecException;

	// !!FN!!
	/**
	 * It expands whole tree of the document. It means all elements in the
	 * document are scanned. It would take a long time.
	 * 
	 * @return the root element of the document.
	 * @throws FennecException
	 */
	ITreeItem expandWholeTree() throws FennecException;
}
