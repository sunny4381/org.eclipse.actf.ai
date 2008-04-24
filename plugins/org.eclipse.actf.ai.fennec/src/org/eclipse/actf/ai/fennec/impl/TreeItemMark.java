/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.fennec.impl;

import java.util.List;

import org.eclipse.actf.ai.fennec.FennecException;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.util.vocab.AbstractTerms;
import org.eclipse.actf.util.vocab.IEvalTarget;




public class TreeItemMark extends TreeItemFennec {
    private static class TreeItemMarkTerms extends TreeItemTerms {
        @Override
        public boolean hasContent(IEvalTarget target) {
            return true;
        }

        @Override
        public boolean hasReadingContent(IEvalTarget target) {
            return true;
        }

        @Override
        public boolean isFlashLastNode(IEvalTarget node) {
            if (node instanceof TreeItemMark) {
                TreeItemMark mark = (TreeItemMark) node;
                if (mark.getType() == MarkType.FLASH_END) 
                    return true;
            }
            return false;
        }

        TreeItemMarkTerms() {
            super(null);
        }
    }

    private static TreeItemTerms treeItemMarkTermsInstance = new TreeItemMarkTerms();

    public enum MarkType {
        FLASH_END
    }

    @Override
    public AbstractTerms getTerms() {
        return treeItemMarkTermsInstance;
    }
    
    private MarkType type;
    
    public TreeItemMark(ITreeItem parent, MarkType type) {
        super(null, parent, null, treeItemMarkTermsInstance);
        this.type = type;
    }
    
    @Override
    public String getUIString() {
        return "";
    }
    
    @Override
    public String getNodeString() {
        return "Mark";
    }
    
    public MarkType getType() {
        return type;
    }
    
    @Override
    TreeItemFennec expandChildItems(int trigger) throws FennecException {
        setChildItems((List) null);
        return this;
    }

}
