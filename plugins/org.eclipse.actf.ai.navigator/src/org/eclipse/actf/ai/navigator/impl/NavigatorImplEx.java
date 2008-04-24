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

import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerException;
import org.eclipse.actf.ai.navigator.IManipulator;
import org.eclipse.actf.ai.navigator.NavigatorPlugin;
import org.eclipse.actf.ai.navigator.preferences.UserInfoPreferenceConstants;
import org.eclipse.actf.ai.navigator.userinfo.IMetaDataModifier;
import org.eclipse.actf.ai.navigator.userinfo.IUserInfoGenerator;
import org.eclipse.actf.ai.navigator.userinfo.impl.AltInputDialog;
import org.eclipse.actf.ai.navigator.userinfo.impl.AltTextEditor;
import org.eclipse.actf.ai.navigator.userinfo.impl.AltTextGuesser;
import org.eclipse.actf.ai.navigator.userinfo.impl.BrowserObserver;
import org.eclipse.actf.ai.navigator.userinfo.impl.HeadingCanceller;
import org.eclipse.actf.ai.navigator.userinfo.impl.LandmarkMaker;
import org.eclipse.actf.ai.navigator.userinfo.impl.MetaDataModifier;
import org.eclipse.actf.ai.navigator.util.ContentShortener;
import org.eclipse.actf.ai.xmlstore.XMLStoreException;
import org.eclipse.actf.model.IWebBrowserACTF;
import org.eclipse.actf.util.ui.PlatformUIUtil;
import org.eclipse.actf.util.vocab.Vocabulary;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


public class NavigatorImplEx extends NavigatorImpl {
    private final IPreferenceStore preferenceStore;

    private final ContentShortener contentShortener;

    private final BrowserObserver observer;

    private IWebBrowserACTF webBrowser;

    public NavigatorImplEx(WebEventListener webEventListener,
                              IWebBrowserACTF webBrowser,
                              int maxRetry, int retryInterval) {
        super(webEventListener, webBrowser, maxRetry, retryInterval);

        this.webBrowser = webBrowser;
        this.preferenceStore = NavigatorPlugin.getDefault().getPreferenceStore();
        this.contentShortener = new ContentShortener(32, getMessageFormatter());
        this.observer = new BrowserObserver(webBrowser);
    }

    public void saveUserInfo() {
        if (getCurrentEntry() != null && !getCurrentEntry().isUserEntry()) {
            speakUnavailableUserInfo();
            return;
        }
        try {
            IMetaDataModifier modifier = new MetaDataModifier();
            // modifier.setGenerator(null);
            modifier.setSite(observer.getTargetFilter());
            modifier.setPageTitle(PlatformUIUtil.getActiveEditor().getTitle());
            modifier.commit(true);
            speakWithFormat("Navigator.ANNOTATION_IS_SAVED", true);
        } catch (XMLStoreException e) {
            e.printStackTrace();
        }
    }

    public void guessAltText() {
        if (getCurrentEntry() != null && !getCurrentEntry().isUserEntry()) {
            speakUnavailableUserInfo();
            return;
        }
        try {
            ITreeItem item = getTreeManager().getCurrentRootItem();
            setMode(IManipulator.KEYHOOK_DISABLED_MODE);
            item.setFocus();
            int k = guessAltTextIter(item);
            if (k > 0) {
                String mes = getMessageFormatter().mes("Navigator.ALT_TEXT_WERE_GUESSED", k);
                speak(mes, true);
                if (preferenceStore.getBoolean(UserInfoPreferenceConstants.AUTO_SAVE))
                    saveUserInfo();
                if (preferenceStore.getBoolean(UserInfoPreferenceConstants.AUTO_REFRESH))
                    refresh();
            }
            else {
                speakWithFormat("Navigator.THERE_IS_NO_MISSING_ALT_TEXT", true);
            }
            item.setFocus();
            setMode(IManipulator.TREE_NAVIGATION_MODE);
        } catch (TreeManagerException e) {
            e.printStackTrace();
        } catch (XMLStoreException e) {
            e.printStackTrace();
        }
    }

    private int guessAltTextIter(ITreeItem item) throws XMLStoreException {
        int k = 0;
        ITreeItem[] v = item.getChildItems();

        for (int i = 0; i < v.length; i++) {
            ITreeItem c = v[i];
            k += guessAltTextIter(c);
        }

        if (!Vocabulary.isAlterable().eval(item)) {
            return k;
        }

        if (isGoodAltText(item.getUIString())) {
            return k;
        }

        AltTextGuesser guesser = new AltTextGuesser(webBrowser, item);
        String newText = guesser.guessByContext();

        if (newText == null || newText.length() == 0) {
            return k;
        }

        IMetaDataModifier modifier = new MetaDataModifier();
        modifier.setGenerator(new AltTextEditor());
        modifier.setSite(observer.getTargetFilter());
        modifier.setPageTitle(PlatformUIUtil.getActiveEditor().getTitle());
        modifier.setItem(item);
        
        modifier.setText(getMessageFormatter().mes("Navigator.ANNOTATION_LINK_TO", newText));
        IUserInfoGenerator.Result result = modifier.commit(false);

        if (result != IUserInfoGenerator.Result.NOTHING)
            ++k;

        return k;
    }

    private boolean isGoodAltText(String s) {
        return s.length() > 0;
    }

