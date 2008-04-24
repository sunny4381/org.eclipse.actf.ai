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

/**
 * INavigatorUI represents a browser tab of the application.
 * This interface defines methods to control browsers from user interface part.
 */
public interface INavigatorUI {
    // Tree Navigation Mode
    /**
     * Mute the media volume.
     */
    void muteMedia();
    /**
     * Decrease the volume of the media which are included in the browser.
     */
    void volumeDownMedia();
    /**
     * Increase the volume of the media which are included in the browser.
     */
    void volumeUpMedia();
    /**
     * Decrease the volume of the media in minimal step.
     */
    void minimalVolumeDownMedia();
    /**
     * Increase the volume of the media in minimal step.
     */
    void minimalVolumeUpMedia();
    
    /**
     * Change the audio track to the previous one.
     */
    void previousTrack();
    /**
     * Change the audio track to the next one.
     */
    void nextTrack();
    /**
     * Stop the media.
     */
    void stopMedia();
    /**
     * Play the media. The medias should be controllable from the out of the application.
     * For example, shortcut key or something.
     */
    void playMedia();
    /**
     * Pause the media.
     */
    void pauseMedia();
    /**
     * Do fast reverse of the media.
     */
    void fastReverse();
    /**
     * Do fast forward of the media.
     */
    void fastForward();
    
    /**
     * Stop the speaking of the voice engine.
     */
    void stopSpeak();

    /**
     * Set the speed of the voice engine to faster.
     */
    void speechSpeedUp();
    /**
     * Set the speed of the voice engine to slower.
     */
    void speechSpeedDown();
    
    /**
     * Move the current position to the tree parent. 
     */
    void treeLeft();
    /**
     * Move the current position to the tree children.
     */
    void treeRight();
    /**
     * Move the current position the the previous sibling.
     * If there is a previous sibling then the current position will be the sibling.
     * If there is no previous sibling then the current position will be not changed.
     */
    void treeUp();
    /**
     * Move the current position to the next sibling
     * If there is a next sibling then the current position will be the sibling.
     * If there is no next sibling then the current position will be not changed.
     */
    void treeDown();

    /**
     * Move the current position to the top of the tree. First node of the tree.
     */
    void treeTop();
    /**
     * Move the current position to the bottom of the tree. Last node of the tree.
     */
    void treeBottom();

    /**
     * @deprecated
     * @see #traverseUp()
     */
    void traverseNodeUp();
    /**
     * @deprecated
     * @see #traverseDown()
     */
    void traverseNodeDown();
    /**
     * Move the current position to the next node which can be read.
     * The traverse is executed in depth first strategy.
     */
    void traverseDown();
    /**
     * Move the current position to the previous node which can be read.
     * The traverse is executed in opposite direction of {@link #traverseDown()}.
     */
    void traverseUp();

    /**
     * Send click event or command to the node of the current position.
     * The user can click the element by alternative way. 
     */
    void click();
    /**
     * Speak the current status of the application.
     */
    void speakCurrentStatus();
    /**
     * Speak the media status of the browser.
     */
    void speakMediaStatus();

    /**
     * Move the current position to the next header element. It will stop at H1, H2, H3, H4 ,H5, H6, and headers defined by the metadata.
     */
    void nextHeader();
    /**
     * Move the current position to the previous header element. It will stop at H1, H2, H3, H4 ,H5, H6, and headers defined by the metadata.
     */
    void previousHeader();

    /**
     * Move the current position to the next inputable element. Text input, text area, select and so on.
     */
    void nextInputable();
    /**
     * Move the current position to the previous inputable element. Text input, text area, select and so on.
     */
    void previousInputable();
    
    /**
     * Move the current position to the next link element.
     */
    void nextLink();
    /**
     * Move the current position to the previous link element.
     */
    void previousLink();

    /**
     * Move the current position to the next object element. Flash, Media Player, and so on.
     */
    void nextObject();
    /**
     * Move the current position to the previous object element. Flash, Media Player, and so on.
     */
    void previousObject();

    /**
     * Move the current position to the node of the left cell, if the current position is table cell. 
     */
    void cellLeft();
    /**
     * Move the current position to the node of the right cell, if the current position is table cell. 
     */
    void cellRight();
    /**
     * Move the current position to the node of the upper cell, if the current position is table cell. 
     */
    void cellUp();
    /**
     * Move the current position to the node of the lower cell, if the current position is table cell. 
     */
    void cellDown();

    // Browser control
    /**
     * Refresh page of the current tab of the browser.
     */
    void navigateRefresh();
    // void goForward();
    // void goBackward();

