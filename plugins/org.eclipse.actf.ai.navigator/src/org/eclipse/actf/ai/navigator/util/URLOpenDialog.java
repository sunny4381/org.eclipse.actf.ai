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
package org.eclipse.actf.ai.navigator.util;

import org.eclipse.actf.ai.internal.navigator.Messages;
import org.eclipse.actf.ui.util.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;




/**
 * This is a dialog to ask the user to input URL.
 */
public class URLOpenDialog {

	private Shell _shell;

	private Text _urlText;
    
    private String _url = "";

	private int _returnCode = 0;

	// Checked:030806
	/**
	 * Constructor.
	 * @param shell The shell to be parent of the dialog.
	 */
	public URLOpenDialog(Shell shell) {
		this._shell = new Shell(shell, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		this._shell.setLayout(new GridLayout());
	}

	// Checked:030806
	private void createButtonControls() {
		Composite composite = new Composite(this._shell, SWT.NULL);

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_END);
		gridData.heightHint = 50;
		composite.setLayoutData(gridData);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.horizontalSpacing = 20;
		layout.marginWidth = 20;
		layout.marginHeight = 10;
		composite.setLayout(layout);

		Button okButton = new Button(composite, SWT.PUSH);
		okButton.setText(IDialogConstants.OK); //$NON-NLS-1$
		okButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
				_returnCode = 1;
                _url = _urlText.getText();
//				BrowserEventListenerManager.getInstance().fireSetAddressTextString(_url);
				_shell.close();
			}
		});

		Button cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText(IDialogConstants.CANCEL); //$NON-NLS-1$
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
            public void widgetSelected(SelectionEvent e) {
				_returnCode = 0;
				_shell.close();
			}
		});

		this._shell.setDefaultButton(okButton);
	}

	// Checked:030806
	private void createSettingControls() {
		GridLayout gridLayout1;

		Composite composite = new Composite(_shell, SWT.NULL);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		composite.setLayout(gridLayout1);

		// information
		Label infoLabel = new Label(composite, SWT.NONE);
		infoLabel.setText(IDialogConstants.OPENFILE_INFO);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		infoLabel.setLayoutData(gridData);

		// URL label
		Label label1 = new Label(composite, SWT.NONE);
		label1.setText("URL: ");

		// Create the TextBox
		_urlText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		_urlText.setText("");

		gridData = new GridData();
		gridData.widthHint = 300;
		_urlText.setLayoutData(gridData);
	}

	// Checked:030806
	/**
	 * Open the dialog and return 1 or 0 when the user click OK button or cancel button respectively.
	 * @return 1 (OK button), 0 (Cancel button)
	 */
	public int open() {
		this._shell.setText(Messages.DialogOpenURL_Open_URL_2);

		createSettingControls();

		createButtonControls();
		this._shell.setSize(375, 150);
		this._shell.open();
		this._shell.setLocation(100, 100);

		Display display = _shell.getDisplay();
		while (!_shell.isDisposed() || !display.readAndDispatch()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		return this._returnCode;
	}
    
    /**
     * @return The URL input by the user.
     */
    public String getUrl(){
        return this._url;
    }
}
