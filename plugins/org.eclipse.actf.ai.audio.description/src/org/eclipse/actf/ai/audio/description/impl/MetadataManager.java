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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.actf.ai.audio.description.IMetadata;
import org.eclipse.actf.ai.audio.description.IMetadataProvider;
import org.eclipse.actf.ai.audio.io.AudioFactory;
import org.eclipse.actf.ai.audio.io.AudioPipeListener;
import org.eclipse.actf.ai.audio.io.IAudioPipe;
import org.eclipse.actf.ai.audio.io.IAudioReader;
import org.eclipse.actf.ai.audio.io.IAudioWriter;
import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.ai.internal.audio.description.DescriptionPlugin;
import org.eclipse.actf.ai.navigator.IMediaControl.IHandle;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.model.dom.dombycom.AnalyzedResult;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.eclipse.actf.model.dom.dombycom.INodeExVideo;
import org.eclipse.actf.model.ui.util.ModelServiceUtils;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MetadataManager implements IVoiceEventListener, AudioPipeListener {

	private IMetadataProvider metadataProvider;

	private IAudioPipe audio;

	private IAudioWriter writer = AudioFactory.createDefaultWriter();

	private int index;

	private int oldIndex;

	private int forceFlag = IMetadata.MASK_NONE;

	private IHandle handle;

	private IVideoControl video;

	@SuppressWarnings("unused")
	private ISoundControl sound;

	private boolean stopFlag = false;

	private int voiceIndex = -1;

	private static boolean requestAdditions = false;

	private boolean restart = false;

	public void setForceFlag(int flag) {
		forceFlag = flag;
	}

	private AnalyzedResult analyzedResult;

	public MetadataManager(IHandle handle, IMetadataProvider metadataProvider) {
		this.handle = handle;
		this.video = handle.getVideoControl();
		this.sound = handle.getSoundControl();

		this.metadataProvider = metadataProvider;
		index = oldIndex = -1;

		final IHandle handle2 = handle;
		Thread thread = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (restart) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								System.out.println("play");
								handle2.getVideoControl().playMedia();
							}
						});
						restart = false;
					}
				}
			};
		};
		thread.start();

		DescriptionPlugin.getDefault().setVoiceEventListener(this);
	}

	private boolean pauseBeforeFlag = false;

	private boolean speakFlag = false;

	private boolean pauseAfterFlag = false;

	private boolean pauseDualingSpeakFlag = false;

	//experimental
	private String addition = "";
	private int additionSpeed;
	private String additionGender;
	private boolean additionFlag;

	public void process(double time) {
		if (stopFlag)
			return;

		DescriptionPlugin.getDefault().getDescriptionView().setTime(time);

		if (metadataProvider == null)
			return;
		index = metadataProvider.getIndex((int) (time * 100));
		// System.out.println(time+",
		// "+metadataProvider.getItem(index).getTime());

		IMetadata curMetadata = null;

		if (oldIndex != index && oldIndex < index) {
			curMetadata = metadataProvider.getItem(index);
			int type = curMetadata.getType();

			pauseBeforeFlag = isIt(type | forceFlag,
					IMetadata.MASK_PAUSE_BEFORE);
			speakFlag = isIt(type | forceFlag, IMetadata.MASK_SPEAK);
			pauseAfterFlag = isIt(type | forceFlag, IMetadata.MASK_PAUSE_AFTER);
			pauseDualingSpeakFlag = isIt(type | forceFlag,
					IMetadata.MASK_PAUSE_DURING_SPEAK);
		}

		if (pauseBeforeFlag || pauseDualingSpeakFlag) {
			pauseBeforeFlag = false;
			voiceIndex = 100;
			processPause();
		}
		if (speakFlag) {
			speakFlag = false;

			requestAdditions = false;

			/*
			 * if (null != curMetadata && curMetadata.hasAddition()) {
			 * additionFlag = true; voiceIndex = 200; addition =
			 * curMetadata.getAddition(); additionSpeed =
			 * curMetadata.getAdditionSpeed(); additionGender =
			 * curMetadata.getAdditionGender(); }
			 */

			processSpeak();
		}
		if (pauseAfterFlag) {
			pauseAfterFlag = false;
			processPause();
		}
		if (pauseDualingSpeakFlag) {
			pauseDualingSpeakFlag = false;
			// processPlay();
		}

		oldIndex = index;
	}

	private boolean isIt(int type, int pause) {
		return (type & pause) == pause;
	}

	private void analyze() {
		// TODO tentative code
		analyzedResult = new AnalyzedResult();
		Document doc = ModelServiceUtils.getActiveModelService()
				.getLiveDocument();
		Node root = null;
		if (doc != null)
			root = doc.getFirstChild();
		if (root instanceof INodeEx) {
			analyzedResult = ((INodeEx) root).analyze(analyzedResult);
		}
	}

	private void processPause() {
		if (DescriptionPlugin.getDefault().getEnable())
			handle.getVideoControl().pauseMedia();
		// pauseMedia();
	}

	private void processPlay() {
		if (DescriptionPlugin.getDefault().getEnable())
			handle.getVideoControl().playMedia();
		// playMedia();
	}

	private void processSpeak() {
		if (DescriptionPlugin.getDefault().getEnable()) {

			/*
			 * int[] volumes = null; volumes = sound.getVolumes(); int[]
			 * volumes2 = new int[volumes.length]; for(int i=0;
			 * i<volumes2.length; i++) volumes2[i] = 200;
			 * sound.setVolumes(volumes2);
			 */
			IMetadata metadata = metadataProvider.getItem(index);

			if (metadata.hasValidWav()) {
				if (audio != null && audio.isActive()) {
					// TODO
					audio.stop();
				}

				// TODO check
				IAudioReader reader;
				try {
					reader = AudioFactory.createAudioReader(new URL(metadata
							.getWavLocal()));
					audio = AudioFactory.createAudioPipe(reader, writer);
					audio.setBufferSize(100);
					audio.setInterval(1);
					audio.prepare();
					audio.addAudioPipeListener(this);
					audio.start();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				say(metadata.getDescription(), metadata.getSpeed(), metadata
						.getGender());

			}
		}
	}

	private void processAdditionSpeak() {
		if (DescriptionPlugin.getDefault().getEnable()) {
			say(addition, additionSpeed, additionGender);
		}
	}

	public void say(String str, int speed, String gender) {
		// System.out.println(str);
		DescriptionPlugin plugin = DescriptionPlugin.getDefault();
		if (plugin.canSpeak()) {
			plugin.speak(str, speed, gender);
			if (voiceIndex != -1) {
				plugin.addSpeakIndex(voiceIndex);
			}
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

	// TODO tentative from here
	// TODO cache
	public boolean pauseMedia() {
		analyze();
		INodeExVideo[] videos = analyzedResult.getVideoNodes();
		boolean r = true;
		for (int i = 0; i < videos.length; i++) {
			r &= videos[i].pauseMedia();
		}
		return r;
	}

	public boolean playMedia() {
		analyze();
		INodeExVideo[] videos = analyzedResult.getVideoNodes();
		boolean r = true;
		for (int i = 0; i < videos.length; i++) {
			r &= videos[i].playMedia();
		}
		return r;
	}

	// tentative end here

	public void indexReceived(int index) {
		if (100 == index) {
			// playMedia();
			handle.getVideoControl().playMedia();
			voiceIndex = -1;
		} else if (200 == index && additionFlag) {
			additionFlag = false;
			if (requestAdditions) {
				voiceIndex = 100;
				handle.getVideoControl().pauseMedia();
				// pauseMedia();
				processAdditionSpeak();
				requestAdditions = false;
			} else {
				voiceIndex = -1;
			}
		}
	}

	// TODO
	public static void requestAdditions() {
		requestAdditions = true;
	}

	public void finished(IAudioPipe pipe) {
		if (voiceIndex == 100) {
			restart = true;
		}
	}

	public void stopped(IAudioPipe pipe) {
		System.out.println("stop");
	}

}
