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
package org.eclipse.actf.ai.tts.sapi.engine;

import java.io.File;

import org.eclipse.actf.ai.tts.ISAPIEngine;
import org.eclipse.actf.ai.tts.sapi.SAPIPlugin;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.util.win32.COMUtil;
import org.eclipse.actf.util.win32.MemoryUtil;
import org.eclipse.actf.util.win32.NativeIntAccess;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.internal.ole.win32.GUID;
import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

/**
 * The implementation of ITTSEngine to use Microsoft Speech API.
 */
public class SapiVoice implements ISAPIEngine, IPropertyChangeListener {

	public static final String ID = "org.eclipse.actf.ai.tts.sapi.engine.SapiVoice"; //$NON-NLS-1$
	public static final String AUDIO_OUTPUT = "org.eclipse.actf.ai.tts.SapiVoice.audioOutput"; //$NON-NLS-1$

	public static final GUID IID_SpFileStream = COMUtil
			.IIDFromString("{947812B3-2AE1-4644-BA86-9E90DED7EC91}"); //$NON-NLS-1$

	public ISpVoice dispSpVoice;
	private Variant varSapiVoice;
	private OleAutomation automation;
	private int idGetVoices;
	private int idGetAudioOutputs;
	private ISpNotifySource spNotifySource = null;
	private static IPreferenceStore preferenceStore = SAPIPlugin.getDefault()
			.getPreferenceStore();
	private boolean isDisposed = false;

