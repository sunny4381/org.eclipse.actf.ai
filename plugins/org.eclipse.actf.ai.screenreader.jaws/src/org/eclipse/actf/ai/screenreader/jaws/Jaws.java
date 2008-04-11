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

package org.eclipse.actf.ai.screenreader.jaws;

import org.eclipse.actf.ai.navigator.IScreenReaderControl;
import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.model.IWebBrowserACTF;




public class Jaws implements ITTSEngine, IScreenReaderControl {
    public static final String JAWS_ON_SCRIPT = "JawsOn";

    public static final String JAWS_OFF_SCRIPT = "JawsOff";

    public static final String JAWS_OBSERVE_SPEECH = "ObserveSpeech";

    public static final String SAYALLOFF = "AiBrowserSayAllOff";

    JawsAPI jaws = JawsAPI.getInstance();
    JawsWindowUtil util = JawsWindowUtil.getInstance();

    public void dispose() {
        // not supported
    }

    public int getSpeed() {
        // not supported
        return 0;
    }

    public void setEventListener(IVoiceEventListener eventListener) {
    	if (util != null)
    		util.setEventListener(eventListener);
    }

    public void setLanguage(String language) {
        // not supported
    }

    public void setSpeed(int speed) {
        // not supported
    }

    public void speak(String text, int flags, int index) {
        if (jaws == null || util == null) return;
        if (index < 0) {
            jaws.JawsSayString(text, flags == TTSFLAG_FLUSH);
        }  else {
            util.JawsShowTextToWindow(text, flags == TTSFLAG_FLUSH, index);
            // Yield.forWhile(10);
            jaws.JawsRunScript(JAWS_OBSERVE_SPEECH);
        }
    }

    public void stop() {
        if (jaws != null && util != null) {
            jaws.JawsStopSpeech();
            util.resetJawsWindowText();
            jaws.JawsRunScript(SAYALLOFF);
        }
    }

    public void screenReaderOff() {
        if (jaws != null) {
            jaws.JawsRunScript(JAWS_OFF_SCRIPT);
        }
    }

    public void screenReaderOn() {
        if (jaws != null) {
            jaws.JawsRunScript(JAWS_ON_SCRIPT);
        }
    }
    
    public void takeBackControl(IWebBrowserACTF browser){
        if(util != null){
            util.TakeBackControl(browser);
        }
    }

    public void setGender(String gender) {
        // TODO Auto-generated method stub
        
    }

    public boolean isAvailable() {
        if (jaws == null) return false;
        return jaws.isAvailable();
    }
}
