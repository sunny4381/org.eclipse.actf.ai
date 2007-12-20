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

import java.util.Vector;

import org.eclipse.actf.ai.tts.ITTSEngine;
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


public class ProTalkerBridge implements OleListener {
    public static final int VOICE_MALE = 0;

    public static final int VOICE_FEMALE = 1;

    public static final String SpeakWebText = "SpeakWebText";

    public static final String AboutDlg = "AboutDlg";

    public static final String GeneralDlg = "GeneralDlg";

    public static final String Pause = "Pause";

    public static final String Start = "Start";

    public static final String Resume = "Resume";

    public static final String Reset = "Reset";

    public static final String Speak = "Speak";

    public static final String SendText = "SendText";

    public static final String AboutBox = "AboutBox";

    public static final String Gender = "Gender";

    public static final String Age = "Age";

    public static final String ModeGuid = "ModeGuid";

    public static final String Speed = "Speed";

    public static final String Pitch = "Pitch";

    public static final String Volume = "Volume";

    public static final String UseButton = "UseButton";

    private OleControlSite site = null;

    private OleAutomation auto = null;

    private OleFrame frame = null;

    private int defaultSpeed = 0;

    private Display display;

    private Vector indexListener = new Vector();

    public ProTalkerBridge(Display display) {
        this.display = display;
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
        defaultSpeed = getSpeed();
    }

    public Variant getProperty(String name) {
        int id = auto.getIDsOfNames(new String[] { name })[0];
        return auto.getProperty(id);
    }

    public void setProperty(String name, Variant value) {
        int id = auto.getIDsOfNames(new String[] { name })[0];
        auto.setProperty(id, value);
    }

    public int getInt(String name) {
        return getProperty(name).getInt();
    }

    public void setInt(String name, int value) {
        setProperty(name, new Variant(value));
    }

    public String getString(String name) {
        return getProperty(name).getString();
    }

    public void setString(String name, String value) {
        setProperty(name, new Variant(value));
    }

    public boolean getBoolean(String name) {
        return getProperty(name).getBoolean();
    }

    public void setBoolean(String name, boolean value) {
        setProperty(name, new Variant(value));
    }

    public void invoke(String name) {
        invoke(name, new Variant[0]);
    }

    public void invoke(String name, final Variant[] arg) {
        final int id = auto.getIDsOfNames(new String[] { name })[0];
        auto.invoke(id, arg);

        /*
         * display.asyncExec(new Runnable() { public void run() { } });
         */
    }

    // methods
    public void speakWebText() {
        invoke(SpeakWebText);
    }

    public void aboutDlg() {
        invoke(AboutDlg);
    }

    public void generalDlg() {
        invoke(GeneralDlg);
    }

    public void pause() {
        invoke(Pause);
    }

    public void start() {
        invoke(Start);
    }

    public void resume() {
        invoke(Resume);
    }

    public void reset() {
        invoke(Reset);
        invoke(Speak, new Variant[] { new Variant(" ") });
        invoke(Reset);
        invoke(Speak, new Variant[] { new Variant(" ") });
    }

    public void sendText(String text) {
        invoke(SendText, new Variant[] { new Variant(text) });
    }

    public void aboutBox() {
        invoke(AboutBox);
    }

    // properties
    public int getGender() {
        return getInt(Gender);
    }

    public int getAge() {
        return getInt(Age);
    }

    public String getModeGuid() {
        return getString(ModeGuid);
    }

    public void setModeGuid(String value) {
        setString(ModeGuid, value);
    }

    public int getSpeed() {
        return getInt(Speed);
    }

    public void setSpeed(int value) {
        setInt(Speed, value);
    }

    public int getPitch() {
        return getInt(Pitch);
    }

    public void setPitch(int value) {
        setInt(Pitch, value);
    }

    public int getVolume() {
        return getInt(Volume);
    }

    public void setVolume(int value) {
        setInt(Volume, value);
    }

    public boolean getUseButton() {
        return getBoolean(UseButton);
    }

    public void setUseButton(boolean value) {
        setBoolean(UseButton, value);
    }

    public void initialize() {
        site.doVerb(OLE.OLEIVERB_SHOW);
    }

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
        // System.out.println(index+", "+text);
    }

    public void handleEvent(OleEvent event) {
        if (event.type == 3) { // Bookmark
            for (int i = 0; i < indexListener.size(); i++) {
                ((IndexListener) indexListener.get(i)).receivedIndex(event.arguments[0].getInt());
            }
        }

        /*
         * System.out.print(event.type + "("); for (int i = 0; i < event.arguments.length; i++) { if (i != 0)
         * System.out.print(","); System.out.print(event.arguments[i].getString()); } System.out.println(")");
         */
    }

    public void setSpeed(double speed) {
        setSpeed((int) (defaultSpeed * speed));
    }

    public void addIndexListener(IndexListener listener) {
        indexListener.add(listener);
    }

    public void removeIndexListener(IndexListener listener) {
        indexListener.remove(listener);
    }

    public void setVoice(int type) {
        if (type == VOICE_MALE) {
            setModeGuid("{904AAB60-5D94-11D0-830A-444553540000}");
        } else if (type == VOICE_FEMALE) {
            setModeGuid("{904AAB61-5D94-11d0-830A-444553540000}");
        }
    }

    public boolean isAvailable() {
        return auto != null;
    }
}
