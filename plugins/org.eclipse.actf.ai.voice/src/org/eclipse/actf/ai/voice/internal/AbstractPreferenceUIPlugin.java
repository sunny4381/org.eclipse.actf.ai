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
package org.eclipse.actf.ai.voice.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public abstract class AbstractPreferenceUIPlugin extends AbstractUIPlugin implements IPropertyChangeListener {

	private List listeners;
	
    public void propertyChange(PropertyChangeEvent event) {
		for( int i=0; i<listeners.size(); i++ ) {
			((IPropertyChangeListener)listeners.get(i)).propertyChange(event);
		}
	}

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
    	if( null == listeners) {
    		listeners = new ArrayList();
    		getPreferenceStore().addPropertyChangeListener(this);
    	}
    	listeners.add(listener);
    }
    
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
    	listeners.remove(listener);
    }

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
    	listeners = null;
    	getPreferenceStore().removePropertyChangeListener(this);
	}
	
	
}
