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
 * The main plugin class to be used in the desktop.
 */
public interface ITreeItem extends IEvalTarget {
    // Get the parent item.  If this is the root, return null.
    ITreeItem getParent();

    // Get the child items.
    // Even if it has no children, return an empty array (size == 0) instead of null.
    ITreeItem[] getChildItems();

    // return nth of the children of this parent.
    int getNth();

    // short text.
    String getUIString();

    // long descriptive text.
    String getDescription();
    
    // Return a text to represent the node.
    // This text is inappropriate to be notified to users.
    String getNodeString();

    // Heading level.  0 means this node is not a heading.
    short getHeadingLevel();

    // Return the URI of the link of this tree item if it has a link.
    String getLinkURI();

    // This is a deprecated API.  It may breaks independence on content types.
    Object getBaseNode();

    AbstractTerms getTerms();
    
    // Return true if this item accepts some input.
    // !FN!
    boolean isInputable();

    // Return true if this item accepts click operations.
    // !FN!
    boolean isClickable();

    // Return true if this item contains some image.
    // !FN!
    boolean isImage();

    // [0]...mimetype [1]...URI [2] ... png clut URI
    // !FN!
    String[] getStillPictureData();

    // Return the URI of the image.
    // String getImageURI();

    // Click this item.
    int doClick() throws TreeManagerException;

    // Optional API.  Do not use it currently.
    int stay() throws TreeManagerException;

    // Highlight this item.
    int highlight() throws TreeManagerException;

    // Unhighlight this item.
    int unhighlight() throws TreeManagerException;
    
    // Set focus to this item.
    // This method only sets the input focus if possible.
    boolean setFocus();

    // Set the text to this item.  (This method may be used for a text edit widget)
    int setText(String text) throws TreeManagerException;

    // Return the set text.
    String getText() throws TreeManagerException;

    // Form
    int getRadioIndex();

    int getRadioTotal();

    void setSelectedIndices(int[] indices);

    int[] getSelectedIndices();

    int getOptionsCount();

    String getOptionTextAt(int i);

    int getListIndex();
    
    int getListTotal();
    
    public String getFormLabel();

    // User Annotation.
    Node serializeQuery(Node parent);

    char getAccessKey();
}
