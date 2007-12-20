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

package org.eclipse.actf.ai.navigator.ui;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;



public class ModeContribution extends ControlContribution {

    public ModeContribution(String id) {
        super(id);
    }

    public static final String MODE_CONTRIBUTION_ID = "navigator.mode";

    private Composite c;

    private Label nvm3;
    
    private String nvm3Text;

    private Label mode;
    
    private String modeText;

    private StatusLineLayoutData data;

    @Override
    protected Control createControl(Composite parent) {
        data = new StatusLineLayoutData();
        data.heightHint = 18;
        c = new Composite(parent, SWT.NULL);
        c.setLayoutData(data);
        RowLayout rl = new RowLayout();
        rl.fill = true;
        rl.justify = true;
        rl.wrap = false;
        c.setLayout(rl);

        nvm3 = new Label(c, SWT.NONE);
        if(nvm3Text != null)
            nvm3.setText(nvm3Text);
        mode = new Label(c, SWT.NONE);
        if(modeText != null)
            mode.setText(modeText);
        
        return c;
    }

    public void setMode(String name) {
        modeText = "  [" + name + "]";
    }

    public void showNVM3Name(String name) {
        nvm3Text = name;
    }
}
