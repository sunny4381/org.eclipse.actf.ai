/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.navigator.voice;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.eclipse.actf.ai.navigator.util.MessageFormatter;
import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.voice.IVoice;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.ai.voice.VoiceUtil;




public class VoiceManager implements IVoice {
    // --------------------------------------------------------------------------------
    // Singleton Class
    // --------------------------------------------------------------------------------
    // Later we should make it synchronized.
    private static class SingletonVoice implements IVoiceEventListener {
        final IVoice voice;
        final LinkedList<VoiceManager> callbackQueue = new LinkedList<VoiceManager>();
        VoiceManager lastVoiceManager;

        public void indexReceived(int index) {
            VoiceManager vm;
            try {
                if (index < 0) {
                    vm = callbackQueue.removeFirst();
                } else {
                    vm = callbackQueue.getFirst();
                }
            } catch (NoSuchElementException e) {
                return;
            }
            if ((vm != null) && (vm.eventListener != null)) {
                vm.eventListener.indexReceived(index);
            }
        }

        void manageCallback() {
            Iterator<VoiceManager> it = callbackQueue.iterator();
            while (it.hasNext()) {
                VoiceManager vm = it.next();
                if (vm.eventListener != null) {
                    vm.eventListener.indexReceived(-1);
                }
            }
            callbackQueue.clear();
        }

        void addEventQueue(VoiceManager vm) {
            callbackQueue.add(vm);
            lastVoiceManager = vm;
        }

        SingletonVoice() {
            this.voice = VoiceUtil.getVoice();
            this.voice.setEventListener(this);
        }
    }

    private static SingletonVoice sv;

    private SingletonVoice getSV() {
        return sv;
    }

    private IVoice getVoice() {
        return getSV().voice;
    }

    private void manageCallback() {
        getSV().manageCallback();
    }

    private void addEventQueue() {
        getSV().addEventQueue(this);
    }

    // --------------------------------------------------------------------------------
    // MessageFormatter Service Function
    // --------------------------------------------------------------------------------

    private static final MessageFormatter messageFormatter = new MessageFormatter("org.eclipse.actf.ai.navigator.message.Speech");

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    // --------------------------------------------------------------------------------
    // Instance Level Class Definition.
    // --------------------------------------------------------------------------------
    
    private IVoiceEventListener eventListener;
    private int currentIndex = 0;

    private String speakString(String str, boolean maleVoice) {
        // Currently do nothing.
        if (false) {
            if (maleVoice) {
                str = "\\Vce=Gender=\"Male\"\\" + str;
            } else {
                str = "\\Vce=Gender=\"Female\"\\" + str;
            }
        }
        return str;
    }

    public void speak(String str, boolean flush, boolean maleVoice) {
        if (flush) manageCallback();
        getVoice().speak(speakString(str, maleVoice), flush);
    }

    public void speakWithFormat(String str, boolean flush, boolean maleVoice) {
        speak(getMessageFormatter().mes(str), flush, maleVoice);
    }

    public int getSpeed() {
        return getVoice().getSpeed();
    }

    public void setSpeed(int speed) {
        getVoice().setSpeed(speed);
    }


    public void speakWithCallback(String str, boolean flush, boolean maleVoice) {
        if (flush) manageCallback();
        if (str.length() > 0) {
            addEventQueue();
            getVoice().speak(speakString(str, maleVoice), flush, currentIndex);
        } else {
            if (eventListener != null) {
                eventListener.indexReceived(-1);
            }
        }
    }

    public void stop() {
        manageCallback();
        getVoice().stop();
    }

    public VoiceManager(IVoiceEventListener listener) {
        if (sv == null) {
            sv = new SingletonVoice();
        }
        this.eventListener = listener;
    }
    
    // --------------------------------------------------------
    // For compatibility reason, we support IVoice functions.
    // --------------------------------------------------------

    public ITTSEngine getTTSEngine() {
        return getVoice().getTTSEngine();
    }

    public void setEventListener(IVoiceEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void speak(String text, boolean flush) {
        speak(text, flush, true);
    }

    public void speak(String text, boolean flush, int index) {
        currentIndex = index;
        speakWithCallback(text, flush, true);
    }
}
