/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Takashi ITOH - initial API and implementation
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.sapi.preferences;

import org.eclipse.actf.ai.tts.TTSRegistry;
import org.eclipse.actf.ai.tts.sapi.Messages;
import org.eclipse.actf.ai.tts.sapi.SAPIPlugin;
import org.eclipse.actf.ai.tts.sapi.engine.SapiVoice;
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

public class SapiPreferencePage extends GroupFieldEditorVoicePreferencePage
		implements IWorkbenchPreferencePage {

	private String orgVoice;
	private String orgAudio;

	public SapiPreferencePage() {
		super();
		setDescription(Messages.tts_sapi_description);
		setPreferenceStore(SAPIPlugin.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
		if (!TTSRegistry.isAvailable(SapiVoice.ID)) {
			setMessage(Messages.tts_sapi_notAvailable);
			return;
		}

		orgVoice = getPreferenceStore().getString(SapiVoice.ID);
		orgAudio = getPreferenceStore().getString(SapiVoice.AUDIO_OUTPUT);

		final ComboFieldEditor voiceEditor;
		addField(voiceEditor = new SapiVoiceFieldEditor(
				Messages.tts_sapi_voicename, getFieldEditorParent()));
		addField(new SapiAudioOutputFieldEditor(Messages.tts_sapi_audiooutput,
				getFieldEditorParent()));

		Composite comp = new Composite(getFieldEditorParent(), SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = voiceEditor.getNumberOfControls();
		comp.setLayoutData(gd);

		Button testButton = new Button(comp, SWT.NONE);
		testButton.setText(Messages.tts_sapi_test);
		testButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SapiTestManager.getInstance().speakTest();
			}
		});
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public boolean performCancel() {
		getPreferenceStore().setValue(SapiVoice.ID, orgVoice);
		getPreferenceStore().setValue(SapiVoice.AUDIO_OUTPUT, orgAudio);
		return super.performCancel();
	}

	@Override
	protected void performApply() {
		super.performApply();

		orgVoice = getPreferenceStore().getString(SapiVoice.ID);
		orgAudio = getPreferenceStore().getString(SapiVoice.AUDIO_OUTPUT);
	}

}
