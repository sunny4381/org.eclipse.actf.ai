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

import javax.sound.sampled.AudioFormat;

import org.eclipse.actf.ai.audio.io.AudioIOException;





public interface IAudioWriter {
    
    String getName();
    
    int write(byte[] data, int offset, int length) throws AudioIOException;

    boolean canWrite();
    
    void open(AudioFormat format) throws AudioIOException;

    void close();

    boolean isClosed();
}
