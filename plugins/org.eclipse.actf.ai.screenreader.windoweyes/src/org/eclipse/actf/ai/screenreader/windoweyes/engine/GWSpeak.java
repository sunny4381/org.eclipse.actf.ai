/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.screenreader.windoweyes.engine;

import java.io.File;

import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.util.win32.COMUtil;
import org.eclipse.actf.util.win32.MemoryUtil;
import org.eclipse.actf.util.win32.WindowUtil;
import org.eclipse.swt.widgets.Display;

/**
 * The implementation of ITTSEngine to use WindowEyes as voice engine.
 */
public class GWSpeak implements ITTSEngine {

	private IGWSpeak dispGWSpeak = null; // Instanceof GWSpeak.Speak ActiveX
	private IVoiceEventListener eventListener = null;
	private boolean notifyEndOfSpeech = false; // Invoke indexReceived() if
	// true
	private long lastNotificationTime = 0; // Last time of indexReceived()

	// Constants
	private static final int DELAY_FIRST = 500; // Delay on the first
	// indexReceived()
	private static final int DELAY_NEXT = 1000; // Delay on the subsequent
	// indexReceived()
	private static final String GWM_WINDOW_CLASS = "GWMExternalControl"; //$NON-NLS-1$
	private static final String GWM_WINDOW_NAME = "External Control"; //$NON-NLS-1$

	private boolean isDisposed = false;

	/**
	 * Constructor
	 */
	public GWSpeak() {
		// check to see if Window-Eyes is running
		if (0 != WindowUtil.FindWindow(GWM_WINDOW_CLASS, GWM_WINDOW_NAME)) {
			int pv = COMUtil.createDispatch(IGWSpeak.IID);
			if (0 != pv) {
				dispGWSpeak = new IGWSpeak(pv);
				// Dispose GWSpeak just before the Display is disposed
				Display.getCurrent().disposeExec(new Runnable() {
					public void run() {
						dispose();
					}
				});
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#dispose()
	 */
	public void dispose() {
		isDisposed = true;
		if (null != dispGWSpeak) {
			eventListener = null;
			stop();
			dispGWSpeak.Release();
			dispGWSpeak = null;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#isAvailable()
	 */
	public boolean isAvailable() {
		return null != dispGWSpeak;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setEventListener(org.eclipse.actf.ai.voice.IVoiceEventListener)
	 */
	public void setEventListener(IVoiceEventListener eventListener) {
		this.eventListener = eventListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#speak(java.lang.String, int, int)
	 */
	public void speak(String text, int flags, int index) {
		if (null == dispGWSpeak)
			return;
		// System.out.println(text+" / "+flags+" / "+index); //$NON-NLS-1$
		// //$NON-NLS-2$
		boolean flushBeforeSpeak = 0 != (TTSFLAG_FLUSH & flags);
		if (flushBeforeSpeak && 0 != lastNotificationTime) {
			// Special handling on subsequent speak
			if (index >= 0) {
				// Do not flush during speak-all
				flushBeforeSpeak = false;
			} else {
				// Do not flash on last text immediate after speak-all
				// System.out.println(System.currentTimeMillis() -
				// lastNotificationTime+"ms after notification"); //$NON-NLS-1$
				flushBeforeSpeak = System.currentTimeMillis()
						- lastNotificationTime > 200;
			}
		}
		char[] data = (text + "\0").toCharArray(); //$NON-NLS-1$
		int bstrText = MemoryUtil.SysAllocString(data);
		try {
			if (flushBeforeSpeak) {
				dispGWSpeak.Silence();
			}
			dispGWSpeak.SpeakString(bstrText);
			if (index >= 0 && null != eventListener) {
				// Report dummy event since GWSpeak does not report
				// end-of-speech
				eventListener.indexReceived(index);
				notifyEndOfSpeech = true;
				int delay = 0 == lastNotificationTime ? DELAY_FIRST
						: DELAY_NEXT;
				Display.getCurrent().timerExec(delay, new Runnable() {
					public void run() {
						if (null != eventListener && notifyEndOfSpeech) {
							eventListener.indexReceived(-1);
							lastNotificationTime = System.currentTimeMillis();
						}
						notifyEndOfSpeech = false;
					}
				});
			}
			lastNotificationTime = 0;
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
		if (null == dispGWSpeak)
			return;
		notifyEndOfSpeech = false;
		lastNotificationTime = 0;
		dispGWSpeak.Silence();
	}

	/*
	 * The following functions are not supported
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#getSpeed()
	 */
	public int getSpeed() {
		return 50;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setGender(java.lang.String)
	 */
	public void setGender(String gender) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setLanguage(java.lang.String)
	 */
	public void setLanguage(String language) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.actf.ai.tts.ITTSEngine#setSpeed(int)
	 */
	public void setSpeed(int speed) {
	}

	public boolean isDisposed() {
		return isDisposed;
	}

	public boolean canSpeakToFile() {
		return false;
	}

	public boolean speakToFile(String text, File file) {
		return false;
	}

}
