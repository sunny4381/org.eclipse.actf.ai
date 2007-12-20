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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.actf.ai.audio.io.AudioIOException;
import org.eclipse.actf.ai.audio.io.IAudioWriter;




public class PlaybackDevice implements IAudioWriter {

    private Mixer playbackMixer;

    private SourceDataLine playbackLine;

    private int limit;

    @Override
    public boolean equals(Object o) {
        if (o instanceof PlaybackDevice) {
            return playbackMixer.equals(((PlaybackDevice) o).playbackMixer);
        }
        return false;
    }

    public PlaybackDevice(Mixer mixer) {
        this.playbackMixer = mixer;
    }

    public PlaybackDevice() {
        this.playbackMixer = null;
    }

    public String getName() {
        if (playbackMixer != null)
            return "Device \"" + playbackMixer.getMixerInfo().getName() + "\"";
        else
            return "Device \"default\"";
    }

    synchronized public void close() {
        if (playbackLine != null) {
            playbackLine.drain();
            playbackLine.close();
        }
    }

    public boolean isClosed() {
        if (playbackLine == null)
            return true;
        return !playbackLine.isOpen();
    }

    public boolean canWrite() {
        return true;
    }

    synchronized public void open(AudioFormat format) throws AudioIOException {

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            if (playbackMixer != null)
                playbackLine = (SourceDataLine) playbackMixer.getLine(info);
            else
                playbackLine = (SourceDataLine) AudioSystem.getLine(info);
            
            playbackLine.open();
            
            limit = playbackLine.available() / 10 * 5;
            
            playbackLine.start();
        } catch (LineUnavailableException e1) {
            e1.printStackTrace();
        }
    }

    synchronized public int write(byte[] data, int offset, int length) throws AudioIOException {
        if (playbackLine.available() > limit) {
            return playbackLine.write(data, offset, length);
        }
        return 0;
    }
}
