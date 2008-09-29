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

package org.eclipse.actf.ai.navigator.impl;

import org.eclipse.actf.ai.internal.navigator.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;




public class FormInputDialog extends Dialog {
    private String formInputString;

    private Text formInputField;

    private boolean multi;

    private boolean pass;

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        
        if(multi)
            newShell.setText(Messages.getString("FormInputDialog.Textarea")); //$NON-NLS-1$
        else if(pass)
            newShell.setText(Messages.getString("FormInputDialog.Password")); //$NON-NLS-1$
        else
            newShell.setText(Messages.getString("FormInputDialog.Text")); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridData gd;
        if (multi) {
            formInputField = new Text(container, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
            gd = new GridData(GridData.FILL_BOTH);
            gd.widthHint = 400;
            gd.heightHint = 200;

            formInputField.addKeyListener(new KeyListener(){
                public void keyPressed(KeyEvent e) {
                    if (e.keyCode == java.awt.event.KeyEvent.VK_TAB) {
                       formInputField.traverse(SWT.TRAVERSE_TAB_NEXT);
                       e.doit = false;
                    }
                }
                public void keyReleased(KeyEvent e) {
                }
            });
        } else {
            if (pass)
                formInputField = new Text(container, SWT.BORDER | SWT.PASSWORD);
            else
                formInputField = new Text(container, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 400;
        }
        gd.horizontalSpan = ((GridLayout) parent.getLayout()).numColumns;
        formInputField.setLayoutData(gd);
        formInputField.setText(formInputString);

        formInputField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                formInputString = formInputField.getText();
            }
        });
        return container;
    }

    String getResult() {
        return formInputString;
    }

    FormInputDialog(Shell parent, String init, boolean multi, boolean pass){
        super(parent);
        this.formInputString = init;
        this.multi = multi;
        this.pass = pass;
    }
}
