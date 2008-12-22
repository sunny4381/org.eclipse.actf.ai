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
package org.eclipse.actf.ai.navigator.views;

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.internal.navigator.Messages;
import org.eclipse.actf.ai.internal.navigator.NavigatorPlugin;
import org.eclipse.actf.ai.navigator.IManipulator;
import org.eclipse.actf.ai.navigator.ui.ModeContribution;
import org.eclipse.actf.ai.navigator.ui.NavigatorUIUtil;
import org.eclipse.actf.util.vocab.Vocabulary;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class NavigatorTreeView extends ViewPart {
    private TreeViewer treeViewer;

    private TableViewer tableViewer;

    private boolean disposed;

    static class ITreeItemLabelProvider extends LabelProvider {
        @Override
        public Image getImage(Object element) {
            // TODO
            return null;
        }

        @Override
        public String getText(Object element) {
            ITreeItem item = (ITreeItem) element;
            String r = item.getUIString();
            if (r.length() > 0)
                return r;
            r = item.getNodeString();
            if (r == null)
                return ""; //$NON-NLS-1$
            return r;
        }
    }

    static class ITreeItemContentProvider extends ArrayContentProvider implements ITreeContentProvider {

        public Object[] getChildren(Object parentElement) {
            ITreeItem item = (ITreeItem) parentElement;
            return item.getChildItems();
        }

        public Object getParent(Object element) {
            ITreeItem item = (ITreeItem) element;
            return item.getParent();
        }

        public boolean hasChildren(Object element) {
            ITreeItem item = (ITreeItem) element;
            ITreeItem[] childItems = item.getChildItems();
            if ((childItems != null) && (childItems.length > 0))
                return true;
            return false;
        }

        @Override
        public Object[] getElements(Object input) {
            return getChildren(input);
        }
    }

    static class TreeItemDetailsContentProvider implements IStructuredContentProvider {
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        public void dispose() {
        }

        private String calcPath(ITreeItem item) {
            StringBuffer ret = new StringBuffer();
            Object o = item.getBaseNode();
            if (!(o instanceof Node))
                return "#none"; //$NON-NLS-1$
            for (Node n = (Node) o; n != null; n = n.getParentNode()) {
                if (n instanceof Document)
                    break;
                ret.insert(0, n.getNodeName());
                ret.insert(0, "/"); //$NON-NLS-1$
            }
            return ret.toString();
        }

        private String calcID(ITreeItem item) {
            Object o = item.getBaseNode();
            if (!(o instanceof Element))
                return ""; //$NON-NLS-1$
            Element e = (Element) o;
            return e.getAttribute("id"); //$NON-NLS-1$
        }

        private String formatStringInHex(String str) {
            StringBuffer ret = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                ret.append(Integer.toHexString(c));
                ret.append(" "); //$NON-NLS-1$
            }
            return ret.toString();
        }

        public Object[] getElements(Object obj) {
            ITreeItem item = (ITreeItem) obj;
            String[] uiStrings = { "UIString", item.getUIString() }; //$NON-NLS-1$
            String[] uiStringsInHex = { "UIString (HEX)", formatStringInHex(item.getUIString()) }; //$NON-NLS-1$
            String[] nodeStrings = { "Node", item.getNodeString() }; //$NON-NLS-1$
            String[] path = { "Path", calcPath(item) }; //$NON-NLS-1$
            String[] id = { "ID", calcID(item) }; //$NON-NLS-1$
            String[] hasContent = { "hasContent", "" + Vocabulary.hasContent().eval(item)}; //$NON-NLS-1$ //$NON-NLS-2$
            String[] heading = { "Heading", "" + item.getHeadingLevel() }; //$NON-NLS-1$ //$NON-NLS-2$
            String[] nodeObject = { "NodeObject", "" + item.getBaseNode() }; //$NON-NLS-1$ //$NON-NLS-2$
            String[] isInputable = { "isInputable", "" + item.isInputable() }; //$NON-NLS-1$ //$NON-NLS-2$
            String[] isConnectable = { "isConnectable", "" + Vocabulary.isConnectable().eval(item) }; //$NON-NLS-1$ //$NON-NLS-2$
            return new Object[] { uiStrings, uiStringsInHex, nodeStrings, path, id, hasContent, heading, nodeObject,
                                  isInputable, isConnectable };
        }
    }

    static class TreeItemDetailsLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object obj, int index) {
            String[] prop = (String[]) obj;
            if (index >= prop.length)
                return null;
            return prop[index];
        }

        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }

        @Override
        public Image getImage(Object obj) {
            // TODO
            return null;
        }
    }

    class TreeItemViewerDoubleClickListener implements IDoubleClickListener {
        public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            Object obj = sel.getFirstElement();
            tableViewer.setInput(obj);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        disposed = true;
    }

    void initView(Composite parent) {
        Layout layout = new FillLayout(SWT.VERTICAL);
        parent.setLayout(layout);
        
        SashForm sash = new SashForm(parent, SWT.VERTICAL);
        
        treeViewer = new TreeViewer(sash);
        treeViewer.setLabelProvider(new ITreeItemLabelProvider());
        treeViewer.setContentProvider(new ITreeItemContentProvider());
        treeViewer.addDoubleClickListener(new TreeItemViewerDoubleClickListener());
        // treeViewer.setAutoExpandLevel(0);

        tableViewer = new TableViewer(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn tableColumnProperty = new TableColumn(table, SWT.LEFT);
        tableColumnProperty.setText(Messages.NavigatorTreeView_property); 
        tableColumnProperty.setWidth(70);
        TableColumn tableColumnValue = new TableColumn(table, SWT.LEFT);
        tableColumnValue.setText(Messages.NavigatorTreeView_value); 
        tableColumnValue.setWidth(100);

        tableViewer.setLabelProvider(new TreeItemDetailsLabelProvider());
        tableViewer.setContentProvider(new TreeItemDetailsContentProvider());

        sash.setWeights(new int[]{90, 10});
    }

    public NavigatorTreeView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        initView(parent);
        NavigatorPlugin.getDefault().setNavigatorTreeView(this);
    }

    @Override
    public void setFocus() {
    }

    private ITreeItem getRoot(ITreeItem item) {
        for (ITreeItem parent = item.getParent(); parent != null; parent = item.getParent()) {
            item = parent;
        }
        return item;
    }

    public void showItem(ITreeItem item) {
        if (disposed) return;
        if (treeViewer.getControl().isDisposed()) return;
        treeViewer.setInput(getRoot(item));
        treeViewer.reveal(item);
        ISelection is = new StructuredSelection(item);
        treeViewer.setSelection(is, true);
    }

    public void clearItem() {
        if (disposed) return;
        treeViewer.setInput(null);
    }

    public boolean isShown() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart part = page.findView(NavigatorUIUtil.NAVIGATOR_TREE_VIEW_ID);
        return part != null;
    }

    public boolean toggleViewShowing() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart part = page.findView(NavigatorUIUtil.NAVIGATOR_TREE_VIEW_ID);

        if (part != null) {
            page.hideView(part);
            return false;
        } else {
            try {
                page.showView(NavigatorUIUtil.NAVIGATOR_TREE_VIEW_ID);
                return true;
            } catch (PartInitException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private ModeContribution getModeContribution(String id) {
        IViewSite viewSite = getViewSite();
        IActionBars actionBars = viewSite.getActionBars();
        IStatusLineManager manager = actionBars.getStatusLineManager();
        return (ModeContribution) manager.find(id);
    }
    private void update(){
        IViewSite viewSite = getViewSite();
        IActionBars actionBars = viewSite.getActionBars();
        IStatusLineManager manager = actionBars.getStatusLineManager();
        manager.update(true);
    }


    public void setMode(IManipulator.Mode mode) {
        ModeContribution mc = getModeContribution(ModeContribution.MODE_CONTRIBUTION_ID);
        if (mc != null) {
            mc.setMode(mode.name);
            update();
        }
    }

    public void showFennecName(String name) {
        ModeContribution mc = getModeContribution(ModeContribution.MODE_CONTRIBUTION_ID);
        if (mc != null) {
            mc.showFennecName(name);
            update();
        }
    }
}
