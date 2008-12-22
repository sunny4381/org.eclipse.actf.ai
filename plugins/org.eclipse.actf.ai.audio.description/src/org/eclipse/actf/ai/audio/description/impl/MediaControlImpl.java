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

import java.util.HashMap;

import org.eclipse.actf.ai.audio.description.IMetadataProvider;
import org.eclipse.actf.ai.internal.audio.description.DescriptionPlugin;
import org.eclipse.actf.ai.internal.audio.description.Messages;
import org.eclipse.actf.ai.navigator.IMediaControl;

public class MediaControlImpl implements IMediaControl {

	private HashMap<IHandle, MetadataManager> map = new HashMap<IHandle, MetadataManager>();
	// private List<MetadataManager> managers = new Vector<MetadataManager>();

	MetadataManager manager;
	MetadataManager oldManager;

	IHandle old = null;

	IHandle handle;

	public void dispose(IHandle handle) {
	}

	public void start(IHandle handle) {
		// if (handle.getSoundControl() == null || handle.getVideoControl() ==
		// null)
		// return;
		initialize(handle);
	}

	private void initialize(IHandle handle) {
		this.handle = handle;
		MetadataManager temp = map.get(handle);
		IMetadataProvider provider = null;

		if (temp == null) {
			String url = handle.getWebBrowser().getURL();
			provider = DescriptionPlugin.getDefault().getMetadata(url);
			temp = new MetadataManager(handle, provider);

			if (handle.getVideoControl() != null)
				handle.getVideoControl().addEventListener(
						new MediaSyncEventListener(handle, temp));
			// System.out.println(temp);
			// map.put(handle, temp);
		}
		if (manager != null) {
			manager.stop();
		}
		manager = temp;
		manager.start();
		DescriptionPlugin.getDefault().setActiveMetadataProvider(provider);

		if (manager.hasMetadata()) {
			if (oldManager != null) {
				System.out.println(oldManager.getMetadataProvider());
				System.out.println(manager.getMetadataProvider());
			}
			if (oldManager == null
					|| oldManager.getMetadataProvider() != manager
							.getMetadataProvider()) {
				if (handle.getVoice() != null) {
					// speakInfo(false);
				}
				oldManager = manager;
			}
		} else {
			// handle.getVoice().speak(Messages.getString("Metadata.notAvailable"),
			// false);
		}
	}

	public void speakInfo(boolean flush) {
		DescriptionPlugin plugin = DescriptionPlugin.getDefault();
		if (!plugin.isAvailable())
			return;
		if (handle.getVoice() == null)
			return;

		if (plugin.getEnable()) {
			handle.getVoice().speak(Messages.AudioDescription_on,
					flush);
		} else {
			handle.getVoice().speak(Messages.AudioDescription_off,
					flush);
		}
	}

	public int toggleEnabled() {
		DescriptionPlugin plugin = DescriptionPlugin.getDefault();
		return plugin.toggleEnable();
	}

	public boolean isAvailable() {
		DescriptionPlugin plugin = DescriptionPlugin.getDefault();
		return plugin.isAvailable();
	}

	public boolean toggleViewShowing() {
		DescriptionPlugin plugin = DescriptionPlugin.getDefault();
		return plugin.getDescriptionView().toggleViewShowing();
	}

	public boolean isEnabled() {
		DescriptionPlugin plugin = DescriptionPlugin.getDefault();
		return plugin.getEnable();
	}
}
