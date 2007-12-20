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
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.actf.ai.audio.io.AudioIOException;
import org.eclipse.actf.ai.audio.io.IAudioReader;




public class RecordDevice implements IAudioReader {

    private Mixer recordMixer;

    private AudioFormat format;

    private TargetDataLine recordLine;

    @Override
    public boolean equals(Object o) {
        if (o instanceof RecordDevice) {
            RecordDevice rd = (RecordDevice) o;
            if (recordMixer.equals(rd.recordMixer)) {
                return format.getChannels() == rd.format.getChannels()
                        && format.getFrameRate() == rd.format.getFrameRate()
                        && format.getSampleSizeInBits() == rd.format.getSampleSizeInBits();
            }
        }
        return false;
    }

    public RecordDevice(Mixer mixer, int sampleRate, int channels) {
        this.recordMixer = mixer;
        format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, channels, 2 * channels, sampleRate,
                false);
        if (!recordMixer.isLineSupported(new DataLine.Info(TargetDataLine.class, format))) {
            check(sampleRate, channels);
        }
    }

    private void check(int sampleRate, int channels) {
        AudioFormat temp;
        int[] samples = new int[] { 48000, 44100, 22050, 16000, 8000 };

        outer: for (int i = 0; i < samples.length; i++) {
            if (samples[i] < sampleRate) {
                temp = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, samples[i], 16, channels, 2 * channels,
                        samples[i], false);
                if (recordMixer.isLineSupported(new DataLine.Info(TargetDataLine.class, temp))) {
                    format = temp;
                    break outer;
                }
            }
        }
    }

    public String getName() {
        return "Device \"" + recordMixer.getMixerInfo().getName() + //
                " (" + (format.getFrameRate() / 1000) + " kHz, " + format.getChannels() + "ch)" + "\"";
    }

    public AudioFormat getAudioFormat() {
        return format;
    }

    synchronized public void close() {
        if (recordLine != null)
            recordLine.close();
    }

    public boolean isClosed() {
        return !recordLine.isOpen();
    }

    public boolean canRead(){
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        return recordMixer.isLineSupported(info);
    }
    
    synchronized public void open() throws AudioIOException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            recordLine = (TargetDataLine) recordMixer.getLine(info);
            recordLine.open(format);
            recordLine.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    

    synchronized public int read(byte[] data, int offset, int length) throws AudioIOException {
        return recordLine.read(data, offset, length);
    }

}
