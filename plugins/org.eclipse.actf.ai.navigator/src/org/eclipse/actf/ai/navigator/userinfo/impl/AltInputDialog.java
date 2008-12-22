/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Masatomo KOBAYASHI - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.navigator.userinfo.impl;

import org.eclipse.actf.ai.internal.navigator.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;




public class AltInputDialog extends Dialog {
    
    private String formInputString = "";

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        newShell.setText(Messages.AltInputDialog_Text);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 400;
        gd.horizontalSpan = ((GridLayout) parent.getLayout()).numColumns;
        
        final Text formInputField = new Text(container, SWT.BORDER);
        formInputField.setLayoutData(gd);
        formInputField.setText(formInputString);
        formInputField.selectAll();

        formInputField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                formInputString = formInputField.getText();
            }
        });
        return container;
    }

    public String getResult() {
        return formInputString;
    }

    public AltInputDialog(Shell parent, String init) {
        super(parent);
        this.formInputString = init;
    }
}
