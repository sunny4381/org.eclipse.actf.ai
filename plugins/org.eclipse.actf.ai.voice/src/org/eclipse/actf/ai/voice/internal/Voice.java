/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.voice.internal;

import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.IVoice;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.ai.voice.VoicePlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


public class Voice implements IVoice, IPropertyChangeListener {

	private ITTSEngine ttsEngine = null;
	
    private IVoiceEventListener eventListener;
	
    private static final IPreferenceStore preferenceStore = VoicePlugin.getDefault().getPreferenceStore();
	
	public Voice() {
		ttsEngine = newTTSEngine();
		VoicePlugin.getDefault().addPropertyChangeListener(this);
		setSpeed();
	}
	
	public ITTSEngine newTTSEngine() {
		ITTSEngine engine = TTSRegistry.createTTSEngine(preferenceStore.getString(PREF_ENGINE));
		if( null == engine ) {
			engine = TTSRegistry.createTTSEngine(TTSRegistry.getDefaultEngine());
		}
		return engine;
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if( PREF_ENGINE.equals(event.getProperty()) ) {
			if( null != ttsEngine ) {
				stop();
				ttsEngine.dispose();
				ttsEngine = newTTSEngine();
				setSpeed();
				setEventListener(eventListener);
			}
		}
		else if( PREF_SPEED.equals(event.getProperty()) ) {
			stop();
			setSpeed();
		}
	}

	public void speak(String text, boolean flush) {
		speak(text,flush,-1);
	}
	
	public void speak(String text, boolean flush, int index) {
		if( null != ttsEngine ) {
			ttsEngine.speak(text,flush?ITTSEngine.TTSFLAG_FLUSH:ITTSEngine.TTSFLAG_DEFAULT,index);
		}
	}
	
	public void stop() {
		if( null != ttsEngine ) {
			ttsEngine.stop();
		}
	}
	
	public int getSpeed() {
		if( null != ttsEngine ) {
			return ttsEngine.getSpeed();
		}
		return -1;
	}
	
	public static int getDefaultSpeed() {
		return preferenceStore.getInt(PREF_SPEED);
	}

	public void setSpeed() {
		setSpeed(getDefaultSpeed());
	}
    
	public void setSpeed(int speed) {
		if( null != ttsEngine ) {
			ttsEngine.setSpeed(speed);
		}
	}
	
	public void setEventListener(IVoiceEventListener eventListener) {
		this.eventListener = eventListener;
		if( null != ttsEngine ) {
			ttsEngine.setEventListener(eventListener);
		}
	}
	
	public void dispose() {
		if( null != ttsEngine ) {
			ttsEngine.dispose();
			ttsEngine = null;
		}
	}
    
    public ITTSEngine getTTSEngine() {
        return ttsEngine;
    }
}
