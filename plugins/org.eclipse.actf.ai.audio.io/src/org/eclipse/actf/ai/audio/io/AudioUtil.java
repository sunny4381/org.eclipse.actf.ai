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

import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;



/**
 * This utility provides some functions to manage AudioFormat in form of human readable.
 */
public class AudioUtil {

    /**
     * @param format The instance of AudioFormat to be used.
     * @return The total number of bytes per second in all channels.
     */
    public static int getBytesPerSecondLR(AudioFormat format) {
        return (int) getBytesPerSecond(format) * format.getChannels();
    }

    /**
     * @param format The instance of AudioFormat to be used.
     * @return The number of bytes per second in one channel.
     */
    public static int getBytesPerSecond(AudioFormat format) {
        return (int) getBitsPerSecond(format) / 8;
    }
    
    /**
     * @param format The instance of AudioFormat to be used.
     * @return The number of bits per second in one channel.
     */
    public static int getBitsPerSecond(AudioFormat format){
        return (int) format.getSampleRate() * format.getSampleSizeInBits();
    }

    /**
     * @param format The instance of AudioFormat to be used.
     * @return The number of samples per second in one channel.
     */
    public static int getSamplesPerSecond(AudioFormat format) {
        return (int) format.getSampleRate();
    }

    /**
     * @param format The instance of AudioFormat to be used.
     * @return The total number of samples per second in all channels.
     */
    public static int getSamplesPerSecondLR(AudioFormat format) {
        return (int) getSamplesPerSecond(format) * format.getChannels();
    }

    /**
     * @param format The instance of AudioFormat to be used.
     * @return The number of bytes per sample in one channel.
     */
    public static int getBytesPerSample(AudioFormat format){
        return format.getSampleSizeInBits() / 8;
    }

    /**
     * @param format The instance of AudioFormat to be used.
     * @return The total number of bytes per sample in all channels.
     */
    public static int getBytesPerSampleLR(AudioFormat format) {
        return getBytesPerSample(format) * format.getChannels();
    }

    /**
     * @param format The instance of AudioFormat to be used.
     * @return The ByteOrder of the format.
     */
    public static ByteOrder getEndian(AudioFormat format) {
        if(format.isBigEndian())
            return ByteOrder.BIG_ENDIAN;
        else
            return ByteOrder.LITTLE_ENDIAN;
    }

}
