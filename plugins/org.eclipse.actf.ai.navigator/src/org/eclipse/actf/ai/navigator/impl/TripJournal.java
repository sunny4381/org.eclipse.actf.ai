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

package org.eclipse.actf.ai.navigator.impl;

import org.eclipse.actf.ai.fennec.treemanager.ILocation;
import org.eclipse.actf.ai.navigator.impl.NavigatorImpl.FocusTabResult;
import org.eclipse.actf.ai.navigator.voice.VoiceManager;




class TripJournal {
    private static final boolean DEBUG = false;
    private static final int JOURNAL_SIZE = 1024;

    private static final VoiceManager voice = new VoiceManager(null);

    private static class TripJournalItem {
        final NavigatorImpl navigator;
        final ILocation location;
        final String url;

        TripJournalItem(NavigatorImpl navigator,
                        ILocation location,
                        String url) {
            this.navigator = navigator;
            this.location = location;
            this.url = url;
        }
    }

    private int startIdx = 0;
    private int endIdx = 0;
    private int currentIdx = 0;
    private int recordIdx = 0;
    private TripJournalItem[] tripJournalItems = new TripJournalItem[JOURNAL_SIZE];

    private boolean backwarding;
    private boolean forwarding;
    

    private int incrementIdx(int idx) {
        idx++;
        if (idx == tripJournalItems.length) {
            idx = 0;
        }
        return idx;
    }

    private int decrementIdx(int idx) {
        if (idx == 0) {
            idx = tripJournalItems.length;
        }
        return --idx;
    }

    private void recordInPlay(NavigatorImpl navigator,
                              ILocation location,
                              String url) {
        TripJournalItem tjiOld = tripJournalItems[recordIdx];
        if ((location == null) && (tjiOld != null)) {
            location = tjiOld.location;
        }
        TripJournalItem tji = new TripJournalItem(navigator, location, url);
        tripJournalItems[recordIdx] = tji;
    }

    public void recordJournal(NavigatorImpl navigator,
                              ILocation location,
                              String url,
                              boolean init) {
        if (init) {
            if (DEBUG) System.err.println("Record-I");
            currentIdx = incrementIdx(currentIdx);
            endIdx = currentIdx;
            tripJournalItems[currentIdx] = new TripJournalItem(navigator, location, url);
            return;
        }

        if (!navigator.isFocused()) {
            if (DEBUG) System.err.println("Not Recorded (out of focus)");
            return;
        }

        if (backwarding) {
            if (DEBUG) System.err.println("Record-B!");
            recordInPlay(navigator, location, url);
            backwarding = false;
        } else if (forwarding) {
            if (DEBUG) System.err.println("Record-F!");
            recordInPlay(navigator, location, url);
            forwarding = false;
        } else {
            if (DEBUG) System.err.println("Record!:" + url);
            TripJournalItem tjiOld = tripJournalItems[currentIdx];
            if (tjiOld != null) {
                if (DEBUG) System.err.println("Old-URL:" + tjiOld.url);
                if (url != null) {
                    if (url.equals(tjiOld.url)) return;
                }
            }
            TripJournalItem tji = new TripJournalItem(navigator, location, url);
            tripJournalItems[currentIdx] = tji;
            currentIdx = incrementIdx(currentIdx);
            endIdx = currentIdx;
            if (startIdx == endIdx) {
                startIdx = incrementIdx(startIdx);
            }
        }
    }

    TripJournal() {
    }

    // --------------------------------------------------------------------------------
    //  Browser Control
    // --------------------------------------------------------------------------------

    private void sayForward() {
        voice.speakWithFormat("TripJournal.FORWARD", true, false);
    }

    private void sayForwardFailed() {
        voice.speakWithFormat("TripJournal.FAILEDTOFORWARD", true, false);
    }

    public void forward(NavigatorImpl currentNavigartor) {
        //if (backwarding || forwarding) return;
        recordIdx = currentIdx;
        int idxFwd = currentIdx;
        for (;;) {
            if (idxFwd == endIdx) {
                sayForwardFailed();
                return;
            }
            idxFwd = incrementIdx(idxFwd);
            TripJournalItem tji = tripJournalItems[idxFwd];
            if (tji == null) {
                // This must not happen, but navigateComplete failed to record a trip log
                // and the entry may be possibly missing.  So try to emulate the forwarding.
                forwarding = true;
                currentIdx = idxFwd;
                currentNavigartor.goForward();
                currentNavigartor.restoreLocation(null);
                return;
            }
            switch (tji.navigator.focusTab()) {
            case STAY:
                // Notice that it will invoke recordJournal method;
                forwarding = true;
                currentIdx = idxFwd;
                tji.navigator.goForward();
                tji.navigator.restoreLocation(tji.location);
                sayForward();
                return;
            case CHANGED:
                currentIdx = idxFwd;
                sayForward();
                return;
            case NOTFOUND:
                break;
            }
        }
    }

    private void sayBackward() {
        voice.speakWithFormat("TripJournal.BACKWARD", true, false);
    }

    private void sayBackwardFailed() {
        voice.speakWithFormat("TripJournal.FAILEDTOBACKWARD", true, false);
    }

    public void backward(NavigatorImpl currentNavigator) {
        //if (backwarding || forwarding) return;
        recordIdx = currentIdx;
        int idxBack = currentIdx;
        if (DEBUG) System.err.println("back:" + currentIdx);
        for (;;) {
            if (idxBack == startIdx) {
                sayBackwardFailed();
                return;
            }
            idxBack = decrementIdx(idxBack);
            TripJournalItem tji = tripJournalItems[idxBack];
            if (tji == null) {
                // Since the item was not recorded, focus the previous item.
                if (idxBack == startIdx) {
                    sayBackwardFailed();
                    return;
                }
                currentIdx = idxBack;
                idxBack = decrementIdx(idxBack);
                tji = tripJournalItems[idxBack];
                if (tji != null) {
                    tji.navigator.focusTab();
                }
                sayBackward();
                return;
            }
            switch (tji.navigator.focusTab()) {
            case STAY:
                // Notice that it will invoke recordJournal method;
                backwarding = true;
                currentIdx = idxBack;
                tji.navigator.goBackward();
                tji.navigator.restoreLocation(tji.location);
                sayBackward();
                return;
            case CHANGED:
                if (currentNavigator == null) {
                    endIdx = idxBack;
                } else {
                    ILocation location = currentNavigator.getLocation();
                    int idx = incrementIdx(idxBack);
                    tripJournalItems[idx] = new TripJournalItem(currentNavigator,
                                                                location,
                                                                currentNavigator.getCurrentURL());
                } 
                currentIdx = idxBack;
                sayBackward();
                return;
            case NOTFOUND:
                break;
            }
        }
    }

    public void tripEnd() {
        forwarding = false;
        backwarding = false;
    }

    public void refreshEnd() {
        TripJournalItem tji = tripJournalItems[currentIdx];
        if (tji == null) return;
        if (tji.navigator.focusTab() == FocusTabResult.STAY) {
            tji.navigator.restoreLocation(tji.location);
        }
    }
}
