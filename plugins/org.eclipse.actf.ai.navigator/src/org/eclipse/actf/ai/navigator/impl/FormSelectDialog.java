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

package org.eclipse.actf.ai.navigator.impl;

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.internal.navigator.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




public class FormSelectDialog extends Dialog {
    ITreeItem selectItem;

    private int[] selectedIndices;
    
    private String[] names;
    
    boolean multiple;

    List list;

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        if (multiple) 
            newShell.setText(Messages.getString("FormSelectDialog.Multiple")); //$NON-NLS-1$
        else
            newShell.setText(Messages.getString("FormSelectDialog.Single")); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridData gd;
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 200;
        gd.heightHint = 200;
        gd.horizontalSpan = ((GridLayout) parent.getLayout()).numColumns;

        if (multiple) 
            list = new List(container, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        else  
            list = new List(container, SWT.V_SCROLL | SWT.BORDER);

        list.setLayoutData(gd);

        Object base = selectItem.getBaseNode();
        if (base instanceof Node) {
            Node node = (Node) base;
            NodeList nl = node.getChildNodes();
            
            for (int i = 0; i < nl.getLength(); i++) {
                Node child = nl.item(i);
                
                if ("OPTION".equals(child.getNodeName())) {
                    Node c = child.getFirstChild();
                    String text = "";
                    if (c != null)
                        text = c.getNodeValue();
                    list.add(text);
                }
            }
        }
        
        names = new String[list.getItemCount()];
        for (int i = 0; i < names.length; i++)
            names[i] = list.getItem(i);

        if (selectedIndices != null) {
            list.select(selectedIndices);
        }
        
        list.addSelectionListener(new SelectionListener(){
            public void widgetSelected(SelectionEvent e) {
                selectedIndices = list.getSelectionIndices();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
                
                selectedIndices = list.getSelectionIndices();
            }
        });
        return container;
    }

    FormSelectDialog(Shell parent, ITreeItem item, boolean multi) {
        super(parent);
        this.selectItem = item;
        this.multiple = multi;
        this.selectedIndices = item.getSelectedIndices();
    }

    public int[] getSelectedIndices() {
        return selectedIndices;
    }
    
    public String getTextAt(int index) {
        if (0 <= index && index < names.length)
            return names[index];
        return "";
    }

    public int getLength() {
        return names.length;
    }
}
