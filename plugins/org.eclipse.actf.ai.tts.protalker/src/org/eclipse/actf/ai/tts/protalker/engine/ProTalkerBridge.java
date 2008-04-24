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

import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleEvent;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleListener;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * ProTalker COM wrapper
 */
public class ProTalkerBridge implements OleListener {
	public static final int VOICE_MALE = 0;

	public static final int VOICE_FEMALE = 1;

	private static final String SpeakWebText = "SpeakWebText";

	private static final String AboutDlg = "AboutDlg";

	private static final String GeneralDlg = "GeneralDlg";

	private static final String Pause = "Pause";

	private static final String Start = "Start";

	private static final String Resume = "Resume";

	private static final String Reset = "Reset";

	private static final String Speak = "Speak";

	private static final String SendText = "SendText";

	private static final String AboutBox = "AboutBox";

	private static final String Gender = "Gender";

	private static final String Age = "Age";

	private static final String ModeGuid = "ModeGuid";

	private static final String Speed = "Speed";

	private static final String Pitch = "Pitch";

	private static final String Volume = "Volume";

	private static final String UseButton = "UseButton";

	private OleControlSite site = null;

	private OleAutomation auto = null;

	private OleFrame frame = null;

	private Vector<IVoiceEventListener> indexListener = new Vector<IVoiceEventListener>();

	ProTalkerBridge(Display display) {
		Shell parent = new Shell();
		parent.setLayout(new FillLayout());
		frame = new OleFrame(parent, SWT.NONE);
		try {
			site = new OleControlSite(frame, SWT.NONE, "PTSVR.PtSvrCtrl.1");
		} catch (SWTException e) {
			site = null;
			return;
		}
		auto = new OleAutomation(site);
		site.doVerb(OLE.OLEIVERB_SHOW);
		for (int i = 0; i < 10; i++) {
			site.addEventListener(i, this);
		}
		setVoice(VOICE_MALE);
	}

	private Variant getProperty(String name) {
		int id = auto.getIDsOfNames(new String[] { name })[0];
		return auto.getProperty(id);
	}

	private void setProperty(String name, Variant value) {
		int id = auto.getIDsOfNames(new String[] { name })[0];
		auto.setProperty(id, value);
	}

	private int getInt(String name) {
		return getProperty(name).getInt();
	}

	private void setInt(String name, int value) {
		setProperty(name, new Variant(value));
	}

	private String getString(String name) {
		return getProperty(name).getString();
	}

	private void setString(String name, String value) {
		setProperty(name, new Variant(value));
	}

	private boolean getBoolean(String name) {
		return getProperty(name).getBoolean();
	}

	private void setBoolean(String name, boolean value) {
		setProperty(name, new Variant(value));
	}

	private void invoke(String name) {
		invoke(name, new Variant[0]);
	}

	private void invoke(String name, final Variant[] arg) {
		final int id = auto.getIDsOfNames(new String[] { name })[0];
		auto.invoke(id, arg);
	}

	// properties
	/**
	 * @return the gender type of this engine.
	 */
	public int getGender() {
		return getInt(Gender);
	}

	/**
	 * @return the age property of this engine.
	 */
	public int getAge() {
		return getInt(Age);
	}

	/**
	 * @return the guid of the current mode.
	 */
	public String getModeGuid() {
		return getString(ModeGuid);
	}

	/**
	 * @param guid
	 *            the guid to be set.
	 */
	public void setModeGuid(String guid) {
		setString(ModeGuid, guid);
	}

	/**
	 * @return the speed property of this engine.
	 */
	public int getSpeed() {
		return getInt(Speed);
	}

	/**
	 * Set the speed of this engine in the scale of this engine.
	 * 
	 * @param value
	 *            the speed property to be set.
	 */
	public void setSpeed(int value) {
		setInt(Speed, value);
	}

	/**
	 * @return the pitch property of this engine.
	 */
	public int getPitch() {
		return getInt(Pitch);
	}

	/**
	 * @param value
	 *            the pitch property to be set.
	 */
	public void setPitch(int value) {
		setInt(Pitch, value);
	}

