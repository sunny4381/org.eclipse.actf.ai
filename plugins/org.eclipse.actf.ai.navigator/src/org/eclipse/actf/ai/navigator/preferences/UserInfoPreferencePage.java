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
package org.eclipse.actf.ai.navigator.preferences;

import org.eclipse.actf.ai.internal.navigator.Messages;
import org.eclipse.actf.ai.internal.navigator.NavigatorPlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class UserInfoPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public UserInfoPreferencePage() {
		super(GRID);
		setPreferenceStore(NavigatorPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.UserInfo_PREFERENCES_NAME);
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
    public void createFieldEditors() {
        addField(
                new BooleanFieldEditor(
                    UserInfoPreferenceConstants.AUTO_SAVE,
                    Messages.UserInfo_SAVE_ANNOTATION, 
                    getFieldEditorParent()));

        /*
        addField(
                new BooleanFieldEditor(
                    UserInfoPreferenceConstants.AUTO_REFRESH,
                    Messages.getString("UserInfo.REFRESH_TREEVIEW"), 

                    getFieldEditorParent()));
        */
        
        //addField(
        //        new BooleanFieldEditor(
        //            UserInfoPreferenceConstants.NONVERBAL_OVERVIEW,
        //            "&Nonverbal Overview",
        //            getFieldEditorParent()));
                    
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}
