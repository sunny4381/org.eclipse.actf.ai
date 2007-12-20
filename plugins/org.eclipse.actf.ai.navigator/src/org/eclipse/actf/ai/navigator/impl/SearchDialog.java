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

import org.eclipse.actf.ai.navigator.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;




public class SearchDialog extends Dialog {
    private static String formInputString = "";

    private Text searchField;
    
    private Button forwardRadio; 
    
    private Button backwardRadio; 
    
    private Button exactButton;
    
    private static boolean isForward = true;
    
    private static boolean exact = false;
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        newShell.setText(Messages.getString("FormInputDialog.Search"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridData gd;
        searchField = new Text(container, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 400;
        gd.horizontalSpan = ((GridLayout) parent.getLayout()).numColumns;
        searchField.setLayoutData(gd);
        searchField.setText(formInputString);
        searchField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                formInputString = searchField.getText();
            }
        });
        
        backwardRadio = new Button(container, SWT.RADIO);
        backwardRadio.setSelection(!isForward);
        backwardRadio.setText(Messages.getString("FormInputDialog.Backward"));
        backwardRadio.addSelectionListener(new SelectionListener(){
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                isForward = false;
            }
        });
        
        forwardRadio = new Button(container, SWT.RADIO);
        forwardRadio.setText(Messages.getString("FormInputDialog.Forward"));
        forwardRadio.setSelection(isForward);
        forwardRadio.addSelectionListener(new SelectionListener(){
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                isForward = true;
            }
        });

        exactButton = new Button(container, SWT.CHECK);
        exactButton.setSelection(exact);
        exactButton.setText(Messages.getString("FormInputDialog.Exact"));
        exactButton.addSelectionListener(new SelectionListener(){
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                exact = exactButton.getSelection();
            }
        });
        searchField.setFocus();
        searchField.setSelection(0, formInputString.length());
        
        return container;
    }

    String getString() {
        return formInputString;
    }
    
    boolean isForward(){
        return isForward;
    }
    
    boolean isExact(){
        return exact;
    }

    SearchDialog(Shell parent) {
        super(parent);
    }

    public void setForward(boolean b) {
        isForward = b;
    }
}
