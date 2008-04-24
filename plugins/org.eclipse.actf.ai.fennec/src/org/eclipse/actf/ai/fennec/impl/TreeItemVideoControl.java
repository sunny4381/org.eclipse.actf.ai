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

import java.util.ArrayList;

import org.eclipse.actf.ai.fennec.IFennecService;
import org.eclipse.actf.ai.fennec.treemanager.IMediaSyncEventListener;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.model.dom.dombycom.AnalyzedResult;
import org.eclipse.actf.model.dom.dombycom.INodeEx;
import org.eclipse.actf.model.dom.dombycom.INodeExVideo;
import org.eclipse.actf.util.timer.WeakSyncTimer;
import org.eclipse.actf.util.vocab.IProposition;
import org.eclipse.actf.util.vocab.Vocabulary;



public class TreeItemVideoControl implements IVideoControl {
    private final IFennecService fennecService;
    private AnalyzedResult analyzedResult;

    private INodeExVideo getCurrentNodeExVideo() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        if (videos.length == 0) return null;
        if (videos.length == 1) return videos[0];

        ITreeItem item = fennecService.getLastTreeItem();
        for (int i = 0; i < videos.length; i++) {
            INodeEx node = videos[i].getReferenceNode();
            IProposition prop = Vocabulary.nodeLocation(node, true);
            if (prop.eval(item)) return videos[i];
        }
        return videos[0];
    }

    public boolean previousTrack() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        boolean r = true;
        for (int i = 0; i < videos.length; i++) {
            r &= videos[i].previousTrack();
        }
        return r;
    }

    public boolean nextTrack() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        boolean r = true;
        for (int i = 0; i < videos.length; i++) {
            r &= videos[i].nextTrack();
        }
        return r;
    }

    public boolean stopMedia() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        boolean r = true;
        for (int i = 0; i < videos.length; i++) {
            r &= videos[i].stopMedia();
        }
        return r;
    }

    public boolean playMedia() {
        INodeExVideo v = getCurrentNodeExVideo();

        // Before playing the media, pause the other media to avoid audio interference.
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        for (int i = 0; i < videos.length; i++) {
            if (videos[i] != v) {
                videos[i].pauseMedia();
            }
        }
        if (v != null) {
            return v.playMedia();
        } else {
            return false;
        }
    }

    public boolean pauseMedia() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        boolean pause = false;
        for (int i = 0; i < videos.length; i++) {
            INodeExVideo.VideoState st = videos[i].getCurrentState();
            if ((st == INodeExVideo.VideoState.STATE_PLAY)
                || (st == INodeExVideo.VideoState.STATE_UNKNOWN)) {
                pause = true;
                break;
            }
        }

        if (pause) {
            boolean r = true;
            for (int i = 0; i < videos.length; i++) {
                r &= videos[i].pauseMedia();
            }
            return r;
        } else {
            INodeExVideo v = getCurrentNodeExVideo();
            if (v != null) {
                return v.playMedia();
            } else {
                return false;
            }
        }
    }

    public IVideoControl.VideoState getVideoState() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        IVideoControl.VideoState st = IVideoControl.VideoState.STATE_OTHER;
        for (int i = 0; i < videos.length; i++) {
            INodeExVideo.VideoState stn = videos[i].getCurrentState();
            switch (stn) {
            case STATE_UNKNOWN:
                break;
            case STATE_PLAY:
                st = IVideoControl.VideoState.STATE_PLAY;
                break;
            case STATE_STOP:
                if (st == IVideoControl.VideoState.STATE_OTHER) {
                    st = IVideoControl.VideoState.STATE_STOP;
                }
                break;
            case STATE_PAUSE:
                if (st == IVideoControl.VideoState.STATE_OTHER) {
                    st = IVideoControl.VideoState.STATE_PAUSE;
                }
                break;
            case STATE_WAITING:
                if (st == IVideoControl.VideoState.STATE_OTHER) {
                    st = IVideoControl.VideoState.STATE_WAITING;
                }
                break;
            case STATE_FASTFORWARD:
                if (st != IVideoControl.VideoState.STATE_PLAY) {
                    st = IVideoControl.VideoState.STATE_FASTFORWARD;
                }
                break;
            case STATE_FASTREVERSE:
                if (st != IVideoControl.VideoState.STATE_PLAY) {
                    st = IVideoControl.VideoState.STATE_FASTREVERSE;
                }
            }
        }
        return st;
    }

    public boolean fastReverse() {
        INodeExVideo v = getCurrentNodeExVideo();
        if (v != null) {
            return v.fastReverse();
        } else {
            return false;
        }
    }

    public boolean fastForward() {
        INodeExVideo v = getCurrentNodeExVideo();
        if (v != null) {
            return v.fastForward();
        } else {
            return false;
        }
    }

    public double getCurrentPosition() {
        INodeExVideo v = getCurrentNodeExVideo();
        if (v != null) {
            return v.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public double getTotalLength() {
        INodeExVideo v = getCurrentNodeExVideo();
        if (v != null) {
            return v.getTotalLength();
        } else {
            return 0;
        }
    }

    public int getCount() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        return videos.length;
    }

    public int getIndex() {
        INodeExVideo[] videos = analyzedResult.getVideoNodes();
        if (videos.length == 0) return -1;
        if (videos.length == 1) return 0;

        ITreeItem item = fennecService.getLastTreeItem();
        for (int i = 0; i < videos.length; i++) {
            INodeEx node = videos[i].getReferenceNode();
            IProposition prop = Vocabulary.nodeLocation(node, true);
            if (prop.eval(item)) return i;
        }
        return 0;
    }


    public static IVideoControl newTreeItemVideoControl(AnalyzedResult ar,
                                                        IFennecService fennecService) {
        return new TreeItemVideoControl(ar, fennecService);
    }

    private TreeItemVideoControl(AnalyzedResult ar,
                                 IFennecService fennecService) {
        this.analyzedResult = ar;
        this.fennecService = fennecService;
    }

    // This ArrayList ensures strong references of currently valid listeners.
    private ArrayList<IMediaSyncEventListener> listeners;
    public boolean addEventListener(IMediaSyncEventListener listener) {
        WeakSyncTimer timer = WeakSyncTimer.getTimer();
        timer.addEventListener(listener);
        if (listeners == null) {
            listeners = new ArrayList<IMediaSyncEventListener>(1);
        }
        listeners.add(listener);
        
        return true;
    }
}
