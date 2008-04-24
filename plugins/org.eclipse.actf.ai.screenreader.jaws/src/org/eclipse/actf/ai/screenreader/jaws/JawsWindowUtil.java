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

import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.model.IWebBrowserACTF;

/**
 * JawsWindowUtil is an utility to control JAWS behavior. The strategy of this
 * utility is being subservient to JAWS.
 */
public class JawsWindowUtil {
	private static JawsAPI jaws = JawsAPI.getInstance();

	private static IVoiceEventListener listener;

	private static JawsWindowUtil instance;

	/**
	 * @return the singleton instance of JawsWindowUtil.
	 */
	public static JawsWindowUtil getInstance() {
		if (instance == null) {
			int handle = JawsPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getShell().handle;
			if (!_initializeWindow(handle))
				return null;
			instance = new JawsWindowUtil();
		}
		return instance;
	}

	private JawsWindowUtil() {

	}

	/**
	 * This utility is handling the voice events occurred in this plug-in.
	 * 
	 * @param eventListener
	 */
	public void setEventListener(IVoiceEventListener eventListener) {
		listener = eventListener;
	}

	/**
	 * This is the wrapper method of native method to control say all mode of
	 * JAWS. This method shows a white window which overlaps the window of
	 * application.
	 * 
	 * @param stringToSpeak
	 *            the string to be spoken.
	 * @param bInterrupt
	 *            if this flag is true then JAWS is stopped speaking and speaks
	 *            the string, Otherwise the string is buffered at the end of the
	 *            speech.
	 * @param index
	 *            the index to be used in voice events.
	 * @return if the invocation is succeeded then true is returned.
	 */
	public boolean JawsShowTextToWindow(String stringToSpeak,
			boolean bInterrupt, int index) {
		boolean ret = _setJawsWindowText(stringToSpeak);
		if (listener != null) {
			listener.indexReceived(index);
		}
		return ret;
	}

	/**
	 * This method is used for taking back control from JAWS.
	 * 
	 * @return if the invocation is succeeded then true is returned.
	 */
	public boolean TakeBackControl() {
		int handle = JawsPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow().getShell().handle;
		return _TakeBackControl(handle);
	}

	/**
	 * This call back method is invoked by native library. Don't call directory
	 * in Java.
	 * 
	 * @param param
	 * @return
	 */
	public static boolean callBack(int param) {
		System.err.println("Callbacked!!!" + param);
		if (listener == null)
			return true;
		if (param == 0) {
			listener.indexReceived(-1);
		} else if (param == 1) {
			jaws.JawsStopSpeech();
			getInstance().resetJawsWindowText();
			jaws.JawsRunScript(Jaws.SAYALLOFF);
			listener.indexReceived(-2);
		}
		return true;
	}

	/**
	 * This method hides the white window shown by
	 * {@link #JawsShowTextToWindow(String, boolean, int)}.
	 */
	public void resetJawsWindowText() {
		_resetJawsWindowText();
	}

	private static native boolean _TakeBackControl(long browser);

	private static native boolean _setJawsWindowText(String text);

	private static native boolean _resetJawsWindowText();

	private static native boolean _initializeWindow(int handle);
}
