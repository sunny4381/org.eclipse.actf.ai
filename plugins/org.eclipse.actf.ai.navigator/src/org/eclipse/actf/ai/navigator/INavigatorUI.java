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
package org.eclipse.actf.ai.navigator;

public interface INavigatorUI {
    // Tree Navigation Mode
    void muteMedia();
    void volumeDownMedia();
    void volumeUpMedia();
    
    void minimalVolumeDownMedia();
    void minimalVolumeUpMedia();
    
    void previousTrack();
    void nextTrack();
    void stopMedia();
    void playMedia();
    void pauseMedia();
    void fastReverse();
    void fastForward();
    
    void stopSpeak();

    void speechSpeedUp();
    void speechSpeedDown();
    
    void treeLeft();
    void treeRight();
    void treeUp();
    void treeDown();

    void treeTop();
    void treeBottom();

    void traverseNodeUp();
    void traverseNodeDown();
    void traverseDown();
    void traverseUp();

    void click();
    void speakCurrentStatus();
    void speakMediaStatus();

    void nextHeader();
    void previousHeader();

    void nextInputable();
    void previousInputable();
    
    void nextLink();
    void previousLink();

    void nextObject();
    void previousObject();

    void cellLeft();
    void cellRight();
    void cellUp();
    void cellDown();

    // Browser control
    void navigateRefresh();
    // void goForward();
    // void goBackward();

    // select NVM3;
    void selectNextNVM3();

    // Form Mode;
    void exitFormMode();
    void submitForm();

    // browser address.
    void enterBrowserAddress();

    // tab operations.
    void closeTab();

    void searchNext();
    void searchPrevious();
    
    void nextHeader1();
    void previousHeader1();
    void nextHeader2();
    void previousHeader2();
    void nextHeader3();
    void previousHeader3();
    void nextHeader4();
    void previousHeader4();
    void nextHeader5();
    void previousHeader5();
    void nextHeader6();
    void previousHeader6();
    
    void nextListItem();
    void previousListItem();
    void nextBlock();
    void previousBlock();

    void nextMedia();
    void previousMedia();
    
    void launchBrowser();
    void toggleLeftViewsShowing();
    void speakAll();
    
    void exportMetadata();
    
    void jumpToAccessKey(char key);
    void showAccessKeyList();
    
    // force start.
    void forceRestart();
    void toggleDescriptionEnable();
    void nextTab();
    void prevTab();

    // User Annotation Extension.
    void nextAlterable();
    void previousAlterable();
    void editAltText();
    void makeLandmark();
    void saveUserInfo();
    void removeUserInfo();
    
    // repairt
    void repairFlash();
}
