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

import java.util.ArrayList;

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.eclipse.actf.util.vocab.DelegationTerms;
import org.eclipse.actf.util.vocab.IEvalTarget;
import org.eclipse.actf.util.vocab.Vocabulary;
import org.eclipse.swt.graphics.Rectangle;
import org.w3c.dom.Node;




public class TreeItemTerms extends DelegationTerms {

    public TreeItemTerms(IEvalTarget delegationTarget) {
        super(delegationTarget);
    }

    private boolean isSymbol(char c) {
        int type = Character.getType(c);
        if (type == Character.OTHER_SYMBOL || type == Character.MODIFIER_SYMBOL || type == Character.MATH_SYMBOL)
            return true;
        return false;
    }

    private boolean isSeparator(char c) {
        switch (Character.getType(c)) {
        case Character.SPACE_SEPARATOR:
        case Character.LINE_SEPARATOR:
        case Character.PARAGRAPH_SEPARATOR:
        case Character.FORMAT:
        case Character.CONTROL:
            return true;
        }
        return false;
    }

    private boolean isPunctuation(char c) {
        int type = Character.getType(c);
        if (type == Character.CONNECTOR_PUNCTUATION || type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION || type == Character.END_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION || type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION)
            return true;
        return false;
    }

    private enum ContentCheckResult {
        TRUE, FALSE, UNKNOWN
    }
    private ContentCheckResult contentCommonCheck(ITreeItem item) {
        Object baseNode = item.getBaseNode();
        if (baseNode instanceof IEvalTarget) {
            if (Vocabulary.hasContent().eval((IEvalTarget) baseNode)) {
                return ContentCheckResult.TRUE;
            }
        }

        if (!isVisibleNode(item))
            return ContentCheckResult.FALSE;
        if (Vocabulary.isSelectOption().eval(item))
            return ContentCheckResult.FALSE;

        return ContentCheckResult.UNKNOWN;
    }

    @Override
    public boolean hasContent(IEvalTarget target) {
        if (!(target instanceof ITreeItem))
            return false;
        ITreeItem item = (ITreeItem) target;
        switch (contentCommonCheck(item)) {
        case TRUE:
            return true;
        case FALSE:
            return false;
        }

        String str = item.getUIString();
        if (str.length() == 0)
            return false;
        return true;
    }