	/**
	 * @return the volume property of this engine.
	 */
	public int getVolume() {
		return getInt(Volume);
	}

	/**
	 * @param value
	 *            the volume property to be set.
	 */
	public void setVolume(int value) {
		setInt(Volume, value);
	}

	/**
	 * @return whether the GUI button is used or not.
	 */
	public boolean getUseButton() {
		return getBoolean(UseButton);
	}

	/**
	 * @param value
	 *            the using button flag to be set.
	 */
	public void setUseButton(boolean value) {
		setBoolean(UseButton, value);
	}

	// methods
	/**
	 * Invoke SpeakWebText method of this engine.
	 */
	public void speakWebText() {
		invoke(SpeakWebText);
	}

	/**
	 * Shows the about dialog.
	 */
	public void aboutDlg() {
		invoke(AboutDlg);
	}

	/**
	 * Shows the dialog of property settings.
	 */
	public void generalDlg() {
		invoke(GeneralDlg);
	}

	/**
	 * Pause the speech.
	 */
	public void pause() {
		invoke(Pause);
	}

	/**
	 * Start to speak.
	 */
	public void start() {
		invoke(Start);
	}

	/**
	 * Resume to speak.
	 */
	public void resume() {
		invoke(Resume);
	}

	/**
	 * Reset the state of this voice engine.
	 */
	public void reset() {
		// It doesn't work well by invoking Reset method once.
		invoke(Reset);
		invoke(Speak, new Variant[] { new Variant(" ") });
		invoke(Reset);
		invoke(Speak, new Variant[] { new Variant(" ") });
	}

	/**
	 * @param text
	 *            the text to be spoken.
	 */
	public void sendText(String text) {
		invoke(SendText, new Variant[] { new Variant(text) });
	}

	/**
	 * Shows the about box.
	 */
	public void aboutBox() {
		invoke(AboutBox);
	}

	/**
	 * @param text
	 *            the text to be spoken.
	 * @param flags
	 *            the flag of the voice engine behavior.
	 * @param index
	 *            the index mark to be set. The index will be returned with
	 *            voice event.
	 * @see ITTSEngine#TTSFLAG_DEFAULT
	 * @see ITTSEngine#TTSFLAG_FLUSH
	 */
	public void speak(String text, int flags, int index) {
		StringBuffer sb = new StringBuffer();
		sb.append("\\Mrk=");
		sb.append(index);
		sb.append("\\");
		sb.append(text);
		sb.append("\\Mrk=-1\\");

		if (flags == ITTSEngine.TTSFLAG_DEFAULT) {
			invoke(Speak, new Variant[] { new Variant(sb.toString()) });
		} else if (flags == ITTSEngine.TTSFLAG_FLUSH) {
			reset();
			invoke(Speak, new Variant[] { new Variant(sb.toString()) });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.ole.win32.OleListener#handleEvent(org.eclipse.swt.ole.win32.OleEvent)
	 */
	public void handleEvent(OleEvent event) {
		if (event.type == 3) { // Bookmark
			for (Enumeration<IVoiceEventListener> e = indexListener.elements(); e
					.hasMoreElements();) {
				e.nextElement().indexReceived(event.arguments[0].getInt());
			}
		}
	}

	/**
	 * @param listener
	 */
	public void addIndexListener(IVoiceEventListener listener) {
		indexListener.add(listener);
	}

	/**
	 * @param listener
	 */
	public void removeIndexListener(IVoiceEventListener listener) {
		indexListener.remove(listener);
	}

	/**
	 * @param type
	 *            the voice type of this engine.
	 * @see #VOICE_MALE
	 * @see #VOICE_FEMALE
	 */
	public void setVoice(int type) {
		if (type == VOICE_MALE) {
			setModeGuid("{904AAB60-5D94-11D0-830A-444553540000}");
		} else if (type == VOICE_FEMALE) {
			setModeGuid("{904AAB61-5D94-11d0-830A-444553540000}");
		}
	}

	/**
	 * @return whether this object can be used or not.
	 */
	public boolean isAvailable() {
		return auto != null;
	}
}
