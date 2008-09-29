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

import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import org.eclipse.actf.ai.audio.io.AudioFactory;
import org.eclipse.actf.ai.audio.io.IAudioPipe;
import org.eclipse.actf.ai.audio.io.IAudioReader;
import org.eclipse.actf.ai.audio.io.IAudioWriter;
import org.eclipse.actf.ai.fennec.IFennecEntry;
import org.eclipse.actf.ai.fennec.IFennecMediator;
import org.eclipse.actf.ai.fennec.treemanager.IAccessKeyList;
import org.eclipse.actf.ai.fennec.treemanager.ILocation;
import org.eclipse.actf.ai.fennec.treemanager.IMediaSyncEventListener;
import org.eclipse.actf.ai.fennec.treemanager.ISoundControl;
import org.eclipse.actf.ai.fennec.treemanager.ITreeItem;
import org.eclipse.actf.ai.fennec.treemanager.ITreeManager;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerException;
import org.eclipse.actf.ai.fennec.treemanager.TreeManagerInterruptedException;
import org.eclipse.actf.ai.fennec.treemanager.ISoundControl.VolumeState;
import org.eclipse.actf.ai.fennec.treemanager.IVideoControl.VideoState;
import org.eclipse.actf.ai.internal.navigator.NavigatorPlugin;
import org.eclipse.actf.ai.navigator.IManipulator;
import org.eclipse.actf.ai.navigator.IMediaControl;
import org.eclipse.actf.ai.navigator.INavigatorUI;
import org.eclipse.actf.ai.navigator.IMediaControl.IHandle;
import org.eclipse.actf.ai.navigator.extension.ManipulatorExtension;
import org.eclipse.actf.ai.navigator.extension.MediaControlExtension;
import org.eclipse.actf.ai.navigator.extension.ScreenReaderExtension;
import org.eclipse.actf.ai.navigator.util.MessageFormatter;
import org.eclipse.actf.ai.navigator.views.NavigatorTreeView;
import org.eclipse.actf.ai.navigator.voice.VoiceManager;
import org.eclipse.actf.ai.voice.IVoice;
import org.eclipse.actf.ai.voice.IVoiceEventListener;
import org.eclipse.actf.model.ui.IModelServiceHolder;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserACTF;
import org.eclipse.actf.ui.util.BrowserLaunch;
import org.eclipse.actf.ui.util.PlatformUIUtil;
import org.eclipse.actf.util.ApplicationArgumentUtil;
import org.eclipse.actf.util.vocab.IProposition;
import org.eclipse.actf.util.vocab.Vocabulary;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


public abstract class NavigatorImpl implements INavigatorUI, IVoiceEventListener {
    private static boolean isDemo = ApplicationArgumentUtil.isAvailable("-demo");

    // private final WebEventListener webEventListener;

    private IWebBrowserACTF webBrowser;

    private IFennecMediator fennecMediator;

    private ITreeManager treeManager;

    private final int maxRetry;

    private final int retryInterval;

    private Display display;

    private IManipulator.Mode mode;
    

    public NavigatorImpl(WebEventListener webEventListener, IWebBrowserACTF webBrowser, int maxRetry, int retryInterval) {
        // this.webEventListener = webEventListener;
        this.webBrowser = webBrowser;
        this.maxRetry = maxRetry;
        this.retryInterval = retryInterval;
        this.display = Display.getCurrent();
    }

    public ITreeManager getTreeManager() {
        return treeManager;
    }

    private NavigatorTreeView getNavigatorTreeView() {
        return NavigatorPlugin.getDefault().getNavigatorTreeView();
    }

    private void sleep(int interval) {
        long startTime = System.currentTimeMillis();
        while (true) {
            boolean busy = display.readAndDispatch();
            if (!busy)
                display.sleep();
            if ((System.currentTimeMillis() - startTime) > interval)
                break;
        }
    }
    
    // -----------------------------------------------------------------------
    // Sound Util
    // -----------------------------------------------------------------------
    
    private void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    private static IAudioPipe audio;

    static {
        if (true) {
            // TODO replace audio file
            URL url = NavigatorPlugin.getDefault().getBundle().getResource("waiting.wav");
            IAudioReader reader = AudioFactory.createAudioReader(url);
            IAudioWriter writer = AudioFactory.createDefaultWriter();
            audio = AudioFactory.createLoopedAudioPipe(reader, writer);
            audio.setBufferSize(100);
            audio.setInterval(1);
            audio.prepare();
        }
    }
    
    private void startProgress() {
        startProgress(2000);
    }
    private void startProgress(int msec) {
        audio.start(msec);
    }
    
    private void endProgress() {
        audio.stop();
    }

    // -----------------------------------------------------------------------
    // Voice
    // -----------------------------------------------------------------------

    private final VoiceManager voiceManager = new VoiceManager(this);

    protected MessageFormatter getMessageFormatter() {
        return voiceManager.getMessageFormatter();
    }

    private VoiceManager getVoiceManager() {
        return voiceManager;
    }

    public void indexReceived(int index) {
        if (speakAllMode && (index == -1)) {
            display.asyncExec(new Runnable() {
                public void run() {
                    traverseDownAll();
                }
            });
        } else if (index == -2) {
            display.asyncExec(new Runnable() {
                public void run() {
                    stopSpeak();
                }
            });
        }
    }
    
    // -----------------------------------------------------------------------
    // Speak basic
    // -----------------------------------------------------------------------

    protected void speakWithFormat(String str) {
        speakWithFormat(str, false);
    }
    
    protected void speakWithFormat(String str, boolean flush) {
        speak(getMessageFormatter().mes(str), flush, true);
    }

    protected void speak(String str, boolean flush) {
        speak(str, flush, true);
    }
    
    protected void speak(String str, boolean flush, boolean maleVoice) {
        if (speakAllMode) {
            getVoiceManager().speakWithCallback(str, flush, maleVoice);
        } else {
            getVoiceManager().speak(str, flush, maleVoice);
        }
    }
    
    private boolean speakAllMode = false;

    private void stopSpeakAll() {
        speakAllMode = false;
    }

    public void speakAll() {
        speakAllMode = true;
        try {
            speakActiveItem(true, false, JumpMode.NONE);
        } catch (TreeManagerException e) {
            e.printStackTrace();
        }
    }

