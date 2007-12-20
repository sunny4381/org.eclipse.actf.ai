/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.sapi.preferences;

import org.eclipse.actf.ai.tts.sapi.Messages;
import org.eclipse.actf.ai.tts.sapi.SAPIPlugin;
import org.eclipse.actf.ai.tts.sapi.engine.SapiVoice;
import org.eclipse.actf.ai.voice.internal.TTSRegistry;
import org.eclipse.actf.ai.voice.preferences.ComboFieldEditor;
import org.eclipse.actf.ai.voice.preferences.GroupFieldEditorVoicePreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class SapiPreferencePage extends GroupFieldEditorVoicePreferencePage
		implements IWorkbenchPreferencePage {

	public SapiPreferencePage() {
		super();
		setDescription(Messages.getString("tts.sapi.description")); //$NON-NLS-1$
		setPreferenceStore(SAPIPlugin.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
        if(!TTSRegistry.isAvailable(SapiVoice.ID)){
            setMessage(Messages.getString("tts.sapi.notAvailable"));
            return;
        }
        final ComboFieldEditor voiceEditor, audioEditor;
        addField(voiceEditor = new SapiVoiceFieldEditor(Messages.getString("tts.sapi.voicename"), getFieldEditorParent())); //$NON-NLS-1$
        addField(audioEditor = new SapiAudioOutputFieldEditor(Messages.getString("tts.sapi.audiooutput"), getFieldEditorParent())); //$NON-NLS-1$
        
        Composite comp = new Composite(getFieldEditorParent(),SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = 0;
        comp.setLayout(layout);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        gd.horizontalSpan = voiceEditor.getNumberOfControls();
        comp.setLayoutData(gd);
        
        Button testButton = new Button(comp,SWT.NONE);
        testButton.setText(org.eclipse.actf.ai.voice.Messages.getString("voice.test"));
        testButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                int voiceIndex = voiceEditor.getComboControl().getSelectionIndex();
                int audioIndex = audioEditor.getComboControl().getSelectionIndex();
                SapiTestManager.getInstance().speakTest(voiceIndex,audioIndex);
            }
        });
	}

    public void init(IWorkbench workbench) {
	}

    public void dispose() {
        super.dispose();
        SapiTestManager.freeInstance();
    }
}
