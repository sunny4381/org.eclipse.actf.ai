/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daisuke SATO - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.tts.protalker.preferences;

import org.eclipse.actf.ai.tts.TTSRegistry;
import org.eclipse.actf.ai.tts.protalker.Messages;
import org.eclipse.actf.ai.tts.protalker.ProTalkerPlugin;
import org.eclipse.actf.ai.tts.protalker.engine.ProTalker;
import org.eclipse.actf.ai.voice.preferences.util.GroupFieldEditorVoicePreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class ProTalkerPreferencePage extends GroupFieldEditorVoicePreferencePage
		implements IWorkbenchPreferencePage {

	public ProTalkerPreferencePage() {
		super();
		setDescription(Messages.tts_protalker_description); 
		setPreferenceStore(ProTalkerPlugin.getDefault().getPreferenceStore());
	}

	public void createFieldEditors() {
        if(!TTSRegistry.isAvailable(ProTalker.ID)){
            setMessage(Messages.tts_protalker_notAvailable);
            return;
        }

		addField(new ProTalkerFieldEditor(Messages.tts_protalker_voice, getFieldEditorParent())); 
	}

	public void init(IWorkbench workbench) {
	}

}
