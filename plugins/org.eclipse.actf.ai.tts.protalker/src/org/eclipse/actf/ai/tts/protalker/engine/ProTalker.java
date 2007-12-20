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



public class ProTalker implements IndexListener, ITTSEngine, IPropertyChangeListener {

    public static final String ID = "org.eclipse.actf.ai.tts.protalker.engine.ProTalker"; //$NON-NLS-1$

    private ProTalkerBridge engine;

    private IVoiceEventListener eventListener = null;

    public ProTalker() {
        engine = new ProTalkerBridge(Display.getDefault());
        engine.addIndexListener(this);
        setVoice();
        ProTalkerPlugin.getDefault().addPropertyChangeListener(this);
    }

    public void setEventListener(IVoiceEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void receivedIndex(int index) {
        if (null != eventListener) {
            eventListener.indexReceived(index);
        }
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if( ID.equals(event.getProperty()) ) {
            stop();
            setVoice();
        }
    }


    public void speak(String text, int flags, int index) {
        engine.speak(text, flags, index);
    }

    public void stop() {
        engine.reset();
    }

    public void dispose() {
        engine.removeIndexListener(this);
        eventListener = null;
    }

    public int getSpeed() {
        return engine.getSpeed();
    }

    public void setSpeed(int speed) {
        engine.setSpeed(speed*8/5+100);
    }

    public void setLanguage(String language) {
        // NOT AVAILABLE
    }
    
    private static IPreferenceStore preferenceStore = ProTalkerPlugin.getDefault().getPreferenceStore();
    public void setVoice(){
        String voiceName = preferenceStore.getString(ID);
        setVoice(voiceName);
    }
    
    public void setVoice(String type){
        if(type.equals("male")){
            engine.setVoice(ProTalkerBridge.VOICE_MALE);
        }
        else if(type.equals("female")){
            engine.setVoice(ProTalkerBridge.VOICE_FEMALE);            
        }
    }

	public void setGender(String gender) {
		// TODO Auto-generated method stub
		
	}

    public int getPriority() {
        return 400;
    }

    public boolean isAvailable() {
        return engine.isAvailable();
    }
}
