/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/

package org.eclipse.actf.ai.fennec.treemanager;


public interface ISoundControl {
    public enum VolumeState {
        MIN,
        MAX,
        OTHER,
        MUTE
    }

    boolean muteMedia();

    VolumeState getVolumeState();

    boolean volumeDownMedia();

    boolean volumeUpMedia();
    
    boolean minimalVolumeDownMedia();
    
    boolean minimalVolumeUpMedia();

    int getCount();
    
    int[] getVolumes();
    
    boolean setVolumes(int[] volumes);
}
