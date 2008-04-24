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
package org.eclipse.actf.ai.tts.protalker.engine;

import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.tts.protalker.ProTalkerPlugin;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

/**
 * The implementation of ITTSEngine to use ProTalker.
 */
public class ProTalker implements ITTSEngine, IPropertyChangeListener {

	public static final String ID = "org.eclipse.actf.ai.tts.protalker.engine.ProTalker"; //$NON-NLS-1$

	private ProTalkerBridge engine;

	public ProTalker() {
		engine = new ProTalkerBridge(Display.getDefault());
		setVoice();
		ProTalkerPlugin.getDefault().addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setEventListener(org.eclipse.actf.ai.voice.IVoiceEventListener)
	 */
	public void setEventListener(IVoiceEventListener eventListener) {
		engine.addIndexListener(eventListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (ID.equals(event.getProperty())) {
			stop();
			setVoice();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#speak(java.lang.String, int, int)
	 */
	public void speak(String text, int flags, int index) {
		engine.speak(text, flags, index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#stop()
	 */
	public void stop() {
		engine.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#getSpeed()
	 */
	public int getSpeed() {
		return engine.getSpeed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setSpeed(int)
	 */
	public void setSpeed(int speed) {
		engine.setSpeed(speed * 8 / 5 + 100);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setLanguage(java.lang.String)
	 */
	public void setLanguage(String language) {
		// NOT AVAILABLE
	}

	private static IPreferenceStore preferenceStore = ProTalkerPlugin
			.getDefault().getPreferenceStore();

	private void setVoice() {
		String voiceName = preferenceStore.getString(ID);
		setVoice(voiceName);
	}

	/**
	 * Set the voice type of the voice engine.
	 * 
	 * @param type
	 *            "male" or "female".
	 */
	public void setVoice(String type) {
		if (type.equals("male")) {
			engine.setVoice(ProTalkerBridge.VOICE_MALE);
		} else if (type.equals("female")) {
			engine.setVoice(ProTalkerBridge.VOICE_FEMALE);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setGender(java.lang.String)
	 */
	public void setGender(String gender) {
		// Not available
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#isAvailable()
	 */
	public boolean isAvailable() {
		return engine.isAvailable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#dispose()
	 */
	public void dispose() {

	}
}
