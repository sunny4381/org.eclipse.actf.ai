/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Hisashi MIYASHITA - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.fennec.treemanager;

import org.eclipse.actf.ai.fennec.IFennecService;
import org.eclipse.actf.ai.fennec.treemanager.impl.TreeManagerImpl;

/**
 * The factory class for ITreeManager.
 */
public class TreeManagerFactory {
	/**
	 * It creates a new instance of ITreeManager from an IFennecService.
	 * 
	 * @param fennecService
	 *            the instance of the IFennecService to be used in the tree
	 *            manager.
	 * @return new instance of the ITreeManager.
	 */
	public static ITreeManager newITreeManager(IFennecService fennecService) {
		return new TreeManagerImpl(fennecService);
	}
}
