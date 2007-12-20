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

package org.eclipse.actf.ai.audio.io;

import java.net.URL;

import javax.sound.sampled.Mixer;

import org.eclipse.actf.ai.audio.io.impl.AudioFileReader;
import org.eclipse.actf.ai.audio.io.impl.AudioPipe;
import org.eclipse.actf.ai.audio.io.impl.LoopedAudioPipe;
import org.eclipse.actf.ai.audio.io.impl.PlaybackDevice;
import org.eclipse.actf.ai.audio.io.impl.RecordDevice;




public class AudioFactory {
    public static IAudioReader createAudioReader(String file) {
        return new AudioFileReader(file);
    }

    public static IAudioReader createAudioReader(URL url) {
        return new AudioFileReader(url);
    }
    
    public static IAudioWriter createDefaultWriter() {
        return new PlaybackDevice();
    }

    public static IAudioPipe createAudioPipe(IAudioReader reader, IAudioWriter writer) {
        return new AudioPipe(reader, writer);
    }
    
    public static IAudioPipe createLoopedAudioPipe(IAudioReader reader, IAudioWriter writer) {
        return new LoopedAudioPipe(reader, writer);
    }

    public static IAudioWriter createAudioWriter(Mixer mixer) {
        return new PlaybackDevice(mixer);
    }

    public static IAudioReader createAudioReader(Mixer mixer, int recordingSmapleRate, int recordingChannels) {
        return new RecordDevice(mixer, recordingSmapleRate, recordingChannels);
    }

    public static void main(String[] args) {
        IAudioReader reader = AudioFactory.createAudioReader("16-16-short-pcm-lpf.wav");
        IAudioWriter writer = AudioFactory.createDefaultWriter();
        IAudioPipe pipe = AudioFactory.createLoopedAudioPipe(reader, writer);
        
        // buffer size for reading (in milli seconds)
        pipe.setBufferSize(100);    
        
        // interval time to try writing (in milli seconds)
        pipe.setInterval(1);       

        // Create new thread and start playing. 
        // The pipe automatically opens reader and writer.
        pipe.prepare();
        
        pipe.start(); 
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            
        pipe.stop();
        
        pipe.start(); 
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            
        pipe.stop();
    }

}
