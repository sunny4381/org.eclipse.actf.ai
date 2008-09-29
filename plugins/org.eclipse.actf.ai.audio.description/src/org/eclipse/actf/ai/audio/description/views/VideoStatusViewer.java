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

package org.eclipse.actf.ai.audio.description.views;

import org.eclipse.actf.ai.audio.description.util.TimeFormatUtil;
import org.eclipse.actf.ai.internal.audio.description.DescriptionPlugin;
import org.eclipse.actf.ai.internal.audio.description.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class VideoStatusViewer {

	private Composite parent;
	private Label status;
	private boolean enable = false;
	private String timeText;

	public VideoStatusViewer(Composite parent) {
		this.parent = parent;
		initialize();
	}

	public void initialize() {
		status = new Label(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		status.setLayoutData(data);
		status.setAlignment(SWT.RIGHT);
		setTime(0);
		enable = DescriptionPlugin.getDefault().getEnable();
	}

	public void setTime(double time) {
		timeText = TimeFormatUtil.getTimeString(time);
		refreshText();
	}

	private void refreshText() {
		if (status.isDisposed())
			return;
		if (enable) {
			status.setText(Messages.getString("AudioDescription.view.enable")
					+ " " + timeText);
		} else {
			status.setText(Messages
					.getString("AudioDescription.view.notEnable")
					+ " " + timeText);
		}
	}

	public void setEnable(boolean b) {
		enable = b;
		refreshText();
	}
}
