package org.eclipse.actf.ai.audio.description.actions;

import org.eclipse.actf.ai.audio.description.impl.MetadataManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ReadAdditionalDescriptionAction implements
		IWorkbenchWindowActionDelegate {

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	public void run(IAction action) {
		MetadataManager.requestAdditions();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
