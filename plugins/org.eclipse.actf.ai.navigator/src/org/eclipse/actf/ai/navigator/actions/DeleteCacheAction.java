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
package org.eclipse.actf.ai.navigator.actions;

import org.eclipse.actf.ai.navigator.extension.MetadataCacheCleanerExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class DeleteCacheAction implements IWorkbenchWindowActionDelegate {
    
	//private IWorkbenchWindow window;

    public void init(IWorkbenchWindow window) {
        //this.window = window;
    }

    public void dispose() {
        
    }

    public void run(IAction action) {
        MetadataCacheCleanerExtension.clearCache();
    }

    public void selectionChanged(IAction action, ISelection selection) {
        
    }
}
