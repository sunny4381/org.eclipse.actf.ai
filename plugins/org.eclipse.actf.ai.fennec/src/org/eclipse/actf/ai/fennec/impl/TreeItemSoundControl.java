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

package org.eclipse.actf.ai.fennec.impl;

import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.model.dom.dombycom.AnalyzedResult;
import org.eclipse.actf.model.dom.dombycom.INodeExSound;



public class TreeItemSoundControl implements ISoundControl {
    private AnalyzedResult analyzedResult;

    public boolean muteMedia() {
        INodeExSound[] sounds = analyzedResult.getSoundNodes();
        boolean r = true;
        for (int i = 0; i < sounds.length; i++) {
            boolean st = sounds[i].getMuteState();
            r &= sounds[i].muteMedia(!st);
        }
        return r;
    }

    public VolumeState getVolumeState() {
        INodeExSound[] sounds = analyzedResult.getSoundNodes();
        boolean mute = true;
        boolean max = true;
        boolean min = true;
        for (int i = 0; i < sounds.length; i++) {
            mute &= sounds[i].getMuteState();
            int vol = sounds[i].getVolume();
            max &= (vol == INodeExSound.VOLUME_MAX);
            min &= (vol == INodeExSound.VOLUME_MIN);
        }
        if (mute) return VolumeState.MUTE;
        if (max) return VolumeState.MAX;
        if (min) return VolumeState.MIN;

        return VolumeState.OTHER;
    }

    private static final int VOLUME_TICK = 150;
    private static final int VOLUME_TICK_MINIMAL = 10;

    public boolean volumeDownMedia() {
        return volumeDownMedia(VOLUME_TICK);
    }
    
    public boolean minimalVolumeDownMedia() {
        return volumeDownMedia(VOLUME_TICK_MINIMAL);
    }
    
    private boolean volumeDownMedia(int tick) {
        INodeExSound[] sounds = analyzedResult.getSoundNodes();
        boolean r = true;
        for (int i = 0; i < sounds.length; i++) {
            int vol = sounds[i].getVolume();
            if (vol < 0) {
                return false;
            } else if (vol < (INodeExSound.VOLUME_MIN + tick)) {
                vol = INodeExSound.VOLUME_MIN;
            } else {
                vol -= tick;
            }
            r &= sounds[i].setVolume(vol);
        }
        return r;
    }

    public boolean volumeUpMedia() {
        return volumeUpMedia(VOLUME_TICK);
    }        
    
    public boolean minimalVolumeUpMedia() {
        return volumeUpMedia(VOLUME_TICK_MINIMAL);
    }

    private boolean volumeUpMedia(int tick) {
        INodeExSound[] sounds = analyzedResult.getSoundNodes();
        boolean r = true;
        for (int i = 0; i < sounds.length; i++) {
            int vol = sounds[i].getVolume();
            if (vol < 0) {
                return false;
            } else if (vol > (INodeExSound.VOLUME_MAX - tick)) {
                vol = INodeExSound.VOLUME_MAX;
            } else {
                vol += tick;
            }
            r &= sounds[i].setVolume(vol);
        }
        return r;
    }

    public int getCount() {
        INodeExSound[] sounds = analyzedResult.getSoundNodes();
        return sounds.length;
    }

    public static ISoundControl newTreeItemSoundControl(AnalyzedResult ar) {
        return new TreeItemSoundControl(ar);
    }

    private TreeItemSoundControl(AnalyzedResult ar) {
        this.analyzedResult = ar;
    }

    public int[] getVolumes(){
        INodeExSound[] sounds = analyzedResult.getSoundNodes();
        boolean r = true;
        int[] volumes = new int[sounds.length];
        for (int i = 0; i < sounds.length; i++) {
            volumes[i] = sounds[i].getVolume();
            
            if (volumes[i] < 0) {
                volumes[i] = 0;
            }
        }
        return volumes;
    }

    public boolean setVolumes(int[] volumes) {
        INodeExSound[] sounds = analyzedResult.getSoundNodes();
        boolean r = true;
        for (int i = 0; i < sounds.length; i++) {
            r &= sounds[i].setVolume(volumes[i]);
        }
        return r;
    }
}
