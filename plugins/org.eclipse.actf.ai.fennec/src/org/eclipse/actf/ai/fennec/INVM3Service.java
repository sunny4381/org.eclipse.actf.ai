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
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.model.dom.dombycom.IFlashNode;
import org.eclipse.actf.util.vocab.IProposition;
import org.w3c.dom.Node;


public interface INVM3Service {
    int UNINIT = 0;
    int NORMAL = 1;
    int ERROR = 1 << 16;

    int getStatus();
    ITreeItem getLastTreeItem();
    ISoundControl getSoundControl();
    IVideoControl getVideoControl();
    IAccessKeyList getAccessKeyList();
    IFlashNode[] getFlashTopNodes();

    int initialize() throws NVM3Exception;
    int moveUpdate(ITreeItem target) throws NVM3Exception;
    int moveUpdate(ITreeItem target, boolean update) throws NVM3Exception;
    int clickUpdate(ITreeItem target) throws NVM3Exception;

    int searchForward(IProposition prop) throws NVM3Exception;
    int searchBackward(IProposition prop) throws NVM3Exception;

    int analyze() throws NVM3Exception;

    int skipToAnchor(String target) throws NVM3Exception;

    // !!FN!!
    ITreeItem expandWholeTree() throws NVM3Exception;
}
