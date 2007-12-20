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

import org.eclipse.actf.ai.tts.ITTSEngine;
import org.eclipse.actf.ai.tts.protalker.Messages;
import org.eclipse.actf.ai.tts.protalker.engine.ProTalker;
import org.eclipse.actf.ai.voice.internal.Voice;
import org.eclipse.actf.ai.voice.preferences.ComboButtonFieldEditor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.widgets.Composite;


public class ProTalkerFieldEditor extends ComboButtonFieldEditor {

	private static final String SAMPLE_TEXT = "Hello. This is test."; //$NON-NLS-1$

	private static final String[][] VOICE_SELECTION = new String[][] {
			{ Messages.getString("tts.protalker.male"), "male" }, { Messages.getString("tts.protalker.female"), "female" } }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private ProTalker proTalker;

	public ProTalkerFieldEditor(String labelText, Composite parent) {
		super(ProTalker.ID, labelText, null, parent);
	}

	protected void initLabelsAndValues(String[][] labelsAndValues) {
		super.initLabelsAndValues(getVoiceNames());
	}

	private String[][] getVoiceNames() {
		if (null == proTalker) {
			try {
				proTalker = new ProTalker();
			}
			catch( Exception e ) {
			}
		}
		return null!=proTalker ? VOICE_SELECTION : new String[0][];
	}

	protected void testPressed(int index) {
		if (index >= 0) {
			proTalker.setVoice(value);
			proTalker.setSpeed(Voice.getDefaultSpeed());
			proTalker.speak(SAMPLE_TEXT, ITTSEngine.TTSFLAG_FLUSH, -1);
		}
	}

	public void setPage(DialogPage dialogPage) {
		super.setPage(dialogPage);
		if( null == dialogPage && null != proTalker ) {
			proTalker.dispose();
			proTalker = null;
		}
	}

}