    // select Fennec;
    /**
     * Change the metadata to the next one.
     * metadata1 -> metadata2 -> ... -> none -> matadata1 -> ...
     */
    void selectNextFennec();

    // Form Mode;
    /**
     * The mode of application is changed from form mode to tree navigation mode.
     */
    void exitFormMode();
    /**
     * Do submit of the current form element.
     */
    void submitForm();

    // browser address.
    /**
     * The keyboard focus is moved into the address bar of the application. 
     */
    void enterBrowserAddress();

    // tab operations.
    /**
     * Close the current focused tab.
     */
    void closeTab();

    /**
     * Open the search dialog to search text in the browser.
     * The option of the direction is set to forward.
     */
    void searchNext();
    /**
     * Open the search dialog to search text in the browser.
     * The option of the direction is set to backward.
     */
    void searchPrevious();
    
    /**
     * Move the current position to the next heading level 1 element. H1.
     */
    void nextHeader1();
    /**
     * Move the current position to the previous heading level 1 element. H1.
     */
    void previousHeader1();
    /**
     * Move the current position to the next heading level 2 element. H2.
     */
    void nextHeader2();
    /**
     * Move the current position to the previous heading level 2 element. H2.
     */
    void previousHeader2();
    /**
     * Move the current position to the next heading level 3 element. H3.
     */
    void nextHeader3();
    /**
     * Move the current position to the previous heading level 3 element. H3.
     */
    void previousHeader3();
    /**
     * Move the current position to the next heading level 4 element. H4.
     */
    void nextHeader4();
    /**
     * Move the current position to the previous heading level 4 element. H4.
     */
    void previousHeader4();
    /**
     * Move the current position to the next heading level 5 element. H5.
     */
    void nextHeader5();
    /**
     * Move the current position to the previous heading level 5 element. H5.
     */
    void previousHeader5();
    /**
     * Move the current position to the next heading level 6 element. H6.
     */
    void nextHeader6();
    /**
     * Move the current position to the previous heading level 6 element. H6.
     */
    void previousHeader6();
    
    /**
     * Move the current position to the next list item.
     */
    void nextListItem();
    /**
     * Move the current position to the previous list item.
     */
    void previousListItem();
    /**
     * Move the current position to the next block.
     * The block is defined by heuristic, it might be a proper size of information block.
     */
    void nextBlock();
    /**
     * Move the current position to the previous block.
	 * @see #nextBlock();
     */
    void previousBlock();

    /**
     * Move the current position to the next media object. Flash, Video player, and so on.
     */
    void nextMedia();
    /**
     * Move the current position to the previous media object. Flash, Video player, and so on.
     */
    void previousMedia();
    
    /**
     * Open the system default browser with the URL which is opened in the current focused browser.
     */
    void launchBrowser();
    /**
     * Toggle show/hide the views that are the left part of the application.
     */
    void toggleLeftViewsShowing();
    /**
     * Start the continuous speaking from the current position.
     * The speaking can be stopped by using {@link #stopSpeak()}.
     */
    void speakAll();
    
    /**
     * Open the file dialog to export the all metadata entry.
     */
    void exportMetadata();
    
    /**
     * Move the current position to the node which has specified access key.
     * @param key The key code of the access key.
     */ 
    void jumpToAccessKey(char key);
    /**
     * Open the dialog which shows the list of the access key in the page.
     */
    void showAccessKeyList();
    
    // force start.
    /**
     * Do restart the analyze of the page.
     * It is used when the analyze is failed because of the timing or something.
     */
    void forceRestart();
    /**
     * Toggle enable/disable the audio description function.
     */
    void toggleDescriptionEnable();
    /**
     * Change the focus to the next tab.
     */
    void nextTab();
    /**
     * Change the focus to the previous tab.
     */
    void prevTab();

    // User Annotation Extension.
    /**
     * Move the current position to the next "alterable" element.
     * "Alterable" means that the text of the element can be replaced by the application.
     * For example, image alternative text.
     */
    void nextAlterable();
    /**
     * Move the current position to the previous "alterable" element.
	 * @see #nextAlterable();
     */
    void previousAlterable();
    /**
     * Open the dialog to edit the alternative text.
     */
    void editAltText();
    /**
     * Make a landmark for the current position.  
     * If there is a landmark on the element of the current position, then the landmark will be removed.
     */
    void makeLandmark();
    /**
     * Save the information about the alternative text and the landmark (user annotation) into the file. 
     */
    void saveUserInfo();
    /**
     * Remove the user annotation which is provided for the current page.
     */
    void removeUserInfo();
    
    // repair
    /**
     * Do repair Flash content in the current page. This is a prototype function.
     */
    void repairFlash();
}