	public SapiVoice() {
		int pv = COMUtil.createDispatch(ISpVoice.IID);
		dispSpVoice = new ISpVoice(pv);
		varSapiVoice = new Variant(dispSpVoice);
		automation = varSapiVoice.getAutomation();
		spNotifySource = ISpNotifySource.getNotifySource(dispSpVoice);
		SAPIPlugin.getDefault().addPropertyChangeListener(this);

		idGetVoices = getIDsOfNames("GetVoices"); //$NON-NLS-1$
		idGetAudioOutputs = getIDsOfNames("GetAudioOutputs"); //$NON-NLS-1$

		// init by using default engine to avoid init error of some TTS engines
		String orgID = preferenceStore.getString(ID);
		preferenceStore.setValue(ID, preferenceStore.getDefaultString(ID));
		setAudioOutputName();
		// switch to actual engine
		preferenceStore.setValue(ID, orgID);
		// setVoiceName();

		// to avoid access violation error at application shutdown
		stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (ID.equals(event.getProperty())) {
			stop();
			setVoiceName();
		} else if (AUDIO_OUTPUT.equals(event.getProperty())) {
			stop();
			setAudioOutputName();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.actf.ai.tts.ITTSEngine#setEventListener(org.eclipse.actf.
	 * ai.voice.IVoiceEventListener)
	 */
	public void setEventListener(IVoiceEventListener eventListener) {
		spNotifySource.setEventListener(eventListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#speak(java.lang.String, int, int)
	 */
	public void speak(String text, int flags, int index) {
		int firstFlag = SVSFlagsAsync;
		if (0 != (TTSFLAG_FLUSH & flags)) {
			firstFlag |= SVSFPurgeBeforeSpeak;
		}
		if (index >= 0) {
			speak("<BOOKMARK mark=\"" + index + "\"/>", firstFlag | SVSFPersistXML); //$NON-NLS-1$ //$NON-NLS-2$
			speak(text, SVSFlagsAsync);
			speak("<BOOKMARK mark=\"-1\"/>", SVSFlagsAsync | SVSFPersistXML); //$NON-NLS-1$
		} else {
			speak(text, firstFlag);
		}
	}

	public void speak(String text, int sapiFlags) {
		char[] data = (text + "\0").toCharArray(); //$NON-NLS-1$
		int bstrText = MemoryUtil.SysAllocString(data);
		try {
			dispSpVoice.Speak(bstrText, sapiFlags);
		} finally {
			MemoryUtil.SysFreeString(bstrText);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#stop()
	 */
	public void stop() {
		speak("", TTSFLAG_FLUSH, -1); //$NON-NLS-1$
	}

	/**
	 * @param rate
	 *            The rate property to be set.
	 * @return The invocation is succeeded then it returns true.
	 */
	public boolean setRate(int rate) {
		return OLE.S_OK == dispSpVoice.put_Rate(rate);
	}

	/**
	 * @return The rate property of the voice engine.
	 */
	public int getRate() {
		NativeIntAccess nia = new NativeIntAccess();
		try {
			if (OLE.S_OK == dispSpVoice.get_Rate(nia.getAddress())) {
				return nia.getInt();
			}
		} finally {
			nia.dispose();
		}
		return -1;
	}

	/**
	 * @param varVoice
	 *            The voice object to be set.
	 * @return The invocation is succeeded then it returns true.
	 */
	public boolean setVoice(Variant varVoice) {
		return OLE.S_OK == dispSpVoice.put_Voice(varVoice.getDispatch()
				.getAddress());
	}

	/**
	 * @param varAudioOutput
	 *            The audio output object to be set.
	 * @return The invocation is succeeded then it returns true.
	 */
	public boolean setAudioOutput(Variant varAudioOutput) {
		return OLE.S_OK == dispSpVoice
				.put_AudioOutput(null != varAudioOutput ? varAudioOutput
						.getDispatch().getAddress() : 0);
	}

	private void setVoiceName() {
		String voiceName = preferenceStore.getString(ID);
		if (voiceName.length() > 0) {
			setVoiceName("name=" + voiceName); //$NON-NLS-1$
		}
	}

	/**
	 * @param voiceName
	 *            The voice name to be set.
	 * @return The invocation is succeeded then it returns true.
	 */
	public boolean setVoiceName(String voiceName) {
		boolean success = false;
		Variant varVoices = getVoices(voiceName, null);
		if (null != varVoices) {
			SpeechObjectTokens tokens = SpeechObjectTokens.getTokens(varVoices);
			if (null != tokens && 0 < tokens.getCount()) {
				Variant varVoice = tokens.getItem(0);
				if (null != varVoice) {
					success = setVoice(varVoice);
				}
			}
			varVoices.dispose();
		}
		if (!success) {
			int index = voiceName.indexOf("name="); //$NON-NLS-1$
			varVoices = getVoices(null, null);
			if (null != varVoices && index > -1) {
				String name = voiceName.substring(index + 5);
				SpeechObjectTokens voiceTokens = SpeechObjectTokens
						.getTokens(varVoices);
				if (null != voiceTokens) {
					int count = voiceTokens.getCount();
					for (int i = 0; i < count; i++) {
						Variant varVoice = voiceTokens.getItem(i);
						if (null != varVoice) {
							SpObjectToken token = SpObjectToken
									.getToken(varVoice);
							if (null != token
									&& name.equals(token.getDescription(0))) {
								success = setVoice(varVoice);
							}
						}
					}
				}
			}
			varVoices.dispose();
		}
		return success;
	}

	private void setAudioOutputName() {
		String audioOutput = preferenceStore.getString(AUDIO_OUTPUT);
		if (audioOutput.length() > 0) {
			setAudioOutputName(audioOutput);
		} else {
			setAudioOutput(null);
		}
	}

	/**
	 * @param audioOutput
	 *            The audio output name to be set.
	 * @return The invocation is succeeded then it returns true.
	 */
	public boolean setAudioOutputName(String audioOutput) {
		boolean success = false;
		Variant varAudioOutputs = getAudioOutputs(null, null);
		if (null != varAudioOutputs) {
			SpeechObjectTokens tokens = SpeechObjectTokens
					.getTokens(varAudioOutputs);
			if (null != tokens) {
				for (int i = 0; i < tokens.getCount(); i++) {
					Variant varAudioOutput = tokens.getItem(i);
					if (null != varAudioOutput) {
						SpObjectToken token = SpObjectToken
								.getToken(varAudioOutput);
						if (null != token
								&& audioOutput.equals(token.getDescription(0))) {
							success = setAudioOutput(varAudioOutput);
							break;
						}
					}
				}
			}
			varAudioOutputs.dispose();
		}
		return success;
	}

	/**
	 * @param requiredAttributes
	 * @param optionalAttributes
	 * @return The tokens of voices.
	 */
	public Variant getVoices(String requiredAttributes,
			String optionalAttributes) {
		return getTokens(idGetVoices, requiredAttributes, optionalAttributes);
	}

	/**
	 * @param requiredAttributes
	 * @param optionalAttributes
	 * @return The tokens of audio outputs.
	 */
	public Variant getAudioOutputs(String requiredAttributes,
			String optionalAttributes) {
		return getTokens(idGetAudioOutputs, requiredAttributes,
				optionalAttributes);
	}

	private Variant getTokens(int id, String requiredAttributes,
			String optionalAttributes) {
		if (null == requiredAttributes) {
			return automation.invoke(id);
		} else if (null == optionalAttributes) {
			return automation.invoke(id, new Variant[] { new Variant(
					requiredAttributes) });
		}
		return automation.invoke(id, new Variant[] {
				new Variant(requiredAttributes),
				new Variant(optionalAttributes) });
	}

	private int getIDsOfNames(String name) {
		int dispid[] = automation.getIDsOfNames(new String[] { name });
		if (null != dispid) {
			return dispid[0];
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#dispose()
	 */
	public void dispose() {
		if (!isDisposed) {
			isDisposed = true;

			varSapiVoice.dispose();

			if (SAPIPlugin.getDefault() != null) {
				SAPIPlugin.getDefault().removePropertyChangeListener(this);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#getSpeed()
	 */
	public int getSpeed() {
		int rate = getRate(); // -10 <= rate <= 10
		return (rate + 10) * 5; // 0 <= speed <= 100
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setSpeed(int)
	 */
	public void setSpeed(int speed) {
		int rate = speed / 5 - 10;
		setRate(rate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setLanguage(java.lang.String)
	 */
	public void setLanguage(String language) {
		String token;
		if (LANG_JAPANESE.equals(language)) {
			token = "language=411"; //$NON-NLS-1$
		} else if (LANG_ENGLISH.equals(language)) {
			token = "language=409;9"; //$NON-NLS-1$
		} else {
			return;
		}
		setVoiceName(token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setGender(java.lang.String)
	 */
	public void setGender(String gender) {
		// TODO
		if (GENDER_MALE.equalsIgnoreCase(gender)) {
			setVoiceName("name=Microsoft Mike");
		} else if (GENDER_FEMALE.equalsIgnoreCase(gender)) {
			setVoiceName("name=Microsoft Mary");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#isAvailable()
	 */
	public boolean isAvailable() {
		return automation != null;
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	public boolean canSpeakToFile() {
		return true;
	}

	public boolean speakToFile(String text, File file) {
		int pv = COMUtil.createDispatch(IID_SpFileStream);
		OleAutomation autoSpFileStream = null;
		boolean speakToFileResult = false;

		if (file == null || (file.exists() && !file.canWrite())) {
			return false;
		}

		Variant varSpFileStream = new Variant(new IDispatch(pv));
		try {
			autoSpFileStream = varSpFileStream.getAutomation();

			// AllowAudioOutputFormatChangesOnNextSet
			// System.out.println(automation.getProperty(7).getBoolean());

			// format
			// System.out.println(autoSpFileStream.getProperty(1).getAutomation().setProperty(1,new
			// Variant(6)));

			// open 100 close 101
			String tmpS = file.toURI().toString();
			if (tmpS.startsWith("file:/")) {
				tmpS = tmpS.substring(6);
			}

			autoSpFileStream.invoke(100, new Variant[] { new Variant(tmpS),
					new Variant(3), new Variant(false) });

			dispSpVoice.put_AudioOutputStream(pv);

			char[] data = (text + "\0").toCharArray(); //$NON-NLS-1$
			int bstrText = MemoryUtil.SysAllocString(data);

			try {
				dispSpVoice.Speak(bstrText, 0);
			} finally {
				MemoryUtil.SysFreeString(bstrText);
			}
			autoSpFileStream.invoke(101);
			autoSpFileStream.dispose();
			autoSpFileStream = null;
			speakToFileResult = true;

		} catch (Exception e) {
			e.printStackTrace();
			if (autoSpFileStream != null) {
				autoSpFileStream.dispose();
				autoSpFileStream = null;
			}
		}
		setAudioOutputName();	//reset output
		return speakToFileResult;
	}
}