    public void traverseDownAll() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.traverse(false);
            }

            public void after(int st) throws TreeManagerException {
                afterMoveForSpeakAll(st, "Navigator.BOTTOM");
            }
        });
    }

    private void afterMoveForSpeakAll(int st, String notMoved) throws TreeManagerException {
        if ((st & ITreeManager.CLICKED) != 0) {
            speakWithFormat("Navigator.CLICK");
        }
        if ((st & ITreeManager.MOVED) != 0) {
            speakActiveItem(true, false, JumpMode.NONE);
            sayLevel();
        } else {
            speakAllMode = false;
            sayLevel(notMoved);
        }
    }

    public void stopSpeak() {
        endProgress();
        stopSpeakAll();
        getVoiceManager().stop();
    }


    // -----------------------------------------------------------------------
    // Say, speak
    // -----------------------------------------------------------------------
    
    private void sayMode(IManipulator.Mode mode) {
        speakWithFormat(mode.name + " mode");
    }
    
    private void sayLevel(String prefix) throws TreeManagerException {
        String mes = getMessageFormatter().mes(prefix);
        speak(mes + " " + getLevelMessage(), true, true);
    }
    
    private String getLevelMessage() throws TreeManagerException {
        int level = treeManager.getLevel();
        String mesLv;
        if (level > 0) {
            mesLv = getMessageFormatter().mes("Navigator.LEVEL", new Object[] { new Integer(level) });
        } else {
            mesLv = getMessageFormatter().mes("Navigator.TOP_LEVEL");
        }
        return mesLv;
    }

    private void sayLevel() throws TreeManagerException {
        // speak(getLevelMessage(), false, true);
    }

    private void sayRetrial() {
        speak(getMessageFormatter().mes("Navigator.UNDONE"), true, true);
    }

    private void speakVideoInfo(boolean flag) throws TreeManagerException {
        IVideoControl vc = treeManager.getVideoControl();
        if (flag || vcCount != vc.getCount()) {
            vcCount = vc.getCount();
            if (vcCount == 0) {
                speakWithFormat("Navigator.NOVIDEO");
            } else if (vcCount == 1) {
                speakWithFormat("Navigator.SINGLEVIDEO");
            } else {
                String text = getMessageFormatter().mes("Navigator.VIDEOCOUNT", new Object[] { vcCount });
                speak(text, false, false);
            }
        }
    }

    private void speakSoundInfo(boolean flag) throws TreeManagerException {
        ISoundControl sc = treeManager.getSoundControl();
        if (flag || scCount != sc.getCount()) {
            scCount = sc.getCount();
            if (scCount == 0) {
                speakWithFormat("Navigator.NOSOUND");
            } else if (scCount == 1) {
                speakWithFormat("Navigator.SINGLESOUND");
            } else {
                String text = getMessageFormatter().mes("Navigator.SOUNDCOUNT", new Object[] { scCount });
                speak(text, false, false);
            }
        }
    }

    private int vcCount, scCount;

    //private boolean mcAvailable = false;

    private void speakMediaInfo() throws TreeManagerException {
        IVideoControl vc = treeManager.getVideoControl();

        StringBuffer buf = new StringBuffer();
        if (vc.getCount() > 0) {
            buf.append(getMessageFormatter().mes("Navigator.VIDEO_AT", 
                    formatTime(vc.getCurrentPosition())));
        }
        if (vc.getTotalLength() > 0) {
            buf.append(getMessageFormatter().mes("Navigator.VIDEO_TOTAL", 
                    formatTime(vc.getTotalLength())));
        }

        vcCount = vc.getCount();
        if (vcCount == 0) {
            speakWithFormat("Navigator.NOVIDEO");
        } else if (vcCount == 1) {
            speakWithFormat("Navigator.SINGLEVIDEO");
        } else {
            int index = vc.getIndex();
            String text = getMessageFormatter().mes("Navigator.VIDEOINDEX", new Object[] { index, vcCount });
            speak(text, false, false);
        }

        speak(buf.toString(), false, false);
    }

    private String formatTime(double currentPosition) {
        int s = (int) currentPosition;
        int m = s / 60;
        s %= 60;
        int h = s / 60;
        m %= 60;
        
        if (h > 0) {
            return getMessageFormatter().mes("Navigator.HH_MM_SS", new Object[] { s, m, h });
        } else if (m > 0) {
            return getMessageFormatter().mes("Navigator.MM_SS", new Object[] { s, m });
        }
        return getMessageFormatter().mes("Navigator.SS", new Object[] { s });
    }
    
    private void speakPageInfo() {
        try {
            speakVideoInfo(false);
            speakSoundInfo(false);

            MediaControlExtension.speakInfo(false);
            
            /*
            if (!mcAvailable && MediaControlExtension.isAvailable()) {
                speakWithFormat("AudioDescription.available");
                mcAvailable = true;
            }*/

        } catch (TreeManagerException e) {
        }
    }

    private void sayFennecName(String fennecName, boolean flag) {
        String text = getMessageFormatter().mes("Navigator.FENNEC_NAME",
                                           new Object[] { fennecName });
        speak(text, flag, true);
    }

    private void sayNoFennec(boolean flag) {
        speakWithFormat("Navigator.NO_FENNEC", flag);
    }

    public void speakCurrentStatus() {
        sayMode(mode);
        try {
            speakActiveItem(false, true, JumpMode.NONE);
            speakPageInfo();
        } catch (TreeManagerException e) {
        }
    }

    public void speakMediaStatus() {
        try {
            speakMediaInfo();
        } catch (TreeManagerException e) {
        }
    }

    public void speakTab(boolean flush) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        IEditorPart editor = page.getActiveEditor();

        IEditorReference[] erefs = page.getEditorReferences();
        boolean found = false;
        int index = 0, total = 0;
        for (int i = 0; i < erefs.length; i++) {
            if (!found)
                index++;
            total++;
            if (erefs[i].getEditor(false).equals(editor)) {
                found = true;
            }
        }

        String buf = "";
        if (total == 1)
            buf = editor.getTitle();
        else
            buf = getMessageFormatter().mes("Navigator.MOVE_TAB", editor.getTitle(), index, total);

        speak(buf, flush, false);
        stopSpeakAll();
    }
    

    private ITreeItem lastHighlighted;

    private void highlight(ITreeItem item) {
        if (lastHighlighted != null) {
            try {
                lastHighlighted.unhighlight();
            } catch (TreeManagerException e) {
            }
        }
        try {
            item.highlight();
        } catch (TreeManagerException e) {
        }
        lastHighlighted = item;
        sleep(1);
    }
    
    protected enum JumpMode {
        HEADING, 
        LISTITEM_TOP, 
        LINK, 
        OBJECT, 
        BLOCK, 
        INPUT, 
        LISTITEM_BOTTOM, 
        ACCESSKEY, 
        MEDIA,
        ALTALABLE,
        NONE,
    }

    private boolean listJumping = false;

    private void speakActiveItem(boolean flush, boolean verbose, JumpMode jumpMode) throws TreeManagerException {
        endProgress();
        ITreeItem item = treeManager.getActiveItem();
        ITreeItem[] siblings = treeManager.getSiblings();

        final MessageFormatter mf = getMessageFormatter();
        if (item == null)
            return;
        // update view
        highlight(item);
        getNavigatorTreeView().showItem(item);

        // get item's UI string
        int idx = item.getNth();
        int st = intervalStart(siblings, idx);
        int end = intervalEnd(siblings, idx);
        StringBuffer bufUIStr = new StringBuffer();
        for (int i = st; i <= end; i++) {
            if (i < siblings.length)
                bufUIStr.append(siblings[i].getUIString());
            else
                System.err.println("st " + st + ", end " + end + ", siblings.length " + siblings.length);
        }
        char accessKey = item.getAccessKey();

        String uiStr = bufUIStr.toString();
        String label = item.getFormLabel();
        String speak = uiStr;

        boolean isLink = Vocabulary.isLink().eval(item);
        int hl = item.getHeadingLevel();
        boolean isHeading = hl > 0;
        boolean isListItem = Vocabulary.isListItem().eval(item);
        boolean isListTop = Vocabulary.isListTop().eval(item);
        boolean isCheckbox = Vocabulary.isCheckbox().eval(item);
        boolean isRadio = Vocabulary.isRadio().eval(item);
        boolean isCombobox = Vocabulary.isCombobox().eval(item);
        //boolean isMultiSelectable = Vocabulary.isMultiSelectable().eval(item);
        boolean isButton = Vocabulary.isButton().eval(item);
        boolean isPassword = Vocabulary.isPassword().eval(item);
        boolean isTextbox = Vocabulary.isTextbox().eval(item);
        boolean isTextarea = Vocabulary.isMultilineEdit().eval(item);
        boolean isFileEdit = Vocabulary.isFileEdit().eval(item);
        boolean isChecked = Vocabulary.isChecked().eval(item);
        boolean isClickable = Vocabulary.isClickable().eval(item);
        boolean isFlashTopNode = Vocabulary.isFlashTopNode().eval(item);
        boolean isFlashLastNode = Vocabulary.isFlashLastNode().eval(item);
        boolean isMedia = Vocabulary.isMedia().eval(item);
        
        

        if (isButton) {
            String widget = mf.mes("Navigator.BUTTON");
            if (uiStr.matches("[0-9]+")) {
                speak = mf.concat(widget, uiStr);
            } else {
                speak = mf.concat(uiStr, widget);
            }
        } else if (isLink) {
            boolean isVisitedLink = Vocabulary.isVisitedLink().eval(item);
            String widget = mf.mes(isVisitedLink ? "Navigator.VISITED_LINK" : "Navigator.LINK");
            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(uiStr, widget);
            } else {
                speak = mf.concat(widget, uiStr);
            }
        } else if (isPassword) {
            if (uiStr.length() > 0)
                uiStr = mf.mes("Navigator.PASSWORD_STAR3");
            String widget = mf.mes("Navigator.PASSWORD");
            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(label, widget, uiStr);
            } else {
                speak = mf.concat(uiStr, widget);
            }
        } else if (isFileEdit) {
            String widget = mf.mes("Navigator.FILE_UPLOAD");
            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(label, widget, uiStr);
            } else {
                speak = mf.concat(widget, uiStr);
            }
        } else if (isTextarea) {
            String widget = mf.mes((uiStr.length() == 0) ? "Navigator.TEXTAREA_EMPTY" : "Navigator.TEXTAREA");
            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(label, widget);
            } else {
                speak = widget;
            }
        } else if (isTextbox) {
            String widget = mf.mes("Navigator.TEXTBOX");

            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(label, widget, uiStr);
            } else {
                speak = mf.concat(widget, uiStr);
            }
        } else if (isCheckbox) {
            String widget = mf.mes("Navigator.CHECKBOX");
            String checked = mf.mes(isChecked ? "Navigator.CHECKED" : "Navigator.NOT_CHECKED");

            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(label, widget, checked);
            } else {
                speak = mf.concat(widget, checked, label);
            }
        } else if (isRadio) {
            String widget = mf.mes("Navigator.RADIO");
            String checked = mf.mes(isChecked ? "Navigator.CHECKED" : "Navigator.NOT_CHECKED");

            int index = item.getRadioIndex();
            int total = item.getRadioTotal();
            String nm = "";
            if (index != 0 && total != 0) {
                nm = mf.mes("Navigator.N_OF_M", index, total);
            }

            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(label, widget, checked, nm);
            } else {
                speak = mf.concat(widget, checked, nm, label);
            }
        } else if (isCombobox) {
            int[] indices = item.getSelectedIndices();
            int total = item.getOptionsCount();

            if (indices.length > 0) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < indices.length; i++) {
                    sb.append(mf.mes("Navigator.COMBO_BOX") + " ");
                    sb.append(item.getOptionTextAt(indices[i]));
                    sb.append(" " + mf.mes("Navigator.N_OF_M", indices[i] + 1, total) + " ");
                }
                speak = mf.concat(uiStr, sb.toString());
            } else {
                speak = mf.concat(uiStr, mf.mes("Navigator.NO_SELECTION"));
            }
            if (jumpMode == JumpMode.LINK) {
                speak = mf.concat(label, uiStr);
            }
        } else if (isFlashTopNode) {
            boolean isMSAAFlash = Vocabulary.isMSAAFlash().eval(item);
            if (isMSAAFlash)
                speak = mf.concat(speak, mf.mes("Navigator.MSAA_FLASH_CONTENT"));
            else
                speak = mf.concat(speak, mf.mes("Navigator.FLASH_CONTENT"));
        } else if (isFlashLastNode) {
            speak = mf.mes("Navigator.FLASH_END");
        }
        if (speak.length() == 0) {
            if (isClickable) {
                speak = "Clickable";
            }
        }
        if (isHeading) {
            String widget = mf.mes("Navigator.HEADING");
            String level = mf.mes("Navigator.LEVEL", hl);

            if (jumpMode == JumpMode.HEADING) {
                speak = mf.concat(speak, widget, level);
            } else {
                speak = mf.concat(widget, level, speak);
            }
        }

        if (isMedia) {
            String media = mf.mes("Navigator.MEDIA");
            speak = mf.concat(media, speak);
        }

        if (isListItem) {
            int index = item.getListIndex();
            int total = item.getListTotal();
            if (total > 1) {
                String nm = mf.mes("Navigator.N_OF_M", index, total);
                speak = mf.concat(speak, nm);
                if (listJumping && ((jumpMode == JumpMode.LISTITEM_TOP && index == 1) //
                        || (jumpMode == JumpMode.LISTITEM_BOTTOM && index == total))) {
                    beep();
                }
                listJumping = true;
            }
        } else {
            listJumping = false;
        }
        if (isListTop) {
            ITreeItem[] children = item.getChildItems();
            int n = 0;
            for (int i = 0; i < children.length; i++) {
                if (Vocabulary.isListItem().eval(children[i])) {
                    n++;
                }
            }
            speak = mf.concat(speak, uiStr, mf.mes("Navigator.LIST_TOP", n));
        }
        
        if (accessKey != 0) {
            speak = mf.concat(speak, mf.mes("Navigator.ACCESSKEY_GUIDE", accessKey));
        }

        if (verbose) {
            String desc = item.getDescription();
            if ((desc != null) && (desc.length() > 0)) {
                speak = mf.concat(speak, desc);
            }
        }

        speak(speak, flush, !isLink);
    }

    private int intervalStart(ITreeItem[] siblings, int st) {
        if (siblings == null)
            return 0;
        for (st = st - 1;; st--) {
            if (st < 0 || siblings.length <= st)
                return 0;
            if (!Vocabulary.isConnectable().eval(siblings[st]))
                return st + 1;
        }
    }

    private int intervalEnd(ITreeItem[] siblings, int end) {
        if (siblings == null)
            return 0;
        for (; end < siblings.length; end++) {
            if (!Vocabulary.isConnectable().eval(siblings[end]))
                return end;
        }
        return end - 1;
    }

    // ---------------------------------------------------------------------
    // Mode Control
    // ---------------------------------------------------------------------
    
    // Used by NavigatorImplExSub
    protected void setMode(IManipulator.Mode mode) {
        if (this.mode == mode)
            return;
        if (this.mode != null) {
            //sayMode(mode);
        }
        this.mode = mode;
        ManipulatorExtension.setMode(mode);
        getNavigatorTreeView().setMode(mode);
        switch (mode.code) {
        case IManipulator.TREE_NAVIGATION_MODE_CODE:
            ScreenReaderExtension.screenReaderOff();
            if (ScreenReaderExtension.isAvailable()) {
                webBrowser.showAddressText(false);
            }
            break;
        case IManipulator.FORM_INPUT_MODE_CODE:
        default:
            ScreenReaderExtension.screenReaderOn();
            break;
        }
    }

    private void enterFormInputMode(ITreeItem item) {
        setMode(IManipulator.FORM_INPUT_MODE);
        item.setFocus();
    }

    private void enterFormInputMode() {
        setMode(IManipulator.FORM_INPUT_MODE);
    }

    // --------------------------------------------------------------------------------
    // Fennec Control
    // --------------------------------------------------------------------------------

    private int currentFennecIdx = 1;
    
    private IFennecEntry currentEntry = null;
    
    protected IFennecEntry getCurrentEntry() {
        return currentEntry;
    }

    private void selectFennec(boolean next, boolean flush) {
        selectFennec(next, flush, true);
    }
    private void selectFennec(boolean next, boolean flush, boolean sayFlag) {
        if (lastHighlighted != null) {
            try {
                lastHighlighted.unhighlight();
            } catch (TreeManagerException e1) {
            }
        }

        IFennecEntry[] entries = fennecMediator.getFennecEntries();
        if (next) {
            if (entries.length == 0) {
                speakWithFormat("Navigator.NO_OTHER_FENNEC", true);
                return;
            }
            currentFennecIdx++;
        }
        if (currentFennecIdx > entries.length)
            currentFennecIdx = 0;
        try {
            restoreLocation(getLocation());
            if (currentFennecIdx == 0) {
                initFennec(null, flush, sayFlag);
            } else {
                initFennec(entries[currentFennecIdx - 1], flush, sayFlag);
            }
            speakActiveItem(false, false, JumpMode.NONE);
        } catch (TreeManagerException e) {
        }
    }

    protected void selectUserFennec() {
        if (lastHighlighted != null) {
            try {
                lastHighlighted.unhighlight();
            } catch (TreeManagerException e1) {
            }
        }

        IFennecEntry[] entries = fennecMediator.getFennecEntries();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].isUserEntry()) {
                try {
                    restoreLocation(getLocation());
                    initFennec(entries[i], true, true);
                    speakActiveItem(false, false, JumpMode.NONE);
                } catch (TreeManagerException e) {
                }
                return;
            }
        }
        speakWithFormat("Navigator.NO_OTHER_FENNEC", true);
        return;
    }

    public void selectNextFennec() {
        selectFennec(true, true);
    }
    
    void setFennecMediator(IFennecMediator fennecMediator) {
        if (this.fennecMediator != null) {
            this.fennecMediator.release();
        }
        this.fennecMediator = fennecMediator;
        // TODO
        this.treeManager = null;
        this.lastHighlighted = null;
        getNavigatorTreeView().clearItem();
    }


    private String getFennecName(IFennecEntry entry) {
        if (entry == null)
            return getMessageFormatter().mes("Navigator.NO_FENNEC_MESSAGE");
        String text = entry.getDocumentation();
        if ((text != null) && (text.length() > 0))
            return text;
        return getMessageFormatter().mes("Navigator.NO_FENNEC_NAME");
    }

    private void initFennec(IFennecEntry entry, boolean flag, boolean sayFlag) throws TreeManagerException {
        currentEntry = entry;
        treeManager = fennecMediator.newTreeManager(entry);
        String fennecName = getFennecName(entry);
        if (sayFlag) {
            if (entry == null) {
                sayNoFennec(flag);
            } else {
                sayFennecName(fennecName, flag);
            }
        }
        getNavigatorTreeView().showFennecName(fennecName);
        setMode(IManipulator.TREE_NAVIGATION_MODE);
        try {
            treeManager.initialize();
            if (sayFlag)
                speakPageInfo();
        } catch (TreeManagerException e) {
            if (sayFlag)
                speakWithFormat(e.getMessage());
        }

        if (!skipToAnchor(webBrowser.getURL())) {
            if (locationToBeRestored != null) {
                try {
                    treeManager.moveToLocation(locationToBeRestored);
                } catch (TreeManagerException e) {
                }
                locationToBeRestored = null;
            }
        }
    }

    public void startNavigation(IWebBrowserACTF webBrowser) {
        startNavigation(webBrowser, true);
    }

    public void startNavigation(IWebBrowserACTF webBrowser, boolean sayFlag) {
        endProgress();
        if (ScreenReaderExtension.isAvailable() && isLeftViewsShown() && !isDemo) {
            toggleLeftViewsShowing();
        }

        if (sayFlag)
            speakWithFormat("Navigator.STARTNAVIGATION");

        try {
            initFennec(fennecMediator.getDefaultFennecEntry(), false, sayFlag);
            currentFennecIdx = 1;
            if (sayFlag) {
                speakActiveItem(false, false, JumpMode.NONE);
            }
        } catch (Exception e) {
            // TODO Retry
            e.printStackTrace();
        }
        ManipulatorExtension.setNavigator(this);
        initializeMovieStartListener();
    }

    public void endNavigation() {
        // TODO
        ManipulatorExtension.disposeExtensions();
    }

    // --------------------------------------------------------------------------------
    // Media Control
    // --------------------------------------------------------------------------------

    private ISoundControl prepareSoundControl() throws TreeManagerException {
        ISoundControl sc = treeManager.getSoundControl();
        if (sc.getCount() == 0) {
            // say("Retry to control sound");
            treeManager.analyze();
            sc = treeManager.getSoundControl();
        }
        return sc;
    }

    private IVideoControl prepareVideoControl() throws TreeManagerException {
        IVideoControl vc = treeManager.getVideoControl();
        if (vc.getCount() == 0) {
            // say("Retry to control video");
            treeManager.analyze();
            vc = treeManager.getVideoControl();
        }
        return vc;
    }

    public void muteMedia() {
        ISoundControl sc;
        try {
            sc = prepareSoundControl();
        } catch (TreeManagerException e) {
            return;
        }
        if (sc == null)
            return;
        if (sc.getCount() == 0) {
            speakWithFormat("Navigator.NOSOUND", true);
            return;
        }
        sc.muteMedia();
        if (sc.getVolumeState() == VolumeState.MUTE) {
            speakWithFormat("Navigator.MUTEON", true);
        } else {
            speakWithFormat("Navigator.MUTEOFF", true);
        }
    }

    public void volumeDownMedia() {
        ISoundControl sc;
        try {
            sc = prepareSoundControl();
        } catch (TreeManagerException e) {
            return;
        }
        if (sc == null)
            return;
        if (sc.getCount() == 0) {
            speakWithFormat("Navigator.NOSOUND", true);
            return;
        }
        sc.volumeDownMedia();
        if (sc.getVolumeState() == VolumeState.MIN || sc.getVolumeState() == VolumeState.MUTE) {
            speakWithFormat("Navigator.VOLUMEMIN", true);
        } else {
            speakWithFormat("Navigator.VOLUMEDOWN", true);
        }
    }

    public void minimalVolumeDownMedia() {
        ISoundControl sc;
        try {
            sc = prepareSoundControl();
        } catch (TreeManagerException e) {
            return;
        }
        if (sc == null)
            return;
        if (sc.getCount() == 0) {
            speakWithFormat("Navigator.NOSOUND", true);
            return;
        }
        sc.minimalVolumeDownMedia();
        if (sc.getVolumeState() == VolumeState.MIN || sc.getVolumeState() == VolumeState.MUTE) {
            speakWithFormat("Navigator.VOLUMEMIN", true);
        } else {
            speakWithFormat("Navigator.VOLUMEDOWN", true);
        }
    }

    public void volumeUpMedia() {
        ISoundControl sc;
        try {
            sc = prepareSoundControl();
        } catch (TreeManagerException e) {
            return;
        }
        if (sc == null)
            return;
        if (sc.getCount() == 0) {
            speakWithFormat("Navigator.NOSOUND", true);
            return;
        }
        sc.volumeUpMedia();
        if (sc.getVolumeState() == VolumeState.MAX) {
            speakWithFormat("Navigator.VOLUMEMAX", true);
        } else {
            speakWithFormat("Navigator.VOLUMEUP", true);
        }
    }

    public void minimalVolumeUpMedia() {
        ISoundControl sc;
        try {
            sc = prepareSoundControl();
        } catch (TreeManagerException e) {
            return;
        }
        if (sc == null)
            return;
        if (sc.getCount() == 0) {
            speakWithFormat("Navigator.NOSOUND", true);
            return;
        }
        sc.minimalVolumeUpMedia();
        if (sc.getVolumeState() == VolumeState.MAX) {
            speakWithFormat("Navigator.VOLUMEMAX", true);
        } else {
            speakWithFormat("Navigator.VOLUMEUP", true);
        }
    }

    private void sayVideoState(IVideoControl vc, boolean doPause, VideoState oldSt) {
        VideoState st = vc.getVideoState();
        switch (st) {
        case STATE_PLAY:
            if (doPause) {
                if (oldSt == VideoState.STATE_PAUSE) {
                    speakWithFormat("Navigator.RESUMEMEDIA");
                } else if (oldSt == VideoState.STATE_PLAY) {
                    speakWithFormat("Navigator.CANNOT_PAUSE");
                }
            } else {
                speakWithFormat("Navigator.PLAYMEDIA");
            }
            break;
        case STATE_STOP:
            speakWithFormat("Navigator.STOPMEDIA");
            break;
        case STATE_PAUSE:
            speakWithFormat("Navigator.PAUSEMEDIA");
            break;
        case STATE_WAITING:
            speakWithFormat("Navigator.WAITINGMEDIA");
            break;
        case STATE_FASTFORWARD:
            speakWithFormat("Navigator.FASTFORWARDMEDIA");
            break;
        case STATE_FASTREVERSE:
            speakWithFormat("Navigator.FASTREVERSEMEDIA");
            break;
        case STATE_OTHER:
            speakWithFormat("Navigator.NOT_AVAILABEL");
            break;
        }
    }

    public void previousTrack() {
        // TODO Auto-generated method stub

    }

    public void nextTrack() {
        // TODO Auto-generated method stub

    }

    public void stopMedia() {
        try {
            IVideoControl vc = prepareVideoControl();
            VideoState st = vc.getVideoState();
            vc.stopMedia();
            sayVideoState(vc, false, st);
        } catch (TreeManagerException e) {
        }
    }

    public void playMedia() {
        try {
            IVideoControl vc = prepareVideoControl();
            VideoState st = vc.getVideoState();
            vc.playMedia();
            sayVideoState(vc, false, st);
        } catch (TreeManagerException e) {
        }
    }

    public void pauseMedia() {
        try {
            IVideoControl vc = prepareVideoControl();
            VideoState st = vc.getVideoState();
            vc.pauseMedia();
            sayVideoState(vc, true, st);
        } catch (TreeManagerException e) {
        }
    }

    public void fastReverse() {
        // TODO Auto-generated method stub

    }

    public void fastForward() {
        // TODO Auto-generated method stub

    }

    // --------------------------------------------------------------------------------
    // Tree Navigation
    // --------------------------------------------------------------------------------


    interface Command {
        int run(int retry) throws TreeManagerException;
        void after(int st) throws TreeManagerException;
    }
    
    private void afterMove(int st, String notMoved) throws TreeManagerException {
        afterMove(st, notMoved, null);
    }

    private void afterMove(int st, String notMoved, JumpMode jumpMode) throws TreeManagerException {
        if ((st & ITreeManager.CLICKED) != 0) {
            speakWithFormat("Navigator.CLICK");
        }
        else if ((st & ITreeManager.MOVED) != 0) {
            speakActiveItem(true, false, jumpMode);
            sayLevel();
        } else {
            sayLevel(notMoved);
        }
        speakAllMode = false;
    }

    private void moveCmd(Command cmd) {
        if (treeManager == null)
            return;
        for (int i = 0; i < maxRetry; i++) {
            try {
                int st = cmd.run(i);
                cmd.after(st);
                return;
            } catch (TreeManagerInterruptedException e) {
                sayRetrial();
                sleep(retryInterval);
            } catch (TreeManagerException e) {
            }
        }
        // navigationThread.sendCmd(cmd);
    }
    
    private boolean skipToAnchor(String url) throws TreeManagerException {
        int hashIdx = url.lastIndexOf('#');
        if (hashIdx < 0)
            return false;
        hashIdx++;
        if (hashIdx >= url.length())
            return false;
        String target = url.substring(hashIdx);
        treeManager.skipToAnchor(target);
        return true;
    }

    public void treeLeft() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.gotoParent();
            }

            public void after(int st) throws TreeManagerException {
                afterMove(st, "Navigator.TOP");
            }
        });
    }

    public void treeRight() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.gotoFirstChild();
            }

            public void after(int st) throws TreeManagerException {
                afterMove(st, "Navigator.NO_SUBITEMS");
            }
        });
    }

    public void treeUp() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.gotoPreviousSibling();
            }

            public void after(int st) throws TreeManagerException {
                afterMove(st, "Navigator.TOP");
            }
        });
    }

    public void treeDown() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.gotoNextSibling();
            }

            public void after(int st) throws TreeManagerException {
                afterMove(st, "Navigator.BOTTOM");
            }
        });
    }

    public void treeTop() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.gotoStartOfPage();
            }

            public void after(int st) throws TreeManagerException {
                speakTab();
                listJumping = false;
                //afterMove(st, "Navigator.TOP");
            }
        });
    }

    public void treeBottom() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.gotoEndOfPage();
            }

            public void after(int st) throws TreeManagerException {
                afterMove(st, "Navigator.BOTTOM");
            }
        });
    }

    public void traverseNodeDown() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                int moved;
                int rFlag = 0;
                do {
                    moved = treeManager.traverse(false);
                    rFlag |= moved;
                } while (((moved & ITreeManager.LEVEL_CHANGED) == 0) && ((moved & ITreeManager.MOVED) != 0));
                return moved | rFlag;
            }

            public void after(int st) throws TreeManagerException {
                afterMove(st, "Navigator.BOTTOM");
            }
        });
    }

    public void traverseNodeUp() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                int moved;
                int rFlag = 0;
                do {
                    moved = treeManager.traverse(true);
                    rFlag |= moved;
                } while (((moved & ITreeManager.LEVEL_CHANGED) == 0) && ((moved & ITreeManager.MOVED) != 0));
                return moved | rFlag;
            }

            public void after(int st) throws TreeManagerException {
                afterMove(st, "Navigator.TOP");
            }
        });
    }

    public void traverseDown() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.traverse(false);
            }

            public void after(int st) throws TreeManagerException {
                if ((st & ITreeManager.MOVED) == 0) {
                    beep();
                    speak(getMessageFormatter().mes("Navigator.BOTTOM"), true, false);
                    speakActiveItem(false, false, JumpMode.NONE);
                } else {
                    afterMove(st, "Navigator.BOTTOM");
                }
            }
        });
    }

    public void traverseUp() {
        moveCmd(new Command() {
            public int run(int r) throws TreeManagerException {
                return treeManager.traverse(true);
            }

            public void after(int st) throws TreeManagerException {
                if ((st & ITreeManager.MOVED) == 0) {
                    beep();
                    speak(getMessageFormatter().mes("Navigator.TOP"), true, false);
                    speakTab(false);
                } else {
                    afterMove(st, "Navigator.TOP");
                }
            }
        });
    }

    public void click() {
        try {
            ITreeItem item = treeManager.getActiveItem();
            if (item == null)
                return;

            // for restoring the class name of the item
            item.unhighlight();

            if (Vocabulary.isFileEdit().eval(item) || //
                    Vocabulary.isCheckbox().eval(item) || //
                    Vocabulary.isRadio().eval(item) || //
                    !(Vocabulary.isInputable().eval(item) || Vocabulary.isSelectable().eval(item)) //
            ) {
                moveCmd(new Command() {
                    public int run(int r) throws TreeManagerException {
                        if (r == 0) {
                            return treeManager.click(true);
                        } else {
                            return treeManager.click(false);
                        }
                    }

                    public void after(int st) throws TreeManagerException {
                        afterMove(st, "Navigator.CLICK");
                    }
                });
                return;
            }

            if (false) {
                enterFormInputMode(item);
            } else {
                setMode(IManipulator.KEYHOOK_DISABLED_MODE);
                //item.setFocus();
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                boolean combo = Vocabulary.isCombobox().eval(item);

                if (combo) {
                    boolean multiple = Vocabulary.isMultiSelectable().eval(item);
                    FormSelectDialog dialog = new FormSelectDialog(shell, item, multiple);

                    if (dialog.open() == InputDialog.OK) {
                        int[] indices = dialog.getSelectedIndices();
                        int total = dialog.getLength();
                        if (indices != null) {
                            item.setSelectedIndices(indices);
                            if (indices.length > 0) {
                                StringBuffer sb = new StringBuffer();
                                for (int i = 0; i < indices.length; i++) {
                                    String text = dialog.getTextAt(indices[i]);
                                    sb.append(text + " " + getMessageFormatter().mes("Navigator.N_OF_M", indices[i] + 1, total)
                                              + " ");
                                }
                                speak(sb.toString(), true);
                            } else {
                                speakWithFormat("Navigator.COMBO_BOX_NOSELECTION");
                            }
                        }
                    }
                } else {
                    boolean multiline = Vocabulary.isMultilineEdit().eval(item);
                    boolean pass = Vocabulary.isPassword().eval(item);
                    FormInputDialog dialog = new FormInputDialog(shell, item.getText(), multiline, pass);

                    if (dialog.open() == InputDialog.OK) {
                        String result = dialog.getResult();
                        if (result != null) {
                            item.setText(dialog.getResult());
                            if (pass) {
                                if (item.getText().length() > 0) {
                                    speakWithFormat(getMessageFormatter().mes("Navigator.PASSWORD_STAR3"));
                                }
                            } else {
                                speakWithFormat(item.getText());
                            }
                        }
                    }
                }
                //item.setFocus();
                setMode(IManipulator.TREE_NAVIGATION_MODE);
            }

            // for restoring the class name of the item
            item.highlight();
        } catch (TreeManagerException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------------------------
    // Skip Navigation
    // --------------------------------------------------------------------------------

    // Used by NavigatorImplExSub
    protected void findNext(final IProposition p, final JumpMode jumpMode) {
        moveCmd(new Command() {
            ILocation current;
            public int run(int r) throws TreeManagerException {
                startProgress();
                current = getLocation();
                return treeManager.findNext(p);
            }

            public void after(int st) throws TreeManagerException {
                if ((st & ITreeManager.MOVED) == 0) {
                    if (jumpMode != JumpMode.LISTITEM_TOP)
                        beep();
                    speak(getMessageFormatter().mes("Navigator.WRAPPING_TO_TOP"), true, false);
                    moveCmd(new Command() {
                        public int run(int r) throws TreeManagerException {
                            treeManager.gotoStartOfPage();
                            return treeManager.findNext(p);
                        }

                        public void after(int st) throws TreeManagerException {
                            if ((st & ITreeManager.MOVED) == 0)
                                treeManager.moveToLocation(current);
                            afterFind(st, jumpMode);
                            endProgress();
                        }
                    });
                } else {
                    afterMove(st, "Navigator.BOTTOM", jumpMode);
                    endProgress();
                }
            }
        });
    }

    private void afterFind(int st, JumpMode jumpMode) throws TreeManagerException {
        afterFind(st, jumpMode, null);
    }

    private void afterFind(int st, JumpMode jumpMode, Object arg) throws TreeManagerException {
        if ((st & ITreeManager.MOVED) != 0) {
            speakActiveItem(false, false, jumpMode);
        } else {
            String buf = "";
            if (jumpMode == JumpMode.HEADING)
                buf = getMessageFormatter().mes("Navigator.NO_HEADING");
            else if (jumpMode == JumpMode.BLOCK)
                buf = getMessageFormatter().mes("Navigator.NO_BLOCK");
            else if (jumpMode == JumpMode.INPUT)
                buf = getMessageFormatter().mes("Navigator.NO_INPUT");
            else if (jumpMode == JumpMode.LINK)
                buf = getMessageFormatter().mes("Navigator.NO_LINK");
            else if (jumpMode == JumpMode.LISTITEM_TOP || jumpMode == JumpMode.LISTITEM_BOTTOM)
                buf = getMessageFormatter().mes("Navigator.NO_LISTITEM");
            else if (jumpMode == JumpMode.OBJECT)
                buf = getMessageFormatter().mes("Navigator.NO_OBJECT");
            else if (jumpMode == JumpMode.MEDIA)
                buf = getMessageFormatter().mes("Navigator.NO_MEDIA");
            else if (jumpMode == JumpMode.ACCESSKEY)
                buf = getMessageFormatter().mes("Navigator.NO_ACCESSKEY", arg);
            else if (jumpMode == JumpMode.ALTALABLE)
                buf = getMessageFormatter().mes("Navigator.NO_ALTALABLE");
                

            speak(buf, true, false);
        }
        speakAllMode = false;
    }

    // Used by NavigatorImplExSub
    protected void findPrevious(final IProposition p, final JumpMode jumpMode) {
        moveCmd(new Command() {
            private ILocation current;
            public int run(int r) throws TreeManagerException {
                startProgress();
                current = getLocation();
                return treeManager.findPrevious(p);
            }

            public void after(int st) throws TreeManagerException {
                if ((st & ITreeManager.MOVED) == 0) {
                    if (jumpMode != JumpMode.LISTITEM_BOTTOM)
                        beep();
                    speak(getMessageFormatter().mes("Navigator.WRAPPING_TO_BOTTOM"), true, false);
                    moveCmd(new Command() {
                        public int run(int r) throws TreeManagerException {
                            treeManager.gotoEndOfPageForFind();
                            return treeManager.findPrevious(p);
                        }

                        public void after(int st) throws TreeManagerException {
                            if ((st & ITreeManager.MOVED) == 0)
                                treeManager.moveToLocation(current);
                            afterFind(st, jumpMode);
                            endProgress();
                        }
                    });
                } else {
                    afterMove(st, "Navigator.TOP", jumpMode);
                    endProgress();
                }
            }
        });
    }

    public void nextHeader() {
        findNext(Vocabulary.isHeading(), JumpMode.HEADING);
    }

    public void previousHeader() {
        findPrevious(Vocabulary.isHeading(), JumpMode.HEADING);
    }

    public void nextInputable() {
        findNext(Vocabulary.or(Vocabulary.isInputable(), Vocabulary.isSelectable()), JumpMode.INPUT);
    }

    public void previousInputable() {
        findPrevious(Vocabulary.or(Vocabulary.isInputable(), Vocabulary.isSelectable()), JumpMode.INPUT);
    }

    public void nextLink() {
        findNext(Vocabulary.or(Vocabulary.isClickable(), Vocabulary.isInputable(), Vocabulary.isSelectable()),
                JumpMode.LINK);
    }

    public void previousLink() {
        findPrevious(Vocabulary.or(Vocabulary.isClickable(), Vocabulary.isInputable(), Vocabulary.isSelectable()),
                JumpMode.LINK);
    }

    public void nextObject() {
        findNext(Vocabulary.isEmbeddedObject(), JumpMode.OBJECT);
    }

    public void previousObject() {
        findPrevious(Vocabulary.isEmbeddedObject(), JumpMode.OBJECT);
    }

    public void nextListItem() {
        findNext(Vocabulary.isListItem(), JumpMode.LISTITEM_TOP);
    }

    public void previousListItem() {
        findPrevious(Vocabulary.isListItem(), JumpMode.LISTITEM_BOTTOM);
    }

    public void nextBlock() {
        findNext(Vocabulary.isBlockJumpPointF(), JumpMode.BLOCK);
    }

    public void previousBlock() {
        findPrevious(Vocabulary.isBlockJumpPointB(), JumpMode.BLOCK);
    }

    public void nextMedia() {
        findNext(Vocabulary.isMedia(), JumpMode.MEDIA);
    }

    public void previousMedia() {
        findPrevious(Vocabulary.isMedia(), JumpMode.MEDIA);
    }

    public void nextHeader1() {
        findNext(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading1()), JumpMode.HEADING);
    }

    public void nextHeader2() {
        findNext(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading2()), JumpMode.HEADING);
    }

    public void nextHeader3() {
        findNext(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading3()), JumpMode.HEADING);
    }

    public void nextHeader4() {
        findNext(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading4()), JumpMode.HEADING);
    }

    public void nextHeader5() {
        findNext(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading5()), JumpMode.HEADING);
    }

    public void nextHeader6() {
        findNext(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading6()), JumpMode.HEADING);
    }

    public void previousHeader1() {
        findPrevious(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading1()), JumpMode.HEADING);
    }

    public void previousHeader2() {
        findPrevious(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading2()), JumpMode.HEADING);
    }

    public void previousHeader3() {
        findPrevious(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading3()), JumpMode.HEADING);
    }

    public void previousHeader4() {
        findPrevious(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading4()), JumpMode.HEADING);
    }

    public void previousHeader5() {
        findPrevious(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading5()), JumpMode.HEADING);
    }

    public void previousHeader6() {
        findPrevious(Vocabulary.and(Vocabulary.isHeading(), Vocabulary.isHeading6()), JumpMode.HEADING);
    }
    
    public void jumpToAccessKey(final char key) {
        moveCmd(new Command() {
            ILocation current;
            public int run(int r) throws TreeManagerException {
                startProgress();
                current = getLocation();
                return treeManager.findNext(Vocabulary.isAccessKey(key));
            }

            public void after(int st) throws TreeManagerException {
                if ((st & ITreeManager.MOVED) == 0) {
                    moveCmd(new Command() {
                        public int run(int r) throws TreeManagerException {
                            treeManager.gotoStartOfPage();
                            return treeManager.findNext(Vocabulary.isAccessKey(key));
                        }

                        public void after(int st) throws TreeManagerException {
                            if ((st & ITreeManager.MOVED) == 0){
                                treeManager.moveToLocation(current);
                                afterFind(st, JumpMode.ACCESSKEY, key);
                            }
                            else
                                afterAccessKeyJump(st);
                            endProgress();
                        }
                    });
                } else {
                    afterAccessKeyJump(st);
                    endProgress();
                }
            }
        });
    }

    private void afterAccessKeyJump(int st) throws TreeManagerException {
        ITreeItem item = treeManager.getActiveItem();

        boolean isCheckbox = Vocabulary.isCheckbox().eval(item);
        boolean isRadio = Vocabulary.isRadio().eval(item);

        if (isRadio || isCheckbox) {
            click();
            speakActiveItem(true, false, JumpMode.NONE);
        } else {
            afterMove(st, "Navigator.BOTTOM", JumpMode.ACCESSKEY);
        }
    }

    public void showAccessKeyList() {
        try {
            setMode(IManipulator.KEYHOOK_DISABLED_MODE);
            IAccessKeyList list = treeManager.getAccessKeyList();
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

            AccessKeyListDialog dialog = new AccessKeyListDialog(shell, list);

            if (dialog.open() == InputDialog.OK) {
                char key = dialog.getSelectedKey();
                if (key != 0)
                    jumpToAccessKey(key);
            }
            setMode(IManipulator.TREE_NAVIGATION_MODE);
        } catch (TreeManagerException e) {
            e.printStackTrace();
        }
    }
    

    public void searchNext() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        SearchDialog dialog = new SearchDialog(shell);
        dialog.setForward(true);
        if (dialog.open() == Window.CANCEL || dialog.getString() == null || dialog.getString().length() == 0)
            return;

        moveCmd(new FindCommand(dialog.getString(), dialog.isForward(), dialog.isExact()));
    }

    public void searchPrevious() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        SearchDialog dialog = new SearchDialog(shell);
        dialog.setForward(false);
        if (dialog.open() == Window.CANCEL || dialog.getString() == null || dialog.getString().length() == 0)
            return;

        moveCmd(new FindCommand(dialog.getString(), dialog.isForward(), dialog.isExact()));
    }

    class FindCommand implements Command {
        private String str;

        private boolean direction;

        private boolean exact;

        FindCommand(String str, boolean direction, boolean exact) {
            this.str = str;
            this.direction = direction;
            this.exact = exact;
        }

        public int run(int r) throws TreeManagerException {
            if (direction)
                return treeManager.findNext(Vocabulary.find(str, exact));
            else
                return treeManager.findPrevious(Vocabulary.find(str, exact));
        }

        public void after(int st) throws TreeManagerException {
            if (direction)
                afterMove(st, "Navigator.BOTTOM");
            else
                afterMove(st, "Navigator.TOP");
        }
    }

    // --------------------------------------------------------------------------------
    // Table Navigation
    // --------------------------------------------------------------------------------

    public void cellLeft() {
        // TODO Auto-generated method stub

    }

    public void cellRight() {
        // TODO Auto-generated method stub

    }

    public void cellUp() {
        // TODO Auto-generated method stub

    }

    public void cellDown() {
        // TODO Auto-generated method stub

    }

    // --------------------------------------------------------------------------------
    // Speech Speed Control
    // --------------------------------------------------------------------------------

    private void saySpeechSpeed(int speed) {
        String mes = getMessageFormatter().mes("Navigator.SPEECHSPEED",
                                          new Object[] { new Integer(speed) });
        speakWithFormat(mes, true);
    }

    public void speechSpeedUp() {
        int nextSpeed = getVoiceManager().getSpeed() + 10;
        if (IVoice.SPEED_MAX < nextSpeed) {
            speakWithFormat("Navigator.SPEECHSPEEDMAX", true);
        } else {
            getVoiceManager().setSpeed(nextSpeed);
            saySpeechSpeed(nextSpeed);
        }
    }

    public void speechSpeedDown() {
        int nextSpeed = getVoiceManager().getSpeed() - 10;
        if (IVoice.SPEED_MIN > nextSpeed) {
            speakWithFormat("Navigator.SPEECHSPEEDMIN", true);
        } else {
            getVoiceManager().setSpeed(nextSpeed);
            saySpeechSpeed(nextSpeed);
        }
    }

    // --------------------------------------------------------------------------------
    // Form input mode.
    // --------------------------------------------------------------------------------

    public void exitFormMode() {
        setMode(IManipulator.TREE_NAVIGATION_MODE);
    }

    public void submitForm() {
        // TODO Auto-generated method stub
    }


    // --------------------------------------------------------------------------------
    // Location Management
    // --------------------------------------------------------------------------------

    public ILocation getLocation() {
        if (treeManager == null)
            return null;
        try {
            return treeManager.getCurrentLocation();
        } catch (TreeManagerException e) {
            return null;
        }
    }

    private ILocation locationToBeRestored;

    public void restoreLocation(ILocation location) {
        locationToBeRestored = location;
    }

    // --------------------------------------------------------------------------------
    // IMediaControl.IHandle delegation class
    // --------------------------------------------------------------------------------
    private class MediaControlHandle implements IMediaControl.IHandle {
        public ISoundControl getSoundControl() {
            if (treeManager == null)
                return null;
            try {
                return treeManager.getSoundControl();
            } catch (TreeManagerException e) {
                return null;
            }
        }

        public IVideoControl getVideoControl() {
            if (treeManager == null)
                return null;
            try {
                return treeManager.getVideoControl();
            } catch (TreeManagerException e) {
                return null;
            }
        }

        public IVoice getVoice() {
            return NavigatorImpl.this.getVoiceManager();
        }

        public IWebBrowserACTF getWebBrowser() {
            return webBrowser;
        }
    }

    private MediaControlHandle handle = new MediaControlHandle();

    IMediaControl.IHandle getMediaControlHandle() {
        return handle;
    }

    public void toggleDescriptionEnable() {
        int result = MediaControlExtension.toggleEnable();
        switch (result) {
        case IMediaControl.TOGGLE_FAIL:
            speakWithFormat("AudioDescription.noExtension");
            break;
        case IMediaControl.STATUS_ON:
            speakWithFormat("AudioDescription.on");
            break;
        case IMediaControl.STATUS_OFF:
            speakWithFormat("AudioDescription.off");
            break;
        case IMediaControl.STATUS_NOT_AVAILABLE:
            speakWithFormat("AudioDescription.notAvailable");
            break;
        }
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    MovieStartListener listener = null;

    private void initializeMovieStartListener() {
        IHandle mediaControlHandle = getMediaControlHandle();
        final IVideoControl video = mediaControlHandle.getVideoControl();
        if (listener != null) {
            listener.stop();
        }
        listener = new MovieStartListener(video);
        if (video != null) {
            video.addEventListener(listener);
        }
    }

    class MovieStartListener implements IMediaSyncEventListener {
        IVideoControl video;

        public MovieStartListener(IVideoControl video) {
            this.video = video;
        }

        private boolean stopFlag = false;

        public void stop() {
            stopFlag = true;
        }

        public double getInterval() {
            return 0.1;
        }

        private boolean topFlag = true;

        public void run() {
            if (stopFlag)
                return;
            double time = video.getCurrentPosition();
            if (topFlag) {
                // Heuristic solution.
                // "stop" operation might locates nearly 0.001?. 0.1 avoids this situnation.
                // 3.0 is a sentinel for waiting the completion of reloading and analyzing page content.
                if (0.1 < time && time < 3.0) {
                    speakWithFormat("Navigator.MOVIE_START");
                    topFlag = false;
                }
            } else {
                if (time < 0.1) {
                    topFlag = true;
                }
            }
        }
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    public enum FocusTabResult {
        STAY, CHANGED, NOTFOUND
    }

    public FocusTabResult focusTab() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        IEditorReference[] erefs = page.getEditorReferences();
        IEditorPart editor = page.getActiveEditor();

        for (int i = 0; i < erefs.length; i++) {
            IEditorPart part = erefs[i].getEditor(false);
            IModelServiceHolder modelServiceHolder = (IModelServiceHolder) part;
            IWebBrowserACTF wb = (IWebBrowserACTF) modelServiceHolder.getModelService();
            if (wb == webBrowser) {
                if (editor != part) {
                    page.activate(part);
                    return FocusTabResult.CHANGED;
                }
                return FocusTabResult.STAY;
            }
        }
        return FocusTabResult.NOTFOUND;
    }

    public boolean isFocused() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        IEditorReference[] erefs = page.getEditorReferences();
        IEditorPart editor = page.getActiveEditor();

        for (int i = 0; i < erefs.length; i++) {
            IEditorPart part = erefs[i].getEditor(false);
            IModelServiceHolder modelServiceHolder = (IModelServiceHolder) part;
            IWebBrowserACTF wb = (IWebBrowserACTF) modelServiceHolder.getModelService();
            if (wb == webBrowser) {
                if (editor != part) {
                    return false;
                }
                return true;
            }
        }
        // Should be regarded as an error.
        System.err.println("Internal Error: WebBrowser:" + webBrowser + " is not managed in the editor");
        return false;
    }

    public String getCurrentURL() {
        return webBrowser.getURL();
    }

    // ----------------------------------------------------------------
    // Browser Control 
    // ----------------------------------------------------------------

    public void enterBrowserAddress() {
        if (PlatformUIUtil.getActiveEditor() == null) {
            speakWithFormat("Navigator.THERE_ARE_NO_TAB");
            return;
        }
        
        webBrowser.setFocusAddressText(true);
        speakWithFormat("Navigator.ENTERBROWSERADDRESS", false);
        enterFormInputMode();
    }
    
    public void forceRestart(boolean flush) {
        selectFennec(false, flush);
        
        // TODO
        MediaControlExtension.doDispose(getMediaControlHandle());
        MediaControlExtension.start(getMediaControlHandle());
        initializeMovieStartListener();
    }

    public void forceRestart() {
        forceRestart(true);
    }
    
    public void navigateRefresh() {
        startProgress();
        String buf = getMessageFormatter().mes("Navigator.REFRESH");
        speak(buf, true, false);
        webBrowser.navigateRefresh();
    }

    public void closeTab() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        IEditorPart editor = page.getActiveEditor();

        if (editor != null && editor instanceof IModelServiceHolder) {
            ManipulatorExtension.setNavigator(null);
            page.closeEditor(editor, false);
        }
    }

    public void nextTab() {
        gotoTab(1);
    }

    public void prevTab() {
        gotoTab(-1);
    }

    private void gotoTab(int n) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        IEditorReference[] erefs = page.getEditorReferences();
        IEditorPart editor = page.getActiveEditor();

        for (int i = 0; i < erefs.length; i++) {
            if (erefs[i].getEditor(false).equals(editor)) {
                n += i;

                IEditorPart part;
                if (n >= erefs.length) {
                    part = erefs[0].getEditor(false);
                } else if (n < 0) {
                    part = erefs[erefs.length - 1].getEditor(false);
                } else {
                    part = erefs[n].getEditor(false);
                }
                if (part != null) {
                    page.activate(part);
                }
            }
        }
        speakTab();
    }


    public void goBackward() {
        webBrowser.goBackward();
    }

    public void goForward() {
        webBrowser.goForward();
    }

    // It is used by the bridge.
    public boolean gotoUrl(String url) {
        webBrowser.navigate(url);
        return true;
    }
    
    public void launchBrowser() {
        BrowserLaunch.launch(webBrowser.getURL());
        String buf = getMessageFormatter().mes("Navigator.LAUNCH_DEFAULT_BROWSER", webBrowser.getURL());
        speak(buf, true, false);
    }

    public boolean isLeftViewsShown() {
        return getNavigatorTreeView().isShown();
    }

    public void toggleLeftViewsShowing() {
        boolean result = getNavigatorTreeView().toggleViewShowing();
        boolean result2 = MediaControlExtension.toggleViewShowing();
        while (result != result2)
            result2 = MediaControlExtension.toggleViewShowing();

        if (result)
            speakWithFormat("Navigator.VIEWS_ARE_OPEND", true);
        else
            speakWithFormat("Navigator.VIEWS_ARE_CLOSED", true);
    }
    
    public void exportMetadata() {
        if (getCurrentEntry() == null) {
            speakWithFormat("Navigator.NO_ANNOTATION");
            return;
        }
        
        String[] ext = { "*.fnc" };
        speakWithFormat("Navigator.EXPORT_ANNOTATION");
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
        fileDialog.setFilterExtensions(ext);
        String path = fileDialog.open();

        if (path != null) {
            if (!path.endsWith(".fnc")) {
                path = path+".fnc";
            }
            File dest = new File(path);
            if (dest.exists()) {
                String title = getMessageFormatter().mes("Navigator.OVERWRITE_CONFIRM");
                String message = getMessageFormatter().mes("Navigator.OVERWRITE_MESSAGE", dest.getName());
                boolean ret = MessageDialog.openQuestion(shell, title, message);
                if (!ret)
                    return;
            }
            if (getCurrentEntry().isUserEntry()) {
                if (getCurrentEntry().export(dest)) {
                    speakWithFormat("Navigator.EXPORT_IS_SUCCEEDED");
                } else {
                    speakWithFormat("Navigator.EXPORT_IS_FAILED");
                }
            }
        }
    }
    
    public void repairFlash() {
        startProgress(0);
        try {
            treeManager.repairFlash();
            speakWithFormat("Navigator.REPAIR_FINISHED");
            restoreLocation(getLocation());
            selectFennec(false, false, false);
        } catch (TreeManagerException e) {
            e.printStackTrace();
        }
        endProgress();
    }
    
    // ---------------------------------------------------------------
    // Web Event
    // ---------------------------------------------------------------
    
    public void speakTitle(String title) {
        try {
           speak(title, true, true);
        } catch (Exception e) {
        }
    }
    
    public void speakTab() {
        speakTab(true);
    }

    public void speakOpenTab() {
        speakWithFormat(getMessageFormatter().mes("Navigator.NEW_TAB"));
    }

    public void navigateComplete() {
        startProgress();
    }

    public void speakCloseTab(String title) {
        speak(getMessageFormatter().mes("Navigator.CLOSE_TAB", title), true, true);
    }

    private long progressTimer = 0;

    private static final int progressInterval = 2000;

    private int prevPercent = -4;

    void beforeNavigation(String uri) {
        prevPercent = -4;
    }

    void progressChange(int progress, int progressMax) {
        if (progressMax == 0)
            return;
        if (progress == -1) {
            progressTimer = 0;
            return;
        }
        long current = System.currentTimeMillis();

        if (current - progressTimer > progressInterval * 5) {
            prevPercent = -4;
        }

        if (progressMax < 10000) {
            progressMax = 1000;
        }
        progressMax = (int) Math.pow(10, ((int) Math.log10(progressMax)) + 1);

        int percent = (progress * 100) / progressMax;
        if (progress * 10 == progressMax) {
            percent = 100;
        }

        if ((progressTimer == 0) || ((current - progressTimer) >= progressInterval)) {
            if (5 < percent && percent < 100) {
                if (percent - prevPercent >= 5 || (prevPercent - percent > 10)) { // && prevPercent - percent < 50)) {
                    sayProgress(percent);

                    // System.out.println("Say " + percent);
                    prevPercent = percent;
                    progressTimer = current;
                }
            }
        }
        // System.out.println(progress + " / " + progressMax + ", " + percent + ", " + prevPercent);
    }

    private void sayProgress(int percent) {
        if (percent < 0)
            percent = 0;
        else if (percent > 100)
            percent = 100;
        String mes = getMessageFormatter().mes("Navigator.PROGRESS", new Object[] { new Integer(percent) });
        speak(mes, true, true);
    }
}