    public void nextAlterable() {
        if (getCurrentEntry() != null && !getCurrentEntry().isUserEntry()) {
            speakUnavailableUserInfo();
            return;
        }
        findNext(Vocabulary.isAlterable(), JumpMode.ALTALABLE);
    }

    public void previousAlterable() {
        if (getCurrentEntry() != null && !getCurrentEntry().isUserEntry()) {
            speakUnavailableUserInfo();
            return;
        }
        findPrevious(Vocabulary.isAlterable(), JumpMode.ALTALABLE);
    }

    public void editAltText() {
        if (getCurrentEntry() != null && !getCurrentEntry().isUserEntry()) {
            speakUnavailableUserInfo();
            return;
        }

        try {
//            refresh();
            ITreeItem item = getTreeManager().getActiveItem();
            if (item == null)
                return;
            if (Vocabulary.isAlterable().eval(item)) {
                setMode(IManipulator.KEYHOOK_DISABLED_MODE);
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                // String oldText = item.getUIString();
                // AltInputDialog dialog = new AltInputDialog(shell, oldText.length() > 0 ? oldText : "(no ALT text)");
                AltInputDialog dialog = new AltInputDialog(shell, item.getUIString());

                if (dialog.open() == InputDialog.OK) {
                    String newText = dialog.getResult();
                    if (newText != null) {
                        IMetaDataModifier modifier = new MetaDataModifier();
                        modifier.setGenerator(new AltTextEditor());
                        modifier.setSite(observer.getTargetFilter());
                        modifier.setPageTitle(PlatformUIUtil.getActiveEditor().getTitle());
                        modifier.setItem(item);
                        modifier.setText(newText);
                        IUserInfoGenerator.Result result = modifier.commit(preferenceStore
                                .getBoolean(UserInfoPreferenceConstants.AUTO_SAVE));
//                        if (result != IUserInfoGenerator.Result.NOTHING) {
                            if (preferenceStore.getBoolean(UserInfoPreferenceConstants.AUTO_REFRESH))
                                refresh();
                            speak(getMessageFormatter().mes(modifier.toString(result),
                                                       newText),
                                  true, false);
//                        }
                    }
                }

                setMode(IManipulator.TREE_NAVIGATION_MODE);
            } else {
                speakWithFormat("Navigator.ITEM_IS_NOT_AVAILABLE", true);
            }
        } catch (TreeManagerException e) {
            e.printStackTrace();
        } catch (XMLStoreException e) {
            e.printStackTrace();
        }
    }

    private void speakUnavailableUserInfo() {
        speakWithFormat("Navigator.ANNOTATION_IS_NOT_AVAILABLE", true);
    }

    public void makeLandmark() {
        if (getCurrentEntry() != null && !getCurrentEntry().isUserEntry()) {
            speakUnavailableUserInfo();
            return;
        }
        try {
            ITreeItem item = getTreeManager().getActiveItem();
            IMetaDataModifier modifier = new MetaDataModifier();
            modifier.setGenerator(getHeadingLevelOf(item) > 0 ? new HeadingCanceller() : new LandmarkMaker());
            //          modifier.setGenerator(Vocabulary.isHeading().eval(item) ? new HeadingCanceller() : new LandmarkMaker());
            modifier.setSite(observer.getTargetFilter());
            modifier.setPageTitle(PlatformUIUtil.getActiveEditor().getTitle());
            modifier.setItem(item);
            IUserInfoGenerator.Result result = modifier.commit(preferenceStore
                                                               .getBoolean(UserInfoPreferenceConstants.AUTO_SAVE));
            if (preferenceStore.getBoolean(UserInfoPreferenceConstants.AUTO_REFRESH))
                refresh();

            speak(getMessageFormatter().mes(modifier.toString(result),
                                       contentShortener.getSummary(item, true)),
                  true, false);
        } catch (TreeManagerException e) {
            e.printStackTrace();
        } catch (XMLStoreException e) {
            e.printStackTrace();
        }
    }

    private short getHeadingLevelOf(ITreeItem item) {
        short level = 0;
        for (ITreeItem i = item; i != null; i = i.getParent()) {
            short k = i.getHeadingLevel();
            if (k < 0)
                return k;
            if (k > 0)
                level = k;
        }
        return level;
    }

    private void refresh() throws TreeManagerException {
        selectUserFennec();
    }
    
    public void removeUserInfo() {
        if (getCurrentEntry() == null) {
            speakWithFormat("Navigator.NO_ANNOTATION");
            return;
        }
        
        try {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

            String title = getMessageFormatter().mes("Navigator.USER_INFO_REMOVE_CONFIRM");
            String message = getMessageFormatter().mes("Navigator.USER_INFO_REMOVE_MESSAGE");
            boolean ret = MessageDialog.openQuestion(shell, title, message);
            if (!ret)
                return;
            
            IMetaDataModifier modifier = new MetaDataModifier();
            modifier.setSite(observer.getTargetFilter());
            modifier.setPageTitle(PlatformUIUtil.getActiveEditor().getTitle());
            if (modifier.remove()) {
                speakWithFormat("Navigator.ANNOTATION_IS_REMOVED", true);
                forceRestart(false);
            } else {
                speakWithFormat("Navigator.NO_ANNOTATION", true);
            }
        } catch (XMLStoreException e) {
            e.printStackTrace();
        }
    }
    
}