    @Override
    public boolean hasReadingContent(IEvalTarget target) {
        if (!(target instanceof ITreeItem))
            return false;
        ITreeItem item = (ITreeItem) target;
        switch (contentCommonCheck(item)) {
        case TRUE:
            return true;
        case FALSE:
            return false;
        }

        String str = item.getUIString();
        // remove only punctuation
        if (str.length() == 0) return false;

        if (Vocabulary.isLink().eval(item))
            return true;
        for (int i = 0; i < str.length(); i++) {
            if (isSeparator(str.charAt(i)) || isPunctuation(str.charAt(i)) //
                    || isSymbol(str.charAt(i)))
                continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isVisibleNode(IEvalTarget target) {
        return true;
    }

    @Override
    public boolean isBlockJumpPointF(IEvalTarget node) {
        return isBlockJumpPoint(false, node);
    }

    @Override
    public boolean isBlockJumpPointB(IEvalTarget node) {
        return isBlockJumpPoint(true, node);
    }

    public boolean isBlockJumpPoint(boolean back, IEvalTarget node) {
        if (!(node instanceof TreeItemFennec))
            return false;
        TreeItemFennec item = (TreeItemFennec) node;

        TreeItemFennec prev;
        if (!back) {
            if (item.getNth() > 0) {
                prev = (TreeItemFennec) item.getParent().getChildItems()[item.getNth() - 1];
                while (prev.hasChild()) {
                    ITreeItem[] items = prev.getChildItems();
                    prev = (TreeItemFennec) items[items.length - 1];
                }
            } else {
                prev = (TreeItemFennec) item.getParent();
            }
        } else {
            if (item.getNth() == item.getParent().getChildItems().length - 1) {
                ITreeItem parent = item.getParent();
                if (parent == null)
                    return false;
                while (parent.getNth() == parent.getParent().getChildItems().length - 1) {
                    parent = parent.getParent();
                    if (parent == null)
                        return false;
                }
                prev = (TreeItemFennec) parent.getParent().getChildItems()[parent.getNth() + 1];
            } else {
                if (item.hasChild()) {
                    prev = (TreeItemFennec) item.getChildItems()[0];
                } else {
                    prev = (TreeItemFennec) item.getParent().getChildItems()[item.getNth() + 1];
                }
            }
        }
        if (prev == null)
            return false;

        item.distance = prev.distance + 1;
        prev.distance = 0;

        if (!(item.getBaseNode() instanceof INodeEx))
            return false;
        if (!(prev.getBaseNode() instanceof INodeEx))
            return false;

        INodeEx nex = (INodeEx) item.getBaseNode();
        INodeEx nex2 = (INodeEx) prev.getBaseNode();
        if (nex == null || nex2 == null)
            return false;

        Rectangle r = nex.getLocation();
        Rectangle r2 = nex2.getLocation();
        if (r == null || r2 == null)
            return false;

        int dist = distance(r, r2);
        item.distance += dist;

        if (r.width < 20)
            return false;
        if (!Vocabulary.hasContent().eval(node))
            return false;
        if (!Vocabulary.hasReadingContent().eval(node))
            return false;
        if (Vocabulary.isClickable().eval(node))
            return false;
        if (item.getNth() > 2 && Vocabulary.isConnectable().eval(node))
            return false;

        if (item.distance > 800) {
            item.distance = 0;
            return true;
        }

        if (!back && !super.isBlockJumpPointF(node))
            return false;
        if (back && !super.isBlockJumpPointB(node))
            return false;

        if (item.distance > 200) {
            item.distance = 0;
            return true;
        }
        return false;
    }

    private int distance(Rectangle r, Rectangle r2) {
        return (int) Math.sqrt(Math.abs(r.x - r2.x) * Math.abs(r.y - r2.y)) //
                + Math.abs(r.x - r2.x) + Math.abs(r.y - r2.y);
    }

    @Override
    public boolean isHeading(int level, IEvalTarget node) {
        if (!(node instanceof ITreeItem))
            return false;
        ITreeItem item = (ITreeItem) node;
        if (level == 0)
            return (item.getHeadingLevel() > 0);
        else
            return item.getHeadingLevel() == level;
    }

    @Override
    public boolean isHeadingJumpPoint(IEvalTarget node) {
        if (!(node instanceof ITreeItem))
            return false;
        ITreeItem item = (ITreeItem) node;

        ITreeItem current = item;
        short r = 0;
        do {
            if (current == null)
                return super.isHeadingJumpPoint(node);
            FennecMetadata meta = ((TreeItemFennec) current).getMetadata();
            if (meta instanceof FennecGeneratedMetadata) {
                r = ((FennecGeneratedMetadata) meta).getHeadingLevelByMetadata(item);
                if (r > 0)
                    return true;
                else if (r == -1)
                    return false;
                current = current.getParent();
            } else
                break;
        } while (r == 0);

        if (((TreeItemFennec) item).getMetadata() == null)
            return false;
        return ((TreeItemFennec) item).getMetadata().getHeadingLevel(item) > 0;
    }

    @Override
    public boolean isConnectable(IEvalTarget node) {
        if (!(node instanceof TreeItemFennec))
            return false;
        TreeItemFennec item = (TreeItemFennec) node;

        if (item.hasChild())
            return false;

        int nth = item.getNth();
        int nextNth = nth + 1;

        ITreeItem parent = ((TreeItemFennec) node).getParent();
        if (parent == null)
            return false;

        ITreeItem[] items = parent.getChildItems();
        if (nextNth >= items.length)
            return false;
        
        Object o = items[nextNth].getBaseNode();
        if (o == null || !(o instanceof Node))
            return false;
        Node n = (Node) o;
            
        return Vocabulary.isReachable(n).eval(node);
    }

    @Override
    public boolean find(String str, boolean exact, IEvalTarget node) {
        if (!(node instanceof ITreeItem))
            return false;

        ITreeItem item = (ITreeItem) node;
        String uiString = item.getUIString();

        if (!exact) {
            uiString = uiString.toLowerCase();
            str = str.toLowerCase();
        }

        if (uiString.indexOf(str) != -1)
            return true;

        /*
         for (int len = str.length() - 1; len > 0; len--) {
         if (uiString.lastIndexOf(str.substring(0, len)) == uiString.length() - len) {
         if (hasChild()) {
         if(Vocabulary.startsWith(str.substring(len), exact).eval(getChildItems()[0])){
         return true;
         }
         continue;
         }

         int nth = item.getNth();
         ITreeItem parent = item.getParent();
         while (parent != null) {
         ITreeItem[] items = parent.getChildItems();
         if (nth + 1 < items.length) {
         if(Vocabulary.startsWith(str.substring(len), exact).eval(items[nth + 1])){
         return true;
         }
         break;
         }
         nth = parent.getNth();
         parent = parent.getParent();
         }
         }
         }*/
        return false;
    }

    @Override
    public boolean startsWith(String str, boolean exact, IEvalTarget node) {
        if (!(node instanceof TreeItemFennec))
            return false;
        TreeItemFennec item = (TreeItemFennec) node;
        String uiString = item.getUIString();

        if (!exact) {
            uiString = uiString.toLowerCase();
            str = str.toLowerCase();
        }

        if (uiString.length() < str.length()) {
            if (str.startsWith(uiString)) {
                int len = uiString.length();
                if (item.hasChild()) {
                    return Vocabulary.startsWith(str.substring(len), exact).eval(item.getChildItems()[0]);
                }

                int nth = item.getNth();
                ITreeItem parent = item.getParent();
                while (parent != null) {
                    ITreeItem[] items = parent.getChildItems();
                    if (nth + 1 < items.length) {
                        return Vocabulary.startsWith(str.substring(len), exact).eval(items[nth + 1]);
                    }
                    nth = parent.getNth();
                    parent = parent.getParent();
                }
            }
        }

        return uiString.startsWith(str);
    }

    @Override
    public boolean nodeLocation(Node refNode, boolean backward, IEvalTarget node) {
        if (!Vocabulary.hasContent().eval(node))
            return false;
        if (!(node instanceof TreeItemFennec))
            return false;
        TreeItemFennec item = (TreeItemFennec) node;

        Node targetNode = item.getNearestNode();
        if (targetNode == null) return false;

        ArrayList<Node> refAncestors = getAncestors(refNode);
        ArrayList<Node> ancestors = getAncestors(targetNode);
        int i = refAncestors.size() - 1;
        int j = ancestors.size() - 1;
        while ((i >= 0) && (j >= 0)) {
            Node refAncestor = refAncestors.get(i);
            Node ancestor = ancestors.get(j);
            if (!(ancestor.isSameNode(refAncestor))) {
                if (!((ancestor instanceof INodeEx)) && (refAncestor instanceof INodeEx))
                    return false;

                int ancestorNth = ((INodeEx) ancestor).getNth();
                int refAncestorNth = ((INodeEx) refAncestor).getNth();
                if (ancestorNth == refAncestorNth)
                    return true;
                if (backward) {
                    return (ancestorNth < refAncestorNth);
                } else {
                    return (ancestorNth > refAncestorNth);
                }
            }
            i--;
            j--;
        }
        return true;
    }

    private ArrayList<Node> getAncestors(Node n) {
        ArrayList<Node> list = new ArrayList<Node>();
        while (n != null) {
            list.add(n);
            n = n.getParentNode();
        }
        return list;
    }
}
