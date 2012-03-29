/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.msp.preferences;

import org.eclipse.actf.ai.tts.TTSRegistry;
import org.eclipse.actf.ai.tts.msp.Messages;
import org.eclipse.actf.ai.tts.msp.MspPlugin;
import org.eclipse.actf.ai.tts.msp.engine.MspVoice;
import org.eclipse.actf.ai.voice.preferences.util.ComboFieldEditor;
import org.eclipse.actf.ai.voice.preferences.util.GroupFieldEditorVoicePreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MspPreferencePage extends GroupFieldEditorVoicePreferencePage
		implements IWorkbenchPreferencePage {

	private String orgVoice;
	private String orgAudio;

	public MspPreferencePage() {
		super();
		setDescription(Messages.tts_msp_description);
		setPreferenceStore(MspPlugin.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
		if (!TTSRegistry.isAvailable(MspVoice.ID)) {
			setMessage(Messages.tts_msp_notAvailable);
			return;
		}

		orgVoice = getPreferenceStore().getString(MspVoice.ID);
		orgAudio = getPreferenceStore().getString(MspVoice.AUDIO_OUTPUT);

		final ComboFieldEditor voiceEditor;
		addField(voiceEditor = new MspVoiceFieldEditor(
				Messages.tts_msp_voicename, getFieldEditorParent()));
		addField(new MspAudioOutputFieldEditor(Messages.tts_msp_audiooutput,
				getFieldEditorParent()));

		Composite comp = new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = voiceEditor.getNumberOfControls();
		comp.setLayoutData(gd);

		Button testButton = new Button(comp, SWT.NONE);
		testButton.setText(Messages.tts_msp_test);
		testButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MspTestManager.getInstance().speakTest();
			}
		});
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performCancel() {
		getPreferenceStore().setValue(MspVoice.ID, orgVoice);
		getPreferenceStore().setValue(MspVoice.AUDIO_OUTPUT, orgAudio);
		return super.performCancel();
	}

	@Override
	protected void performApply() {
		super.performApply();

		orgVoice = getPreferenceStore().getString(MspVoice.ID);
		orgAudio = getPreferenceStore().getString(MspVoice.AUDIO_OUTPUT);
	}

}
