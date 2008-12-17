/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.screenreader.jaws;

import org.eclipse.actf.ai.navigator.IScreenReaderControl;
import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserACTF;

/**
 * The implementation of ITTSEngine to use JAWS as voice engine.
 */
public class Jaws implements ITTSEngine, IScreenReaderControl {
	public static final String JAWS_ON_SCRIPT = "JawsOn";

	public static final String JAWS_OFF_SCRIPT = "JawsOff";

	public static final String JAWS_OBSERVE_SPEECH = "ObserveSpeech";

	public static final String SAYALLOFF = "AiBrowserSayAllOff";

	JawsAPI jaws = JawsAPI.getInstance();
	JawsWindowUtil util = JawsWindowUtil.getInstance();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#dispose()
	 */
	public void dispose() {
		// not supported
	}
	
	public boolean isDisposed() {
		return false;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#getSpeed()
	 */
	public int getSpeed() {
		// not supported
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setEventListener(org.eclipse.actf.ai.voice.IVoiceEventListener)
	 */
	public void setEventListener(IVoiceEventListener eventListener) {
		if (util != null)
			util.setEventListener(eventListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setLanguage(java.lang.String)
	 */
	public void setLanguage(String language) {
		// not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setSpeed(int)
	 */
	public void setSpeed(int speed) {
		// not supported
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#speak(java.lang.String, int, int)
	 */
	public void speak(String text, int flags, int index) {
		if (jaws == null || util == null)
			return;
		if (index < 0) {
			jaws.JawsSayString(text, flags == TTSFLAG_FLUSH);
		} else {
			util.JawsShowTextToWindow(text, flags == TTSFLAG_FLUSH, index);
			// Yield.forWhile(10);
			jaws.JawsRunScript(JAWS_OBSERVE_SPEECH);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#stop()
	 */
	public void stop() {
		if (jaws != null && util != null) {
			jaws.JawsStopSpeech();
			util.resetJawsWindowText();
			jaws.JawsRunScript(SAYALLOFF);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.navigator.IScreenReaderControl#screenReaderOff()
	 */
	public void screenReaderOff() {
		if (jaws != null) {
			jaws.JawsRunScript(JAWS_OFF_SCRIPT);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.navigator.IScreenReaderControl#screenReaderOn()
	 */
	public void screenReaderOn() {
		if (jaws != null) {
			jaws.JawsRunScript(JAWS_ON_SCRIPT);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.navigator.IScreenReaderControl#takeBackControl()
	 */
	public void takeBackControl() {
		if (util != null) {
			util.TakeBackControl();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setGender(java.lang.String)
	 */
	public void setGender(String gender) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#isAvailable()
	 */
	public boolean isAvailable() {
		if (jaws == null)
			return false;
		return jaws.isAvailable();
	}
	
}
