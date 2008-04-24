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

import org.eclipse.actf.util.vocab.AbstractTerms;
import org.eclipse.actf.util.vocab.IEvalTarget;
import org.w3c.dom.Node;

/**
 * ITreeItem interface defines the methods to be implemented by the
 * representation of the tree node of the Fennec tree.
 */
public interface ITreeItem extends IEvalTarget {
	/**
	 * Get the parent item. If this is the root, return null.
	 * 
	 * @return the parent ITreeItem.
	 */
	ITreeItem getParent();

	/**
	 * Get the child items. Even if it has no children, return an empty array
	 * (size == 0) instead of null.
	 * 
	 * @return the array of the child ITreeItems.
	 */
	ITreeItem[] getChildItems();

	/**
	 * @return the nth of the children of this parent.
	 */
	int getNth();

	/**
	 * @return the short text of the item.
	 */
	String getUIString();

	/**
	 * @return the long descriptive text of the item.
	 */
	String getDescription();

	/**
	 * This text is inappropriate to be notified to users.
	 * 
	 * @return the text to represent the item.
	 */
	String getNodeString();

	/**
	 * Heading level. 0 means this node is not a heading.
	 * 
	 * @return the heading level of the item.
	 */
	short getHeadingLevel();

	/**
	 * @return the URI of the link of this tree item if it has a link.
	 */
	String getLinkURI();

	/**
	 * @deprecated This is a deprecated API. It may breaks independence on
	 *             content types.
	 * @return the base node of the item.
	 */
	Object getBaseNode();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.util.vocab.IEvalTarget#getTerms()
	 */
	AbstractTerms getTerms();

	// !FN!
	/**
	 * @return true if this item accepts some input.
	 */
	boolean isInputable();

	// !FN!
	/**
	 * @return true if this item accepts click operations.
	 */
	boolean isClickable();

	// !FN!
	/**
	 * @return true if this item contains some image.
	 */
	boolean isImage();

	// !FN!
	/**
	 * @return [0]...mimetype [1]...URI [2] ... png clut URI
	 */
	String[] getStillPictureData();

	/**
	 * Click the item.
	 * 
	 * @return {@link ITreeManager#CLICKED} or {@link ITreeManager#NOACTION}
	 * @throws TreeManagerException
	 */
	int doClick() throws TreeManagerException;

	/**
	 * Optional API. Do not use it currently.
	 * 
	 * @return
	 * @throws TreeManagerException
	 */
	int stay() throws TreeManagerException;

	/**
	 * Highlight the item.
	 * 
	 * @return {@link ITreeManager#NOACTION}
	 * @throws TreeManagerException
	 */
	int highlight() throws TreeManagerException;

	/**
	 * Unhighlight the item.
	 * 
	 * @return {@link ITreeManager#NOACTION}
	 * @throws TreeManagerException
	 */
	int unhighlight() throws TreeManagerException;

	/**
	 * Set focus to this item. This method only sets the input focus if
	 * possible.
	 * 
	 * @return
	 */
	boolean setFocus();

	/**
	 * Set the text to this item. (This method may be used for a text edit
	 * widget)
	 * 
	 * @param text the text to be set.
	 * @return {@link ITreeManager#NOACTION}
	 * @throws TreeManagerException
	 */
	int setText(String text) throws TreeManagerException;

	/**
	 * Return the set text.
	 * @return the text of the item.
	 * @throws TreeManagerException
	 */
	String getText() throws TreeManagerException;

	// Form
	/**
	 * @return the index of the item in the radio group. 
	 */
	int getRadioIndex();

	/**
	 * @return the total number of radio buttons in the radio group.
	 */
	int getRadioTotal();

	/**
	 * @param indices the indices to be set to the item.
	 */
	void setSelectedIndices(int[] indices);

	/**
	 * @return the indices of the item.
	 */
	int[] getSelectedIndices();

	/**
	 * @return the number of the options in the item.
	 */
	int getOptionsCount();

	/**
	 * @param index the index of the option.
	 * @return the text of the option specified the index.
	 */
	String getOptionTextAt(int index);

	/**
	 * @return the index of the item in the list.
	 */
	int getListIndex();

	/**
	 * @return the number of the lists item in the list including the item.
	 */
	int getListTotal();

	/**
	 * @return the label text of the item.
	 */
	public String getFormLabel();

	// User Annotation.
	/**
	 * @param parent the target node to be serialized.
	 * @return the serialized result in form of Node.
	 */
	Node serializeQuery(Node parent);

	/**
	 * @return the code of the access key declared of the item.
	 */
	char getAccessKey();
}
