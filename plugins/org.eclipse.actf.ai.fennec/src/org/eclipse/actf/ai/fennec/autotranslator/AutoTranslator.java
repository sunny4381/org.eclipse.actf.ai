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
package org.eclipse.actf.ai.fennec.autotranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.actf.ai.fennec.impl.FennecMode;
import org.eclipse.actf.ai.fennec.impl.TreeItemMark;
import org.eclipse.actf.ai.fennec.impl.TreeItemFennec;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.model.dom.dombycom.IFlashNode;
import org.eclipse.actf.model.dom.dombycom.IImageElement;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.eclipse.actf.util.vocab.Vocabulary;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class AutoTranslator {
    private static final boolean AUTO_COLLAPSE = true;

    private static final boolean AUTO_INDENT = true;

    private static boolean canCollapse(TreeItemFennec item) {
        if (true) {
            return Vocabulary.isReducible().eval(item);
        } else {
            if (Vocabulary.hasContent().eval(item))
                return false;
            ITreeItem[] cc = item.getChildItems();
            for (int i = 0; i < cc.length; i++) {
                TreeItemFennec item2 = (TreeItemFennec) cc[i];
                if (Vocabulary.hasContent().eval(item2))
                    return false;
            }
            return true;
        }
    }

    private static TreeItemFennec simplify(TreeItemFennec item, TreeItemFennec pItem) {
        if (AUTO_COLLAPSE) {
            // pattern pItem - item(X) - child => pItem - child
            if (!Vocabulary.hasContent().eval(item)) {
                ITreeItem[] cc = item.getChildItems();
                if (cc.length == 0) {
                    return null;
                }
                if (cc.length == 1) {
                    TreeItemFennec item2 = (TreeItemFennec) cc[0];
                    item2.forceParent(pItem);
                    item2.addMetadata(item);
                    return item2;
                }
            }
            
            // pattern item - child(X) - grandChild => item - grandChild
            ITreeItem[] childItems = item.getChildItems();
            if (childItems.length == 1) {
                TreeItemFennec item2 = (TreeItemFennec) childItems[0];
                if (!Vocabulary.hasContent().eval(item2)){
                    ITreeItem[] childItems2 = item2.getChildItems();
                    item.setChildItems(childItems2);
                    item.addMetadata(item2);
                }
            }
        }

        return item;
    }

    private static void adjustLabelIndent(TreeItemFennec item) {
        // TODO
        //        var itemCount = item._childItems.length;
        //        if( itemCount>1 ) {
        //            var heading;
        //            var topIsLabel = true;
        //            for( var i=0, child; child=item._childItems[i]; i++ ) {
        //            if( topIsLabel ) {
        //                topIsLabel = child._childItems.length ? (i>0) : (i==0);
        //            }
        //            if( child.isHeadingNode() ) {
        //                // Collapse heading node tree
        //                child._join();
        //                heading = child._childItems.length==0 ? child : null;
        //            }
        //            else if( heading ) {
        //                // Move child item into heading item children
        //                heading._childItems.push(child);
        //                item._childItems.splice(i--,1);
        //                topIsLabel = false;
        //                        heading._labelHead = true;
        //            }
        //            }
        //            if( topIsLabel ) {
        //            // 1st child is a label. Move item 2-n into item 1 children
        //                    heading = item._childItems[0];
        //            heading._childItems = item._childItems.splice(1,item._childItems.length-1);
        //                    heading._labelHead = true;
        //            }
        //            if( (item._childItems.length != itemCount) && Options.AUTO_COLLAPSE ) {
        //            // Re-adjust tree collaption because tree structure has changed
        //            for( var i=0, child; child=item._childItems[i]; i++ ) {
        //                child._simplify();
        //            }
        //            item._simplify();
        //            }
        //        }

    }

    private static List<ITreeItem> buildTreeItemContinued(FennecMode mode, TreeItemFennec item, INodeEx nex, int depth) {
        List<ITreeItem> childItemList = new ArrayList<ITreeItem>();
        ITreeItem lastItem = null;
        
        if (nex instanceof IFlashNode) {
            IFlashNode fn = (IFlashNode) nex;
            nex = fn.getMSAA();
            lastItem = new TreeItemMark(item, TreeItemMark.MarkType.FLASH_END);
            
            if ((nex != null && Vocabulary.getNormalFlashMode() == Vocabulary.FlashMode.NO_FLASH)
                    || (nex == null && Vocabulary.getWindowlessFlashMode() == Vocabulary.FlashMode.NO_FLASH)) {
                return childItemList;
            }
            
            if ((nex != null && Vocabulary.getNormalFlashMode() == Vocabulary.FlashMode.FLASH_DOM)
                    || (nex == null && Vocabulary.getWindowlessFlashMode() == Vocabulary.FlashMode.FLASH_DOM)) {
                IFlashNode[] translated = fn.translate();
                for (int i = 0; i < translated.length; i++) {
                    TreeItemFennec newItem = mode.generateItem(item, translated[i]);
                    if (newItem == null) continue;
                    childItemList.add(newItem);
                }
                if (Vocabulary.isFlashTopNode().eval(fn)) {
                    childItemList.add(lastItem);
                }
                
                item.setChildItems(childItemList);
                return childItemList;
            }
        }
        
        if (nex instanceof IImageElement) {
            IImageElement image = (IImageElement) nex;
            if (image.hasUsemap()) {
                Element map = image.getMap();
                if (map == null) return childItemList;
                NodeList nl = map.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    if (n instanceof Element) {
                        TreeItemFennec newItem = mode.generateItem(item, n);
                        childItemList.add(newItem);
                    }
                }
                item.setChildItems(childItemList);
                return childItemList;
            }
        }

        if (depth <= 0) {
            item.setChildItems(childItemList);
            return childItemList;
        }

        TreeItemFennec childItem = null;
        NodeList nl = nex.getChildNodes();
        int len = nl.getLength();
        boolean hasContent = Vocabulary.hasContent().eval(item);
        for (int i = 0; i < len; i++) {
            Node cn = nl.item(i);

            if (hasContent) {
                childItem = buildTreeItem(mode, item, cn, depth - 1);
            } else {
                childItem = buildTreeItem(mode, item, cn, depth);
            }
            if (childItem == null)
                continue;

            if (true && AUTO_COLLAPSE && canCollapse(childItem)) {
                ITreeItem[] cc = childItem.getChildItems();
                if ((cc != null) && (cc.length > 0)) {
                    for (int j = 0; j < cc.length; j++) {
                        if (!(cc[j] instanceof TreeItemFennec))
                            continue;
                        TreeItemFennec temp = (TreeItemFennec) cc[j];
                        temp.addMetadata(childItem);
                    }
                    childItemList.addAll(Arrays.asList(cc));
                }
            } else {
                childItemList.add(childItem);
            }
        }
        
        if (lastItem != null)
            childItemList.add(lastItem);

        item.setChildItems(childItemList);
        return childItemList;
    }

    private static TreeItemFennec buildTreeItem(FennecMode mode, TreeItemFennec pItem, Node n, int depth) {
        if (!(n instanceof INodeEx)) {
            return null;
        }

        INodeEx  nex = (INodeEx) n;

        if (!Vocabulary.isValidNode().eval(nex)) {
            return null;
        }

        TreeItemFennec item = mode.generateItem(pItem, n);
        if (item == null) return null;
        buildTreeItemContinued(mode, item, nex, depth);

        if (AUTO_INDENT) {
            adjustLabelIndent(item);
        }

        return simplify(item, pItem);
    }

    public static TreeItemFennec translate(FennecMode mode, TreeItemFennec pItem, Node n) {

        return buildTreeItem(mode, pItem, n, 1);
    }

    public static TreeItemFennec translateContinued(FennecMode mode, TreeItemFennec item, Node n) {
        if (!(n instanceof INodeEx))
            return item;
        buildTreeItemContinued(mode, item, (INodeEx) n, 2);
        // item = simplify(item, (TreeItemFennec) item.getParent());
        return item;
    }
}
