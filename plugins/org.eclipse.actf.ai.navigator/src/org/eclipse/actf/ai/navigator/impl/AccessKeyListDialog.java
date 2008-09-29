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

import org.eclipse.actf.ai.fennec.treemanager.IAccessKeyList;
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




public class AccessKeyListDialog extends Dialog {
    IAccessKeyList accessKeyList;

    private char selectedKey = 0;

    private String[] names;

    List list;

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        
        newShell.setText(Messages.getString("AccessKeyListDialog.Title")); //$NON-NLS-1$

    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridData gd;
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 200;
        gd.heightHint = 200;
        gd.horizontalSpan = ((GridLayout) parent.getLayout()).numColumns;

        list = new List(container, SWT.V_SCROLL | SWT.BORDER);

        list.setLayoutData(gd);

        for (int i = 0; i < accessKeyList.size(); i++) {
            char key = accessKeyList.getAccessKeyAt(i);
            String str = accessKeyList.getUIStringAt(i);
            list.add(key+" "+str);
        }

        names = new String[list.getItemCount()];
        for (int i = 0; i < names.length; i++)
            names[i] = list.getItem(i);

        list.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                selectedKey = accessKeyList.getAccessKeyAt(list.getSelectionIndex());
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                selectedKey = accessKeyList.getAccessKeyAt(list.getSelectionIndex());
            }
        });
        return container;
    }

    AccessKeyListDialog(Shell parent, IAccessKeyList list) {
        super(parent);
        this.accessKeyList = list;
    }

    public char getSelectedKey() {
        return selectedKey;
    }
}
