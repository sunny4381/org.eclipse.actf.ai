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

package org.eclipse.actf.ai.audio.io.impl;

import java.util.ArrayList;

import org.eclipse.actf.ai.audio.io.AudioPipeListener;
import org.eclipse.actf.ai.audio.io.IAudioPipe;
import org.eclipse.actf.ai.audio.io.IAudioReader;
import org.eclipse.actf.ai.audio.io.IAudioWriter;




public class LoopedAudioPipe implements IAudioPipe, AudioPipeListener {
    
    IAudioPipe target;
    
    boolean isActive;
    
    boolean joining = false;

    private ArrayList<AudioPipeListener> listeners = new ArrayList<AudioPipeListener>();
    
    public LoopedAudioPipe(IAudioReader reader, IAudioWriter writer) {
        target = new AudioPipe(reader, writer);
        target.addAudioPipeListener(this);
    }
    
    public void addAudioPipeListener(AudioPipeListener listener) {
        listeners.add(listener);
    }

    public boolean isActive() {
        return isActive;
    }

    public void join() {
        joining = true;
        target.join();
        fireFinished();
    }

    public void setBufferSize(int miliSeconds) {
        target.setBufferSize(miliSeconds);
    }

    public void setInterval(int interval) {
        target.setInterval(interval);
    }

    public void setPriority(int priority) {
        target.setPriority(priority);
    }
    
    public void prepare() {
        target.prepare();
    }

    public void start() {
        start(0);
    }

    public void start(int delay) {
        target.start(delay);
        isActive = true;
        joining = false;
    }

    public void stop() {
        target.stop();
        isActive = false;
        fireStopped();
    }

    private void fireFinished() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).finished(this);
        }
    }

    private void fireStopped() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).stopped(this);
        }
    }
    
    public void finished(IAudioPipe pipe) {
        if (!joining)
            pipe.start();
    }
    
    public void stopped(IAudioPipe pipe) {
    }
}
