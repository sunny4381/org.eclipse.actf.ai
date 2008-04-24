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

package org.eclipse.actf.ai.audio.description.impl;

import org.eclipse.actf.ai.fennec.treemanager.IMediaSyncEventListener;
import org.eclipse.actf.ai.navigator.IMediaControl.IHandle;

public class MediaSyncEventListener implements IMediaSyncEventListener {

	private static final double EVENT_INTERVAL = 0.1; // 100ms

	private IHandle handle;
	private MetadataManager manager;

	public MediaSyncEventListener(IHandle handle, MetadataManager manager) {
		this.handle = handle;
		this.manager = manager;
	}

	public void run() {
		if (handle.getVideoControl() != null) {
			double time = handle.getVideoControl().getCurrentPosition();
			manager.process(time);
		}
	}

	public double getInterval() {
		return EVENT_INTERVAL;
	}
}
