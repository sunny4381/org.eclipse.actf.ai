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



/**
 * This shows the mode information of the navigator.
 */
public class ModeContribution extends ControlContribution {

    /**
     * Default constructor.
     */
    public ModeContribution() {
        super(MODE_CONTRIBUTION_ID);
    }

    /**
     * The ID of this UI contribution.
     */
    public static final String MODE_CONTRIBUTION_ID = "navigator.mode";

    private Composite c;

    private Label fennec;
    
    private String metadataText;

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

        fennec = new Label(c, SWT.NONE);
        if(metadataText != null)
            fennec.setText(metadataText);
        mode = new Label(c, SWT.NONE);
        if(modeText != null)
            mode.setText(modeText);
        
        return c;
    }

    /**
     * <i>metadataText  [modeText]</i> will be shown.
     * @param name The mode name.
     */
    public void setMode(String name) {
        modeText = "  [" + name + "]";
    }

    /**
     * <i>metadataText  [modeText]</i> will be shown.
     * @param name The name of the metadata name.
     */
    public void showFennecName(String name) {
        metadataText = name;
    }
}
