/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kentarou FUKUDA - initial API and implementation
 *******************************************************************************/
package org.eclipse.actf.ai.navigator.ui;

import org.eclipse.jface.action.IMenuManager;

/**
 * Utility class for Navigator UIs.
 */
public class NavigatorUIUtil {

	/**
	 * The navigator related menus will be added to this IMenuManager.
	 */
	public static IMenuManager menuManager;

	/**
	 * The view ID of the Navigator Tree View.
	 */
	public static final String NAVIGATOR_TREE_VIEW_ID = "org.eclipse.actf.ai.navigator.views.NavigatorTreeView"; //$NON-NLS-1$

}
