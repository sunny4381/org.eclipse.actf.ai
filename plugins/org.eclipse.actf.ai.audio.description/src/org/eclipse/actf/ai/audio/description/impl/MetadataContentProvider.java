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

package org.eclipse.actf.ai.audio.description.impl;

import org.eclipse.actf.ai.audio.description.IMetadata;
import org.eclipse.actf.ai.audio.description.IMetadataProvider;
import org.eclipse.actf.ai.audio.description.util.TimeFormatUtil;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;

public class MetadataContentProvider extends LabelProvider implements
		IStructuredContentProvider, ITableLabelProvider {

	@Override
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IMetadataProvider) {
			IMetadataProvider provider = (IMetadataProvider) inputElement;

			String[][] ret = new String[provider.getSize()][2];
			for (int i = 0; i < ret.length; i++) {
				IMetadata metadata = provider.getItem(i);
				ret[i][0] = ""
						+ TimeFormatUtil
								.getTimeString(metadata.getStartTime() / 100.0);
				ret[i][1] = "" + metadata.getDescription();
			}
			return ret;
		}
		return null;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof String[]) {
			return ((String[]) element)[columnIndex];
		}
		return "";
	}

}
