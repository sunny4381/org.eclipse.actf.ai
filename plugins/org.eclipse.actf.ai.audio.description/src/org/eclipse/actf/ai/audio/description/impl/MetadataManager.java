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

import org.eclipse.actf.ai.audio.description.IMetadata;
import org.eclipse.actf.ai.audio.description.IMetadataProvider;
import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.ai.internal.audio.description.DescriptionPlugin;
import org.eclipse.actf.ai.navigator.IMediaControl.IHandle;

public class MetadataManager {

	private IMetadataProvider metadataProvider;

	private int index;

	private int oldIndex;

	private int forceFlag = IMetadata.MASK_NONE;

	private IHandle handle;

	private IVideoControl video;

	@SuppressWarnings("unused")
	private ISoundControl sound;

	private boolean stopFlag = false;

	public void setForceFlag(int flag) {
		forceFlag = flag;
	}

	public MetadataManager(IHandle handle, IMetadataProvider metadataProvider) {
		this.handle = handle;
		this.video = handle.getVideoControl();
		this.sound = handle.getSoundControl();

		this.metadataProvider = metadataProvider;
		index = oldIndex = -1;
	}

	private boolean pauseBeforeFlag = false;

	private boolean speakFlag = false;

	private boolean pauseAfterFlag = false;

	public void process(double time) {
		if (stopFlag)
			return;

		DescriptionPlugin.getDefault().getDescriptionView().setTime(time);

		if (metadataProvider == null)
			return;
		index = metadataProvider.getIndex((int) (time * 100));
		// System.out.println(time+",
		// "+metadataProvider.getItem(index).getTime());

		if (oldIndex != index && oldIndex < index) {
			int type = metadataProvider.getItem(index).getType();

			pauseBeforeFlag = isIt(type | forceFlag,
					IMetadata.MASK_PAUSE_BEFORE);
			speakFlag = isIt(type | forceFlag, IMetadata.MASK_SPEAK);
			pauseAfterFlag = isIt(type | forceFlag, IMetadata.MASK_PAUSE_AFTER);
		}

		if (pauseBeforeFlag) {
			pauseBeforeFlag = false;
			processPause();
		}
		if (speakFlag) {
			speakFlag = false;
			processSpeak();
		}
		if (pauseAfterFlag) {
			pauseAfterFlag = false;
			processPlay();
		}

		oldIndex = index;
	}

	private boolean isIt(int type, int pause) {
		return (type & pause) == pause;
	}

	private void processPause() {
		if (DescriptionPlugin.getDefault().getEnable())
			video.pauseMedia();
	}

	private void processPlay() {
		if (DescriptionPlugin.getDefault().getEnable())
			video.playMedia();
	}

	private void processSpeak() {
		if (DescriptionPlugin.getDefault().getEnable()) {

			/*
			 * int[] volumes = null; volumes = sound.getVolumes(); int[]
			 * volumes2 = new int[volumes.length]; for(int i=0; i<volumes2.length;
			 * i++) volumes2[i] = 200; sound.setVolumes(volumes2);
			 */

			String desc = metadataProvider.getItem(index).getDescription();
			say(desc);
		}
	}

	public void say(String str) {
		// System.out.println(str);
		DescriptionPlugin plugin = DescriptionPlugin.getDefault();

		if (plugin.canSpeak()) {
			plugin.speak(str);
		} else {
			handle.getVoice().speak(str, true);
		}
	}

	public void stop() {
		stopFlag = true;
	}

	public void start() {
		stopFlag = false;
		DescriptionPlugin.getDefault().getDescriptionView().setInput(
				metadataProvider);
	}

	public boolean hasMetadata() {
		if (metadataProvider == null)
			return false;
		return metadataProvider.hasMetadata();
	}

	public Object getMetadataProvider() {
		return metadataProvider;
	}
}
