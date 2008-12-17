/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.tts.sapi.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.actf.ai.tts.ISAPIEngine;
import org.eclipse.actf.ai.tts.TTSRegistry;
import org.eclipse.actf.ai.tts.sapi.SAPIPlugin;
import org.eclipse.actf.ai.tts.sapi.engine.SapiVoice;
import org.eclipse.actf.ai.tts.sapi.engine.SpObjectToken;
import org.eclipse.actf.ai.tts.sapi.engine.SpeechObjectTokens;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.ole.win32.Variant;

public class SapiTestManager {

	private static SapiTestManager instance;

	private SapiVoice sapiVoice;
	private String[][] voiceNames;
	private String[][] audioOutputNames;

	private static final String SAMPLE_TEXT = "Hello. This is test."; //$NON-NLS-1$

	public SapiTestManager() {
		sapiVoice = (SapiVoice) TTSRegistry.createTTSEngine(SapiVoice.ID);
		Variant varVoices = sapiVoice.getVoices(null, null);
		if (null != varVoices) {
			SpeechObjectTokens voiceTokens = SpeechObjectTokens
					.getTokens(varVoices);
			if (null != voiceTokens) {
				String exclude = Platform.getResourceString(SAPIPlugin
						.getDefault().getBundle(), "%voice.exclude"); //$NON-NLS-1$
				List<String[]> voiceList = new ArrayList<String[]>();
				int count = voiceTokens.getCount();
				for (int i = 0; i < count; i++) {
					Variant varVoice = voiceTokens.getItem(i);
					if (null != varVoice) {
						SpObjectToken token = SpObjectToken.getToken(varVoice);
						if (null != token) {
							String voiceName = token.getDescription(0);
							if (null == exclude || !exclude.equals(voiceName)) {
								voiceList.add(new String[] { voiceName,
										voiceName });
							}
						}
					}
				}
				voiceNames = voiceList.toArray(new String[voiceList.size()][]);
			}
			varVoices.dispose();
		}
		Variant varAudioOutputs = sapiVoice.getAudioOutputs(null, null);
		if (null != varAudioOutputs) {
			SpeechObjectTokens audioOutputTokens = SpeechObjectTokens
					.getTokens(varAudioOutputs);
			if (null != audioOutputTokens) {
				List<String[]> audioOutputList = new ArrayList<String[]>();
				int count = audioOutputTokens.getCount();
				for (int i = 0; i < count; i++) {
					Variant varAudioOutput = audioOutputTokens.getItem(i);
					if (null != varAudioOutput) {
						SpObjectToken token = SpObjectToken
								.getToken(varAudioOutput);
						if (null != token) {
							String audioOutputName = token.getDescription(0);
							audioOutputList.add(new String[] { audioOutputName,
									audioOutputName });
						}
					}
				}
				audioOutputNames = audioOutputList
						.toArray(new String[audioOutputList.size()][]);
			}
			varAudioOutputs.dispose();
		}
	}

	public static SapiTestManager getInstance() {
		if (null == instance) {
			instance = new SapiTestManager();
		}
		return instance;
	}

	public String[][] getVoiceNames() {
		return voiceNames;
	}

	public String getVoiceName(int index) {
		if (null != voiceNames && index < voiceNames.length) {
			return voiceNames[index][1];
		}
		return null;
	}

	public String[][] getAudioOutputNames() {
		return audioOutputNames;
	}

	public String getAudioOutputName(int index) {
		if (null != audioOutputNames && index < audioOutputNames.length) {
			return audioOutputNames[index][1];
		}
		return null;
	}

	public void speakTest() {
		sapiVoice.speak(SAMPLE_TEXT, ISAPIEngine.SVSFDefault);
	}
}
