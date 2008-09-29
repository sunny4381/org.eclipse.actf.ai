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

import java.io.File;

import org.eclipse.actf.ai.navigator.IBrowserControl;
import org.eclipse.actf.ai.navigator.IManipulator;
import org.eclipse.actf.ai.navigator.extension.ManipulatorExtension;
import org.eclipse.actf.ai.navigator.util.URLOpenDialog;
import org.eclipse.actf.ai.navigator.voice.VoiceManager;
import org.eclipse.actf.ai.xmlstore.XMLStoreServiceUtil;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserACTF;
import org.eclipse.actf.model.ui.editor.browser.IWebBrowserNavigationEventListener;
import org.eclipse.actf.model.ui.editor.browser.WebBrowserNavigationEvent;
import org.eclipse.actf.model.ui.util.ModelServiceUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;




public class BrowserControlImpl implements IBrowserControl, IWebBrowserNavigationEventListener {
    private final TripJournal tripJournal;

    private final WebEventListener webEventListener;

    private final VoiceManager voiceManager = new VoiceManager(null);

    public void backward() {
        tripJournal.backward(webEventListener.getFocused());
    }

    public void forward() {
        tripJournal.forward(webEventListener.getFocused());
    }

    BrowserControlImpl(WebEventListener webEventListener, TripJournal tripJournal) {
        this.tripJournal = tripJournal;
        this.webEventListener = webEventListener;
    }

    public void goBack(WebBrowserNavigationEvent e) {
        tripJournal.backward(webEventListener.getFocused());
    }

    public void goForward(WebBrowserNavigationEvent e) {
        tripJournal.forward(webEventListener.getFocused());
    }

    public void refresh(WebBrowserNavigationEvent e) {
        IWebBrowserACTF wb = e.getBrowser();
        WebEventListener.BrowserState bs = webEventListener.getBrowserState(wb);
        if (bs == null)
            return;
        bs.getNavigator().navigateRefresh();
    }

    public void stop(WebBrowserNavigationEvent e) {
        IWebBrowserACTF wb = e.getBrowser();
        wb.navigateStop();
    }

    public void exportAllMetadata() {
        String[] ext = { "*.fnz" };

        voiceManager.speakWithFormat("Navigator.EXPORT_ALL_ANNOTATIONS", true, true);
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
        fileDialog.setFilterExtensions(ext);
        String path = fileDialog.open();

        if (path != null) {
            if (!path.endsWith(".fnz")) {
                path = path + ".fnz";
            }
            File dest = new File(path);
            if (dest.exists()) {
                String title = voiceManager.getMessageFormatter().mes("Navigator.OVERWRITE_CONFIRM");
                String message = voiceManager.getMessageFormatter().mes("Navigator.OVERWRITE_MESSAGE", dest.getName());
                boolean ret = MessageDialog.openQuestion(shell, title, message);
                if (!ret)
                    return;
            }
            if (XMLStoreServiceUtil.getXMLStoreService().exportAllAnnotations(dest)) {
                voiceManager.speakWithFormat("Navigator.EXPORT_IS_SUCCEEDED", true, true);
            } else {
                voiceManager.speakWithFormat("Navigator.EXPORT_IS_FAILED", true, true);
            }
        }
    }

    public void importMetadata() {
        String[] ext = { "*.fnc;*.fnz;*.xml" };

        voiceManager.speakWithFormat("Navigator.IMPORT_FENNEC_FILE", true, true);
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
        fileDialog.setFilterExtensions(ext);
        String path = fileDialog.open();

        if (path != null) {
            File src = new File(path);

            boolean r = XMLStoreServiceUtil.getXMLStoreService().importMetadata(src);
            XMLStoreServiceUtil.getXMLStoreService().getRootStore().refleshAll();
            NavigatorImpl navigator = webEventListener.getFocused();
            if (navigator != null)
                navigator.forceRestart();
            if (r) {
                voiceManager.speakWithFormat("Navigator.IMPORT_IS_SUCCEEDED", true, true);
            } else {
                voiceManager.speakWithFormat("Navigator.IMPORT_IS_FAILED", true, true);
            }
        }
    }


    public void openTab() {
        voiceManager.stop();
        
        URLOpenDialog openURLDialog = new URLOpenDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
        if (1 == openURLDialog.open()) {
            ManipulatorExtension.setMode(IManipulator.FORM_INPUT_MODE);
            String sUrl = openURLDialog.getUrl();
            ModelServiceUtils.openInExistingEditor(sUrl);
            ManipulatorExtension.setMode(IManipulator.TREE_NAVIGATION_MODE);
        }
    }
}
